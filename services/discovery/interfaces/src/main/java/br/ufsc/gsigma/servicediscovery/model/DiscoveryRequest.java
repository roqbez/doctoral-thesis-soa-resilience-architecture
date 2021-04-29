package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBIntegerMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class DiscoveryRequest implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private QoSInformation qoSInformation;

	@XmlElementWrapper
	@XmlElement(name = "item")
	private List<DiscoveryRequestItem> itens = new LinkedList<DiscoveryRequestItem>();

	private br.ufsc.gsigma.catalog.services.model.Process process;

	private Integer maxResultsPerServiceClass;

	private Integer totalNumberOfCompositions;

	private boolean useOptimalSelection;

	private Integer initialNumberOfQoSLevels = 10;

	private Integer maxNumberOfQoSLevels = 160;

	private boolean checkServicesAvailability;

	@XmlJavaTypeAdapter(JAXBIntegerMapAttributeAdapter.class)
	private Map<String, Integer> maxCandidatesPerParticipant = new HashMap<String, Integer>();

	@Override
	public Object clone() {

		DiscoveryRequest clone = new DiscoveryRequest();

		clone.setItens(itens);
		clone.setProcess(process);
		clone.setQoSInformation(qoSInformation);
		clone.setMaxResultsPerServiceClass(maxResultsPerServiceClass);
		clone.setTotalNumberOfCompositions(totalNumberOfCompositions);
		clone.setUseOptimalSelection(useOptimalSelection);
		clone.setInitialNumberOfQoSLevels(initialNumberOfQoSLevels);
		clone.setMaxNumberOfQoSLevels(maxNumberOfQoSLevels);
		clone.setCheckServicesAvailability(checkServicesAvailability);
		clone.setMaxCandidatesPerParticipant(maxCandidatesPerParticipant);

		return clone;
	}

	public DiscoveryRequest() {
	}

	public DiscoveryRequest(Process process, QoSInformation qoSInformation) {
		this.process = process;
		this.qoSInformation = qoSInformation;
	}

	public DiscoveryRequest(DiscoveryRequestItem item, Map<String, Double> qoSWeights) {
		this.itens.add(item);
		this.qoSInformation = new QoSInformation(qoSWeights);
	}

	public DiscoveryRequest(DiscoveryRequestItem item) {
		this.itens.add(item);
	}

	public DiscoveryRequest(DiscoveryRequestItem item, Map<String, Double> qoSWeights, Map<String, Double> globalQoSDelta) {
		this.itens.add(item);
		this.qoSInformation = new QoSInformation(qoSWeights, globalQoSDelta);
	}

	public DiscoveryRequest(Collection<DiscoveryRequestItem> itens, Map<String, Double> qoSWeights, Map<String, Double> globalQoSDelta) {
		this.itens.addAll(itens);
		this.qoSInformation = new QoSInformation(qoSWeights, globalQoSDelta);
	}

	public DiscoveryRequest(String... serviceClassifications) {
		for (String serviceClassification : serviceClassifications) {
			itens.add(new DiscoveryRequestItem(serviceClassification));
		}
	}

	public DiscoveryRequest(Collection<String> serviceClassifications) {
		for (String serviceClassification : serviceClassifications)
			itens.add(new DiscoveryRequestItem(serviceClassification));
	}

	public DiscoveryRequest(List<String> serviceClassifications, Map<String, Double> qoSWeights) {
		for (String serviceClassification : serviceClassifications)
			this.itens.add(new DiscoveryRequestItem(serviceClassification));
		this.qoSInformation = new QoSInformation(qoSWeights);
	}

	public DiscoveryRequest(String serviceClassification, Map<String, Double> qoSWeights) {
		this.itens.add(new DiscoveryRequestItem(serviceClassification));
		this.qoSInformation = new QoSInformation(qoSWeights);
	}

	public DiscoveryRequest(String serviceClassification, Map<String, Double> qoSWeights, String qoSKey, QoSValueComparisionType comparisionType, Integer qoSValue) {
		DiscoveryRequestItem item = new DiscoveryRequestItem(serviceClassification);

		QoSInformation q = new QoSInformation();
		q.addQoSConstraint(qoSKey, comparisionType, qoSValue);
		item.setQoSInformation(q);

		this.itens.add(item);
		this.qoSInformation = new QoSInformation(qoSWeights);
	}

	public DiscoveryRequest(String serviceClassification, QoSInformation qoSInformation) {
		this.itens.add(new DiscoveryRequestItem(serviceClassification, qoSInformation));
		this.qoSInformation = qoSInformation;
	}

	public DiscoveryRequest(String serviceClassification, QoSInformation qoSInformation, int maxResults) {
		this.itens.add(new DiscoveryRequestItem(serviceClassification, qoSInformation, maxResults));
		this.qoSInformation = qoSInformation;
	}

	public DiscoveryRequest(String serviceClassification, int maxResults) {
		this.itens.add(new DiscoveryRequestItem(serviceClassification, maxResults));
	}

	public DiscoveryRequest(DiscoveryRequestItem... discoveryRequestItens) {
		for (DiscoveryRequestItem item : discoveryRequestItens) {
			this.itens.add(item);
		}
	}

	public int getMaxCandidatesFromParticipantName(String participantName) {
		Integer n = maxCandidatesPerParticipant != null ? maxCandidatesPerParticipant.get(participantName) : null;
		return n != null ? n : 5;
	}

	public DiscoveryRequestItem getItemByTaxonomyClassification(String taxonomyClassification) {
		if (itens != null) {
			for (DiscoveryRequestItem i : itens) {
				if (taxonomyClassification.equals(i.getServiceClassification()))
					return i;
			}
		}
		return null;
	}

	public void setQoSConstraints(List<QoSConstraint> qoSConstraints) {
		if (qoSInformation == null) {
			qoSInformation = new QoSInformation();
		}
		qoSInformation.setQoSConstraints(qoSConstraints);
	}

	public void setQoSWeights(Map<String, Double> qoSWeights) {
		if (qoSInformation == null) {
			qoSInformation = new QoSInformation();
		}
		qoSInformation.setQoSWeights(qoSWeights);
	}

	public void setGlobalQoSDelta(Map<String, Double> globalQoSDelta) {
		if (qoSInformation == null) {
			qoSInformation = new QoSInformation();
		}
		qoSInformation.setGlobalQoSDelta(globalQoSDelta);
	}

	public List<DiscoveryRequestItem> getItens() {
		return itens;
	}

	public void setItens(List<DiscoveryRequestItem> itens) {
		this.itens = itens;
	}

	public br.ufsc.gsigma.catalog.services.model.Process getProcess() {
		return process;
	}

	public void setProcess(br.ufsc.gsigma.catalog.services.model.Process process) {
		this.process = process;
	}

	public QoSInformation getQoSInformation() {
		return qoSInformation;
	}

	public void setQoSInformation(QoSInformation qoSInformation) {
		this.qoSInformation = qoSInformation;
	}

	public Integer getMaxResultsPerServiceClass() {
		return maxResultsPerServiceClass;
	}

	public void setMaxResultsPerServiceClass(Integer maxResultsPerServiceClass) {
		this.maxResultsPerServiceClass = maxResultsPerServiceClass;
	}

	public Integer getTotalNumberOfCompositions() {
		return totalNumberOfCompositions;
	}

	public void setTotalNumberOfCompositions(Integer totalNumberOfCompositions) {
		this.totalNumberOfCompositions = totalNumberOfCompositions;
	}

	public boolean isUseOptimalSelection() {
		return useOptimalSelection;
	}

	public void setUseOptimalSelection(boolean useOptimalSelection) {
		this.useOptimalSelection = useOptimalSelection;
	}

	public Integer getInitialNumberOfQoSLevels() {
		return initialNumberOfQoSLevels;
	}

	public void setInitialNumberOfQoSLevels(Integer initialNumberOfQoSLevels) {
		this.initialNumberOfQoSLevels = initialNumberOfQoSLevels;
	}

	public Integer getMaxNumberOfQoSLevels() {
		return maxNumberOfQoSLevels;
	}

	public void setMaxNumberOfQoSLevels(Integer maxNumberOfQoSLevels) {
		this.maxNumberOfQoSLevels = maxNumberOfQoSLevels;
	}

	public boolean isCheckServicesAvailability() {
		return checkServicesAvailability;
	}

	public void setCheckServicesAvailability(boolean checkServicesAvailability) {
		this.checkServicesAvailability = checkServicesAvailability;
	}

	public Map<String, Integer> getMaxCandidatesPerParticipant() {
		return maxCandidatesPerParticipant;
	}

	public void setMaxCandidatesPerParticipant(Map<String, Integer> maxCandidatesPerParticipant) {
		this.maxCandidatesPerParticipant = maxCandidatesPerParticipant;
	}

	@Override
	public String toString() {
		return "DiscoveryRequest [itens=" + itens + ", qoSInformation=" + qoSInformation + "]";
	}

}
