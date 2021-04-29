package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBDoubleMapAttributeAdapter;
import br.ufsc.gsigma.servicediscovery.support.jaxb.ServiceClassificationQoSConstraintsAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessQoSInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlJavaTypeAdapter(ServiceClassificationQoSConstraintsAttributeAdapter.class)
	private Map<String, List<QoSConstraint>> localQoSConstraints;

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> globalQoSDelta;

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> globalQoSMinValue;

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> globalQoSMaxValue;

	public Map<String, List<QoSConstraint>> getLocalQoSConstraints() {
		return localQoSConstraints;
	}

	public void setLocalQoSConstraints(Map<String, List<QoSConstraint>> localQoSConstraints) {
		this.localQoSConstraints = localQoSConstraints;
	}

	public Map<String, Double> getGlobalQoSDelta() {
		return globalQoSDelta;
	}

	public void setGlobalQoSDelta(Map<String, Double> globalQoSDelta) {
		this.globalQoSDelta = globalQoSDelta;
	}

	public Map<String, Double> getGlobalQoSMinValue() {
		return globalQoSMinValue;
	}

	public void setGlobalQoSMinValue(Map<String, Double> globalQoSMinValue) {
		this.globalQoSMinValue = globalQoSMinValue;
	}

	public Map<String, Double> getGlobalQoSMaxValue() {
		return globalQoSMaxValue;
	}

	public void setGlobalQoSMaxValue(Map<String, Double> globalQoSMaxValue) {
		this.globalQoSMaxValue = globalQoSMaxValue;
	}

}
