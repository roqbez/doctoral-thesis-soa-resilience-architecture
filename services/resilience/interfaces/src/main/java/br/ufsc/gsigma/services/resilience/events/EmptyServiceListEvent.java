package br.ufsc.gsigma.services.resilience.events;

public class EmptyServiceListEvent extends ResilienceEvent {

	private static final long serialVersionUID = 1L;

	private String serviceNamespace;

	private String applicationId;

	public EmptyServiceListEvent(String serviceNamespace, String applicationId) {
		this.serviceNamespace = serviceNamespace;
		this.applicationId = applicationId;
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public String getApplicationId() {
		return applicationId;
	}

	@Override
	public String toString() {
		return "EmptyServiceListEvent [serviceNamespace=" + serviceNamespace + ", applicationId=" + applicationId + "]";
	}

}
