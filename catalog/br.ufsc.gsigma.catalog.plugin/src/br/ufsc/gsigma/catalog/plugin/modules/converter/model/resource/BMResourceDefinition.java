package br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource;

public class BMResourceDefinition extends BMAbstractResource {

	public BMResourceDefinition() {

	}

	public BMResourceDefinition(String name) {
		setName(name);
	}

	@Override
	public String getType() {
		return "ResourceDefinition";
	}
}
