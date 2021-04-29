package br.ufsc.gsigma.services.execution.events;

import java.util.Date;

import javax.xml.namespace.QName;

public class ProcessInstanceFinishedEvent extends ProcessEvent {

	private static final long serialVersionUID = 1L;

	private Long processInstanceId;

	private Date processCreationTime;

	private Date processFinishTime;

	private int processStateCode;

	private String processStateName;

	public ProcessInstanceFinishedEvent(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName, Long processInstanceId,
			Date processCreationTime, Date processFinishTime, int processStateCode, String processStateName) {
		super(processExecutionServiceURL, applicationId, processURL, processId, processName);
		this.processInstanceId = processInstanceId;
		this.processCreationTime = processCreationTime;
		this.processFinishTime = processFinishTime;
		this.processStateCode = processStateCode;
		this.processStateName = processStateName;
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public Date getProcessCreationTime() {
		return processCreationTime;
	}

	public Date getProcessFinishTime() {
		return processFinishTime;
	}

	public int getProcessStateCode() {
		return processStateCode;
	}

	public String getProcessStateName() {
		return processStateName;
	}

}
