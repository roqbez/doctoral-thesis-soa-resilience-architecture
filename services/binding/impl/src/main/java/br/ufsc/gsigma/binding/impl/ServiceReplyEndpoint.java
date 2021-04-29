package br.ufsc.gsigma.binding.impl;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.xml.namespace.QName;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.http.HttpProducer;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.infinispan.AdvancedCache;

import br.ufsc.gsigma.binding.camel.ThreadLocalHttpConnectionManagerParams;
import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.converters.ServiceProtocolConverter;
import br.ufsc.gsigma.binding.converters.ServiceProtocolConverterFactory;
import br.ufsc.gsigma.binding.converters.SoapServiceProtocolConverter;
import br.ufsc.gsigma.binding.events.ServiceReplyEvent;
import br.ufsc.gsigma.binding.support.ExchangeUtil;
import br.ufsc.gsigma.binding.support.RequestTracker;
import br.ufsc.gsigma.infrastructure.util.AppContext;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.ws.Headers;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;

public class ServiceReplyEndpoint extends HttpEndpoint {

	private static final Logger logger = Logger.getLogger(ServiceReplyEndpoint.class);

	private ThreadLocalHttpConnectionManagerParams connManagerParams = new ThreadLocalHttpConnectionManagerParams();

	private BindingEngine bindingEngine;

	public ServiceReplyEndpoint(String endPointURI, HttpComponent component, HttpClientParams clientParams, HttpConnectionManager httpConnectionManager, HttpClientConfigurer clientConfigurer) throws URISyntaxException {
		super(endPointURI, component, clientParams, httpConnectionManager, clientConfigurer);

		connManagerParams.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, Integer.MAX_VALUE);
		connManagerParams.setMaxTotalConnections(Integer.MAX_VALUE);

		httpConnectionManager.setParams(connManagerParams);

