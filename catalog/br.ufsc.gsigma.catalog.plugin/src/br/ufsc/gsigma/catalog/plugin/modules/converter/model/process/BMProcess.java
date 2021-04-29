package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;
import java.util.List;

public class BMProcess extends BMTask {
	
	private static final long serialVersionUID = 1L;

	private List<BMStart> startNodes = new ArrayList<BMStart>();
	private List<BMEnd> endNodes = new ArrayList<BMEnd>();
	private List<BMStop> stopNodes = new ArrayList<BMStop>();
	private List<BMDecision> decisions = new ArrayList<BMDecision>();
	private List<BMFork> forks = new ArrayList<BMFork>();
	private List<BMMerge> merges = new ArrayList<BMMerge>();
	private List<BMTask> tasks = new ArrayList<BMTask>();
	private List<BMConnection> connections = new ArrayList<BMConnection>();

	private BMProcessInformationExtension processInformationExtension;

	public BMProcess() {
	}

	public BMProcess(String name) {
		this.name = name;
	}

	public List<BMAbstractComponent> getComponents() {
		List<BMAbstractComponent> result = new ArrayList<BMAbstractComponent>();
		result.addAll(startNodes);
		result.addAll(endNodes);
		result.addAll(stopNodes);
		result.addAll(decisions);
		result.addAll(merges);
		result.addAll(forks);
		result.addAll(tasks);
		return result;
	}

	public String getType() {
		return "process";
	}

	public List<BMStart> getStartNodes() {
		return startNodes;
	}

	public void setStartNodes(List<BMStart> startNodes) {
		this.startNodes = startNodes;
	}

	public List<BMEnd> getEndNodes() {
		return endNodes;
	}

	public void setEndNodes(List<BMEnd> endNodes) {
		this.endNodes = endNodes;
	}

	public List<BMStop> getStopNodes() {
		return stopNodes;
	}

	public void setStopNodes(List<BMStop> stopNodes) {
		this.stopNodes = stopNodes;
	}

	public List<BMDecision> getDecisions() {
		return decisions;
	}

	public void setDecisions(List<BMDecision> decisions) {
		this.decisions = decisions;
	}

	public List<BMTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<BMTask> tasks) {
		this.tasks = tasks;
	}

	public List<BMFork> getForks() {
		return forks;
	}

	public void setForks(List<BMFork> forks) {
		this.forks = forks;
	}

	public List<BMMerge> getMerges() {
		return merges;
	}

	public void setMerges(List<BMMerge> merges) {
		this.merges = merges;
	}

	public List<BMConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<BMConnection> connections) {
		this.connections = connections;
	}

	public BMProcessInformationExtension getProcessInformationExtension() {
		return processInformationExtension;
	}

	public void setProcessInformationExtension(BMProcessInformationExtension processInformationExtension) {
		this.processInformationExtension = processInformationExtension;
	}

}