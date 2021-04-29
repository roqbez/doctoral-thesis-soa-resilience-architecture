package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class BMAbstractComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	protected ArrayList<BMInputContactPoint> inputContactPoint = new ArrayList<BMInputContactPoint>();
	protected ArrayList<BMOutputContactPoint> outputContactPoint = new ArrayList<BMOutputContactPoint>();
	protected String name;
	protected String type;
	protected int id;

	public BMAbstractComponent() {
		buidDefaultContactPoints();
	}

	public void clearContactPoints() {
		inputContactPoint.clear();
		outputContactPoint.clear();
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return name;
	}

	public BMInputContactPoint addInputContactPoint(String name) {
		BMInputContactPoint contactPoint = new BMInputContactPoint(this, name);
		inputContactPoint.add(contactPoint);
		return contactPoint;
	}

	public BMOutputContactPoint addOutputContactPoint(String name) {
		BMOutputContactPoint contactPoint = new BMOutputContactPoint(this, name);
		outputContactPoint.add(contactPoint);
		return contactPoint;
	}

	public ArrayList<BMInputContactPoint> getInputContactPoint() {
		return inputContactPoint;
	}

	public ArrayList<BMOutputContactPoint> getOutputContactPoint() {
		return outputContactPoint;
	}

	protected abstract void buidDefaultContactPoints();

}