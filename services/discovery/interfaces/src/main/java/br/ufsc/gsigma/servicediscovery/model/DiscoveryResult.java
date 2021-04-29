package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBIntegerMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "successful", "numberOfMatchingServices", "totalNumberOfCompositions", "numberOfMatchingServicesPerServiceClassification", "totalNumberServicesPerServiceClassification", "processQoSInfo", "compositions", "matchingServices" })
public class DiscoveryResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private boolean successful;

	@XmlAttribute
	private int numberOfMatchingServices;

	@XmlAttribute
	private Integer totalNumberOfCompositions;

	@XmlElementWrapper
	@XmlElement(name = "matchingService")
	private List<DiscoveredService> matchingServices;

	@XmlJavaTypeAdapter(JAXBIntegerMapAttributeAdapter.class)
	private Map<String, Integer> numberOfMatchingServicesPerServiceClassification = new LinkedHashMap<String, Integer>();

	@XmlJavaTypeAdapter(JAXBIntegerMapAttributeAdapter.class)
	private Map<String, Integer> totalNumberServicesPerServiceClassification = new LinkedHashMap<String, Integer>();

	private ProcessQoSInfo processQoSInfo;

	@XmlElementWrapper(name = "compositions")
	@XmlElement(name = "composition")
	private List<ServicesComposition> compositions = new LinkedList<ServicesComposition>();

	public Map<String, List<DiscoveredService>> groupByServiceClassificationProviderUtility() {

		Map<String, List<DiscoveredService>> m = new TreeMap<String, List<DiscoveredService>>();

		for (Entry<String, List<DiscoveredService>> e : groupByServiceClassification().entrySet()) {

			String serviceClassification = e.getKey();

			List<DiscoveredService> services = new ArrayList<DiscoveredService>(e.getValue());

			Collections.sort(services, new Comparator<DiscoveredService>() {
				@Override
				public int compare(DiscoveredService s1, DiscoveredService s2) {

					int c1 = s1.getServiceProvider().getUtility() == null && s2.getServiceProvider().getUtility() == null ? //
					0 : s2.getServiceProvider().getUtility().compareTo(s1.getServiceProvider().getUtility());

					if (c1 == 0) {
						return s2.getUtility().compareTo(s1.getUtility());
					} else {
						return c1;
					}
				}
			});

			m.put(serviceClassification, services);
		}

		return m;
	}

	public Map<String, List<DiscoveredService>> groupByServiceClassification() {

		Map<String, List<DiscoveredService>> m = new TreeMap<String, List<DiscoveredService>>();

		if (matchingServices != null) {

			for (DiscoveredService s : matchingServices) {

				List<DiscoveredService> l = m.get(s.getServiceClassification());

				if (l == null) {
					l = new LinkedList<DiscoveredService>();
					m.put(s.getServiceClassification(), l);
				}
				l.add(s);
			}
		}

		return m;
	}

	public int getNumberOfMatchingServices() {
		return numberOfMatchingServices;
	}

	public void setNumberOfMatchingServices(int numberOfMatchingServices) {
		this.numberOfMatchingServices = numberOfMatchingServices;
	}

	public Integer getTotalNumberOfCompositions() {
		return totalNumberOfCompositions;
	}

	public void setTotalNumberOfCompositions(Integer totalNumberOfCompositions) {
		this.totalNumberOfCompositions = totalNumberOfCompositions;
	}

	@SuppressWarnings("unchecked")
	public List<DiscoveredService> getMatchingServices() {
		if (matchingServices == null)
			return Collections.EMPTY_LIST;
		else
			return matchingServices;
	}

	public void setMatchingServices(List<DiscoveredService> matchingServices) {

		this.matchingServices = matchingServices;

		if (matchingServices != null)
			numberOfMatchingServices = matchingServices.size();

		if (numberOfMatchingServicesPerServiceClassification != null) {
			for (String k : numberOfMatchingServicesPerServiceClassification.keySet()) {
				numberOfMatchingServicesPerServiceClassification.put(k, 0);
			}
		}

		for (DiscoveredService s : matchingServices) {

			Integer c = numberOfMatchingServicesPerServiceClassification.get(s.getServiceClassification());

			if (c == null)
				c = 1;
			else
				c++;

			numberOfMatchingServicesPerServiceClassification.put(s.getServiceClassification(), c);
		}
	}

	public Map<String, Integer> getNumberOfMatchingServicesPerServiceClassification() {
		return numberOfMatchingServicesPerServiceClassification;
	}

	public void setNumberOfMatchingServicesPerServiceClassification(Map<String, Integer> numberOfMatchingServicesPerServiceClassification) {
		this.numberOfMatchingServicesPerServiceClassification = numberOfMatchingServicesPerServiceClassification;
	}

	public Map<String, Integer> getTotalNumberServicesPerServiceClassification() {
		return totalNumberServicesPerServiceClassification;
	}

	public void setTotalNumberServicesPerServiceClassification(Map<String, Integer> totalNumberServicesPerServiceClassification) {
		this.totalNumberServicesPerServiceClassification = totalNumberServicesPerServiceClassification;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public ProcessQoSInfo getProcessQoSInfo() {
		return processQoSInfo;
	}

	public void setProcessQoSInfo(ProcessQoSInfo processQoSInfo) {
		this.processQoSInfo = processQoSInfo;
	}

	public List<ServicesComposition> getCompositions() {
		return compositions;
	}

	public void setCompositions(List<ServicesComposition> compositions) {
		this.compositions = compositions;
	}

}
