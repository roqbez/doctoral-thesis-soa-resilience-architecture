package br.ufsc.gsigma.servicediscovery.support;

import java.io.Serializable;

public class CompositionParticipantTask implements Serializable {

	private static final long serialVersionUID = 1L;

	private String participantName;

	private String taskName;

	private String taxonomyClassification;

	public CompositionParticipantTask(String participantName, String taskName, String taxonomyClassification) {
		this.participantName = participantName;
		this.taskName = taskName;
		this.taxonomyClassification = taxonomyClassification;
	}

	public String getParticipantName() {
		return participantName;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getTaxonomyClassification() {
		return taxonomyClassification;
	}

}