		this.bindingEngine = AppContext.getBean(BindingEngine.class);
	}

	@Override
	public Producer createProducer() throws Exception {
		return new ServiceInvokerProducer(this);
	}

	public class ServiceInvokerProducer extends HttpProducer {

		public ServiceInvokerProducer(HttpEndpoint endpoint) {
			super(endpoint);
		}

		@Override
		public void process(Exchange exchange) throws Exception {

			RequestBindingContext requestBindingContext = exchange.getIn().getHeader(ExchangeHeaders.HEADER_REQUEST_BINDING_CONTEXT, RequestBindingContext.class);

			int responseCode = -1;

			String responseText = null;

			String processURL = null;

			String endpoint = null;

			String requestCorrelationId = null;

			RequestTracker requestTracker = null;

			String responsePayload = null;

			String errorMessage = null;

			ExchangeUtil.removeUnusedHeaders(exchange);

			AdvancedCache<String, RequestTracker> requestTrackerCache = bindingEngine.getRequestTrackerCache();

			requestCorrelationId = requestBindingContext.getRequestCorrelationId();

			ServiceEndpointInfo service = requestBindingContext.getInvokedServiceInfo().getServiceEndpointInfo();

			endpoint = service.getServiceEndpointURL();

			processURL = requestBindingContext.getProcessURL();

			requestTracker = requestCorrelationId != null ? requestTrackerCache.get(requestCorrelationId) : null;

			boolean abort = requestTracker == null || requestTracker.isCompleted() || requestTracker.isReplyInFlight();

			if (abort) {

				logger.info(new ExecutionLogMessage("Request reply " + (requestTracker != null && requestTracker.isReplyInFlight() ? "in flight" : "already handled") + " for service " + endpoint + ", processName=" + requestBindingContext.getProcessName() + ", processInstanceId="
						+ requestBindingContext.getProcessInstanceId() + ", requestCorrelationId=" + requestCorrelationId + " --> " + processURL) //
								.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
								.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
								.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));

				Message answer = exchange.getOut();
				answer.setHeader(Exchange.HTTP_RESPONSE_CODE, 204);
				exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
				return;
			}

			requestTracker.setReplyInFlight(true);
			requestTrackerCache.replace(requestCorrelationId, requestTracker);

			try {

				exchange.getIn().setHeader(Exchange.HTTP_URI, processURL);

				logger.info(new ExecutionLogMessage(
						"Forwarding reply from service " + endpoint + " for processName=" + requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId() + ", requestCorrelationId=" + requestCorrelationId + " --> " + processURL)
								.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
								.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
								.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));

				byte[] payload = IOUtils.toByteArray(exchange.getIn().getBody(InputStream.class));

				exchange.getIn().setBody(payload);

				int currentAttempt = 0;
				int maxAttempts = 5;

				boolean stop = false;

				next_attempt: while (!stop) {

					boolean error = false;

					Exception ex = null;

					try {

						super.process(exchange);

						responseCode = exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
						responseText = exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);

						error = isError(responseCode);

						if (error) {
							errorMessage = responseText;
						}

						if (responseCode != -1) {
							InputStream is = (InputStream) exchange.getOut().getBody();
							is.reset();
							responsePayload = IOUtils.toString(is, StandardCharsets.UTF_8);
						}

					} catch (Exception e) {
						error = true;
						ex = e;
					}

					boolean containsErrorCodes = responseCode == 503 || responseCode == 500;

					// boolean ignoreResp = StringUtils.contains(responsePayload, "<ignore>true</ignore>");

					boolean retry = ++currentAttempt <= maxAttempts && error && containsErrorCodes;

					if (retry) {

						if (responseCode == 500) {
							Thread.sleep(500L);
						} else {
							Thread.sleep(1500L);
						}

						logger.info(new ExecutionLogMessage("Retrying forwarding reply (" + currentAttempt + "/" + maxAttempts + ") from service " + endpoint + " for processName=" + requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId()
								+ " to endpointURL=" + processURL + (!StringUtils.isEmpty(errorMessage) || !StringUtils.isEmpty(responsePayload) ? " --> " + responseCode + " - " + StringUtils.defaultIfBlank(responsePayload, errorMessage) : "")) //
										.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
										.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
										.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()));

						continue next_attempt;
					}

					if (ex != null) {
						throw ex;
					}

					stop = true;
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				errorMessage = e.getMessage();
				// throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
			} finally {

				bindingEngine.untrackRequest(requestTracker.getSequence(), requestCorrelationId, requestTracker.getServiceNamespace());

				if (isError(responseCode)) {

					if (responseCode != -1) {
						errorMessage = responsePayload;
					}

					logger.error(new ExecutionLogMessage("Error (" + (StringUtils.defaultIfBlank(responseText, String.valueOf(responseCode))) + ") forwarding reply from service " + endpoint + " for processName=" + requestBindingContext.getProcessName() + ", processInstanceId="
							+ requestBindingContext.getProcessInstanceId() + " to endpointURL=" + processURL + (!StringUtils.isEmpty(errorMessage) ? " --> " + errorMessage : ""))//
									.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
									.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
									.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
									.addTransientAttribute(LogConstants.SERVICE_RESPONSE_CODE, responseCode) //
									.addTransientAttribute(LogConstants.SERVICE_RESPONSE_TEXT, responseText));
				} else {

					QName processId = new QName(requestBindingContext.getProcessNS(), requestBindingContext.getProcessName());

					ServiceReplyEvent serviceReplyEvent = new ServiceReplyEvent(processId, //
							requestBindingContext.getProcessName(), //
							requestBindingContext.getProcessInstanceId(), //
							requestBindingContext.getApplicationId(), //
							requestBindingContext.getInvokedServiceInfo(), //
							requestBindingContext.getRequestCorrelationId());

					EventSender.getInstance().sendEvent(serviceReplyEvent, -1);

					ServiceProtocolConverter protocolConverter = ServiceProtocolConverterFactory.get((String) exchange.getIn().getHeader(Headers.HEADER_SERVICE_PROTOCOL));

					if (protocolConverter == null) {
						protocolConverter = ServiceProtocolConverterFactory.get(SoapServiceProtocolConverter.class.getName());
					}

					if (logger.isInfoEnabled()) {

						logger.info( //
								new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_MEDIATION_REPLY,
										"Successfully forwarded reply (" + responseCode + ") from service " + endpoint + " for processName=" + requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId() + " --> " + responsePayload) //
												.addAttribute(LogConstants.PROCESS_NAME, requestBindingContext.getProcessName()) //
												.addAttribute(LogConstants.PROCESS_INSTANCE_ID, requestBindingContext.getProcessInstanceId()) //
												.addTransientAttribute(LogConstants.SERVICE_PROTOCOL, protocolConverter.getName()) //
												.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
												.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
												.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()) //
												.addTransientAttribute(LogConstants.SERVICE_RESPONSE_CODE, responseCode) //
												.addTransientAttribute(LogConstants.SERVICE_RESPONSE_TEXT, responseText)); //
					}
				}
			}
		}

		private boolean isError(int responseCode) {
			return responseCode < 200 || responseCode >= 300;
		}
	}
}
