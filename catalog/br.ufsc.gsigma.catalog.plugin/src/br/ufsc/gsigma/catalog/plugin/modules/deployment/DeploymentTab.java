package br.ufsc.gsigma.catalog.plugin.modules.deployment;

import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTab;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.ui.framework.WidgetFactory;

public class DeploymentTab extends AbstractCatalogTab {

	public String getName() {
		return "Deployment";
	}

	@Override
	protected AbstractContentSection createSection(WidgetFactory ivFactory) {
		return new DeploymentDetailsSection(this.ivFactory);
	}

}
