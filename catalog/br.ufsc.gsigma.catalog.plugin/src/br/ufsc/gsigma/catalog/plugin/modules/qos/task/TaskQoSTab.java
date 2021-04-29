package br.ufsc.gsigma.catalog.plugin.modules.qos.task;

import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTab;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.ui.framework.WidgetFactory;

public class TaskQoSTab extends AbstractCatalogTab {

	public String getName() {
		return "QoS Constraints";
	}

	@Override
	protected AbstractContentSection createSection(WidgetFactory ivFactory) {
		return new TaskQoSDetailsSection(this.ivFactory);
	}
}
