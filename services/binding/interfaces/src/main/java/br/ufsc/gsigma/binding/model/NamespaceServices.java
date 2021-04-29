package br.ufsc.gsigma.binding.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import br.ufsc.gsigma.services.model.ServiceEndpointInfo;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)

public class NamespaceServices implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private String serviceNamespace;

	@XmlElementWrapper
	@XmlElement(name = "service")
	private Collection<ServiceEndpointInfo> services = new LinkedList<ServiceEndpointInfo>();

	public NamespaceServices() {
	}

	public NamespaceServices(String serviceNamespace, Collection<ServiceEndpointInfo> services) {
		this.serviceNamespace = serviceNamespace;
		this.services.addAll(services);
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
	}

	public Collection<ServiceEndpointInfo> getServices() {
		return services;
	}

	public void setServices(Collection<ServiceEndpointInfo> services) {
		this.services = services;
	}

}
