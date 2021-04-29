package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;
import java.util.List;

public class BMFork extends BMAbstractComponent {
	
	private static final long serialVersionUID = 1L;

	private List<BMInputBranch> inputBranches = new ArrayList<BMInputBranch>();

	private List<BMOutputBranch> outputBranches = new ArrayList<BMOutputBranch>();

	public BMFork() {
	}

	public BMFork(String name) {
		this.name = name;
	}

	public List<BMInputBranch> getInputBranches() {
		return inputBranches;
	}

	public void setInputBranches(List<BMInputBranch> inputBranches) {
		this.inputBranches = inputBranches;
	}

	public List<BMOutputBranch> getOutputBranches() {
		return outputBranches;
	}

	public void setOutputBranches(List<BMOutputBranch> outputBranches) {
		this.outputBranches = outputBranches;
	}

	@Override
	protected void buidDefaultContactPoints() {
		inputContactPoint.add(new BMInputContactPoint(this, "Input"));
		outputContactPoint.add(new BMOutputContactPoint(this, "Output1"));
		outputContactPoint.add(new BMOutputContactPoint(this, "Output2"));
	}

}