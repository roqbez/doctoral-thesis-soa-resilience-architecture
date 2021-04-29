package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource.BMRole;

public class BMRoleResourceRequisite extends BMAbstractResourceRequisite {

	public BMRoleResourceRequisite() {
	}

	public BMRoleResourceRequisite(String name, BMRole role) {
		setName(name);
		this.resource = role;
	}

	public BMRole getRole() {
		return (BMRole) resource;
	}

	public void setRole(BMRole role) {
		this.resource = role;

	}

	@Override
	public String getType() {
		return "Role";
	}

}
