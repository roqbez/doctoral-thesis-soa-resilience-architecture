package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.data.BMBusinessItem;

public abstract class BMAbstractContactPoint {

	private BMAbstractComponent component;
	private String name;

	private BMBusinessItem associatedData;

	public BMAbstractContactPoint() {

	}

	public BMAbstractContactPoint(BMAbstractComponent component, String name) {
		this.component = component;
		this.name = name;
	}

	public BMAbstractComponent getComponent() {
		return component;
	}

	public void setComponent(BMAbstractComponent component) {
		this.component = component;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BMBusinessItem getAssociatedData() {
		return associatedData;
	}

	public void setAssociatedData(BMBusinessItem associatedData) {
		this.associatedData = associatedData;
	}

	@Override
	public String toString() {
		return name;
	}

}
