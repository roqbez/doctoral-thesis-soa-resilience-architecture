package br.ufsc.gsigma.catalog.plugin.modules.converter.model;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.data.BMDataModel;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessModel;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource.BMResourceModel;

public class ProjectDefinition {

	private BMResourceModel resourceModel = new BMResourceModel();

	private BMProcessModel processModel = new BMProcessModel();

	private BMDataModel dataModel = new BMDataModel();

	public ProjectDefinition() {
		resourceModel.addResourceCatalog("Resources", "Resources");
		processModel.addProcessCatalog("Processes", "Processes");
		dataModel.addDataCatalog("Documents", "Documents");
	}

	public BMResourceModel getResourceModel() {
		return resourceModel;
	}

	public void setResourceModel(BMResourceModel resourceModel) {
		this.resourceModel = resourceModel;
	}

	public BMProcessModel getProcessModel() {
		return processModel;
	}

	public void setProcessModel(BMProcessModel processModel) {
		this.processModel = processModel;
	}

	public BMDataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(BMDataModel dataModel) {
		this.dataModel = dataModel;
	}

}
