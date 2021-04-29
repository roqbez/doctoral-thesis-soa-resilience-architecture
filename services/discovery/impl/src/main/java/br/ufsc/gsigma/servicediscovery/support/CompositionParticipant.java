package br.ufsc.gsigma.servicediscovery.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompositionParticipant implements Serializable {

	private static final long serialVersionUID = 1L;

	private String participantName;

	private Map<String, CompositionParticipantTask> tasks = new LinkedHashMap<String, CompositionParticipantTask>();

	private Map<String, CompositionParticipantProvider> providers = new LinkedHashMap<String, CompositionParticipantProvider>();

	public CompositionParticipant(String participantName) {
		this.participantName = participantName;
	}

	public CompositionParticipantTask getTask(String taskName) {
		return tasks.get(taskName);
	}

	public void addTask(CompositionParticipantTask task) {
		tasks.put(task.getTaskName(), task);
	}

	public Collection<CompositionParticipantTask> getTasks() {
		return tasks.values();
	}

	public CompositionParticipantProvider getProvider(String providerName) {
		return providers.get(providerName);
	}

	public void addProvider(CompositionParticipantProvider provider) {
		providers.put(provider.getProviderName(), provider);
	}

	public List<CompositionParticipantProvider> getProviders() {
		return new ArrayList<CompositionParticipantProvider>(providers.values());
	}

	public String getParticipantName() {
		return participantName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((participantName == null) ? 0 : participantName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositionParticipant other = (CompositionParticipant) obj;
		if (participantName == null) {
			if (other.participantName != null)
				return false;
		} else if (!participantName.equals(other.participantName))
			return false;
		return true;
	}

}
