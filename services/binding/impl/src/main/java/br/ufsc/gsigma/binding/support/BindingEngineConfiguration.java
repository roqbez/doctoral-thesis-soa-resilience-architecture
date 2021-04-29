package br.ufsc.gsigma.binding.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;

public class BindingEngineConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private String applicationId;

	private String token;

	private Map<String, Collection<ServiceEndpointInfo>> mapNamespaceToServices = new HashMap<String, Collection<ServiceEndpointInfo>>();

	private long creationTime;

	private long version;

	private ResilienceInfo resilienceInfo;

	private Map<String, BindingEngineConfiguration> applicationInstanceConfigurations = new HashMap<String, BindingEngineConfiguration>();

	private ExecutionContext executionContext;

	public BindingEngineConfiguration(String applicationId, String token, ResilienceInfo resilienceInfo, ExecutionContext executionContext) {
		this.applicationId = applicationId;
		this.token = token;
		this.resilienceInfo = resilienceInfo;
		this.creationTime = System.currentTimeMillis();
		this.executionContext = executionContext;
	}

	public ResilienceInfo getResilienceInfo() {
		return resilienceInfo;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public BindingEngineConfiguration getInstanceBindingConfiguration(String applicationInstanceId) {
		return applicationInstanceConfigurations.get(applicationInstanceId);
	}

	public void addInstanceBindingConfiguration(String applicationInstanceId, BindingEngineConfiguration cfg) {
		applicationInstanceConfigurations.put(applicationInstanceId, cfg);
	}

	public Collection<String> getApplicationInstanceIds() {
		return new TreeSet<String>(applicationInstanceConfigurations.keySet());
	}

	public void addServiceForNamespace(String namespace, ServiceEndpointInfo service) {
		Collection<ServiceEndpointInfo> services = mapNamespaceToServices.get(namespace);
		if (services == null) {
			services = new LinkedHashSet<ServiceEndpointInfo>();
			mapNamespaceToServices.put(namespace, services);
		}
		services.add(service);
	}

	public Map<String, Collection<ServiceEndpointInfo>> getServicesPerNamespace() {
		return Collections.unmodifiableMap(mapNamespaceToServices);
	}

	@SuppressWarnings("unchecked")
	public Collection<ServiceEndpointInfo> getServicesForNamespace(String namespace) {
		Collection<ServiceEndpointInfo> services = mapNamespaceToServices.get(namespace);
		return services != null ? Collections.unmodifiableCollection(services) : Collections.EMPTY_LIST;
	}

	public String getToken() {
		return token;
	}

	public String getApplicationId() {
		return applicationId;
	}

	@Override
	public String toString() {
		return "BindingEngineConfiguration [applicationId=" + applicationId + ", token=" + token + "]";
	}

}
