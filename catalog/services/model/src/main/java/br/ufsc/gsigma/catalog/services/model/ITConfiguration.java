package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ITConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private ServicesInformation servicesInformation;

	private boolean persistentProcess = true;

	private ResilienceConfiguration resilienceConfiguration;

	private InfrastructureProvider infrastructureProvider;

	public ITConfiguration() {
	}

	public ITConfiguration(ServicesInformation servicesInformation) {
		this.servicesInformation = servicesInformation;
	}

	public ServicesInformation getServicesInformation() {
		return servicesInformation;
	}

	public void setServicesInformation(ServicesInformation servicesInformation) {
		this.servicesInformation = servicesInformation;
	}

	public boolean isPersistentProcess() {
		return persistentProcess;
	}

	public void setPersistentProcess(boolean persistentProcess) {
		this.persistentProcess = persistentProcess;
	}

	public ResilienceConfiguration getResilienceConfiguration() {
		return resilienceConfiguration;
	}

	public void setResilienceConfiguration(ResilienceConfiguration resilienceConfiguration) {
		this.resilienceConfiguration = resilienceConfiguration;
	}

	public InfrastructureProvider getInfrastructureProvider() {
		return infrastructureProvider;
	}

	public void setInfrastructureProvider(InfrastructureProvider infrastructureProvider) {
		this.infrastructureProvider = infrastructureProvider;
	}

}
