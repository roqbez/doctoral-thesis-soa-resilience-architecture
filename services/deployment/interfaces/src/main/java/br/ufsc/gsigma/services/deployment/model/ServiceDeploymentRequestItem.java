package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBStringMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDeploymentRequestItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String containerName;

	@XmlElementWrapper
	@XmlElement(name = "containerPortMapping")
	private String[] containerPortMapping;

	@XmlJavaTypeAdapter(JAXBStringMapAttributeAdapter.class)
	private Map<String, String> containerVariables = new HashMap<String, String>();

	private String imageName;

	private String serviceClassification;

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public Map<String, String> getContainerVariables() {
		return containerVariables;
	}

	public void setContainerVariables(Map<String, String> containerVariables) {
		this.containerVariables = containerVariables;
	}

	public String[] getContainerPortMapping() {
		return containerPortMapping;
	}

	public void setContainerPortMapping(String[] containerPortMapping) {
		this.containerPortMapping = containerPortMapping;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

}