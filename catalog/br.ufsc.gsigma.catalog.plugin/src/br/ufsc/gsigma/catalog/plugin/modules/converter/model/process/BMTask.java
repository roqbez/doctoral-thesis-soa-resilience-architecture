package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class BMTask extends BMAbstractComponent {
	
	private static final long serialVersionUID = 1L;

	public Hashtable<Integer, BMRepository> repositoriesInput = new Hashtable<Integer, BMRepository>();
	public Hashtable<Integer, BMRepository> repositoriesOutput = new Hashtable<Integer, BMRepository>();

	public List<BMAbstractResourceRequisite> resourceRequisites = new ArrayList<BMAbstractResourceRequisite>();

	private BMTaskInformationExtension taskInformationExtension;

	public BMTask() {
	}

	public BMTask(String name) {
		this.name = name;
	}

	protected void buidDefaultContactPoints() {
		inputContactPoint.add(new BMInputContactPoint(this, "Input"));
		outputContactPoint.add(new BMOutputContactPoint(this, "Output"));
	}

	public String getType() {
		return "task";
	}

	public List<BMAbstractResourceRequisite> getResourceRequisites() {
		return resourceRequisites;
	}

	public void setResourceRequisites(List<BMAbstractResourceRequisite> resourceRequisites) {
		this.resourceRequisites = resourceRequisites;
	}

	public BMTaskInformationExtension getTaskInformationExtension() {
		return taskInformationExtension;
	}

	public void setTaskInformationExtension(BMTaskInformationExtension taskInformationExtension) {
		this.taskInformationExtension = taskInformationExtension;
	}

}