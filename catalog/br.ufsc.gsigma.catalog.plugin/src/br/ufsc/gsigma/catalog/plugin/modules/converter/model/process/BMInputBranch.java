package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;
import java.util.List;

public class BMInputBranch {

	private String name;

	private List<BMInputContactPoint> inputContactPoints = new ArrayList<BMInputContactPoint>();

	public BMInputBranch() {

	}

	public BMInputBranch(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<BMInputContactPoint> getInputContactPoints() {
		return inputContactPoints;
	}

	public void setInputContactPoints(List<BMInputContactPoint> inputContactPoints) {
		this.inputContactPoints = inputContactPoints;
	}

}
