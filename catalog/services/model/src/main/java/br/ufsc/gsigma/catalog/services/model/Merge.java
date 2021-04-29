package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "merge")
@XmlRootElement(name = "merge")
public class Merge extends FlowControlComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_process", nullable = false)
	private Process process;

	public Merge() {

	}

	public Merge(Process process, String name) {
		this.process = process;
		this.name = name;
	}

	@Override
	public Process getProcess() {
		return process;
	}

	@Override
	public void setProcess(Process process) {
		this.process = process;
	}

	@Override
	public ConnectableComponent buildDefaultContactPoints() {
		inputContactPoints.add(new InputContactPoint(this, "Input1"));
		inputContactPoints.add(new InputContactPoint(this, "Input2"));
		outputContactPoints.add(new OutputContactPoint(this, "Output"));
		return this;
	}

}
