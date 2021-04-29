package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process;

import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTab;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.ui.framework.WidgetFactory;

public class ProcessServiceAssociationTab extends AbstractCatalogTab {

	public String getName() {
		return "Service Discovery and Composition";
	}

	@Override
	protected AbstractContentSection createSection(WidgetFactory ivFactory) {
		return new ProcessServiceAssociationDetailsSection(this.ivFactory);
	}

}
