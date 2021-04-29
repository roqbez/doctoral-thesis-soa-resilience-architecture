package br.ufsc.gsigma.services.resilience.events;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.services.model.ServiceEndpointInfo;

public class ServiceUnavailableEvent extends ResilienceEvent {

	private static final long serialVersionUID = 1L;

	private ServiceEndpointInfo service;

	private String applicationId;

	private List<String> applicationInstanceIds = new LinkedList<String>();

	public ServiceUnavailableEvent(ServiceEndpointInfo service, String applicationId, String applicationInstanceId) {
		this.service = service;
		this.applicationId = applicationId;
		this.applicationInstanceIds.add(applicationInstanceId);
	}

	public ServiceUnavailableEvent(ServiceEndpointInfo service, String applicationId, Collection<String> applicationInstanceIds) {
		this.service = service;
		this.applicationId = applicationId;
		this.applicationInstanceIds.addAll(applicationInstanceIds);
	}

	public ServiceEndpointInfo getService() {
		return service;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public List<String> getApplicationInstanceIds() {
		return applicationInstanceIds;
	}

	@Override
	public String toString() {
		return "ServiceUnavailableEvent [service=" + service + ", applicationInstanceIds=" + applicationInstanceIds + "]";
	}

}
