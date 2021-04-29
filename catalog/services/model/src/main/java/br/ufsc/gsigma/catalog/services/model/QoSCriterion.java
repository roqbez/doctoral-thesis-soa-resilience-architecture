package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Root
@Entity
@Table(name = "qos_criterion")
public class QoSCriterion implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private String qoSItem;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private String comparisionType;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private String qoSAttribute;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private Double qoSValue = 0D;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_task", nullable = false)
	private Task task;

	@XmlAttribute
	@Attribute
	private boolean managed;

	public QoSCriterion() {

	}

	public QoSCriterion(String qoSItem, String qoSAttribute) {
		this.qoSItem = qoSItem;
		this.qoSAttribute = qoSAttribute;
	}

	public QoSCriterion(String qoSItem, String qoSAttribute, String comparisionType, Double qoSValue) {
		this.qoSItem = qoSItem;
		this.qoSAttribute = qoSAttribute;
		this.comparisionType = (comparisionType != null ? comparisionType.toUpperCase() : comparisionType);
		this.qoSValue = qoSValue;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getQoSKey() {
		return qoSItem + "." + qoSAttribute;
	}

	public String getQoSItem() {
		return qoSItem;
	}

	public void setQoSItem(String qoSItem) {
		this.qoSItem = qoSItem;
	}

	public String getComparisionType() {
		return comparisionType != null ? comparisionType.toUpperCase() : comparisionType;
	}

	public void setComparisionType(String comparisionType) {
		this.comparisionType = (comparisionType != null ? comparisionType.toUpperCase() : comparisionType);
	}

	public String getQoSAttribute() {
		return qoSAttribute;
	}

	public void setQoSAttribute(String qoSAttribute) {
		this.qoSAttribute = qoSAttribute;
	}

	public Double getQoSValue() {
		return qoSValue;
	}

	public void setQoSValue(Double qoSValue) {
		this.qoSValue = qoSValue;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public boolean isManaged() {
		return managed;
	}

	public void setManaged(boolean managed) {
		this.managed = managed;
	}

	@Override
	public String toString() {
		return qoSItem + "." + qoSAttribute + " " + getComparisionType() + " " + qoSValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((qoSAttribute == null) ? 0 : qoSAttribute.hashCode());
		result = prime * result + ((qoSItem == null) ? 0 : qoSItem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QoSCriterion other = (QoSCriterion) obj;
		if (qoSAttribute == null) {
			if (other.qoSAttribute != null)
				return false;
		} else if (!qoSAttribute.equals(other.qoSAttribute))
			return false;
		if (qoSItem == null) {
			if (other.qoSItem != null)
				return false;
		} else if (!qoSItem.equals(other.qoSItem))
			return false;
		return true;
	}

}
