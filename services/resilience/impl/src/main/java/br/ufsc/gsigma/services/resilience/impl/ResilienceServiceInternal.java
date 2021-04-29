package br.ufsc.gsigma.services.resilience.impl;

import java.util.Collection;
import java.util.List;

import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.support.SOAApplication;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationInstance;
import br.ufsc.gsigma.services.resilience.support.SOAService;

public interface ResilienceServiceInternal {

	public Collection<SOAService> getSOAServices(Collection<ServiceEndpointInfo> services);

	public SOAService getSOAService(ServiceEndpointInfo e);

	public void monitorSOAApplication(SOAApplication application, BindingConfiguration bindingConfiguration);

	public void monitorSOAApplication(SOAApplication application, Collection<ServiceEndpointInfo> boundServices);

	public void monitorSOAApplication(SOAApplication application);

	public void monitorSOAApplications(Collection<SOAApplication> applications);

	public void stopMonitoringSOAApplication(SOAApplication application);

	public SOAApplication getSOAApplication(String applicationId);

	public void receiveBusEvent(Event event) throws Exception;

	public Object getLockMonitor();

	public Collection<SOAApplication> getSOAApplications(SOAService service);

	public SOAApplicationInstance getSOAApplicationInstance(SOAApplication application, Long processInstanceId);

	public List<SOAApplicationInstance> getSOAApplicationInstances(String applicationId, boolean onlyRunning);

	public List<String> getSOAApplicationIds();

	public Collection<String> getSOAApplicationIds(SOAService service);

}
