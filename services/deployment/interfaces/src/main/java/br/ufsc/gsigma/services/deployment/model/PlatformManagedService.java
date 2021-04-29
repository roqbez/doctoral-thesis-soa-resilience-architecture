package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBStringMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class PlatformManagedService implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	private int replicas;

	@XmlElementWrapper
	@XmlElement(name = "container")
	private List<ServiceContainer> containers = new LinkedList<ServiceContainer>();

	@XmlJavaTypeAdapter(JAXBStringMapAttributeAdapter.class)
	private Map<String, String> variables = new HashMap<String, String>();

	public PlatformManagedService() {
	}

	public PlatformManagedService(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getServiceClassification() {
		return variables != null ? variables.get(ServiceContainer.PARAM_SERVICE_CLASSIFICATION) : null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ServiceContainer> getContainers() {
		return containers;
	}

	public void setContainers(List<ServiceContainer> containers) {
		this.containers = containers;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", name=" + name + ", replicas=" + replicas + ", containers=" + containers + "]";
	}

}
