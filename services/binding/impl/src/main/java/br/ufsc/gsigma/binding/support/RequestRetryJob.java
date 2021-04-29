package br.ufsc.gsigma.binding.support;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.http.HttpProducer;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.log4j.Logger;
import org.infinispan.AdvancedCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Objects;

import br.ufsc.gsigma.binding.camel.ThreadLocalHttpConnectionManagerParams;
import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.impl.BindingEngine;
import br.ufsc.gsigma.binding.impl.ServiceInvokerEndpoint;
import br.ufsc.gsigma.binding.util.ServiceEndpointUtil;
import br.ufsc.gsigma.infrastructure.util.ObjectHolder;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

@Component
public class RequestRetryJob implements Runnable {

	private static final Logger logger = Logger.getLogger(RequestRetryJob.class);

	@Autowired
	private BindingEngine bindingEngine;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private InfinispanSupport infinispanSupport;

	private HttpProducer producer;

	private ExecutorService pool = Executors.newFixedThreadPool(4, new NamedThreadFactory("Binding Service Request Tracker Job"));

	@PostConstruct
	public void setup() throws Exception {
		this.producer = (HttpProducer) camelContext.getEndpoint("http://0.0.0.0?throwExceptionOnFailure=false").createProducer();

		ThreadLocalHttpConnectionManagerParams connManagerParams = new ThreadLocalHttpConnectionManagerParams();
		connManagerParams.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, Integer.MAX_VALUE);
		connManagerParams.setMaxTotalConnections(Integer.MAX_VALUE);

