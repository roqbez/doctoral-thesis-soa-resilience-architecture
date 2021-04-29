package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.simpleframework.xml.ElementList;

import br.ufsc.gsigma.common.hash.HashUtil.NotHash;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "process")
@XmlRootElement(name = "process")
public class Process extends Task implements Serializable {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "task")
	@ElementList
	private List<Task> tasks = new LinkedList<Task>();

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "startEvent")
	@ElementList
	private List<StartEvent> startEvents = new LinkedList<StartEvent>();

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "endEvent")
	@ElementList
	private List<EndEvent> endEvents = new LinkedList<EndEvent>();

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "decision")
	@ElementList
	private List<Decision> decisions = new LinkedList<Decision>();

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "fork")
	@ElementList
	private List<Fork> forks = new LinkedList<Fork>();

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "junction")
	@ElementList
	private List<Junction> junctions = new LinkedList<Junction>();

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "merge")
	@ElementList
	private List<Merge> merges = new LinkedList<Merge>();

	@OneToMany(mappedBy = "process", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "connection")
	@ElementList
	private List<Connection> connections = new LinkedList<Connection>();

	@XmlTransient
	@NotHash
	@ManyToOne
	@JoinColumn(name = "id_process_tree_description", nullable = false)
	private ProcessTreeDescription processTreeDescription;

	@Transient
	@XmlTransient
	@NotHash
	private ITConfiguration itConfiguration;

	public Process() {

	}

	public Process(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<StartEvent> getStartEvents() {
		return startEvents;
	}

	public void setStartEvents(List<StartEvent> startEvents) {
		this.startEvents = startEvents;
	}

	public List<EndEvent> getEndEvents() {
		return endEvents;
	}

	public void setEndEvents(List<EndEvent> endEvents) {
		this.endEvents = endEvents;
	}

	public List<Decision> getDecisions() {
		return decisions;
	}

	public void setDecisions(List<Decision> decisions) {
		this.decisions = decisions;
	}

	public List<Fork> getForks() {
		return forks;
	}

	public void setForks(List<Fork> forks) {
		this.forks = forks;
	}

	public List<Junction> getJunctions() {
		return junctions;
	}

	public void setJunctions(List<Junction> junctions) {
		this.junctions = junctions;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}

	public List<Merge> getMerges() {
		return merges;
	}

	public void setMerges(List<Merge> merges) {
		this.merges = merges;
	}

	public ProcessTreeDescription getProcessTreeDescription() {
		return processTreeDescription;
	}

	public void setProcessTreeDescription(ProcessTreeDescription processTreeDescription) {
		this.processTreeDescription = processTreeDescription;
	}

	public ITConfiguration getItConfiguration() {
		return itConfiguration;
	}

	public void setItConfiguration(ITConfiguration itConfiguration) {
		this.itConfiguration = itConfiguration;
	}

	public StartEvent addStartEvent(String name) {
		StartEvent event = new StartEvent(this, name);
		event.buildDefaultContactPoints();
		startEvents.add(event);
		return event;
	}

	public EndEvent addEndEvent(String name) {
		EndEvent event = new EndEvent(this, name);
		event.buildDefaultContactPoints();
		endEvents.add(event);
		return event;
	}

	public Task addTask(String name) {
		Task task = new Task(this, name);
		task.buildDefaultContactPoints();
		tasks.add(task);
		return task;
	}

	public Process addConnection(OutputContactPoint output, InputContactPoint input) {
		connections.add(new Connection(this, output, input));
		return this;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" {");

		sb.append("tasks=" + (tasks != null ? tasks.size() : 0));
		sb.append(", startEvents=" + (startEvents != null ? startEvents.size() : 0));
		sb.append(", endEvents=" + (endEvents != null ? endEvents.size() : 0));
		sb.append(", decisions=" + (decisions != null ? decisions.size() : 0));
		sb.append(", forks=" + (forks != null ? forks.size() : 0));
		sb.append(", junctions=" + (junctions != null ? junctions.size() : 0));
		sb.append(", merges=" + (merges != null ? merges.size() : 0));
		sb.append(", connections=" + (connections != null ? connections.size() : 0));
		sb.append("}");

		return sb.toString();
	}

}
