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
public class ProcessStandardCategory implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String name;

	@Attribute(required = false)
	private String processPath;

	@ElementList
	private List<ProcessStandardCategory> subCategories = new ArrayList<ProcessStandardCategory>();

	@Element(required = false)
	private ProcessStandardCategory parent;

	public ProcessStandardCategory() {

	}

	public ProcessStandardCategory(String name) {
		this.name = name;
	}

	public ProcessStandardCategory(ProcessStandardCategory parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProcessStandardCategory> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(List<ProcessStandardCategory> subCategories) {
		this.subCategories = subCategories;
	}

	public String getProcessPath() {
		return processPath;
	}

	public void setProcessPath(String processPath) {
		this.processPath = processPath;
	}

	@XmlTransient
	public ProcessStandardCategory getParent() {
		return parent;
	}

	public void setParent(ProcessStandardCategory parent) {
		this.parent = parent;
	}

	public ProcessStandardCategory addChild(String name) {
		ProcessStandardCategory p = new ProcessStandardCategory(this, name);
		subCategories.add(p);
		return this;
	}

	public ProcessStandardCategory addChildWithSubChilds(String name) {
		ProcessStandardCategory p = new ProcessStandardCategory(this, name);
		subCategories.add(p);
		return p;
	}

}
