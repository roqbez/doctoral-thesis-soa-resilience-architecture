package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import br.ufsc.gsigma.catalog.services.model.DeploymentServer;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider;

@Root
public class BMInfrastructureProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String id;

	@Attribute
	private String name;

	@Attribute
	private String serverType;

	@ElementList(name = "DeploymentServers")
	private List<BMDeploymentServer> deploymentServers = new ArrayList<BMDeploymentServer>();

	@ElementList(name = "OrchestratorServers", required = false)
	private List<BMDeploymentServer> orchestratorServers = new ArrayList<BMDeploymentServer>();

	public BMInfrastructureProvider() {
	}

	public BMInfrastructureProvider(InfrastructureProvider provider) {
		this.id = provider.getId();
		this.name = provider.getName();
		this.serverType = provider.getServerType() != null ? provider.getServerType().toString() : null;

		if (!CollectionUtils.isEmpty(provider.getDeploymentServers())) {
			for (DeploymentServer s : provider.getDeploymentServers()) {
				deploymentServers.add(new BMDeploymentServer(s));
			}
		}

		if (!CollectionUtils.isEmpty(provider.getOrchestratorServers())) {
			for (DeploymentServer s : provider.getOrchestratorServers()) {
				orchestratorServers.add(new BMDeploymentServer(s));
			}
		}
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

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public List<BMDeploymentServer> getDeploymentServers() {
		return deploymentServers;
	}

	public void setDeploymentServers(List<BMDeploymentServer> deploymentServers) {
		this.deploymentServers = deploymentServers;
	}

	public List<BMDeploymentServer> getOrchestratorServers() {
		return orchestratorServers;
	}

	public void setOrchestratorServers(List<BMDeploymentServer> orchestratorServers) {
		this.orchestratorServers = orchestratorServers;
	}

}