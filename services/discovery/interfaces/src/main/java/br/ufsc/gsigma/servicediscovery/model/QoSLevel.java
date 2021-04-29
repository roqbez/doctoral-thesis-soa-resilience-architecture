package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class QoSLevel implements Serializable, Cloneable, Comparable<QoSLevel> {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String qoSKey;

	@XmlAttribute
	private int totalNumberOfLevels;

	@XmlAttribute
	private int number;

	@XmlAttribute
	private double value;

	@XmlAttribute
	private Double negativeUtility;

	@XmlAttribute
	private Double positiveUtility;

	@XmlAttribute
	private String serviceClassification;

	private transient List<Double> values = new LinkedList<Double>();

	private transient Integer varNumber;

	@Override
	public int compareTo(QoSLevel o) {
		return new Double(value).compareTo(o.getValue());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		QoSLevel level = new QoSLevel();

		level.setQoSKey(getQoSKey());
		level.setTotalNumberOfLevels(getTotalNumberOfLevels());
		level.setNumber(getNumber());
		level.setValue(getValue());
		level.setServiceClassification(getServiceClassification());

		return level;
	}

	public QoSLevel() {
	}

	public QoSLevel(String serviceClassification, String qoSKey, int number, int totalNumberOfLevels, double value, int varNumber) {
		this.serviceClassification = serviceClassification;
		this.qoSKey = qoSKey;
		this.number = number;
		this.totalNumberOfLevels = totalNumberOfLevels;
		this.value = value;
		this.varNumber = varNumber;
		this.values.add(value);
	}

	public QoSLevel(String serviceClassification, String qoSKey, int number, int totalNumberOfLevels, double value) {
		this.serviceClassification = serviceClassification;
		this.qoSKey = qoSKey;
		this.number = number;
		this.totalNumberOfLevels = totalNumberOfLevels;
		this.value = value;
		this.values.add(value);
	}

	public QoSLevel(String serviceClassification, String qoSKey, int number, int totalNumberOfLevels, double value, double negativeUtility, double positiveUtility) {
		this.serviceClassification = serviceClassification;
		this.qoSKey = qoSKey;
		this.number = number;
		this.totalNumberOfLevels = totalNumberOfLevels;
		this.value = value;
		this.values.add(value);
		this.negativeUtility = negativeUtility;
		this.positiveUtility = positiveUtility;
	}

	public QoSLevel(String serviceClassification, String qoSKey, int number, int totalNumberOfLevels, double value, double utility, boolean isNegativeUtility) {
		this(serviceClassification, qoSKey, number, totalNumberOfLevels, value, isNegativeUtility ? utility : 0, isNegativeUtility ? 0 : utility);
	}

	public QoSLevel(String serviceClassification, String qoSKey, int number, int totalNumberOfLevels) {
		this.serviceClassification = serviceClassification;
		this.qoSKey = qoSKey;
		this.number = number;
		this.totalNumberOfLevels = totalNumberOfLevels;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getTotalNumberOfLevels() {
		return totalNumberOfLevels;
	}

	public void setTotalNumberOfLevels(int totalNumberOfLevels) {
		this.totalNumberOfLevels = totalNumberOfLevels;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public List<Double> getValues() {
		return values;
	}

	public void setValues(List<Double> values) {
		this.values = values;
	}

	public Double getNegativeUtility() {
		return negativeUtility;
	}

	public void setNegativeUtility(Double negativeUtility) {
		this.negativeUtility = negativeUtility;
	}

	public Double getPositiveUtility() {
		return positiveUtility;
	}

	public void setPositiveUtility(Double positiveUtility) {
		this.positiveUtility = positiveUtility;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public Integer getVarNumber() {
		return varNumber;
	}

	public void setVarNumber(Integer varNumber) {
		this.varNumber = varNumber;
	}

	public String getQoSKey() {
		return qoSKey;
	}

	public void setQoSKey(String qoSKey) {
		this.qoSKey = qoSKey;
	}

	@Override
	public String toString() {
		return "QoSLevel [" + serviceClassification + " -> " + qoSKey + "." + number + "] --> " + value;
	}
}
