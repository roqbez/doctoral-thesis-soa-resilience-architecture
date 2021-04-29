package br.ufsc.gsigma.binding.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.infinispan.AdvancedCache;
import org.infinispan.transaction.LockingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.ufsc.gsigma.binding.bootstrap.RunBindingService;
import br.ufsc.gsigma.binding.camel.CacheInputStreamInInterceptor;
import br.ufsc.gsigma.binding.camel.ServiceProtocolConverterInInterceptor;
import br.ufsc.gsigma.binding.camel.SoapMessageModeInInterceptor;
import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.interfaces.BindingService;
import br.ufsc.gsigma.binding.support.BindingEngineConfiguration;
import br.ufsc.gsigma.binding.support.ExchangeUtil;
import br.ufsc.gsigma.binding.support.InfinispanSupport;
import br.ufsc.gsigma.binding.support.RequestRetryJob;
import br.ufsc.gsigma.binding.support.RequestTracker;
import br.ufsc.gsigma.binding.util.BindingServiceConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.ws.Headers;
import br.ufsc.gsigma.infrastructure.ws.ServiceClient;
import br.ufsc.gsigma.infrastructure.ws.WSMessageUtils;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessContract;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessRequest;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareScheduledExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.cxf.ConfigurableWstxInputInInterceptor;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfLoggingFeature;
import br.ufsc.gsigma.infrastructure.ws.cxf.ExecutionContextFeature;
import br.ufsc.gsigma.infrastructure.ws.cxf.ThreadLocalHolderFeature;
import br.ufsc.gsigma.servicediscovery.locator.DiscoveryServiceLocator;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformation;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;
import br.ufsc.gsigma.servicediscovery.model.ServicesQoSInformation;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.events.ServiceUnavailableEvent;

@Component
public class BindingEngine {

	private static final Logger logger = LoggerFactory.getLogger(BindingEngine.class);

	static final String SERVICE_MEDIATION_PATH = "service";

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private InfinispanSupport infinispanSupport;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private BindingService bindingService;

	@Autowired
	private RequestRetryJob requestRetryJob;

	private AdvancedCache<String, BindingEngineConfiguration> configurationsCache;

	private AdvancedCache<String, RequestTracker> requestTrackerCache;

	private ServiceAccessContract serviceClient;

