package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBStringMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class PlatformManagedServiceDeploymentRequestItem implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum PortPublishMode {
		HOST, INGRESS
	}

	private String serviceName;

	private int replicas = 1;

	private PortPublishMode portPublishMode;

	private List<String> placementConstraints = new ArrayList<String>();

	@XmlElementWrapper
	@XmlElement(name = "servicePortMapping")
	private String[] servicePortMapping;

	@XmlJavaTypeAdapter(JAXBStringMapAttributeAdapter.class)
	private Map<String, String> containerVariables = new HashMap<String, String>();

	private String imageName;

	private String serviceClassification;

	public PortPublishMode getPortPublishMode() {
		return portPublishMode;
	}

	public void setPortPublishMode(PortPublishMode portPublishMode) {
		this.portPublishMode = portPublishMode;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	public List<String> getPlacementConstraints() {
		return placementConstraints;
	}

	public void setPlacementConstraints(List<String> placementConstraints) {
		this.placementConstraints = placementConstraints;
	}

	public String[] getServicePortMapping() {
		return servicePortMapping;
	}

	public void setServicePortMapping(String[] servicePortMapping) {
		this.servicePortMapping = servicePortMapping;
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

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	@Override
	public String toString() {
		return "[serviceName=" + serviceName + ", imageName=" + imageName + ", serviceClassification=" + serviceClassification + ", replicas=" + replicas + ", servicePortMapping=" + Arrays.toString(servicePortMapping) + ", portPublishMode=" + portPublishMode + ", containerVariables="
				+ containerVariables + ", placementConstraints=" + placementConstraints + "]";
	}

}