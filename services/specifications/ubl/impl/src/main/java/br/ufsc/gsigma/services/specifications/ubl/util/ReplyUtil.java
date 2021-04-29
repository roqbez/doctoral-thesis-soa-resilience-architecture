package br.ufsc.gsigma.services.specifications.ubl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.ws.Headers;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.cxf.ExecutionContextOutInterceptor;
import br.ufsc.gsigma.services.specifications.ubl.cxf.JsonTransformOutInterceptor;
import br.ufsc.gsigma.services.specifications.ubl.cxf.RequestBindingContextOutInterceptor;

public abstract class ReplyUtil {

	private static final CustomWSMessageUtils messageUtils = CustomWSMessageUtils.getInstance();

	private static final ExecutorService pool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newCachedThreadPool());

	public static void sendReplySoapMessage(boolean interactive, Logger logger, Object objectToSend, String endPointURL, String soapAction) {

		final Long delay = !interactive ? Util.getSleepDelay() : 0;

		final ExecutionContext executionContext = ExecutionContext.get();

		final RequestBindingContext requestBindingContext = RequestBindingContext.get();

		final boolean isJson = BooleanUtils.isTrue((Boolean) PhaseInterceptorChain.getCurrentMessage().get("JSON_REQUEST"));

		AbstractPhaseInterceptor<SoapMessage> interceptor = new AbstractPhaseInterceptor<SoapMessage>(Phase.WRITE) {
			@Override
			public void handleMessage(SoapMessage message) throws Fault {
				ExecutionContextOutInterceptor.setExecutionContext(executionContext);
				RequestBindingContextOutInterceptor.setRequestBindingContext(requestBindingContext);
			}
		};

		pool.submit(() -> {

			Map<String, String> headers = new HashMap<String, String>();

			headers.put(Headers.HEADER_SERVICE_REPLY, "true");

			if (delay > 0) {
				Util.serviceSleep(logger, delay);
			}

			boolean stop = false;

			int currentAttempt = 0;
			int maxAttempts = 0;

			next_attempt: while (!stop) {
				try {

					List<Interceptor<? extends Message>> outInterceptors = new ArrayList<Interceptor<? extends Message>>();
					outInterceptors.add(interceptor);

					if (isJson) {
						outInterceptors.add(JsonTransformOutInterceptor.INSTANCE);
					}

					messageUtils.sendMessageOneAway(objectToSend, endPointURL, soapAction, headers, outInterceptors);

					return null;

				} catch (Exception e) {

					boolean retry = currentAttempt++ <= maxAttempts;

					String serviceNamespace = requestBindingContext != null && requestBindingContext.getInvokedServiceInfo() != null && requestBindingContext.getInvokedServiceInfo().getServiceEndpointInfo() != null
							? requestBindingContext.getInvokedServiceInfo().getServiceEndpointInfo().getServiceNamespace() : null;

					if (retry) {
						logger.warn("Error sending message for " + serviceNamespace + ": " + e.getMessage() + ". Retrying request (" + currentAttempt + "/" + maxAttempts + ")");
						try {
							Thread.sleep(500L);
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
							return null;
						}
						continue next_attempt;
					} else {
						logger.error("Error sending message for " + serviceNamespace + ": " + e.getMessage(), e);
					}
				}
				stop = true;
			}

			return null;

			// messageUtils.sendMessage(objectToSend, endPointURL, soapAction, headers, isJson ? Collections.singletonList(JsonTransformOutInterceptor.INSTANCE) : null);
		});
	}
}
