package br.ufsc.gsigma.catalog.plugin.attributes;

import org.eclipse.swt.widgets.Composite;

import com.ibm.btools.blm.ui.attributesview.content.general.DescriptionGeneralWithLinksTab;

public class ReadOnlyDescriptionGeneralWithLinksTab extends DescriptionGeneralWithLinksTab {

	@Override
	public void createClientArea(Composite paramComposite) {
		super.createClientArea(paramComposite);
		ivSection.setDescriptionEnabled(false);
	}
}
