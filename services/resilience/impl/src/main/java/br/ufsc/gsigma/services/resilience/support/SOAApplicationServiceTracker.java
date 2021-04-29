package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;

import br.ufsc.gsigma.binding.events.ServiceInvokedEvent;
import br.ufsc.gsigma.binding.events.ServiceReplyEvent;
import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.services.execution.events.ProcessTaskFinishedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessTaskStartedEvent;

public class SOAApplicationServiceTracker implements Serializable {

	private static final long serialVersionUID = 1L;

	private Task businessTask;

	private String serviceClassification;

	private String serviceKey;

	private String serviceEndpointURL;

	private int invocationNumber;

	private ProcessTaskStartedEvent processTaskStartedEvent;

	private ProcessTaskFinishedEvent processTaskFinishedEvent;

	private ServiceInvokedEvent serviceInvokedEvent;

	private ServiceReplyEvent serviceReplyEvent;

	private boolean complete = false;

	private SOAApplicationInstance applicationInstance;

	public SOAApplicationServiceTracker(SOAApplicationInstance applicationInstance, Task businessTask, String serviceClassification, int invocationNumber) {
		this.applicationInstance = applicationInstance;
		this.businessTask = businessTask;
		this.serviceClassification = serviceClassification;
		this.invocationNumber = invocationNumber;
	}

	public boolean wasComplete() {
		return complete;
	}

	public boolean isComplete() {

		if (!complete) {
			complete = (processTaskStartedEvent != null && processTaskFinishedEvent != null);

			if (complete) {
				long duration = applicationInstance.getServicesDuration();

				applicationInstance.setServicesDuration(duration + getDuration());
			}
		}

		return complete;
	}

	public long getDuration() {

		if (processTaskFinishedEvent == null || processTaskStartedEvent == null) {
			return -1;
		}

		return processTaskFinishedEvent.getFinishTime().getTime() - processTaskStartedEvent.getStartTime().getTime();
	}

	public Task getBusinessTask() {
		return businessTask;
	}

	public SOAApplicationInstance getApplicationInstance() {
		return applicationInstance;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getServiceEndpointURL() {
		return serviceEndpointURL;
	}

	public void setServiceEndpointURL(String serviceEndpointURL) {
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public int getInvocationNumber() {
		return invocationNumber;
	}

	public ProcessTaskStartedEvent getProcessTaskStartedEvent() {
		return processTaskStartedEvent;
	}

	public void setProcessTaskStartedEvent(ProcessTaskStartedEvent processTaskStartedEvent) {
		this.processTaskStartedEvent = processTaskStartedEvent;
	}

	public ProcessTaskFinishedEvent getProcessTaskFinishedEvent() {
		return processTaskFinishedEvent;
	}

	public void setProcessTaskFinishedEvent(ProcessTaskFinishedEvent processTaskFinishedEvent) {
		this.processTaskFinishedEvent = processTaskFinishedEvent;
	}

	public ServiceInvokedEvent getServiceInvokedEvent() {
		return serviceInvokedEvent;
	}

	public void setServiceInvokedEvent(ServiceInvokedEvent serviceInvokedEvent) {
		this.serviceInvokedEvent = serviceInvokedEvent;
	}

	public ServiceReplyEvent getServiceReplyEvent() {
		return serviceReplyEvent;
	}

	public void setServiceReplyEvent(ServiceReplyEvent serviceReplyEvent) {
		this.serviceReplyEvent = serviceReplyEvent;
	}

}
