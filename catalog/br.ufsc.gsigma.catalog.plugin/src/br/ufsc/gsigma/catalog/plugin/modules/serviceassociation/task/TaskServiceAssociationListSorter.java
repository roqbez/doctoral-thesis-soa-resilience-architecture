package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMQoSValue;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServiceAssociation;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;

public class TaskServiceAssociationListSorter extends ViewerSorter implements Cloneable {

	public final static int SERVICE_NAME = 1;
	public final static int SERVICE_ENDPOINT = 2;
	public final static int SERVICE_PROTOCOL = 3;
	public final static int SERVICE_PROVIDER_NAME = 4;
	public final static int SERVICE_PROVIDER_REPUTATION = 5;
	public final static int SERVICE_UTILITY = 6;
	public final static int QOS_VALUE = 7;

	private int criteria;

	private QoSAttribute qoSAttribute;

	private Map<Integer, Boolean> mapReverseSort = new HashMap<Integer, Boolean>();

	private Map<String, Boolean> mapReverseSortQoS = new HashMap<String, Boolean>();

	private boolean reverse;

	@Override
	public Object clone() {
		TaskServiceAssociationListSorter c = new TaskServiceAssociationListSorter();
		c.mapReverseSort = mapReverseSort;
		c.mapReverseSortQoS = mapReverseSortQoS;
		return c;
	}

	public void toggleReverse() {

		if (criteria == QOS_VALUE) {

			Boolean b = mapReverseSortQoS.get(qoSAttribute.getKey());

			if (b == null) {
				b = false;
			}
			mapReverseSortQoS.put(qoSAttribute.getKey(), !b);

			this.reverse = b;

		} else {

			Boolean b = mapReverseSort.get(criteria);

			if (b == null) {
				b = false;
			}
			mapReverseSort.put(criteria, !b);

			this.reverse = b;

		}
	}

	public int compare(Viewer viewer, Object o1, Object o2) {

		BMServiceAssociation serviceAssociation1 = this.reverse ? (BMServiceAssociation) o2 : (BMServiceAssociation) o1;
		BMServiceAssociation serviceAssociation2 = this.reverse ? (BMServiceAssociation) o1 : (BMServiceAssociation) o2;

		switch (criteria) {
		case SERVICE_NAME:
			return serviceAssociation1.getServiceName().compareTo(serviceAssociation2.getServiceName());
		case SERVICE_ENDPOINT:
			return serviceAssociation1.getServiceEndpoint().compareTo(serviceAssociation2.getServiceEndpoint());
		case SERVICE_PROTOCOL:
			return ObjectUtils.compare(serviceAssociation1.getServiceProtocolConverter(), serviceAssociation2.getServiceProtocolConverter());
		case SERVICE_PROVIDER_NAME:
			return serviceAssociation1.getServiceProviderName().compareTo(serviceAssociation2.getServiceProviderName());
		case SERVICE_UTILITY:
			Double serviceUtility1 = serviceAssociation1.getServiceUtility();
			Double serviceUtility2 = serviceAssociation2.getServiceUtility();
			if (serviceUtility1 == null)
				return -1;
			if (serviceUtility2 == null)
				return 1;
			return serviceUtility1.compareTo(serviceUtility2);
		case SERVICE_PROVIDER_REPUTATION:
			return serviceAssociation1.getServiceProviderReputation().compareTo(serviceAssociation2.getServiceProviderReputation());
		case QOS_VALUE:
			return getQoSValue(serviceAssociation1).compareTo(getQoSValue(serviceAssociation2));
		default:
			return 0;
		}
	}

	private Double getQoSValue(BMServiceAssociation serviceAssociation) {

		List<BMQoSValue> serviceQoSInformation = serviceAssociation.getQoSValues();

		if (serviceQoSInformation != null) {
			for (BMQoSValue q : serviceQoSInformation) {
				if (q.getQoSItem().equals(qoSAttribute.getQoSItem()) && q.getQoSAttribute().equals(qoSAttribute.getName()))
					return q.getQoSValue();
			}
		}
		return 0D;
	}

	public int getCriteria() {
		return criteria;
	}

	public void setCriteria(int criteria) {
		this.criteria = criteria;
	}

	public QoSAttribute getQoSAttribute() {
		return qoSAttribute;
	}

	public void setQoSAttribute(QoSAttribute qoSAttribute) {
		this.qoSAttribute = qoSAttribute;
	}

}
