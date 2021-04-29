package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;
import java.util.List;

public class BMProcessModel {

	private List<BMProcessCatalog> processCatalogs = new ArrayList<BMProcessCatalog>();

	public BMProcessCatalog addProcessCatalog(String id, String name) {
		BMProcessCatalog processCatalog = new BMProcessCatalog(id, name);
		processCatalogs.add(processCatalog);
		return processCatalog;
	}

	public List<BMProcessCatalog> getProcessCatalogs() {
		return processCatalogs;
	}

	public void setProcessCatalogs(List<BMProcessCatalog> processCatalogs) {
		this.processCatalogs = processCatalogs;
	}

}
