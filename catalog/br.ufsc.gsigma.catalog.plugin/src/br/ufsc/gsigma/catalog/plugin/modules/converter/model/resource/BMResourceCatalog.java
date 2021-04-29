package br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.AbstractCatalog;

public class BMResourceCatalog extends AbstractCatalog {

	private List<BMAbstractResource> resources = new ArrayList<BMAbstractResource>();

	public BMResourceCatalog() {
	}

	public BMResourceCatalog(String id, String name) {
		super(id, name);
	}

	public BMRole addRole(String name) {
		BMRole r = new BMRole(getId() + "##" + name);
		resources.add(r);
		return r;
	}

	public List<BMAbstractResource> getResources() {
		return resources;
	}

	public void setResources(List<BMAbstractResource> resources) {
		this.resources = resources;
	}

	@Override
	public boolean isEmpty() {
		return resources.isEmpty();
	}

}
