package br.ufsc.gsigma.catalog.plugin.util.ui;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.ui.framework.WidgetFactory;

public class AbstractCatalogTabContentSection extends AbstractContentSection {

	public AbstractCatalogTabContentSection(WidgetFactory paramWidgetFactory) {
		super(paramWidgetFactory);
	}

	@Override
	public Control createControl(Composite paramComposite) {

		ivParent = paramComposite;

		if (isClipped()) {
			ivSectionControl = ivFactory.createClippedComposite(ivParent);
		} else {
			ivSectionControl = ivFactory.createComposite(ivParent);
		}
		ivSectionControl.setLayout(new GridLayout());
		ivSectionControl.setLayoutData(new GridData(GridData.FILL_BOTH));

		ivSeparator = ivFactory.createComposite(ivSectionControl);
		ivSeparator.setBackground(ivFactory.getColor("SectionSeparator"));

		ivClientArea = ivFactory.createComposite(ivSectionControl);
		ivClientArea.setBackground(ivSectionControl.getBackground());
		ivClientArea.setLayout(new GridLayout());
		ivClientArea.setLayoutData(new GridData(GridData.FILL_BOTH));

		createClient(ivClientArea);

		refresh();

		return ivSectionControl;
	}

}
