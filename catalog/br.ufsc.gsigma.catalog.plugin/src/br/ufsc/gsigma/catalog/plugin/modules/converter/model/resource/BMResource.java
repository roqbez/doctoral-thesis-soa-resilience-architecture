package br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource;

public class BMResource extends BMAbstractResource {

	public BMResource() {

	}

	public BMResource(String name) {
		setName(name);
	}

	@Override
	public String getType() {
		return "Resource";
	}

}
