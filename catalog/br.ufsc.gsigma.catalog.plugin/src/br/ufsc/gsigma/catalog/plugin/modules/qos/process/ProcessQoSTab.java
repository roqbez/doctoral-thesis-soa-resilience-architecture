package br.ufsc.gsigma.catalog.plugin.modules.qos.process;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process.ProcessServiceAssociationTab;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTab;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.ui.framework.WidgetFactory;

public class ProcessQoSTab extends AbstractCatalogTab {

	private ProcessServiceAssociationTab processServiceAssociationTab;

	public ProcessQoSTab(ProcessServiceAssociationTab processServiceAssociationTab) {
		this.processServiceAssociationTab = processServiceAssociationTab;
	}

	public String getName() {
		return "End-to-end QoS";
	}

	@Override
	public Control createControl(Composite paramComposite, WidgetFactory paramWidgetFactory) {
		return super.createControl(paramComposite, paramWidgetFactory);
	}
	
	@Override
	protected AbstractContentSection createSection(WidgetFactory ivFactory) {
		return new ProcessQoSDetailsSection(this.ivFactory, processServiceAssociationTab);
	}

}
