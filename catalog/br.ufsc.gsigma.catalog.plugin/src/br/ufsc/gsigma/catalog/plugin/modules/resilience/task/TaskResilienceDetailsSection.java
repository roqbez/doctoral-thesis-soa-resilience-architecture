package br.ufsc.gsigma.catalog.plugin.modules.resilience.task;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTabContentSection;

public class TaskResilienceDetailsSection extends AbstractCatalogTabContentSection {

	private TaskResilienceUI taskResilienceUI;

	public TaskResilienceDetailsSection(WidgetFactory widgetFactory) {
		super(widgetFactory);
	}

	protected Composite createClient(final Composite paramComposite) {

		super.createClient(paramComposite);

		gridData = new GridData(GridData.FILL_BOTH);

		layout = new GridLayout(1, false);

		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		this.taskResilienceUI = new TaskResilienceUI(mainComposite, SWT.NONE);

		return mainComposite;
	}

	@Override
	public void setModelAccessor(ModelAccessor modelAccessor) {
		this.taskResilienceUI.setModelAccessor(modelAccessor);
	}

}
