package br.ufsc.gsigma.services.events;

import br.ufsc.gsigma.infrastructure.util.messaging.Event;

public class ServiceStoppedEvent extends Event {

	private static final long serialVersionUID = 1L;

	private String serviceNamespace;

	private String serviceEndpointURL;

	public ServiceStoppedEvent(String serviceNamespace, String serviceEndpointURL) {
		this.serviceNamespace = serviceNamespace;
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public String getServiceEndpointURL() {
		return serviceEndpointURL;
	}

}
