package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class QoSThreshold implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String qoSKey;

	@XmlAttribute
	private Double qoSMinValue;

	@XmlAttribute
	private Double qoSMaxValue;

	public QoSThreshold() {
	}

	public QoSThreshold(String qoSKey, Double qoSMinValue, Double qoSMaxValue) {
		this.qoSKey = qoSKey;
		this.qoSMinValue = qoSMinValue;
		this.qoSMaxValue = qoSMaxValue;
	}

	public String getQoSKey() {
		return qoSKey;
	}

	public void setQoSKey(String qoSKey) {
		this.qoSKey = qoSKey;
	}

	public Double getQoSMinValue() {
		return qoSMinValue;
	}

	public void setQoSMinValue(Double qoSMinValue) {
		this.qoSMinValue = qoSMinValue;
	}

	public Double getQoSMaxValue() {
		return qoSMaxValue;
	}

	public void setQoSMaxValue(Double qoSMaxValue) {
		this.qoSMaxValue = qoSMaxValue;
	}

}
