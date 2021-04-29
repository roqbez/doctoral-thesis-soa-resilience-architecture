package br.ufsc.gsigma.binding.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import br.ufsc.gsigma.services.model.ServiceEndpointInfo;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BindingConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private String applicationId;

	@XmlElement
	private String bindingToken;

	@XmlElement
	private String serviceMediationEndpoint;

	@XmlElementWrapper
	@XmlElement(name = "serviceConfiguration")
	private List<NamespaceServices> boundServices = new LinkedList<NamespaceServices>();

	private long version;

	public boolean containsService(String serviceKey) {
		if (boundServices != null) {
			for (NamespaceServices n : boundServices) {
				for (ServiceEndpointInfo s : n.getServices()) {
					if (s.getServiceKey().equals(serviceKey))
						return true;
				}
			}
		}
		return false;
	}

	public String getServiceMediationEndpoint() {
		return serviceMediationEndpoint;
	}

	public void setServiceMediationEndpoint(String serviceMediationEndpoint) {
		this.serviceMediationEndpoint = serviceMediationEndpoint;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getBindingToken() {
		return bindingToken;
	}

	public void setBindingToken(String bindingToken) {
		this.bindingToken = bindingToken;
	}

	public List<ServiceEndpointInfo> getAllServices() {

		List<ServiceEndpointInfo> services = new LinkedList<ServiceEndpointInfo>();

		if (boundServices != null) {
			for (NamespaceServices n : boundServices) {
				if (n.getServices() != null)
					services.addAll(n.getServices());
			}
		}
		return services;
	}

	public NamespaceServices getNamespaceServices(String namespace) {

		if (boundServices != null) {
			for (NamespaceServices n : boundServices) {
				if (n.getServiceNamespace().equals(namespace))
					return n;
			}
		}
		return null;
	}

	public List<NamespaceServices> getBoundServices() {
		return boundServices;
	}

	public void setBoundServices(List<NamespaceServices> boundServices) {
		this.boundServices = boundServices;
	}

	@Override
	public String toString() {
		int n = 0;
		for (NamespaceServices s : boundServices) {
			n += s.getServices() != null ? s.getServices().size() : 0;
		}
		return "BindingConfiguration [version=" + version + ", serviceMediationEndpoint=" + serviceMediationEndpoint + ", bindingToken=" + bindingToken + ", boundServices=" + n + "]";
	}

}
