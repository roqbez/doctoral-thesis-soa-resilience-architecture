package br.ufsc.gsigma.catalog.plugin.modules.resilience.process;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTabContentSection;

public class ResilienceDetailsSection extends AbstractCatalogTabContentSection {

	private ResilienceUI resilienceUI;

	public ResilienceDetailsSection(WidgetFactory widgetFactory) {
		super(widgetFactory);
	}

	protected Composite createClient(final Composite paramComposite) {

		super.createClient(paramComposite);

		gridData = new GridData(GridData.FILL_BOTH);

		layout = new GridLayout(1, false);

		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		this.resilienceUI = new ResilienceUI(mainComposite, SWT.NONE);

		return mainComposite;
	}

	@Override
	public void setModelAccessor(ModelAccessor modelAccessor) {
		this.resilienceUI.setModelAccessor(modelAccessor);
	}

}
