package br.ufsc.gsigma.binding.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.http.HttpProducer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.binding.camel.ThreadLocalHttpConnectionManagerParams;
import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.converters.ConvertedMessage;
import br.ufsc.gsigma.binding.converters.ServiceProtocolConverter;
import br.ufsc.gsigma.binding.converters.ServiceProtocolConverterFactory;
import br.ufsc.gsigma.binding.converters.SoapServiceProtocolConverter;
import br.ufsc.gsigma.binding.events.ServiceInvokedEvent;
import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.binding.support.BindingEngineConfiguration;
import br.ufsc.gsigma.binding.support.ExchangeUtil;
import br.ufsc.gsigma.binding.support.RequestTracker;
import br.ufsc.gsigma.infrastructure.util.AppContext;
import br.ufsc.gsigma.infrastructure.util.ObjectHolder;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.ws.WSMessageUtils;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.events.EmptyServiceListEvent;
import br.ufsc.gsigma.services.resilience.events.ServiceUnavailableEvent;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

public class ServiceInvokerEndpoint extends HttpEndpoint {

	private static final Logger logger = Logger.getLogger(ServiceInvokerEndpoint.class);

	public static final int INVOKE_SERVICES_LIST_MAX_RETRIES = 5;

	public static final int OBTAIN_BINDING_CFG_MAX_RETRIES = 5;

	public ServiceInvokerEndpoint(String endPointURI, HttpComponent component, HttpClientParams clientParams, HttpConnectionManager httpConnectionManager, HttpClientConfigurer clientConfigurer) throws URISyntaxException {
		super(endPointURI, component, clientParams, httpConnectionManager, clientConfigurer);

		ThreadLocalHttpConnectionManagerParams connManagerParams = new ThreadLocalHttpConnectionManagerParams();
		connManagerParams.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, Integer.MAX_VALUE);
		connManagerParams.setMaxTotalConnections(Integer.MAX_VALUE);

