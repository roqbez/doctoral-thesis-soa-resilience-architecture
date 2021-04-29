package br.ufsc.gsigma.services.execution.events;

import java.util.Date;

import javax.xml.namespace.QName;

public class ProcessInstanceStartedEvent extends ProcessEvent {

	private static final long serialVersionUID = 1L;

	private Long processInstanceId;

	private Date processCreationTime;

	public ProcessInstanceStartedEvent(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName, Long processInstanceId,
			Date processCreationTime) {
		super(processExecutionServiceURL, applicationId, processURL, processId, processName);
		this.processInstanceId = processInstanceId;
		this.processCreationTime = processCreationTime;
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public Date getProcessCreationTime() {
		return processCreationTime;
	}

}
