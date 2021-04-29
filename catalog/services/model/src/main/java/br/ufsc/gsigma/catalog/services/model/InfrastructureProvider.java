package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class InfrastructureProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum InfrastructureServerType {
		DOCKER
	}

	private String id;

	private String name;

	private InfrastructureServerType serverType;

	@XmlElementWrapper(name = "deploymentServer")
	private List<DeploymentServer> deploymentServers = new ArrayList<DeploymentServer>();

	@XmlElementWrapper(name = "orchestratorServer")
	private List<DeploymentServer> orchestratorServers = new ArrayList<DeploymentServer>();

	public InfrastructureProvider() {

	}

	public InfrastructureProvider(String name, InfrastructureServerType serverType, List<DeploymentServer> deploymentServers, List<DeploymentServer> orchestratorServers) {
		this.name = name;
		this.serverType = serverType;
		this.deploymentServers = deploymentServers;
		this.orchestratorServers = orchestratorServers;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InfrastructureServerType getServerType() {
		return serverType;
	}

	public void setServerType(InfrastructureServerType serverType) {
		this.serverType = serverType;
	}

	public List<DeploymentServer> getDeploymentServers() {
		return deploymentServers;
	}

	public void setDeploymentServers(List<DeploymentServer> deploymentServers) {
		this.deploymentServers = deploymentServers;
	}

	public List<DeploymentServer> getOrchestratorServers() {
		return orchestratorServers;
	}

	public void setOrchestratorServers(List<DeploymentServer> orchestratorServers) {
		this.orchestratorServers = orchestratorServers;
	}

}
