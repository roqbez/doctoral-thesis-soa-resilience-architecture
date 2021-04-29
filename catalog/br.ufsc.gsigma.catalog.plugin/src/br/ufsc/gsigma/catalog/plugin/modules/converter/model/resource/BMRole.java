package br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource;

public class BMRole extends BMAbstractResource {

	public BMRole() {

	}

	public BMRole(String name) {
		setName(name);
	}

	@Override
	public String getType() {
		return "Role";
	}

}
