package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServicesDeployment implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElementWrapper
	@XmlElement(name = "serviceContainer")
	private List<ServiceContainer> serviceContainers = new LinkedList<ServiceContainer>();

	@XmlElementWrapper
	@XmlElement(name = "platformManagedService")
	private List<PlatformManagedService> managedServices = new LinkedList<PlatformManagedService>();

	private boolean error;

	private String errorMessage;

	public List<ServiceContainer> getServiceContainers() {
		return serviceContainers;
	}

	public void setServiceContainers(List<ServiceContainer> serviceContainers) {
		this.serviceContainers = serviceContainers;
	}

	public List<PlatformManagedService> getManagedServices() {
		return managedServices;
	}

	public void setManagedServices(List<PlatformManagedService> managedServices) {
		this.managedServices = managedServices;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
