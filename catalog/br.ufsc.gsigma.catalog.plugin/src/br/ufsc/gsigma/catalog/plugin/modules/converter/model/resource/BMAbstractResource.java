package br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource;

public abstract class BMAbstractResource {

	private String name;

	public BMAbstractResource() {
	}
	
	public abstract String getType();

	public BMAbstractResource(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
