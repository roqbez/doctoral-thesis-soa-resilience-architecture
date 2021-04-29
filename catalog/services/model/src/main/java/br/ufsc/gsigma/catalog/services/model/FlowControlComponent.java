package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Root
public abstract class FlowControlComponent extends ConnectableComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy = "flowControlComponent", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "inputBranch")
	@ElementList
	private List<InputBranch> inputBranches = new ArrayList<InputBranch>();

	@OneToMany(mappedBy = "flowControlComponent", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "outputBranch")
	@ElementList
	private List<OutputBranch> outputBranches = new ArrayList<OutputBranch>();

	public FlowControlComponent() {

	}

	@XmlTransient
	public abstract Process getProcess();

	public abstract void setProcess(Process process);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<InputBranch> getInputBranches() {
		return inputBranches;
	}

	public void setInputBranches(List<InputBranch> inputBranches) {
		this.inputBranches = inputBranches;
	}

	public List<OutputBranch> getOutputBranches() {
		return outputBranches;
	}

	public void setOutputBranches(List<OutputBranch> outputBranches) {
		this.outputBranches = outputBranches;
	}

}
