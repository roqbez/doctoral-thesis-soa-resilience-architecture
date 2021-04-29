package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import br.ufsc.gsigma.catalog.services.model.DeploymentServer;

@Root
public class BMDeploymentServer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String name;

	@Attribute
	private String address;

	public BMDeploymentServer() {
	}

	public BMDeploymentServer(DeploymentServer server) {
		this.name = server.getName();
		this.address = server.getAddress();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
