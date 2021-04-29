package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource.BMAbstractResource;

public abstract class BMAbstractResourceRequisite {

	private String name;

	protected BMAbstractResource resource;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public abstract String getType();

}
