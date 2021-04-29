package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBDoubleMapAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class QoSInformation implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "qoSConstraint")
	private List<QoSConstraint> qoSConstraints = new LinkedList<QoSConstraint>();

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> qoSWeights = new HashMap<String, Double>();

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	private Map<String, Double> globalQoSDelta = new HashMap<String, Double>();

	public QoSInformation() {
	}

	public QoSInformation(Map<String, Double> qoSWeights) {
		this.qoSWeights = qoSWeights;
	}

	public QoSInformation(Map<String, Double> qoSWeights, Map<String, Double> globalQoSDelta) {
		this.qoSWeights = qoSWeights;
		this.globalQoSDelta = globalQoSDelta;
	}

	public QoSInformation(List<QoSConstraint> qoSConstraints) {
		this.qoSConstraints = qoSConstraints;
	}

	public List<QoSConstraint> getQoSConstraints() {
		return qoSConstraints;
	}

	public Map<String, Double> getQoSWeights() {
		return qoSWeights;
	}

	public void setQoSWeights(Map<String, Double> qoSWeights) {
		this.qoSWeights = qoSWeights;
	}

	public void setQoSConstraints(List<QoSConstraint> qoSConstraints) {
		this.qoSConstraints = qoSConstraints;
	}

	public boolean hasQoSConstraints() {
		return qoSConstraints != null && !qoSConstraints.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public List<String> getQoSConstraintsKeys() {
		if (hasQoSConstraints()) {
			Set<String> keys = new LinkedHashSet<String>();

			for (QoSConstraint q : qoSConstraints)
				keys.add(q.getQoSKey());

			return new ArrayList<String>(keys);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public void removeManagedQoSConstraints() {
		ListIterator<QoSConstraint> it = qoSConstraints.listIterator();
		while (it.hasNext()) {
			QoSConstraint q = it.next();
			if (q.isManaged())
				it.remove();
		}
	}

	public QoSConstraint addManagedQoSConstraint(QoSConstraint q) {
		this.qoSConstraints.add(q);
		q.setManaged(true);
		return q;
	}

	public QoSConstraint addManagedQoSConstraint(String qoSKey, QoSValueComparisionType comparisionType, double qoSValue) {
		QoSConstraint q = addQoSConstraint(qoSKey, comparisionType, qoSValue);
		q.setManaged(true);
		return q;
	}

	public QoSConstraint addQoSConstraint(String qoSKey, String comparisionType, double qoSValue) {
		QoSValueComparisionType qoSValueComparisionType = getQoSValueComparisionType(comparisionType);
		return addManagedQoSConstraint(qoSKey, qoSValueComparisionType, qoSValue);
	}

	public static QoSValueComparisionType getQoSValueComparisionType(String comparisionType) {
		return QoSValueComparisionType.valueOf(comparisionType.toUpperCase());
	}

	public QoSConstraint addQoSConstraint(String qoSKey, QoSValueComparisionType comparisionType, double qoSValue) {
		QoSConstraint q = new QoSConstraint(qoSKey, comparisionType, qoSValue);
		this.qoSConstraints.add(q);
		return q;
	}

	public QoSConstraint getQoSConstraint(String qoSKey) {
		return getQoSConstraint(qoSConstraints, qoSKey);
	}

	public static QoSConstraint getQoSConstraint(List<QoSConstraint> qoSConstraints, String qoSKey) {

		if (qoSConstraints == null)
			return null;

		for (QoSConstraint q : qoSConstraints)
			if (qoSKey.equals(q.getQoSKey()))
				return q;

		return null;
	}

	public Map<String, Double> getGlobalQoSDelta() {
		return globalQoSDelta;
	}

	public void setGlobalQoSDelta(Map<String, Double> globalQoSDelta) {
		this.globalQoSDelta = globalQoSDelta;
	}

	@Override
	public String toString() {
		return "[qoSConstraints=" + qoSConstraints + ", qoSWeights=" + qoSWeights + "]";
	}

}