		httpConnectionManager.setParams(connManagerParams);
	}

	@Override
	public Producer createProducer() throws Exception {
		return new ServiceInvokerProducer(this);
	}

	public static class ServiceInvokerProducer extends HttpProducer {

		public ServiceInvokerProducer(ServiceInvokerEndpoint serviceInvokerEndpoint) {
			super(serviceInvokerEndpoint);
		}

		public void processSuper(Exchange exchange) throws Exception {
			super.process(exchange);
		}

		@Override
		public void process(Exchange exchange) throws Exception {

			try {

				final String serviceNamespace = exchange.getIn().getHeader(ExchangeHeaders.HEADER_SERVICE_NAMESPACE, String.class);

				final ExecutionContext executionContext = exchange.getIn().getHeader(ExchangeHeaders.HEADER_EXECUTION_CONTEXT, ExecutionContext.class);

				final RequestBindingContext requestBindingContext = exchange.getIn().getHeader(ExchangeHeaders.HEADER_REQUEST_BINDING_CONTEXT, RequestBindingContext.class);

				ExchangeUtil.removeUnusedHeaders(exchange);

				SOAPMessage soapMessage = null;

				org.apache.cxf.message.Message cxfMessage = (SoapMessage) exchange.getIn().getHeader(CxfConstants.CAMEL_CXF_MESSAGE);

				List<?> list = cxfMessage.getContent(List.class);

				if (list != null && !list.isEmpty()) {
					soapMessage = (SOAPMessage) list.get(0);
				}

				if (requestBindingContext == null) {
					ExchangeUtil.error(exchange, new IllegalArgumentException("Request Binding Context not found"));
					return;
				}

				final String requestCorrelationId = DigestUtils.sha1Hex(serviceNamespace + requestBindingContext.getProcessInstanceId());
				requestBindingContext.setRequestCorrelationId(requestCorrelationId);

				WSMessageUtils.removeSoapHeader(soapMessage, ExecutionContext.QNAME);
				WSMessageUtils.removeSoapHeader(soapMessage, RequestBindingContext.QNAME);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				soapMessage.writeTo(baos);

				byte[] soapMessageBytes = baos.toByteArray();

				final BindingEngine bindingEngine = AppContext.getBean(BindingEngine.class);

				// Track earlier to help clustering and retry
				RequestTracker requestTracker = bindingEngine.trackRequest(soapMessageBytes, executionContext, requestBindingContext, serviceNamespace);

				long waitDelay = ExecutionContext.getAttribute(executionContext, ExecutionAttributes.ATT_BINDING_WAIT_FOR_NEW_BINDING_CONFIGURATION_TIMEOUT, Long.class, ResilienceParams.WAIT_FOR_NEW_BINDING_CONFIGURATION_TIMEOUT);

				final String token = requestBindingContext.getToken();

				ObjectHolder<Long> originalBindingEngineConfigurationVersionHolder = new ObjectHolder<Long>();

				ObjectHolder<BindingEngineConfiguration> bindingEngineConfigurationHolder = new ObjectHolder<BindingEngineConfiguration>();

				ObjectHolder<List<ServiceEndpointInfo>> servicesListHolder = new ObjectHolder<List<ServiceEndpointInfo>>();

				try {
					prepare(token, serviceNamespace, waitDelay, OBTAIN_BINDING_CFG_MAX_RETRIES, originalBindingEngineConfigurationVersionHolder, bindingEngineConfigurationHolder, servicesListHolder);
				} catch (IllegalArgumentException | IllegalStateException e) {
					ExchangeUtil.error(exchange, e.getMessage());
					return;
				}

				Long originalBindingEngineConfigurationVersion = originalBindingEngineConfigurationVersionHolder.get();
				List<ServiceEndpointInfo> servicesList = servicesListHolder.get();
				BindingEngineConfiguration bindingEngineConfiguration = bindingEngineConfigurationHolder.get();

				final Processor processor = new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						processSuper(exchange);
					}
				};

				invokeServices(token, serviceNamespace, waitDelay, INVOKE_SERVICES_LIST_MAX_RETRIES, OBTAIN_BINDING_CFG_MAX_RETRIES, originalBindingEngineConfigurationVersion, bindingEngineConfiguration, servicesList, exchange, executionContext, requestBindingContext, soapMessage, processor,
						getHttpClient(), requestTracker, logger);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw e;
			}
		}

		@Override
		protected HttpMethod createMethod(Exchange exchange) throws Exception {
			HttpMethod method = super.createMethod(exchange);
			return method;
		}

	}

	public static boolean isResponseError(int responseCode) {
		return responseCode < 200 || responseCode >= 300;
	}

	@SuppressWarnings("unchecked")
	public static void prepare(final String token, final String serviceNamespace, long waitDelay, int maxRetries, ObjectHolder<Long> originalBindingEngineConfigurationVersionHolder, ObjectHolder<BindingEngineConfiguration> bindingEngineConfigurationHolder,
			ObjectHolder<List<ServiceEndpointInfo>> servicesListHolder) {

		BindingEngine bindingEngine = AppContext.getBean(BindingEngine.class);

		bindingEngineConfigurationHolder.set(bindingEngine.getBindingConfiguration(token));

		if (bindingEngineConfigurationHolder.get() == null) {
			throw new IllegalArgumentException("Configuration not found for token " + token);
		}

		if (originalBindingEngineConfigurationVersionHolder != null) {
			originalBindingEngineConfigurationVersionHolder.set(bindingEngineConfigurationHolder.get().getVersion());
		}

		final Collection<ServiceEndpointInfo> services = bindingEngineConfigurationHolder.get().getServicesForNamespace(serviceNamespace);

		if (CollectionUtils.isEmpty(services)) {

			boolean stopRequestingBindingCfg = false;

			int retryAttempt = 0;

			long currentBindingTimestamp = bindingEngineConfigurationHolder.get().getCreationTime();

			boolean becCache = true;

			retry_get_binding_cfg: while (!stopRequestingBindingCfg) {

				logger.warn(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Can't continue with current binding configuration (version " + bindingEngineConfigurationHolder.get().getVersion() + "). Waiting for a new one until " + waitDelay + " ms timeout"));

				logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Sending services empty list event (" + retryAttempt + "/" + maxRetries + ") for namespace " + serviceNamespace));
				EventSender.getInstance().sendEvent(new EmptyServiceListEvent(serviceNamespace, bindingEngineConfigurationHolder.get().getApplicationId()));

				long s = System.currentTimeMillis();
				BindingEngineConfiguration newBindingCfg = bindingEngine.waitForNewBindingConfiguration(token, currentBindingTimestamp, becCache, waitDelay, TimeUnit.MILLISECONDS);
				long d = System.currentTimeMillis() - s;

				if (newBindingCfg == null) {
					logger.warn(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Can't obtain a new binding configuration waiting " + waitDelay + " ms"));
				} else {
					logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Obtained a new binding configuration (version " + newBindingCfg.getVersion() + ") waiting a total of " + d + " ms"));

					StringBuilder sb = new StringBuilder("The binding configuration (version " + newBindingCfg.getVersion() + ") is aware of these instances: " + newBindingCfg.getApplicationInstanceIds() + " and contains services:");

					for (Entry<String, Collection<ServiceEndpointInfo>> e : newBindingCfg.getServicesPerNamespace().entrySet()) {
						sb.append("\n\t" + e.getKey());
						if (!CollectionUtils.isEmpty(e.getValue())) {
							for (ServiceEndpointInfo se : e.getValue()) {
								sb.append("\n\t\t" + se.getServiceEndpointURL());
							}
						} else {
							sb.append("\n\t\t(none services)");
						}
					}

					logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, sb.toString()));
				}

				Collection<ServiceEndpointInfo> newServices = newBindingCfg != null ? newBindingCfg.getServicesForNamespace(serviceNamespace) : Collections.EMPTY_LIST;

				if (!CollectionUtils.isEmpty(newServices)) {
					servicesListHolder.set(new ArrayList<ServiceEndpointInfo>(newServices));
					bindingEngineConfigurationHolder.set(newBindingCfg);
				} else {

					boolean retry = ++retryAttempt <= maxRetries;

					if (retry) {
						logger.warn(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "The binding configuration" + (newBindingCfg != null ? " at version " + newBindingCfg.getVersion() : "") + " doesn't contain any service for '" + serviceNamespace + "'" //
								+ (retry ? ". Waiting for a new one (" + retryAttempt + "/" + maxRetries + ")" : "")));

						if (newBindingCfg != null) {
							currentBindingTimestamp = newBindingCfg.getCreationTime();
							becCache = false;
						}

						continue retry_get_binding_cfg;

					} else {
						logger.warn(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Unable to invoke any service (totalAttempts=" + maxRetries + ") of namespace '" + serviceNamespace + "' (empty candidate services list)"));
						throw new IllegalStateException("Unable to invoke any service of namespace '" + serviceNamespace + "' (empty candidate services list)");
					}

				}
				stopRequestingBindingCfg = true;
			}
		} else {
			servicesListHolder.set(new ArrayList<ServiceEndpointInfo>(services));
		}
	}

	public static void invokeServices(final String token, final String serviceNamespace, long waitDelay, int invokeServicesListMaxRetries, int bindingCfgMaxRetries, Long originalBindingEngineConfigurationVersion, BindingEngineConfiguration bindingEngineConfiguration,
			List<ServiceEndpointInfo> servicesList, Exchange exchange, final ExecutionContext executionContext, final RequestBindingContext requestBindingContext, SOAPMessage soapMessage, Processor processor, HttpClient httpClient, RequestTracker requestTracker, Logger logger) throws Exception {

		final Long headerRequestTimestamp = (Long) exchange.getIn().getHeader(ExchangeHeaders.HEADER_REQUEST_TIMESTAMP);
		exchange.getIn().removeHeader(ExchangeHeaders.HEADER_REQUEST_TIMESTAMP);

		final ThreadLocalHttpConnectionManagerParams connManagerParams = (ThreadLocalHttpConnectionManagerParams) httpClient.getHttpConnectionManager().getParams();

		final BindingEngine bindingEngine = AppContext.getBean(BindingEngine.class);

		final boolean scheduleRetry = requestTracker != null;

		long invokeRequestTimestamp;

		int w = 1;

		int n = servicesList.size();

		boolean stop = false;

		final String applicationId = bindingEngineConfiguration.getApplicationId();

		int invokeServicesListRetryAttempt = 0;

		try {

			retry_invoke_service: while (!stop) {

				for (int i = 0; i < n; i++) {

					ServiceEndpointInfo service = servicesList.get(i);

					String targetUrl = service.getServiceEndpointURL();

					logger.debug( //
							new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Trying to invoke service " + targetUrl + " (" + (i + 1) + "/" + n + ")") //
									.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
									.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
									.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
									.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()) //
					);

					exchange.getIn().setHeader(Exchange.HTTP_URI, targetUrl);

					int invokeAttempt = w++;

					String sc = service.getServiceClassification();
					String scSuffix = sc.substring(sc.lastIndexOf('/', sc.lastIndexOf('/') - 1) + 1).replace("/", "_");

					int repeatCount = 0;

					if (executionContext != null) {
						for (InvokedServiceInfo is : executionContext.getAttributes(LogConstants.PREFIX_INVOKED_SERVICE + scSuffix, InvokedServiceInfo.class).values()) {
							if (!is.isError()) {
								repeatCount++;
							}
						}

					} else {
						logger.warn("executionContext is null for invocation");
					}

					String suffix = scSuffix + "_" + repeatCount + "_" + invokeAttempt;

					String invokedServiceField = LogConstants.PREFIX_INVOKED_SERVICE + suffix;

					InvokedServiceInfo invokedServiceInfo = new InvokedServiceInfo(service, bindingEngineConfiguration.getVersion(), bindingEngineConfiguration.getResilienceInfo(), repeatCount, invokeAttempt);

					String errorMessage = null;

					ServiceProtocolConverter protocolConverter = null;

					try {
						RequestBindingContext reqBindingContext = null;
						try {

							reqBindingContext = (RequestBindingContext) requestBindingContext.clone();

							ExecutionContext ectx = (ExecutionContext) executionContext.clone();
							ectx.addTransientAttribute(ServiceEndpointInfo.class.getName(), service);

							invokedServiceInfo.setInvokeStartTime(headerRequestTimestamp != null ? headerRequestTimestamp : System.currentTimeMillis());

							reqBindingContext.setOriginalBindingEngineConfigurationVersion(originalBindingEngineConfigurationVersion);
							reqBindingContext.setInvokedServiceInfo(invokedServiceInfo);

							ExecutionContext bindingCfgEctx = bindingEngineConfiguration.getExecutionContext();

							String protocolConverterClazz = null;

							if (bindingCfgEctx != null && BooleanUtils.isTrue(ExecutionContext.getFlagValue(bindingCfgEctx, ExecutionFlags.FLAG_FORCE_PROTOCOL_CONVERTER))) {

								protocolConverterClazz = ExecutionContext.getAttribute(bindingCfgEctx, ExecutionAttributes.ATT_PROTOCOL_CONVERTER, String.class, SoapServiceProtocolConverter.class.getName());

							} else {

								protocolConverterClazz = StringUtils.defaultIfBlank(service.getServiceProtocolConverter(), SoapServiceProtocolConverter.class.getName());

								boolean randomProtocolConverter = BooleanUtils.isTrue(ExecutionContext.getFlagValue(executionContext, ExecutionFlags.FLAG_RANDOM_SERVICE_PROTOCOL_CONVERTER));

								if (randomProtocolConverter) {
									List<String> l = ServiceProtocolConverterFactory.getServiceProtocolConverterNames();
									protocolConverterClazz = l.get(new Random().nextInt(l.size()));
								}
							}

							protocolConverter = ServiceProtocolConverterFactory.get(protocolConverterClazz);

							ConvertedMessage convertedMessage = protocolConverter.convertRequestMessage(soapMessage, exchange.getIn().getHeaders(), ectx, reqBindingContext);

							exchange.getIn().setBody(convertedMessage.getPayload());
							exchange.getIn().getHeaders().putAll(convertedMessage.getHeaders());

							for (String headerName : convertedMessage.getRemovedHeaders()) {
								exchange.getIn().removeHeader(headerName);
							}

							connManagerParams.setThreadLocalParameter(HttpConnectionParams.CONNECTION_TIMEOUT, ResilienceParams.DEFAULT_HTTP_CONNECTION_TIMEOUT);
							connManagerParams.setThreadLocalParameter(HttpConnectionParams.SO_TIMEOUT, ResilienceParams.DEFAULT_HTTP_READ_TIMEOUT);

							if (service.getServiceAccessToken() != null) {
								exchange.getIn().getHeaders().put("Authorization", service.getServiceAccessToken());
							}

							invokeRequestTimestamp = System.currentTimeMillis();

							processor.process(exchange);

						} finally {
							connManagerParams.resetThreadLocal();
						}

						int responseCode = exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
						String responseText = exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);

						// TODO: improve...
						if (isResponseError(responseCode)) {
							errorMessage = responseCode + " - " + responseText;
						} else {

							if (scheduleRetry) {
								requestTracker.setInFlight(false);
								requestTracker.setReplyInFlight(false);
								bindingEngine.updateTrackRequest(requestTracker, reqBindingContext, headerRequestTimestamp, invokeRequestTimestamp);
							}

							QName processId = new QName(requestBindingContext.getProcessNS(), requestBindingContext.getProcessName());

							ServiceInvokedEvent serviceInvokedEvent = new ServiceInvokedEvent(processId, //
									requestBindingContext.getProcessName(), //
									requestBindingContext.getProcessInstanceId(), //
									requestBindingContext.getApplicationId(), //
									invokedServiceInfo, //
									requestBindingContext.getRequestCorrelationId() //
							);

							EventSender.getInstance().sendEvent(serviceInvokedEvent, -1);

							// InputStream is = (InputStream) exchange.getOut().getBody();
							// is.reset();
							// String serviceResponse = IOUtils.toString(is, StandardCharsets.UTF_8);

							logger.info( //
									new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION,
											"Service (" + protocolConverter.getName() + ") successfully invoked at " + targetUrl + " (" + (i + 1) + "/" + n + ") for processName=" + requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId()) //
													.addTransientAttribute(LogConstants.SERVICE_PROTOCOL, protocolConverter.getName()) //
													.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
													.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
													.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
													.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()) //
													.addTransientAttribute(LogConstants.SERVICE_RESPONSE_CODE, responseCode) //
													.addTransientAttribute(LogConstants.SERVICE_RESPONSE_TEXT, responseText) //
							// .addTransientAttribute(LogConstants.SERVICE_RESPONSE, serviceResponse) //
							);
							stop = true;
							break;
						}

					} catch (IOException e) {
						errorMessage = e.getMessage();
					}

					if (errorMessage != null) {

						invokedServiceInfo.setError(true);

						logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Sending service unavailable event for " + service.getServiceEndpointURL()) //
								.addTransientAttribute(LogConstants.SERVICE_PROTOCOL, protocolConverter.getName()) //
								.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
								.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
								.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
								.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()));

						EventSender.getInstance().sendEvent(new ServiceUnavailableEvent(service, requestBindingContext.getApplicationId(), requestBindingContext.getProcessInstanceId()));

						if (i < n - 1) {

							ExecutionContext.get().addAttribute(invokedServiceField, invokedServiceInfo);

							logger.debug( //
									new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Failed to invoke service (" + protocolConverter.getName() + ") " + targetUrl + " (" + errorMessage + "). Trying next of list (" + (n - i - 1) + " remaining)") //
											.addTransientAttribute(LogConstants.SERVICE_PROTOCOL, protocolConverter.getName()) //
											.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
											.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
											.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
											.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()) //
							);

							// last service
						} else {

							logger.debug( //
									new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Failed to invoke last service (" + protocolConverter.getName() + ") " + targetUrl + " (" + errorMessage + "). Proceeding with a max of " + bindingCfgMaxRetries + " retries to obtain new services") //
											.addTransientAttribute(LogConstants.SERVICE_PROTOCOL, protocolConverter.getName()) //
											.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
											.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
											.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
											.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()));

							boolean stopRequestingBindingCfg = false;

							int bindingCfgRetryAttempt = 0;

							long currentBindingTimestamp = bindingEngineConfiguration.getCreationTime();

							retry_get_binding_cfg: while (!stopRequestingBindingCfg) {

								logger.warn( //
										new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Can't continue with current binding configuration. Waiting for a new one until " + waitDelay + " ms timeout") //
												.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
												.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));

								long s = System.currentTimeMillis();

								BindingEngineConfiguration newBindingCfg = bindingEngine.waitForNewBindingConfiguration(token, currentBindingTimestamp, true, waitDelay, TimeUnit.MILLISECONDS);
								long d = System.currentTimeMillis() - s;

								if (newBindingCfg != null) {

									Collection<ServiceEndpointInfo> newServices = newBindingCfg.getServicesForNamespace(serviceNamespace);

									if (newServices != null && !newServices.isEmpty()) {

										servicesList = new ArrayList<ServiceEndpointInfo>(newServices);

										n = servicesList.size();

										logger.info( //
												new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Received a new binding configuration at version " + newBindingCfg.getVersion() + " with " + n + " service(s) waiting a total of " + d + " ms, so retrying with the new ones") //
														.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
														.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));

										logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "The binding configuration (version " + newBindingCfg.getVersion() + ") is aware of these instances: " + newBindingCfg.getApplicationInstanceIds()));

										bindingEngineConfiguration = newBindingCfg;

										continue retry_invoke_service;

									} else {

										boolean retry = ++bindingCfgRetryAttempt <= bindingCfgMaxRetries;

										if (retry) {

											logger.warn( //
													new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION,
															"The binding configuration at version " + newBindingCfg.getVersion() + " doesn't contain any service for '" + serviceNamespace + "'" //
																	+ (retry ? ". Waiting for a new one (" + bindingCfgRetryAttempt + "/" + bindingCfgMaxRetries + ")" : "")) //
																			.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
																			.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));

											logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Sending services empty list event (" + bindingCfgRetryAttempt + "/" + bindingCfgMaxRetries + ") for namespace " + serviceNamespace));
											EventSender.getInstance().sendEvent(new EmptyServiceListEvent(serviceNamespace, applicationId));

											currentBindingTimestamp = newBindingCfg.getCreationTime();
											continue retry_get_binding_cfg;
										}
									}
								} else {
									logger.warn( //
											new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Can't obtain a new binding configuration for " + serviceNamespace + " waiting " + waitDelay + " ms") //
													.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
													.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));
								}
								stopRequestingBindingCfg = true;
							}

							// ExecutionContext.get().addAttribute(invokedServiceField + "_" + invokedServiceInfo.getInvokeAttempt(), invokedServiceInfo);
							ExecutionContext.get().addAttribute(invokedServiceField, invokedServiceInfo);

							boolean retry = ++invokeServicesListRetryAttempt <= invokeServicesListMaxRetries;

							if (retry) {

								logger.warn( //
										new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Can't invoke any service from list for " + serviceNamespace + ". Trying again (" + invokeServicesListRetryAttempt + "/" + invokeServicesListMaxRetries + ")") //
												.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
												.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));

								continue retry_invoke_service;

							} else {

								logger.warn( //
										new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION, "Failed to invoke last service " + targetUrl + " with token " + token + " (" + errorMessage + ")") //
												.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, targetUrl) //
												.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
												.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
												.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()) //
								);

								ExchangeUtil.error(exchange, "Unable to invoke any service of type '" + service.getServiceClassification() + "'");
								stop = true;
							}
						}
					}
				}
			}

		} finally {

			if (ExchangeUtil.isError(exchange)) {

				if (scheduleRetry) {
					requestTracker.setInFlight(false);
					bindingEngine.updateTrackRequest(requestTracker);
				}
			}
		}
	}

}
