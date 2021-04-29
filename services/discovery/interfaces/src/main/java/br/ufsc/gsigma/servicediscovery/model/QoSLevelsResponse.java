package br.ufsc.gsigma.servicediscovery.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBDoubleMapAttributeAdapter;
import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBIntegerMapAttributeAdapter;
import br.ufsc.gsigma.servicediscovery.support.jaxb.QoSLevelsByServiceClassificationAttributeAdapter;
import br.ufsc.gsigma.servicediscovery.support.jaxb.QoSLevelsByVirtualTaskAttributeAdapter;
import br.ufsc.gsigma.servicediscovery.support.jaxb.QoSValuesByServiceClassificationAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class QoSLevelsResponse {

	@XmlJavaTypeAdapter(JAXBIntegerMapAttributeAdapter.class)
	private Map<String, Integer> numberOfServicesByServiceClassification = new LinkedHashMap<String, Integer>();

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> maxServiceUtilityByServiceClassification = new LinkedHashMap<String, Double>();

	@XmlJavaTypeAdapter(QoSValuesByServiceClassificationAttributeAdapter.class)
	private Map<String, Map<String, Double>> maxQoSValueByServiceClassification = new HashMap<String, Map<String, Double>>();

	@XmlJavaTypeAdapter(QoSValuesByServiceClassificationAttributeAdapter.class)
	private Map<String, Map<String, Double>> minQoSValueByServiceClassification = new HashMap<String, Map<String, Double>>();

	@XmlJavaTypeAdapter(QoSLevelsByServiceClassificationAttributeAdapter.class)
	private Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> qoSLevelsByServiceClassification = new LinkedHashMap<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>>();

	@XmlJavaTypeAdapter(QoSLevelsByVirtualTaskAttributeAdapter.class)
	private Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> qoSLevelsByVirtualTask = new LinkedHashMap<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>>();

	public Map<String, Integer> getNumberOfServicesByServiceClassification() {
		return numberOfServicesByServiceClassification;
	}

	public void setNumberOfServicesByServiceClassification(Map<String, Integer> numberOfServicesByServiceClassification) {
		this.numberOfServicesByServiceClassification = numberOfServicesByServiceClassification;
	}

	public Map<String, Double> getMaxServiceUtilityByServiceClassification() {
		return maxServiceUtilityByServiceClassification;
	}

	public void setMaxServiceUtilityByServiceClassification(Map<String, Double> maxServiceUtilityByServiceClassification) {
		this.maxServiceUtilityByServiceClassification = maxServiceUtilityByServiceClassification;
	}

	public Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> getQoSLevelsByServiceClassification() {
		return qoSLevelsByServiceClassification;
	}

	public void setQoSLevelsByServiceClassification(Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> qoSLevelsByServiceClassification) {
		this.qoSLevelsByServiceClassification = qoSLevelsByServiceClassification;
	}

	public Map<String, Map<String, Double>> getMaxQoSValueByServiceClassification() {
		return maxQoSValueByServiceClassification;
	}

	public void setMaxQoSValueByServiceClassification(Map<String, Map<String, Double>> maxQoSValueByServiceClassification) {
		this.maxQoSValueByServiceClassification = maxQoSValueByServiceClassification;
	}

	public Map<String, Map<String, Double>> getMinQoSValueByServiceClassification() {
		return minQoSValueByServiceClassification;
	}

	public void setMinQoSValueByServiceClassification(Map<String, Map<String, Double>> minQoSValueByServiceClassification) {
		this.minQoSValueByServiceClassification = minQoSValueByServiceClassification;
	}

	public Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> getQoSLevelsByVirtualTask() {
		return qoSLevelsByVirtualTask;
	}

	public void setQoSLevelsByVirtualTask(Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> qoSLevelsByVirtualTask) {
		this.qoSLevelsByVirtualTask = qoSLevelsByVirtualTask;
	}

}
