package br.ufsc.gsigma.services.resilience.model;

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
public class ServicesCheckResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElementWrapper
	@XmlElement(name = "serviceKey")
	private List<String> availableServices = new LinkedList<String>();

	@XmlElementWrapper
	@XmlElement(name = "serviceKey")
	private List<String> unavailableServices = new LinkedList<String>();

	@XmlElementWrapper
	@XmlElement(name = "serviceEndpointURL")
	private List<String> availableServiceEndpointURLs = new LinkedList<String>();

	@XmlElementWrapper
	@XmlElement(name = "serviceEndpointURL")
	private List<String> unavailableServiceEndpointURLs = new LinkedList<String>();

	public boolean isServiceAvailable(ServiceEndpointInfo service) {
		return isServiceAvailable(service.getServiceKey());
	}

	public boolean isServiceAvailable(String serviceKey) {
		return availableServices != null && availableServices.contains(serviceKey);
	}

	public List<String> getAvailableServices() {
		return availableServices;
	}

	public void setAvailableServices(List<String> availableServices) {
		this.availableServices = availableServices;
	}

	public List<String> getUnavailableServices() {
		return unavailableServices;
	}

	public void setUnavailableServices(List<String> unavailableServices) {
		this.unavailableServices = unavailableServices;
	}

	public List<String> getAvailableServiceEndpointURLs() {
		return availableServiceEndpointURLs;
	}

	public void setAvailableServiceEndpointURLs(List<String> availableServiceEndpointURLs) {
		this.availableServiceEndpointURLs = availableServiceEndpointURLs;
	}

	public List<String> getUnavailableServiceEndpointURLs() {
		return unavailableServiceEndpointURLs;
	}

	public void setUnavailableServiceEndpointURLs(List<String> unavailableServiceEndpointURLs) {
		this.unavailableServiceEndpointURLs = unavailableServiceEndpointURLs;
	}

}
