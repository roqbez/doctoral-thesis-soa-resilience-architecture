package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class QoSConstraint implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String qoSKey;

	@XmlAttribute
	private Double qoSValue;

	@XmlAttribute
	private QoSValueComparisionType comparisionType = QoSValueComparisionType.LE;

	private transient boolean managed;

	public QoSConstraint() {

	}

	public QoSConstraint(String qoSItemName, String qoSAttributeName, QoSValueComparisionType comparisionType, double qoSValue) {
		this.qoSKey = qoSItemName + "." + qoSAttributeName;
		this.comparisionType = comparisionType;
		this.qoSValue = qoSValue;
	}

	public QoSConstraint(String qoSKey, QoSValueComparisionType comparisionType, double qoSValue) {
		this.qoSKey = qoSKey;
		this.comparisionType = comparisionType;
		this.qoSValue = qoSValue;
	}

	public String getQoSKey() {
		return qoSKey;
	}

	public void setQoSKey(String qoSKey) {
		this.qoSKey = qoSKey;
	}

	public String getQoSItem() {
		return getQoSKey().split("\\.")[0];
	}

	public String getQoSAttribute() {
		return getQoSKey().split("\\.")[1];
	}

	public Double getQoSValue() {
		return qoSValue;
	}

	public void setQoSValue(double qoSValue) {
		this.qoSValue = qoSValue;
	}

	public QoSValueComparisionType getComparisionType() {
		return comparisionType;
	}

	public void setComparisionType(QoSValueComparisionType comparisionType) {
		this.comparisionType = comparisionType;
	}

	public boolean isManaged() {
		return managed;
	}

	public void setManaged(boolean managed) {
		this.managed = managed;
	}

	@Override
	public String toString() {
		return qoSKey + " " + comparisionType + " " + qoSValue;
	}

}
