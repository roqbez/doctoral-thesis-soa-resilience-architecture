package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "qoSItemName", "qoSAttributeName", "qoSKey", "qoSValue", "qoSItemId", "qoSUnit" })
public class ServiceQoSInformationItem implements Serializable, Comparable<ServiceQoSInformationItem> {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String qoSKey;

	@XmlAttribute
	private Double qoSValue;

	@XmlAttribute
	private Integer qoSItemId;

	@XmlAttribute
	private String qoSItemName;

	@XmlAttribute
	private String qoSAttributeName;

	@XmlAttribute
	private String qoSUnit;

	private transient DiscoveredService service;

	public ServiceQoSInformationItem() {

	}

	public ServiceQoSInformationItem(Integer qoSItemId, String qoSItemName, String qoSAttributeName, double qoSValue, String qoSUnit) {
		this.qoSKey = qoSItemName + "." + qoSAttributeName;
		this.qoSItemId = qoSItemId;
		this.qoSItemName = qoSItemName;
		this.qoSAttributeName = qoSAttributeName;
		this.qoSValue = qoSValue;
		this.qoSUnit = qoSUnit;
	}

	public boolean isQoSAttribute(QoSAttribute qoSAttribute) {

		if (qoSAttribute == null)
			return false;

		boolean b1 = getQoSKey().equals(qoSAttribute.getKey());
		boolean b2 = qoSItemId != null && qoSItemId.equals(qoSAttribute.getQoSItem()) && qoSAttributeName != null && qoSAttributeName.equals(qoSAttribute.getName());

		return b1 || b2;
	}

	public String getQoSKey() {
		if (qoSKey == null)
			qoSKey = qoSItemName + "." + qoSAttributeName;
		return qoSKey;
	}

	public Integer getQoSItemId() {
		return qoSItemId;
	}

	public void setQoSItemId(Integer qoSItemId) {
		this.qoSItemId = qoSItemId;
	}

	public String getQoSItemName() {
		return qoSItemName;
	}

	public void setQoSItemName(String qoSItemName) {
		this.qoSItemName = qoSItemName;
		this.qoSKey = qoSItemName + "." + qoSAttributeName;
	}

	public String getQoSAttributeName() {
		return qoSAttributeName;
	}

	public void setQoSAttributeName(String qoSAttributeName) {
		this.qoSAttributeName = qoSAttributeName;
		this.qoSKey = qoSItemName + "." + qoSAttributeName;
	}

	public Double getQoSValue() {
		return qoSValue;
	}

	public void setQoSValue(double qoSValue) {
		this.qoSValue = qoSValue;
	}

	public String getQoSUnit() {
		return qoSUnit;
	}

	public void setQoSUnit(String qoSUnit) {
		this.qoSUnit = qoSUnit;
	}

	public DiscoveredService getService() {
		return service;
	}

	public void setService(DiscoveredService service) {
		this.service = service;
	}

	@Override
	public String toString() {
		return getQoSKey() + "=" + qoSValue;
	}

	@Override
	public int compareTo(ServiceQoSInformationItem o) {
		return getQoSKey().compareTo(o.getQoSKey());
	}

}
