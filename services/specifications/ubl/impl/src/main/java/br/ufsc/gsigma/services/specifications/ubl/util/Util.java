package br.ufsc.gsigma.services.specifications.ubl.util;

import java.util.Map;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public abstract class Util {

	public static void serviceSleep(Logger logger) {
		Long delay = getSleepDelay();
		serviceSleep(logger, delay);
	}

	public static void serviceSleep(Logger logger, Long delay) {
		logger.info("Sleeping for " + delay);
		sleep(delay);
	}

	public static Long getSleepDelay() {
		Long delay = ExecutionContext.getAttribute(ExecutionAttributes.ATT_INVOKED_SERVICE_EXECUTION_DELAY, Long.class, null);

		if (delay == null) {
			try {

				RequestBindingContext requestBindingContext = RequestBindingContext.get();

				if (requestBindingContext != null) {
					InvokedServiceInfo invokedServiceInfo = requestBindingContext.getInvokedServiceInfo();
					if (invokedServiceInfo != null) {
						Map<String, Double> qoSValues = invokedServiceInfo.getServiceEndpointInfo().getQoSValues();
						if (qoSValues != null) {
							Double responseTime = qoSValues.get("Performance.ResponseTime");
							if (responseTime != null) {
								delay = responseTime.longValue();
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}

		if (delay == null)
			delay = Constants.DEFAULT_DELAY;
		return delay;
	}

	public static void sleep(long millis) {
		if (millis > 0) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
		}
	}

	public static String getServiceNamespace(Object serviceImpl) {

		Class<? extends Object> clazz = serviceImpl.getClass();

		String namespace = getNamespaceFromWebServiceAnnotation(clazz);

		if (namespace != null)
			return namespace;

		for (Class<?> interfaceClass : clazz.getInterfaces()) {

			namespace = getNamespaceFromWebServiceAnnotation(interfaceClass);

			if (namespace != null)
				return namespace;

		}
		return null;
	}

	public static String getNamespaceFromWebServiceAnnotation(Class<?> clazz) {

		WebService annot = clazz.getAnnotation(WebService.class);

		if (annot != null) {
			if (annot.targetNamespace() != null)
				return annot.targetNamespace();
		}
		return null;
	}

}
