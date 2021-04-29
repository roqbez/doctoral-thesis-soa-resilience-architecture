package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import br.ufsc.gsigma.services.resilience.events.ServiceUnavailableEvent;

public class AnalysisRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private SOAApplication application;

	private List<SOAApplicationInstance> applicationInstances;

	@SuppressWarnings("unchecked")
	private Collection<SOAService> services = Collections.EMPTY_LIST;

	private ServiceUnavailableEvent serviceUnavailableEvent;

	public AnalysisRequest(SOAApplication application) {
		this.application = application;
	}

	public AnalysisRequest(SOAApplication application, List<SOAApplicationInstance> applicationInstances, SOAService service) {
		this(application, applicationInstances, service, null);
	}

	public AnalysisRequest(SOAApplication application, List<SOAApplicationInstance> applicationInstances, SOAService service, ServiceUnavailableEvent serviceUnavailableEvent) {
		this.application = application;
		this.applicationInstances = applicationInstances;
		this.services = Collections.singleton(service);
		this.serviceUnavailableEvent = serviceUnavailableEvent;
	}

	public AnalysisRequest(SOAApplication application, Collection<SOAService> services) {
		this.application = application;
		this.services = services;
	}

	public SOAApplication getApplication() {
		return application;
	}

	public void setApplication(SOAApplication application) {
		this.application = application;
	}

	public ServiceUnavailableEvent getServiceUnavailableEvent() {
		return serviceUnavailableEvent;
	}

	public List<SOAApplicationInstance> getApplicationInstances() {
		return applicationInstances;
	}

	public void setApplicationInstances(List<SOAApplicationInstance> applicationInstances) {
		this.applicationInstances = applicationInstances;
	}

	public Collection<SOAService> getServices() {
		return services;
	}

	public void setServices(Collection<SOAService> services) {
		this.services = services;
	}

}
