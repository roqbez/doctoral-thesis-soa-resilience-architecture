package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import br.ufsc.gsigma.common.hash.HashUtil.NotHash;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "task")
@XmlRootElement(name = "task")
public class Task extends ConnectableComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "ontology_classification")
	@Attribute(required = false)
	protected String taxonomyClassification;

	@OneToMany(mappedBy = "task", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "taskParticipant")
	@ElementList(required = false)
	protected List<TaskParticipant> participants = new ArrayList<TaskParticipant>();

	@OneToMany(mappedBy = "task", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "taskResource")
	@ElementList(required = false)
	protected List<TaskResource> resources = new ArrayList<TaskResource>();

	@NotHash
	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_parent_process", nullable = true)
	private Process process;

	public Task() {
	}

	public Task(Process process, String name) {
		this.process = process;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TaskParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<TaskParticipant> participants) {
		this.participants = participants;
	}

	public List<TaskResource> getResources() {
		return resources;
	}

	public void setResources(List<TaskResource> resources) {
		this.resources = resources;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public String getTaxonomyClassification() {
		return taxonomyClassification;
	}

	public void setTaxonomyClassification(String taxonomyClassification) {
		this.taxonomyClassification = taxonomyClassification;
	}

	@Override
	public ConnectableComponent buildDefaultContactPoints() {
		inputContactPoints.add(new InputContactPoint(this, "Input"));
		outputContactPoints.add(new OutputContactPoint(this, "Output"));
		return this;
	}

	public Task addParticipant(String function, Participant participant) {
		TaskParticipant taskParticipant = new TaskParticipant();
		taskParticipant.setName(function);
		taskParticipant.setTask(this);
		taskParticipant.setParticipant(participant);
		participants.add(taskParticipant);
		return this;
	}

	public Task addResource(String function, Resource resource) {
		TaskResource taskResource = new TaskResource();
		taskResource.setName(function);
		taskResource.setTask(this);
		taskResource.setResource(resource);
		resources.add(taskResource);
		return this;
	}

}
