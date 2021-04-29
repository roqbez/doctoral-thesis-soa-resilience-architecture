package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBDoubleMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class QoSLevelsRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "serviceClassification")
	private List<String> serviceClassifications = new LinkedList<String>();

	@XmlElement(name = "qoSKey")
	private List<String> qoSKeys = new LinkedList<String>();

	@XmlElement(name = "numberOfLevels")
	private List<Integer> numbersOfLevels = new LinkedList<Integer>();

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> qoSWeights = new HashMap<String, Double>();

	private br.ufsc.gsigma.catalog.services.model.Process process;

	public QoSLevelsRequest() {
	}

	public QoSLevelsRequest(List<String> serviceClassifications, List<String> qoSKeys, Map<String, Double> qoSWeights, Integer[] numbersOfLevels) {
		this.serviceClassifications = serviceClassifications;
		this.qoSKeys = qoSKeys;
		this.qoSWeights = qoSWeights;
		this.numbersOfLevels = Arrays.asList(numbersOfLevels);
	}

	public List<String> getServiceClassifications() {
		return serviceClassifications;
	}

	public void setServiceClassifications(List<String> serviceClassifications) {
		this.serviceClassifications = serviceClassifications;
	}

	public List<String> getQoSKeys() {
		return qoSKeys;
	}

	public void setQoSKeys(List<String> qoSKeys) {
		this.qoSKeys = qoSKeys;
	}

	public List<Integer> getNumbersOfLevels() {
		return numbersOfLevels;
	}

	public void setNumbersOfLevels(List<Integer> numbersOfLevels) {
		this.numbersOfLevels = numbersOfLevels;
	}

	public Map<String, Double> getQoSWeights() {
		return qoSWeights;
	}

	public void setQoSWeights(Map<String, Double> qoSWeights) {
		this.qoSWeights = qoSWeights;
	}

	public br.ufsc.gsigma.catalog.services.model.Process getProcess() {
		return process;
	}

	public void setProcess(br.ufsc.gsigma.catalog.services.model.Process process) {
		this.process = process;
	}

}
