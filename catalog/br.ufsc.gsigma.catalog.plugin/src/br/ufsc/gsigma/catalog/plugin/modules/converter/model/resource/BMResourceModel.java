package br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource;

import java.util.ArrayList;
import java.util.List;

public class BMResourceModel {

	private List<BMResourceCatalog> resourceCatalogs = new ArrayList<BMResourceCatalog>();

	public BMResourceCatalog addResourceCatalog(String id, String name) {
		BMResourceCatalog resourceCatalog = new BMResourceCatalog(id, name);
		resourceCatalogs.add(resourceCatalog);
		return resourceCatalog;
	}

	public List<BMResourceCatalog> getResourceCatalogs() {
		return resourceCatalogs;
	}

	public void setResourceCatalogs(List<BMResourceCatalog> resourceCatalogs) {
		this.resourceCatalogs = resourceCatalogs;
	}

}
