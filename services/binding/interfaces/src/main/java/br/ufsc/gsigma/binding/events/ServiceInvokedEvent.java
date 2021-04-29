package br.ufsc.gsigma.binding.events;

import javax.xml.namespace.QName;

import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.infrastructure.util.messaging.Event;

public class ServiceInvokedEvent extends Event {

	private static final long serialVersionUID = 1L;

	private QName processId;

	private String processName;

	private String processInstanceId;

	private InvokedServiceInfo invokedServiceInfo;

	private String requestCorrelationId;

	private String applicationId;

	public ServiceInvokedEvent(QName processId, String processName, String processInstanceId, String applicationId, InvokedServiceInfo invokedServiceInfo, String requestCorrelationId) {
		this.processId = processId;
		this.processName = processName;
		this.processInstanceId = processInstanceId;
		this.applicationId = applicationId;
		this.invokedServiceInfo = invokedServiceInfo;
		this.requestCorrelationId = requestCorrelationId;
	}

	public QName getProcessId() {
		return processId;
	}

	public void setProcessId(QName processId) {
		this.processId = processId;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public InvokedServiceInfo getInvokedServiceInfo() {
		return invokedServiceInfo;
	}

	public void setInvokedServiceInfo(InvokedServiceInfo invokedServiceInfo) {
		this.invokedServiceInfo = invokedServiceInfo;
	}

	public String getServiceClassification() {
		return this.invokedServiceInfo.getServiceEndpointInfo().getServiceClassification();
	}

	public int getInvocationNumber() {
		return this.invokedServiceInfo.getRepeatCount() + 1;
	}

	public String getRequestCorrelationId() {
		return requestCorrelationId;
	}

	public void setRequestCorrelationId(String requestCorrelationId) {
		this.requestCorrelationId = requestCorrelationId;
	}

}
