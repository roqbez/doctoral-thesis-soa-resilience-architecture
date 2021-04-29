package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ProcessServiceAssociationListSorter extends ViewerSorter implements Cloneable {

	protected static final int TASK_NAME = 1;
	protected static final int TASK_TAXONOMY = 2;
	protected static final int QOS_CONSTRAINTS = 3;
	protected static final int MAX_RESULTS = 4;
	protected static final int MATCHING_SERVICES = 5;
	protected static final int TOTAL_SERVICES = 6;
	protected static final int PARTICIPANT = 7;

	private int criteria;

	private Map<Integer, Boolean> mapReverseSort = new HashMap<Integer, Boolean>();

	private boolean reverse;

	@Override
	public Object clone() {
		ProcessServiceAssociationListSorter c = new ProcessServiceAssociationListSorter();
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

		TaskServiceAssociationInfo serviceAssociation1 = this.reverse ? (TaskServiceAssociationInfo) o2 : (TaskServiceAssociationInfo) o1;
		TaskServiceAssociationInfo serviceAssociation2 = this.reverse ? (TaskServiceAssociationInfo) o1 : (TaskServiceAssociationInfo) o2;

		switch (criteria) {

		case TASK_NAME:
			return ObjectUtils.compare(serviceAssociation1.getTaskName(), serviceAssociation2.getTaskName());

		case TASK_TAXONOMY:
			return ObjectUtils.compare(serviceAssociation1.getTaskTaxonomy(), serviceAssociation2.getTaskTaxonomy());

		case QOS_CONSTRAINTS:
			return ObjectUtils.compare(serviceAssociation1.getQoSConstraints(), serviceAssociation2.getQoSConstraints());

		case MAX_RESULTS:
			return ObjectUtils.compare(serviceAssociation1.getMaxNumberOfServicesForDiscovery(), serviceAssociation2.getMaxNumberOfServicesForDiscovery());

		case MATCHING_SERVICES:
			return ObjectUtils.compare(serviceAssociation1.getMatchingServices(), serviceAssociation2.getMatchingServices());

		case TOTAL_SERVICES:
			return ObjectUtils.compare(serviceAssociation1.getTotalServices(), serviceAssociation2.getTotalServices());

		case PARTICIPANT:
			return ObjectUtils.compare(serviceAssociation1.getParticipantName(), serviceAssociation2.getParticipantName());

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