	private ExecutorService cachedPool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newCachedThreadPool(new NamedThreadFactory("Binding Service Pool")));

	private ScheduledExecutorService scheduler = new ExecutionContextAwareScheduledExecutorServiceDecorator(Executors.newScheduledThreadPool(1, new NamedThreadFactory("Binding Service Request Tracker Scheduler")));

	private Map<String, Collection<BlockingQueue<BindingEngineConfiguration>>> waitBFQueue = new HashMap<String, Collection<BlockingQueue<BindingEngineConfiguration>>>();

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public Collection<BindingEngineConfiguration> getBindingConfigurations() {
		return configurationsCache.values();
	}

	public BindingEngineConfiguration getBindingConfiguration(String token) {
		return configurationsCache.get(token);
	}

	public AdvancedCache<String, RequestTracker> getRequestTrackerCache() {
		return requestTrackerCache;
	}

	public InfinispanSupport getInfinispanSupport() {
		return infinispanSupport;
	}

	private void scheduleRequestTracker() {
		scheduler.scheduleAtFixedRate(requestRetryJob, 0, 5, TimeUnit.SECONDS);
	}

	public RequestTracker trackRequest(byte[] soapMessageBytes, ExecutionContext executionContext, RequestBindingContext requestBindingContext, String serviceNamespace) throws Exception {
		return trackRequest(soapMessageBytes, executionContext, requestBindingContext, serviceNamespace, null, -1L);
	}

	public RequestTracker trackRequest(byte[] soapMessageBytes, ExecutionContext executionContext, RequestBindingContext requestBindingContext, String serviceNamespace, Long requestTimestamp, long invokeRequestTimestamp) throws Exception {

		int sequence = 1;

		String processInstanceId = requestBindingContext != null ? requestBindingContext.getProcessInstanceId() : null;

		synchronized (requestTrackerCache) {
			for (RequestTracker r : requestTrackerCache.values()) {
				if (processInstanceId.equals(r.getRequestBindingContext() != null ? r.getRequestBindingContext().getProcessInstanceId() : null)) {
					sequence = Math.max(sequence, r.getSequence()) + 1;
				}
			}
		}

		RequestTracker tracker = new RequestTracker(getNodeId(), sequence, requestBindingContext.getRequestCorrelationId(), serviceNamespace, invokeRequestTimestamp, executionContext, requestBindingContext);

		if (requestTimestamp != null) {
			tracker.getRequestHeaders().put(ExchangeHeaders.HEADER_REQUEST_TIMESTAMP, requestTimestamp);
		}

		tracker.setSoapMessageBytes(soapMessageBytes);

		requestTrackerCache.put(tracker.getRequestCorrelationId(), tracker);

		return tracker;
	}

	public RequestTracker updateTrackRequest(RequestTracker tracker, RequestBindingContext reqBindingContext, Long requestTimestamp, long invokeRequestTimestamp) throws Exception {

		tracker.setRequestBindingContext(reqBindingContext);

		if (requestTimestamp != null) {
			tracker.getRequestHeaders().put(ExchangeHeaders.HEADER_REQUEST_TIMESTAMP, requestTimestamp);
		}

		tracker.setRequestTimestamp(invokeRequestTimestamp);

		return updateTrackRequest(tracker);
	}

	public RequestTracker updateTrackRequest(RequestTracker tracker) throws Exception {

		String nodeId = getNodeId();

		String previousNodeId = tracker.getNodeId();

		if (!nodeId.equals(previousNodeId)) {
			logger.info("Changing nodeId of requestTracker " + tracker.getRequestCorrelationId() + " of " + tracker.getServiceNamespace() + " from " + previousNodeId + " to " + nodeId);
		}

		tracker.setNodeId(nodeId);

		requestTrackerCache.replace(tracker.getRequestCorrelationId(), tracker);
		return tracker;
	}

	public RequestTracker untrackRequest(int sequence, String requestCorrelationId, String serviceNamespace) {
		if (requestCorrelationId != null) {
			return requestTrackerCache.replace(requestCorrelationId, new RequestTracker(getNodeId(), sequence, requestCorrelationId, serviceNamespace, true));
		} else {
			return null;
		}
	}

	public String getNodeId() {
		return infinispanSupport.getMyAddress().toString();
	}

	public void addConfiguration(BindingEngineConfiguration configuration) {

		cachedPool.submit(() -> {

			synchronized (waitBFQueue) {
				String token = configuration.getToken();

				BindingEngineConfiguration previous = configurationsCache.get(token);

				configuration.setVersion(previous != null ? previous.getVersion() + 1 : 1);

				// Discovering Services QoS
				try {

					boolean requestAccess = true;

					if (requestAccess) {

						// Request Access to services

						ServiceAccessRequest serviceAccessRequest = new ServiceAccessRequest("EV 1", configuration.getApplicationId());

						List<ServiceEndpointInfo> unavailableServices = new LinkedList<ServiceEndpointInfo>();

						for (Entry<String, Collection<ServiceEndpointInfo>> e : configuration.getServicesPerNamespace().entrySet()) {

							for (ServiceEndpointInfo s : new ArrayList<ServiceEndpointInfo>(e.getValue())) {

								if (s.getServiceAccessToken() == null) {
									try {

										String serviceAccessToken = ServiceEndpointInfo.getServiceAccessToken(s, serviceClient, serviceAccessRequest);
										s.setServiceAccessToken(serviceAccessToken);

									} catch (Exception ex) {
										logger.warn("Can't obtain access token for " + s.getServiceEndpointURL() + " --> " + ex.getMessage());
										unavailableServices.add(s);
										e.getValue().remove(s);
									}
								}
							}
						}

						for (ServiceEndpointInfo service : unavailableServices) {
							EventSender.getInstance().sendEvent(new ServiceUnavailableEvent(service, configuration.getApplicationId(), configuration.getApplicationInstanceIds()));
						}
					}

					Map<String, ServiceEndpointInfo> mapServices = configuration.getServicesPerNamespace().values().stream().flatMap(x -> x.stream()) //
							.filter(x -> !x.isAdhoc()) //
							.collect(Collectors.toMap(ServiceEndpointInfo::getServiceKey, s -> s));

					StringBuilder sb = null;

					if (!mapServices.isEmpty() && logger.isInfoEnabled()) {
						sb = new StringBuilder();
						sb.append("Going to discover QoS Values for " + mapServices.size() + " service(s)");

						if (logger.isDebugEnabled()) {

							Collection<ServiceEndpointInfo> services = mapServices.values().stream().sorted(new Comparator<ServiceEndpointInfo>() {
								@Override
								public int compare(ServiceEndpointInfo s1, ServiceEndpointInfo s2) {
									return s1.getServiceNamespace().compareTo(s2.getServiceNamespace());
								}
							}).collect(Collectors.toList());

							sb.append(":");
							for (ServiceEndpointInfo s : services) {
								sb.append("\n\t" + s.getServiceKey());
							}
						}

						logger.info(sb.toString());
					}

					if (!mapServices.isEmpty()) {

						ServicesQoSInformation servicesQoSInformation = DiscoveryServiceLocator.get().getServicesQoSInformation(new ArrayList<String>(mapServices.keySet()));

						if (logger.isInfoEnabled()) {
							sb = new StringBuilder();
							sb.append("Adding binding configuration for applicationId=" + configuration.getApplicationId() + " and version " + configuration.getVersion());

							if (logger.isDebugEnabled()) {
								sb.append(". The QoS attributes for bound services (" + mapServices.size() + ") are:");
							}
						}

						for (ServiceQoSInformation s : servicesQoSInformation.getServicesQoSInformation()) {

							if (logger.isDebugEnabled()) {
								sb.append("\n\tQoS Attributes for " + s.getServiceKey() + ":");
							}

							ServiceEndpointInfo serviceEndpointInfo = mapServices.get(s.getServiceKey());

							if (s.getQoSItens().isEmpty()) {

								logger.warn("Can't find QoS Attributes for service --> " + s.getServiceKey());

							} else {
								for (ServiceQoSInformationItem q : s.getQoSItens()) {

									boolean includeQosAtt = //
											q.getQoSKey().equals("Availability.Availability") || //
									q.getQoSKey().equals("Cost.ExecutionCost") || //
									q.getQoSKey().equals("Performance.ResponseTime") || //
									q.getQoSKey().equals("Performance.Throughput");

									if (includeQosAtt) {

										serviceEndpointInfo.getQoSValues().put(q.getQoSKey(), q.getQoSValue());

										if (logger.isDebugEnabled()) {
											sb.append("\n\t\t" + q.getQoSKey() + "=" + q.getQoSValue());
										}
									}
								}
							}

						}
						if (logger.isInfoEnabled()) {
							logger.info(sb.toString());
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				configurationsCache.put(token, configuration);
				putInWaitBFQueue(configuration);
			}
		});
	}

	public BindingEngineConfiguration removeConfiguration(String bindingToken) {
		synchronized (waitBFQueue) {
			BindingEngineConfiguration configuration = configurationsCache.remove(bindingToken);
			putInWaitBFQueue(bindingToken, null);
			return configuration;
		}
	}

	public void clearBindingConfigurations() {
		synchronized (waitBFQueue) {
			configurationsCache.clear();
			shutdownWaitBFQueue();
		}
	}

	private void putInWaitBFQueue(BindingEngineConfiguration configuration) {
		putInWaitBFQueue(configuration.getToken(), configuration);

	}

	private void putInWaitBFQueue(String token, BindingEngineConfiguration configuration) {
		synchronized (waitBFQueue) {
			Collection<BlockingQueue<BindingEngineConfiguration>> queues = waitBFQueue.get(token);

			if (queues != null && queues.size() > 0) {

				if (configuration != null)
					logger.debug("Notifying " + queues.size() + " consumer(s) about availability of a new binding configuration");
				else
					logger.debug("Notifying " + queues.size() + " consumer(s) about binding configuration removal");

				for (BlockingQueue<BindingEngineConfiguration> q : queues) {
					try {
						q.put(configuration);
					} catch (InterruptedException e) {
					}
				}
				queues.clear();
				waitBFQueue.remove(token);
			}
		}
	}

	private void shutdownWaitBFQueue() {
		synchronized (waitBFQueue) {

			for (Collection<BlockingQueue<BindingEngineConfiguration>> queues : waitBFQueue.values()) {

				if (queues != null) {
					for (BlockingQueue<BindingEngineConfiguration> q : queues) {
						try {
							q.put(null);
						} catch (InterruptedException e) {
						}
					}
				}
				queues.clear();
			}
			waitBFQueue.clear();
		}
	}

	public BindingEngineConfiguration waitForNewBindingConfiguration(String token, long currentBindingTimestamp, boolean useCache, long timeout, TimeUnit unit) {

		BlockingQueue<BindingEngineConfiguration> q = null;

		synchronized (waitBFQueue) {

			BindingEngineConfiguration cfg = configurationsCache.get(token);

			if (cfg != null && useCache && cfg.getCreationTime() > currentBindingTimestamp) {
				return cfg;
			}

			Collection<BlockingQueue<BindingEngineConfiguration>> queues = waitBFQueue.get(token);

			if (queues == null) {
				queues = new LinkedList<BlockingQueue<BindingEngineConfiguration>>();
				waitBFQueue.put(token, queues);
			}
			q = new ArrayBlockingQueue<BindingEngineConfiguration>(1);
			queues.add(q);
		}

		try {
			return q.poll(timeout, unit);
		} catch (InterruptedException e) {
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostConstruct
	public void setup() throws Exception {

		this.configurationsCache = (AdvancedCache) infinispanSupport.getCache("binding-configuration", LockingMode.OPTIMISTIC).getAdvancedCache();

		if (logger.isInfoEnabled() && !this.configurationsCache.isEmpty()) {
			StringBuilder sb = new StringBuilder("Loading binding configurations from store:");
			for (BindingEngineConfiguration cfg : configurationsCache.values()) {
				sb.append("\n\tToken: " + cfg.getToken());
				for (Entry<String, Collection<ServiceEndpointInfo>> e : cfg.getServicesPerNamespace().entrySet()) {
					sb.append("\n\t\t" + e.getKey());
					for (ServiceEndpointInfo s : e.getValue()) {
						sb.append("\n\t\t\t" + s.getServiceEndpointURL());
					}
				}
			}
			logger.info(sb.toString());
		}

		this.requestTrackerCache = (AdvancedCache) infinispanSupport.getCache("request-tracker", LockingMode.PESSIMISTIC).getAdvancedCache();

		if (logger.isInfoEnabled() && !this.requestTrackerCache.isEmpty()) {
			StringBuilder sb = new StringBuilder("Loading requests track from store:");

			for (Entry<String, RequestTracker> e : requestTrackerCache.entrySet()) {
				sb.append("\n\t" + e.getKey());
			}
			logger.info(sb.toString());
		}

		this.serviceClient = ServiceClient.getClient(ServiceAccessContract.class, true);

		setupRoutes();

		scheduleRequestTracker();
	}

	private void setupRoutes() throws Exception {
		// String serviceHost = RunBindingService.getServiceHost();
		Integer servicePort = RunBindingService.getServicePort();

		String uri = "http://" + "0.0.0.0" + ":" + servicePort + "/" + SERVICE_MEDIATION_PATH;

		logger.info("Creating endpoint for " + uri);

		final CxfEndpoint from = (CxfEndpoint) camelContext.getEndpoint("cxf:" + uri + "?dataFormat=RAW");
		from.setServiceName(BindingServiceConstants.WS_SERVICE_NAME);
		from.setPortName(BindingServiceConstants.WS_SERVICE_PORT_NAME);

		final InterceptorProvider interceptorProvider = getInterceptorProvider(from);

		ThreadLocalHolderFeature.configureIn(interceptorProvider);
		ExecutionContextFeature.configure(interceptorProvider);
		CxfLoggingFeature.configureIn(interceptorProvider);

		// Necessary to parse SOAP messages
		from.getInInterceptors().add(new ServiceProtocolConverterInInterceptor());
		from.getInInterceptors().add(new CacheInputStreamInInterceptor()); // cache IS before Stax Interceptor and reset it after
		from.getInInterceptors().add(new ConfigurableWstxInputInInterceptor());
		from.getInInterceptors().add(new StaxInInterceptor());
		from.getInInterceptors().add(new SAAJInInterceptor());
		from.getInInterceptors().add(new ReadHeadersInterceptor(null));
		from.getInInterceptors().add(new SoapActionInInterceptor());
		from.getInInterceptors().add(new SoapMessageModeInInterceptor());

		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(from) //
						.process(new Processor() {
							@Override
							public void process(Exchange exchange) throws Exception {

								Message in = exchange.getIn();

								org.apache.cxf.message.Message cxfMessage = (SoapMessage) in.getHeader(CxfConstants.CAMEL_CXF_MESSAGE);

								HttpServletRequest httpRequest = (HttpServletRequest) cxfMessage.get(AbstractHTTPDestination.HTTP_REQUEST);

								String method = httpRequest.getMethod();

								if (logger.isDebugEnabled()) {
									logger.debug("Received " + method + " request from " + httpRequest.getRemoteAddr());
								}

								if ("GET".equals(method)) {
									exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain;charset=UTF-8");
									exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
									exchange.getOut().setBody("mediation endpoint ok");
									exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
									return;
								}

								List<?> list = cxfMessage.getContent(List.class);

								if (list != null && !list.isEmpty()) {

									SOAPMessage soapMessage = (SOAPMessage) list.get(0);

									RequestBindingContext requestBindingContext = WSMessageUtils.getObjectFromSoapHeader(soapMessage, RequestBindingContext.QNAME, RequestBindingContext.class);

									exchange.getIn().setHeader(ExchangeHeaders.HEADER_REQUEST_BINDING_CONTEXT, requestBindingContext);

									String requestTimestamp = WSMessageUtils.getObjectFromSoapHeader(soapMessage, Headers.HEADER_REQUEST_TIMESTAMP, String.class);

									if (requestTimestamp != null) {
										exchange.getIn().setHeader(ExchangeHeaders.HEADER_REQUEST_TIMESTAMP, Long.parseLong(requestTimestamp));
									}

								}

								exchange.getIn().setHeader(ExchangeHeaders.HEADER_EXECUTION_CONTEXT, ExecutionContext.get());
							}
						}).choice() //
						//
						.when(header(Headers.HEADER_SERVICE_REPLY).isEqualTo("true")) //
						.to("servicereply://?throwExceptionOnFailure=false")
						//
						.otherwise() //
						.process(new RequestToServiceProcessor()) //
						.to("serviceinvoker://?throwExceptionOnFailure=false");
			}
		});
	}

	private InterceptorProvider getInterceptorProvider(final CxfEndpoint from) {

		InterceptorProvider interceptorProvider = new InterceptorProvider() {

			@Override
			public List<Interceptor<? extends org.apache.cxf.message.Message>> getOutInterceptors() {
				return from.getOutInterceptors();
			}

			@Override
			public List<Interceptor<? extends org.apache.cxf.message.Message>> getOutFaultInterceptors() {
				return from.getOutFaultInterceptors();
			}

			@Override
			public List<Interceptor<? extends org.apache.cxf.message.Message>> getInInterceptors() {
				return from.getInInterceptors();
			}

			@Override
			public List<Interceptor<? extends org.apache.cxf.message.Message>> getInFaultInterceptors() {
				return from.getInFaultInterceptors();
			}
		};

		return interceptorProvider;
	}

	private final class RequestToServiceProcessor implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {

			Message in = exchange.getIn();

			org.apache.cxf.message.Message cxfMessage = (SoapMessage) in.getHeader(CxfConstants.CAMEL_CXF_MESSAGE);

			List<?> list = cxfMessage.getContent(List.class);

			if (list != null && !list.isEmpty()) {

				SOAPMessage soapMessage = (SOAPMessage) list.get(0);

				RequestBindingContext requestBindingContext = exchange.getIn().getHeader(ExchangeHeaders.HEADER_REQUEST_BINDING_CONTEXT, RequestBindingContext.class);

				String token = requestBindingContext.getToken();

				if (token == null) {
					ExchangeUtil.error(exchange, new IllegalArgumentException("Request token not provided"));
					return;
				}

				Node bodyContent = WSMessageUtils.getSoapBodyElement(soapMessage);

				if (bodyContent == null) {
					ExchangeUtil.error(exchange, new IllegalArgumentException("Body content can't be null"));
					return;
				}

				String serviceNamespace = bodyContent.getNamespaceURI();

				if (serviceNamespace == null) {
					ExchangeUtil.error(exchange, new IllegalArgumentException("Service namespace can't be null"));
					return;
				}

				NodeList childNodes = bodyContent.getChildNodes();

				for (int i = 0; i < childNodes.getLength(); i++) {
					Node item = childNodes.item(i);
					if (item.getLocalName().equals("processContext")) {
						for (int i2 = 0; i2 < item.getChildNodes().getLength(); i2++) {
							Node item2 = item.getChildNodes().item(i2);

							// Replacing callbackEndpoint (binding service must handle service reply)
							if (item2.getLocalName().equals("callbackEndpoint")) {
								requestBindingContext.setProcessURL(item2.getTextContent());
								item2.setTextContent(bindingService.getServiceMediationEndpoint());
							}
						}
					}
				}

				exchange.getIn().setHeader(ExchangeHeaders.HEADER_SERVICE_NAMESPACE, serviceNamespace);
			}
		}
	}

}
