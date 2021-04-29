package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBDoubleMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServicesComposition implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private int ranking;

	@XmlElementWrapper(name = "services")
	@XmlElement(name = "service")
	private List<ServiceInfo> services = new LinkedList<ServiceInfo>();

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> qoSValues;

	@XmlAttribute
	private Double compositionUtility;

	public ServicesComposition() {
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public List<ServiceInfo> getServices() {
		return services;
	}

	public void setServices(List<ServiceInfo> services) {
		this.services = services;
	}

	public Map<String, Double> getQoSValues() {
		return qoSValues;
	}

	public void setQoSValues(Map<String, Double> qoSValues) {
		this.qoSValues = qoSValues;
	}

	public Double getCompositionUtility() {
		return compositionUtility;
	}

	public void setCompositionUtility(Double compositionUtility) {
		this.compositionUtility = compositionUtility;
	}

}
