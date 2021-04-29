package br.ufsc.gsigma.catalog.plugin.modules.resilience.process;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTab;

public class ProcessResilienceTab extends AbstractCatalogTab {

	public String getName() {
		return "Resilience";
	}

	@Override
	protected AbstractContentSection createSection(WidgetFactory ivFactory) {
		return new ResilienceDetailsSection(this.ivFactory);
	}

}
