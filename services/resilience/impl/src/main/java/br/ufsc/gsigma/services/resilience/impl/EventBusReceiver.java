package br.ufsc.gsigma.services.resilience.impl;

import org.apache.camel.Consume;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.infrastructure.util.messaging.MessageEndpoints;

@Component
public class EventBusReceiver {

	private static final Logger logger = Logger.getLogger(EventBusReceiver.class);

	@Autowired
	private ResilienceServiceInternal resilienceServiceInternal;

	@Autowired
	private ResilienceMonitoring resilienceMonitoring;

	@Consume(uri = MessageEndpoints.EVENT_BUS_ENDPOINT)
	public void receiveBusEvent(Event event) throws Exception {

		logger.debug("Received event --> " + event);

		try {
			resilienceServiceInternal.receiveBusEvent(event);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		try {
			resilienceMonitoring.receiveBusEvent(event);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
