package br.ufsc.gsigma.binding.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BindingConfigurationRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private String applicationId;

	@XmlElement
	private List<String> applicationInstanceIds;

	@XmlElementWrapper
	@XmlElement(name = "serviceEndpoint")
	private List<ServiceEndpointInfo> serviceEndpoints = new LinkedList<ServiceEndpointInfo>();

	private ResilienceInfo resilienceInfo;

	private ExecutionContext executionContext;

	public BindingConfigurationRequest() {
	}

	public void addServiceEndpoint(ServiceEndpointInfo endpoint) {
		if (serviceEndpoints == null)
			serviceEndpoints = new LinkedList<ServiceEndpointInfo>();
		serviceEndpoints.add(endpoint);
	}

	public void addServiceEndpoints(Collection<ServiceEndpointInfo> endpoints) {
		if (serviceEndpoints == null)
			serviceEndpoints = new LinkedList<ServiceEndpointInfo>();
		serviceEndpoints.addAll(endpoints);
	}

	public BindingConfigurationRequest(String applicationId, ExecutionContext executionContext) {
		this.applicationId = applicationId;
		this.executionContext = executionContext;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public List<String> getApplicationInstanceIds() {
		return applicationInstanceIds;
	}

	public void setApplicationInstanceIds(List<String> applicationInstanceIds) {
		this.applicationInstanceIds = applicationInstanceIds;
	}

	public List<ServiceEndpointInfo> getServiceEndpoints() {
		return serviceEndpoints;
	}

	public void setServiceEndpoints(List<ServiceEndpointInfo> serviceEndpoints) {
		this.serviceEndpoints = serviceEndpoints;
	}

	public ResilienceInfo getResilienceInfo() {
		return resilienceInfo;
	}

	public void setResilienceInfo(ResilienceInfo resilienceInfo) {
		this.resilienceInfo = resilienceInfo;
	}

	public Map<String, Collection<ServiceEndpointInfo>> getServicesPerNamespace() {

		Map<String, Collection<ServiceEndpointInfo>> mapNamespaceToServices = new LinkedHashMap<String, Collection<ServiceEndpointInfo>>();

		for (ServiceEndpointInfo service : serviceEndpoints) {
			String namespace = service.getServiceNamespace();
			Collection<ServiceEndpointInfo> services = mapNamespaceToServices.get(namespace);
			if (services == null) {
				services = new LinkedHashSet<ServiceEndpointInfo>();
				mapNamespaceToServices.put(namespace, services);
			}
			services.add(service);
		}
		return mapNamespaceToServices;
	}

	@Override
	public String toString() {
		return "BindingConfigurationRequest [applicationId=" + applicationId + "]";
	}

}
