package br.ufsc.gsigma.services.execution.events;

import java.util.Date;

import javax.xml.namespace.QName;

public class ProcessTaskFinishedEvent extends ProcessEvent {

	private static final long serialVersionUID = 1L;

	private Long processInstanceId;

	private Date finishTime;

	private String serviceClassification;

	private int invocationNumber;

	public ProcessTaskFinishedEvent(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName, Long processInstanceId, String serviceClassification, int invocationNumber, Date finishTime) {
		super(processExecutionServiceURL, applicationId, processURL, processId, processName);
		this.processInstanceId = processInstanceId;
		this.finishTime = finishTime;
		this.serviceClassification = serviceClassification;
		this.invocationNumber = invocationNumber;
	}

	public int getInvocationNumber() {
		return invocationNumber;
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

}
