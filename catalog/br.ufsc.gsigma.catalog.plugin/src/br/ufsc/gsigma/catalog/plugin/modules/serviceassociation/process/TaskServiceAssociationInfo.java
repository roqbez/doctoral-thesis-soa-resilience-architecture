package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process;

public class TaskServiceAssociationInfo {

	private String taskName;

	private String taskTaxonomy;

	private int qoSConstraints;

	private int matchingServices;

	private Integer totalServices;

	private int maxNumberOfServicesForDiscovery;

	private String participantName;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}

	public String getTaskTaxonomy() {
		return taskTaxonomy;
	}

	public void setTaskTaxonomy(String taskTaxonomy) {
		this.taskTaxonomy = taskTaxonomy;
	}

	public int getQoSConstraints() {
		return qoSConstraints;
	}

	public void setQoSConstraints(int qoSConstraints) {
		this.qoSConstraints = qoSConstraints;
	}

	public int getMatchingServices() {
		return matchingServices;
	}

	public void setMatchingServices(Integer matchingServices) {
		this.matchingServices = matchingServices;
	}

	public Integer getTotalServices() {
		return totalServices;
	}

	public void setTotalServices(Integer totalServices) {
		this.totalServices = totalServices;
	}

	public int getMaxNumberOfServicesForDiscovery() {
		return maxNumberOfServicesForDiscovery;
	}

	public void setMaxNumberOfServicesForDiscovery(int maxNumberOfServicesForDiscovery) {
		this.maxNumberOfServicesForDiscovery = maxNumberOfServicesForDiscovery;
	}

}
