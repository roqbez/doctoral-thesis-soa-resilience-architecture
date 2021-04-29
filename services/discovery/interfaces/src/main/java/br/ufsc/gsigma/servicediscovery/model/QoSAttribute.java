package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class QoSAttribute implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum QoSValueUtilityDirection {
		POSITIVE, NEGATIVE
	}

	public enum QoSValueAggregationType {
		SUM, PRODUCT, MIN, MAX, VALUE
	}

	@XmlAttribute
	private String key;

	@XmlAttribute
	private Integer qoSItemId;

	@XmlAttribute
	private String qoSItem;

	@XmlAttribute
	private String name;

	@XmlAttribute
	private String unit;

	@XmlAttribute
	private QoSValueUtilityDirection valueUtilityDirection;

	@XmlAttribute
	private QoSValueAggregationType sequentialAggregationType;

	@XmlAttribute
	private QoSValueAggregationType loopAggregationType;

	@XmlAttribute
	private QoSValueAggregationType parallelAggregationType;

	@XmlAttribute
	private QoSValueAggregationType conditionalAggregationType;

	public QoSAttribute() {
	}

	public QoSAttribute(Integer qoSItemId, String qoSItem, String name, String unit) {
		this.qoSItemId = qoSItemId;
		this.qoSItem = qoSItem;
		this.name = name;
		this.unit = unit;
	}

	public String getKey() {
		if (key == null) {
			key = qoSItem + "." + name;
		}
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		key = qoSItem + "." + name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getQoSItem() {
		return qoSItem;
	}

	public void setQoSItem(String qoSItem) {
		this.qoSItem = qoSItem;
		key = qoSItem + "." + name;
	}

	public Integer getQoSItemId() {
		return qoSItemId;
	}

	public void setQoSItemId(Integer qoSItemId) {
		this.qoSItemId = qoSItemId;
	}

	public QoSValueUtilityDirection getValueUtilityDirection() {
		return valueUtilityDirection;
	}

	public void setValueUtilityDirection(QoSValueUtilityDirection valueUtilityDirection) {
		this.valueUtilityDirection = valueUtilityDirection;
	}

	public QoSValueAggregationType getSequentialAggregationType() {
		return sequentialAggregationType;
	}

	public void setSequentialAggregationType(QoSValueAggregationType sequentialAggregationType) {
		this.sequentialAggregationType = sequentialAggregationType;
	}

	public QoSValueAggregationType getLoopAggregationType() {
		return loopAggregationType;
	}

	public void setLoopAggregationType(QoSValueAggregationType loopAggregationType) {
		this.loopAggregationType = loopAggregationType;
	}

	public QoSValueAggregationType getParallelAggregationType() {
		return parallelAggregationType;
	}

	public void setParallelAggregationType(QoSValueAggregationType parallelAggregationType) {
		this.parallelAggregationType = parallelAggregationType;
	}

	public QoSValueAggregationType getConditionalAggregationType() {
		return conditionalAggregationType;
	}

	public void setConditionalAggregationType(QoSValueAggregationType conditionalAggregationType) {
		this.conditionalAggregationType = conditionalAggregationType;
	}

	@Override
	public String toString() {
		return getKey();
	}

}
