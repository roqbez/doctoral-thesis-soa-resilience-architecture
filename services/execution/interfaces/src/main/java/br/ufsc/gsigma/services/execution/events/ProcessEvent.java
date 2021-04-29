package br.ufsc.gsigma.services.execution.events;

import javax.xml.namespace.QName;

public class ProcessEvent extends ExecutionServiceEvent {

	private static final long serialVersionUID = 1L;

	private String applicationId;

	private String processURL;

	private QName processId;

	private String processName;

	public ProcessEvent(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName) {
		super(processExecutionServiceURL);
		this.applicationId = applicationId;
		this.processURL = processURL;
		this.processId = processId;
		this.processName = processName;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getProcessURL() {
		return processURL;
	}

	public QName getProcessId() {
		return processId;
	}

	public String getProcessName() {
		return processName;
	}

}
