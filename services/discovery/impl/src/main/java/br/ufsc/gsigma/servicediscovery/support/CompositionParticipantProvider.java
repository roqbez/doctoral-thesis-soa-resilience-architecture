package br.ufsc.gsigma.servicediscovery.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;

public class CompositionParticipantProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	private String participantName;

	private String providerName;

	private Map<String, Set<DiscoveredService>> services = new LinkedHashMap<String, Set<DiscoveredService>>();

	public CompositionParticipantProvider(String participantName, String providerName) {
		this.participantName = participantName;
		this.providerName = providerName;
	}

	public Collection<DiscoveredService> getServicesByTaxonomyClassification(String taxonomyClassification) {
		return services.get(taxonomyClassification);
	}

	public Map<String, Set<DiscoveredService>> groupByServiceClassification() {
		return services;
	}

	public Set<Set<DiscoveredService>> getGroupedServices() {
		return new LinkedHashSet<Set<DiscoveredService>>(services.values());
	}

	public void addService(DiscoveredService service) {

		Set<DiscoveredService> l = services.get(service.getServiceClassification());

		if (l == null) {
			l = new LinkedHashSet<DiscoveredService>();
			services.put(service.getServiceClassification(), l);
		}

		if (!l.add(service)) {
			l.remove(service);
			l.add(service);
		}
	}

	public void removeService(DiscoveredService service) {

		Set<DiscoveredService> l = services.get(service.getServiceClassification());

		if (l != null) {
			l.remove(service);
		}
	}

	public String getParticipantName() {
		return participantName;
	}

	public String getProviderName() {
		return providerName;
	}

	@Override
	public String toString() {
		return participantName + ":" + providerName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((participantName == null) ? 0 : participantName.hashCode());
		result = prime * result + ((providerName == null) ? 0 : providerName.hashCode());
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
		CompositionParticipantProvider other = (CompositionParticipantProvider) obj;
		if (participantName == null) {
			if (other.participantName != null)
				return false;
		} else if (!participantName.equals(other.participantName))
			return false;
		if (providerName == null) {
			if (other.providerName != null)
				return false;
		} else if (!providerName.equals(other.providerName))
			return false;
		return true;
	}

}
