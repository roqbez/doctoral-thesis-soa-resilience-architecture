package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBStringMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceContainer implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PARAM_SERVICE_CLASSIFICATION = "SERVICE_CLASSIFICATION";

	public static final String PARAM_SERVICE_PATH = "SERVICE_PATH";

	public static final String PARAM_SERVICE_HOST = "HOST";

	public static final String PARAM_SERVICE_PORT = "PORT";

	private String containerId;

	private String taskId;

	private String containerName;

	private String serviceId;

	@XmlElementWrapper
	@XmlElement(name = "containerPortMapping")
	private String[] containerPortMapping;

	@XmlJavaTypeAdapter(JAXBStringMapAttributeAdapter.class)
	private Map<String, String> containerVariables;

	private String imageName;

	private String deploymentServer;

	private String serviceClassification;

	public String getServicePath() {
		return containerVariables != null ? containerVariables.get(PARAM_SERVICE_PATH) : null;
	}

	public int getServicePort() {
		return containerVariables != null ? Integer.valueOf(containerVariables.get(PARAM_SERVICE_PORT)) : null;
	}

	public String getServiceEndpointURL() {
		return getServiceEndpointURL(deploymentServer);
	}

	public String getServiceEndpointURL(String hostName) {
		return "http://" + hostName + ":" + getServicePort() + "/services/" + getServicePath();
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String[] getContainerPortMapping() {
		return containerPortMapping;
	}

	public void setContainerPortMapping(String[] containerPortMapping) {
		this.containerPortMapping = containerPortMapping;
	}

	public Map<String, String> getContainerVariables() {
		return containerVariables;
	}

	public void setContainerVariables(Map<String, String> containerVariables) {
		this.containerVariables = containerVariables;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getDeploymentServer() {
		return deploymentServer;
	}

	public void setDeploymentServer(String deploymentServer) {
		this.deploymentServer = deploymentServer;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public String toString() {
		return "[containerId=" + containerId + (containerName != null ? ", containerName=" + containerName : "") + ", serviceClassification=" + serviceClassification + ", deploymentServer=" + deploymentServer + "]";
	}

}
