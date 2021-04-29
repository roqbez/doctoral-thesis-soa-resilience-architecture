package br.ufsc.gsigma.catalog.plugin.util.ui;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.ibm.btools.blm.ui.attributesview.content.AbstractContentPage;
import com.ibm.btools.blm.ui.attributesview.content.AbstractContentSection;
import com.ibm.btools.blm.ui.attributesview.content.common.AttributesviewUtil;
import com.ibm.btools.ui.framework.WidgetFactory;

public abstract class AbstractCatalogTab extends AbstractContentPage {

	protected AbstractContentSection ivSection = null;

	private Control ivSectionControl = null;

	protected abstract AbstractContentSection createSection(WidgetFactory ivFactory);

	public abstract String getName();

	public String getProfileElementId() {
		return null;
	}

	public AbstractContentSection getSection() {
		return ivSection;
	}
	
	

	public void createClientArea(Composite paramComposite) {

		if (this.ivSection == null)
			this.ivSection = createSection(this.ivFactory);
		if (this.ivSectionControl == null)
			this.ivSectionControl = this.ivSection.createControl(paramComposite);
		// this.ivSection.setGridData(this.ivSectionControl);
		// createInfoArea(paramComposite);
		this.sections.add(0, this.ivSection);
	}

	public String setInput(Object paramObject) {

		if (AttributesviewUtil.isValidModel(paramObject)) {
			super.setInput(paramObject);
			this.ivSection.setModelAccessor(this.ivModelAccessor);
			this.ivSection.refresh();

			setSectionVisible(this.sections);
		} else {
			setSectionInvisible(this.sections);
		}
		return this.pageTitle;
	}

	@SuppressWarnings("rawtypes")
	protected void setSectionVisible(List paramList) {
		int i = paramList.size();
		if (i > 0) {
			for (int j = 0; j < i; j++) {
				((AbstractContentSection) paramList.get(j)).getControl().getParent().setVisible(true);
			}
			ivPageComposite.layout();
		}
	}

	@SuppressWarnings("rawtypes")
	protected void setSectionInvisible(List paramList) {
		int i = paramList.size();
		if (i > 0) {
			for (int j = 0; j < i; j++) {
				((AbstractContentSection) paramList.get(j)).getControl().getParent().setVisible(false);
			}
			ivPageComposite.layout();
		}
	}

}
