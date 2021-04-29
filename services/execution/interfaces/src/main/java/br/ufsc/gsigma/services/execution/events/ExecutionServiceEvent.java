package br.ufsc.gsigma.services.execution.events;

import br.ufsc.gsigma.infrastructure.util.messaging.Event;

public abstract class ExecutionServiceEvent extends Event {

	private static final long serialVersionUID = 1L;

	private String processExecutionServiceURL;

	public ExecutionServiceEvent(String processExecutionServiceURL) {
		this.processExecutionServiceURL = processExecutionServiceURL;
	}

	public String getProcessExecutionServiceURL() {
		return processExecutionServiceURL;
	}

}
