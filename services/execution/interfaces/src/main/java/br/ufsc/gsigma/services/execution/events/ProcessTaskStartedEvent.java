package br.ufsc.gsigma.services.execution.events;

import java.util.Date;

import javax.xml.namespace.QName;

public class ProcessTaskStartedEvent extends ProcessEvent {

	private static final long serialVersionUID = 1L;

	private Long processInstanceId;

	private Date startTime;

	private String serviceClassification;

	private int invocationNumber;

	public ProcessTaskStartedEvent(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName, Long processInstanceId, String serviceClassification, int invocationNumber, Date startTime) {
		super(processExecutionServiceURL, applicationId, processURL, processId, processName);
		this.processInstanceId = processInstanceId;
		this.startTime = startTime;
		this.serviceClassification = serviceClassification;
		this.invocationNumber = invocationNumber;
	}

	public int getInvocationNumber() {
		return invocationNumber;
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

}
