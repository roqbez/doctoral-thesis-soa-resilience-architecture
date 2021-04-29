package br.ufsc.gsigma.services.execution.events;

import javax.xml.namespace.QName;

public class ProcessUndeployedEvent extends ProcessEvent {

	private static final long serialVersionUID = 1L;

	public ProcessUndeployedEvent(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName) {
		super(processExecutionServiceURL, applicationId, processURL, processId, processName);
	}

}
