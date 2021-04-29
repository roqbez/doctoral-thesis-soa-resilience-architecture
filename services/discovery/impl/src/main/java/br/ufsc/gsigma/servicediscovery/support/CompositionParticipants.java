package br.ufsc.gsigma.servicediscovery.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;

public class CompositionParticipants implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, CompositionParticipant> participants = new LinkedHashMap<String, CompositionParticipant>();

	public List<String> getParticipantsNames() {
		return new ArrayList<String>(participants.keySet());
	}

	public CompositionParticipant getParticipant(String participantName) {
		return participants.get(participantName);
	}

	public CompositionParticipant addParticipant(CompositionParticipant compositionParticipant) {
		return participants.put(compositionParticipant.getParticipantName(), compositionParticipant);
	}

	public Collection<CompositionParticipant> getParticipants() {
		return participants.values();
	}

	public void replaceService(DiscoveredService service) {

		String providerName = service.getServiceProvider().getName();

		for (CompositionParticipant p : getParticipants()) {
			CompositionParticipantProvider provider = p.getProvider(providerName);
			if (provider != null) {
				provider.addService(service);
			}
		}
	}

	public void addServices(Collection<DiscoveredService> services) {
		for (DiscoveredService s : services) {
			addService(s);
		}
	}

	public void addService(DiscoveredService service) {

		String providerName = service.getServiceProvider().getName();

		for (CompositionParticipant p : getParticipants()) {
			CompositionParticipantProvider provider = p.getProvider(providerName);
			if (provider != null) {
				provider.addService(service);
			}
		}
	}

	public void removeService(DiscoveredService service) {

		String providerName = service.getServiceProvider().getName();

		for (CompositionParticipant p : getParticipants()) {
			CompositionParticipantProvider provider = p.getProvider(providerName);
			if (provider != null) {
				provider.removeService(service);
			}
		}
	}
}
