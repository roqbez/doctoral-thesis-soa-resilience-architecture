package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import br.ufsc.gsigma.services.execution.events.ProcessInstanceFinishedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessInstanceStartedEvent;

public class SOAApplicationInstance implements Serializable, Comparable<SOAApplicationInstance> {

	private static final long serialVersionUID = 1L;

	private List<SOAApplicationServiceTracker> invokedServices = new CopyOnWriteArrayList<>();

	private Long instanceId;

	private boolean complete;

	private long servicesDuration;

	private String applicationId;

	private ProcessInstanceStartedEvent processInstanceStartedEvent;

	private ProcessInstanceFinishedEvent processInstanceFinishedEvent;

	public SOAApplicationInstance(String applicationId, Long instanceId) {
		this.applicationId = applicationId;
		this.instanceId = instanceId;
	}

	public String getKey() {
		return getKey(applicationId, instanceId);
	}

	public static String getKey(String applicationId, Long instanceId) {
		return applicationId + "#" + instanceId;
	}

	public void updateCache() {
		InfinispanCaches.getInstance().getSOAApplicationInstances().replace(getKey(), this);
	}

	public void updateCacheAsync() {
		InfinispanCaches.getInstance().getSOAApplicationInstances().replaceAsync(getKey(), this);
	}

	public String getApplicationId() {
		return applicationId;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public long getServicesDuration() {
		return servicesDuration;
	}

	public void setServicesDuration(long servicesDuration) {
		this.servicesDuration = servicesDuration;
	}

	public long getProcessInstanceDuration() {

		if (processInstanceFinishedEvent == null || processInstanceStartedEvent == null) {
			return -1;
		}

		return processInstanceFinishedEvent.getProcessFinishTime().getTime() - processInstanceStartedEvent.getProcessCreationTime().getTime();
	}

	public long getStartTime() {
		return processInstanceStartedEvent != null ? processInstanceStartedEvent.getProcessCreationTime().getTime() : 0;
	}

	public Long getProcessDeadline() {

		SOAApplication application = getApplication();

		Double responseTimeConstraint = application.getResponseTimeConstraint();
		if (responseTimeConstraint != null && processInstanceStartedEvent != null) {
			return processInstanceStartedEvent.getProcessCreationTime().getTime() + responseTimeConstraint.longValue();
		} else {
			return 0L;
		}
	}

	public SOAApplication getApplication() {
		return InfinispanCaches.getInstance().getSOAApplications().get(applicationId);
	}

	public Double getEngineOverhead() {
		if (complete) {
			return ((double) getProcessInstanceDuration() / (double) getServicesDuration()) - 1;
		} else {
			return -1D;
		}
	}

	public Long getInstanceId() {
		return instanceId;
	}

	public ProcessInstanceStartedEvent getProcessInstanceStartedEvent() {
		return processInstanceStartedEvent;
	}

	public void setProcessInstanceStartedEvent(ProcessInstanceStartedEvent processInstanceStartedEvent) {
		this.processInstanceStartedEvent = processInstanceStartedEvent;
	}

	public ProcessInstanceFinishedEvent getProcessInstanceFinishedEvent() {
		return processInstanceFinishedEvent;
	}

	public void setProcessInstanceFinishedEvent(ProcessInstanceFinishedEvent processInstanceFinishedEvent) {
		this.processInstanceFinishedEvent = processInstanceFinishedEvent;
	}

	public List<SOAApplicationServiceTracker> getInvokedServices() {
		return invokedServices;
	}

	public SOAApplicationServiceTracker getInvokedService(String serviceClassification) {
		return getInvokedService(serviceClassification, 1);
	}

	public SOAApplicationServiceTracker getInvokedService(String serviceClassification, int invocationNumber) {
		for (SOAApplicationServiceTracker s : invokedServices) {
			if (s.getServiceClassification().equals(serviceClassification) && s.getInvocationNumber() == invocationNumber) {
				return s;
			}
		}
		return null;
	}

	@Override
	public int compareTo(SOAApplicationInstance i) {
		return instanceId.compareTo(i.instanceId);
	}
}
