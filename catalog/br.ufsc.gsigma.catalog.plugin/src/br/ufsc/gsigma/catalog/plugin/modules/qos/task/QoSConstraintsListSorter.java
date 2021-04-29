package br.ufsc.gsigma.catalog.plugin.modules.qos.task;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import br.ufsc.gsigma.catalog.plugin.util.QoSUtil;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;

public class QoSConstraintsListSorter extends ViewerSorter implements Cloneable {

	public final static int QOS_ITEM = 1;
	public final static int QOS_ATTRIBUTE = 2;
	public final static int QOS_COMPARISON = 3;
	public final static int QOS_VALUE = 4;
	public final static int QOS_UNIT = 5;
	public final static int QOS_DERIVED_FROM_GLOBAL = 6;

	private int criteria;

	private Map<Integer, Boolean> mapReverseSort = new HashMap<Integer, Boolean>();

	private boolean reverse;

	@Override
	public Object clone() {
		QoSConstraintsListSorter c = new QoSConstraintsListSorter();
		c.mapReverseSort = mapReverseSort;
		return c;
	}

	public void toggleReverse() {

		Boolean b = mapReverseSort.get(criteria);

		if (b == null) {
			b = false;
		}
		mapReverseSort.put(criteria, !b);

		this.reverse = b;
	}

	public int compare(Viewer viewer, Object o1, Object o2) {

		QoSCriterion qoSCriteria1 = this.reverse ? (QoSCriterion) o2 : (QoSCriterion) o1;
		QoSCriterion qoSCriteria2 = this.reverse ? (QoSCriterion) o1 : (QoSCriterion) o2;

		switch (criteria) {
		case QOS_ITEM:
			return qoSCriteria1.getQoSItem().compareTo(qoSCriteria2.getQoSItem());
		case QOS_ATTRIBUTE:
			return qoSCriteria1.getQoSAttribute().compareTo(qoSCriteria2.getQoSAttribute());
		case QOS_COMPARISON:
			return qoSCriteria1.getComparisionType().compareTo(qoSCriteria2.getComparisionType());
		case QOS_VALUE:
			return qoSCriteria1.getQoSValue().compareTo(qoSCriteria2.getQoSValue());
		case QOS_UNIT:
			String qoSUnit1 = QoSUtil.getEnabledQoSAttribute(qoSCriteria1.getQoSKey()).getUnit();
			String qoSUnit2 = QoSUtil.getEnabledQoSAttribute(qoSCriteria2.getQoSKey()).getUnit();
			return qoSUnit1.compareTo(qoSUnit2);
		case QOS_DERIVED_FROM_GLOBAL:
			return new Boolean(qoSCriteria1.isManaged()).compareTo(qoSCriteria2.isManaged());
		default:
			return 0;
		}
	}

	public int getCriteria() {
		return criteria;
	}

	public void setCriteria(int criteria) {
		this.criteria = criteria;
	}

}
