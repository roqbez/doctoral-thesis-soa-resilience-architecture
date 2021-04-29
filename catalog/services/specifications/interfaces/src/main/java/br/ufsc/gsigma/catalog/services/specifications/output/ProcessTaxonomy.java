package br.ufsc.gsigma.catalog.services.specifications.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class ProcessTaxonomy implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String name;

	@Attribute
	private String taxonomyClassification;

	@ElementList
	private List<ProcessTaxonomy> childs = new ArrayList<ProcessTaxonomy>();

	@ElementList
	private List<ProcessTaskTaxonomy> tasks = new ArrayList<ProcessTaskTaxonomy>();

	@Element(required = false)
	private ProcessTaxonomy parent;

	@Attribute(required = false)
	private String taxonomyName;

	public ProcessTaxonomy() {

	}

	public ProcessTaxonomy(String name) {
		setName(name);
	}

	public ProcessTaxonomy(String name, String taxonomyName) {
		this.taxonomyName = taxonomyName;
		setName(name);
	}

	public ProcessTaxonomy(ProcessTaxonomy parent, String name) {
		setParent(parent);
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (name != null)
			if (parent != null)
				this.taxonomyClassification = parent.getTaxonomyClassification() + "/" + name.toLowerCase().replaceAll(" ", "");
			else if (taxonomyName != null)
				this.taxonomyClassification = taxonomyName.toLowerCase().replaceAll(" ", "");
			else
				taxonomyClassification = null;

	}

	public String getTaxonomyClassification() {
		return taxonomyClassification;
	}

	public void setTaxonomyClassification(String taxonomyClassification) {
		this.taxonomyClassification = taxonomyClassification;
	}

	public String getTaxonomyName() {
		return taxonomyName;
	}

	public void setTaxonomyName(String taxonomyName) {
		this.taxonomyName = taxonomyName;
	}

	public List<ProcessTaxonomy> getChilds() {
		return childs;
	}

	public void setChilds(List<ProcessTaxonomy> childs) {
		this.childs = childs;
	}

	public List<ProcessTaskTaxonomy> getTasks() {
		return tasks;
	}

	public void setTasks(List<ProcessTaskTaxonomy> tasks) {
		this.tasks = tasks;
	}

	@XmlTransient
	public ProcessTaxonomy getParent() {
		return parent;
	}

	public void setParent(ProcessTaxonomy parent) {
		this.parent = parent;
	}

	public ProcessTaxonomy addChild(String name) {
		ProcessTaxonomy p = new ProcessTaxonomy(this, name);
		childs.add(p);
		return this;
	}

	public ProcessTaxonomy addChildWithSubChilds(String name) {
		ProcessTaxonomy p = new ProcessTaxonomy(this, name);
		childs.add(p);
		return p;
	}

	public ProcessTaxonomy addTask(String name, String participantName) {
		ProcessTaskTaxonomy p = new ProcessTaskTaxonomy(this, name, participantName);
		tasks.add(p);
		return this;
	}

}
