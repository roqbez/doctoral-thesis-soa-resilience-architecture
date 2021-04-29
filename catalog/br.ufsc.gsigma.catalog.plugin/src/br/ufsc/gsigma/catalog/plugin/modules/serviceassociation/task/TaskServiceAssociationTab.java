package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.task;

import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTab;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.ui.framework.WidgetFactory;

public class TaskServiceAssociationTab extends AbstractCatalogTab {

	public String getName() {
		return "Service Association";
	}

	@Override
	protected AbstractContentSection createSection(WidgetFactory ivFactory) {
		return new TaskServiceAssociationDetailsSection(this.ivFactory);
	}

}
