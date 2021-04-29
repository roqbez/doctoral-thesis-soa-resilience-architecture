package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;
import java.util.List;

public class BMOutputBranch {

	private String name;

	private List<BMOutputContactPoint> outputContactPoints = new ArrayList<BMOutputContactPoint>();

	private String condition;

	private Float probabilityPercentage;

	public BMOutputBranch() {

	}

	public BMOutputBranch(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<BMOutputContactPoint> getOutputContactPoints() {
		return outputContactPoints;
	}

	public void setOutputContactPoints(List<BMOutputContactPoint> outputContactPoints) {
		this.outputContactPoints = outputContactPoints;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Float getProbabilityPercentage() {
		return probabilityPercentage;
	}

	public void setProbabilityPercentage(Float probabilityPercentage) {
		this.probabilityPercentage = probabilityPercentage;
	}

}
