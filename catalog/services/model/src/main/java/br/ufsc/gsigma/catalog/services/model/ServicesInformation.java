package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.common.hash.HashUtil.NotHash;
import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBDoubleMapAttributeAdapter;

@XmlRootElement(name = "servicesInformation")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServicesInformation implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotHash
	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> qoSWeights = new LinkedHashMap<String, Double>();

	@NotHash
	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> globalQoSDelta = new LinkedHashMap<String, Double>();

	@NotHash
	@XmlElementWrapper
	@XmlElement(name = "qoSCriterion")
	private List<QoSCriterion> qoSCriterions = new ArrayList<QoSCriterion>();

	@XmlElementWrapper
	@XmlElement(name = "serviceConfig")
	private List<ServiceConfig> servicesConfig = new LinkedList<ServiceConfig>();

	public ServiceConfig getServiceConfig(String classification) {
		if (servicesConfig != null) {
			for (ServiceConfig cfg : servicesConfig) {
				if (classification.equals(cfg.getClassification()))
					return cfg;
			}
		}
		return null;
	}

	public List<String> getServicesClassifications() {

		List<String> result = new LinkedList<String>();

		for (ServiceConfig cfg : servicesConfig) {
			result.add(cfg.getClassification());
		}

		return result;
	}

	public boolean hasAnyBoundService() {
		if (servicesConfig == null || servicesConfig.isEmpty())
			return false;

		for (ServiceConfig cfg : servicesConfig) {
			if (cfg.getServiceAssociations() != null && !cfg.getServiceAssociations().isEmpty())
				return true;
		}
		return false;
	}

	public List<ServiceConfig> getServicesConfig() {
		return servicesConfig;
	}

	public void setServicesConfig(List<ServiceConfig> servicesConfig) {
		this.servicesConfig = servicesConfig;
	}

	public ServiceConfig createServiceConfig(String classification) {
		if (this.servicesConfig == null)
			servicesConfig = new LinkedList<ServiceConfig>();
		ServiceConfig cfg = new ServiceConfig(classification);
		servicesConfig.add(cfg);
		return cfg;
	}

	public List<QoSCriterion> getQoSCriterions() {
		return qoSCriterions;
	}

	public void setQoSCriterions(List<QoSCriterion> qoSCriterions) {
		this.qoSCriterions = qoSCriterions;
	}

	public Map<String, Double> getQoSWeights() {
		return qoSWeights;
	}

	public void setQoSWeights(Map<String, Double> qoSWeights) {
		this.qoSWeights = qoSWeights;
	}

	public Map<String, Double> getGlobalQoSDelta() {
		return globalQoSDelta;
	}

	public void setGlobalQoSDelta(Map<String, Double> globalQoSDelta) {
		this.globalQoSDelta = globalQoSDelta;
	}

}