		this.producer.getHttpClient().getHttpConnectionManager().setParams(connManagerParams);
	}

	@Override
	public void run() {

		if (!infinispanSupport.isLeader()) {
			return;
		}

		try {

			final AdvancedCache<String, RequestTracker> requestTrackerCache = bindingEngine.getRequestTrackerCache();

			// Sorting by process instance id and timestamp
			Collection<RequestTracker> reqs = new TreeSet<RequestTracker>();

			for (RequestTracker req : requestTrackerCache.values()) {
				if (!req.isCompleted()) {
					reqs.add(req);
				}
			}

			if (!reqs.isEmpty()) {

				if (logger.isDebugEnabled()) {

					DateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

					StringBuilder sb = new StringBuilder();
					sb.append("Checking the following requests for reply:");
					for (RequestTracker req : reqs) {

						RequestBindingContext requestBindingContext = req.getRequestBindingContext();

						sb.append("\n\tseq=" + req.getSequence() + ", timestamp=" + (req.getRequestTimestamp() != -1 ? sdf.format(new Date(req.getRequestTimestamp())) : "N/A") + ", inFlight=" + req.isInFlight() + ", replyInFlight=" + req.isReplyInFlight() + ", processName="
								+ requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId() + ", serviceClassification=" + ServiceEndpointUtil.namespaceToServiceClassification(req.getServiceNamespace()));
					}
					logger.debug(sb.toString());
				}

				List<Future<?>> futures = new ArrayList<Future<?>>(reqs.size());

				for (RequestTracker req : new ArrayList<RequestTracker>(reqs)) {

					final boolean isFromThisNode = Objects.equal(infinispanSupport.getMyAddress().toString(), req.getNodeId());

					if ((req.isInFlight() || req.isReplyInFlight()) && isFromThisNode) {
						// if (req.isInFlight() || req.isReplyInFlight()) {
						continue;
					}

					// Check if there is other req for same process instance in flight

					String processInstanceId = req.getRequestBindingContext() != null ? req.getRequestBindingContext().getProcessInstanceId() : null;

					if (processInstanceId != null) {
						for (RequestTracker req2 : reqs) {
							if (req2.getRequestBindingContext() != null) {

								RequestBindingContext requestBindingContext = req2.getRequestBindingContext();

								String processInstanceId2 = req2.getRequestBindingContext() != null ? req2.getRequestBindingContext().getProcessInstanceId() : null;
								if (Objects.equal(processInstanceId, processInstanceId2) && !Objects.equal(req.getRequestCorrelationId(), req2.getRequestCorrelationId())) {

									if (req2.isInFlight() || req2.isReplyInFlight()) {

										String serviceClassification = ServiceEndpointUtil.namespaceToServiceClassification(req2.getServiceNamespace());

										logger.warn(new ExecutionLogMessage("Can't retry request right now for processName=" + requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId() + ", serviceNamespace="
												+ ServiceEndpointUtil.namespaceToServiceClassification(req.getServiceNamespace()) + " because the request for " + serviceClassification + " is in flight").addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
														.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
														.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
														.addAttribute(LogConstants.SERVICE_CLASSIFICATION, serviceClassification));
										continue;
									}
								}
							}
						}
					}

					futures.add(pool.submit(() -> {
						try {

							transactionTemplate.execute((txStatus) -> {

								try {

									String requestCorrelationId = req.getRequestCorrelationId();

									// requestTrackerCache.lock(requestCorrelationId);

									RequestTracker request = requestTrackerCache.get(requestCorrelationId);

									if (request == null || request.isCompleted()) {
										return null;
									}

									boolean fromFailedNode = !isFromThisNode;

									long now = System.currentTimeMillis();

									long expirarationDiffMillis = request.getExpirarationDiffMillis();

									boolean notTried = request.getRequestTimestamp() == -1;

									boolean receiveTimeout = now - request.getRequestTimestamp() > expirarationDiffMillis;

									boolean retry = request.getRetryCount() < request.getMaxRetries();

									RequestBindingContext requestBindingContext = request.getRequestBindingContext();

									String serviceNamespace = request.getServiceNamespace();

									String serviceClassification = ServiceEndpointUtil.namespaceToServiceClassification(serviceNamespace);

									if ((notTried || receiveTimeout || fromFailedNode) && retry) {

										request.setRetryCount(request.getRetryCount() + 1);
										request.setRequestTimestamp(now);
										request.setExpirarationDiffMillis((long) (expirarationDiffMillis * (request.getRetryMultiplier())));

										requestTrackerCache.replace(request.getRequestCorrelationId(), request);

										ObjectHolder<Long> originalBindingEngineConfigurationVersionHolder = new ObjectHolder<Long>();
										ObjectHolder<BindingEngineConfiguration> bindingEngineConfigurationHolder = new ObjectHolder<BindingEngineConfiguration>();
										ObjectHolder<List<ServiceEndpointInfo>> servicesListHolder = new ObjectHolder<List<ServiceEndpointInfo>>();

										ExecutionContext executionContext = request.getExecutionContext();

										String token = requestBindingContext.getToken();

										long waitDelay = ExecutionContext.getAttribute(executionContext, ExecutionAttributes.ATT_BINDING_WAIT_FOR_NEW_BINDING_CONFIGURATION_TIMEOUT, Long.class, ResilienceParams.WAIT_FOR_NEW_BINDING_CONFIGURATION_TIMEOUT);

										ServiceInvokerEndpoint.prepare(token, serviceNamespace, waitDelay, ServiceInvokerEndpoint.OBTAIN_BINDING_CFG_MAX_RETRIES, originalBindingEngineConfigurationVersionHolder, bindingEngineConfigurationHolder, servicesListHolder);

										Exchange exchange = producer.createExchange();

										exchange.getIn().getHeaders().putAll(request.getRequestHeaders());

										SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(request.getSoapMessageBytes()));

										logger.info(new ExecutionLogMessage("Retrying request (" + request.getRetryCount() + "/" + request.getMaxRetries() + ") " + (fromFailedNode ? "from failed node '" + request.getNodeId() + "'" : "") + " for processName=" + requestBindingContext.getProcessName()
												+ ", processInstanceId=" + requestBindingContext.getProcessInstanceId() + " --> " + ServiceEndpointUtil.namespaceToServiceClassification(serviceNamespace)) //
														.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
														.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
														.addAttribute(LogConstants.SERVICE_CLASSIFICATION, serviceClassification));

										ServiceInvokerEndpoint.invokeServices(token, serviceNamespace, waitDelay, ServiceInvokerEndpoint.INVOKE_SERVICES_LIST_MAX_RETRIES, ServiceInvokerEndpoint.OBTAIN_BINDING_CFG_MAX_RETRIES, originalBindingEngineConfigurationVersionHolder.get(),
												bindingEngineConfigurationHolder.get(), servicesListHolder.get(), exchange, executionContext, requestBindingContext, soapMessage, producer, producer.getHttpClient(), null, logger);

									} else if (!retry) {

										logger.info(new ExecutionLogMessage("Removing exausted retry request (notTried=" + notTried + ", timeout=" + receiveTimeout + ", fromFailedNode=" + fromFailedNode + ") for processName=" + requestBindingContext.getProcessName() + ", processInstanceId="
												+ requestBindingContext.getProcessInstanceId() + " --> " + ServiceEndpointUtil.namespaceToServiceClassification(serviceNamespace)) //
														.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
														.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
														.addAttribute(LogConstants.SERVICE_CLASSIFICATION, serviceClassification));

										requestTrackerCache.remove(requestCorrelationId);
									}
									return null;

								} catch (Exception e) {
									throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
								}

							});

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}));

				}

//				for (Future<?> f : futures) {
//					try {
//						f.get();
//					} catch (Exception e) {
//						logger.error(e.getMessage(), e);
//					}
//				}
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}

	}

}
