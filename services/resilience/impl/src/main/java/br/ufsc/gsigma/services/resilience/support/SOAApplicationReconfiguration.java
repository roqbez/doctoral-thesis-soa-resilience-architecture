package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SOAApplicationReconfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private SOAApplication application;

	private List<SOAApplicationInstance> applicationInstances;

	private Collection<SOAService> availableServices = new CopyOnWriteArrayList<SOAService>();

	private Collection<SOAService> unavailableServices = new CopyOnWriteArrayList<SOAService>();

	public SOAApplicationReconfiguration(SOAApplication application, List<SOAApplicationInstance> applicationInstances) {
		this.application = application;
		this.applicationInstances = applicationInstances;
	}

	public SOAApplication getApplication() {
		return application;
	}

	public List<SOAApplicationInstance> getApplicationInstances() {
		return applicationInstances;
	}

	public Collection<SOAService> getAvailableServices() {
		return availableServices;
	}

	public Collection<SOAService> getUnavailableServices() {
		return unavailableServices;
	}

}
