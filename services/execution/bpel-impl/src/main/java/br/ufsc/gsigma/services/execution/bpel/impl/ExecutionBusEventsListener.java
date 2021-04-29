package br.ufsc.gsigma.services.execution.bpel.impl;

import org.apache.camel.Consume;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.infrastructure.util.messaging.MessageEndpoints;

public class ExecutionBusEventsListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExecutionBusEventsListener.class);

	@Consume(uri = MessageEndpoints.EVENT_BUS_ENDPOINT)
	public void receiveBusEvent(Event event) {
		// logger.info("Received event: " + event);
	}

}
