package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMQoSValue;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServicesComposition;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;

public class ProcessCompositionListSorter extends ViewerSorter implements Cloneable {

	protected static final int RANKING = 1;
	protected static final int SERVICES = 2;
	protected static final int TYPE = 3;
	protected static final int PROCESS_UTILITY = 4;
	protected static final int QOS_VALUE = 5;

	private int criteria;

	private QoSAttribute qoSAttribute;

	private Map<Integer, Boolean> mapReverseSort = new HashMap<Integer, Boolean>();

	private Map<String, Boolean> mapReverseSortQoS = new HashMap<String, Boolean>();

	private boolean reverse;

	@Override
	public Object clone() {
		ProcessCompositionListSorter c = new ProcessCompositionListSorter();
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

		BMServicesComposition servicesComposition1 = this.reverse ? (BMServicesComposition) o2 : (BMServicesComposition) o1;
		BMServicesComposition servicesComposition2 = this.reverse ? (BMServicesComposition) o1 : (BMServicesComposition) o2;

		switch (criteria) {

		case RANKING:
			return servicesComposition1.getRanking().compareTo(servicesComposition2.getRanking());

		case SERVICES:
			return servicesComposition1.getServicesAlias().compareTo(servicesComposition2.getServicesAlias());

		case TYPE:
			return servicesComposition1.getType().compareTo(servicesComposition2.getType());

		case PROCESS_UTILITY:
			Double processUtility1 = servicesComposition1.getUtility();
			Double processUtility2 = servicesComposition2.getUtility();
			if (processUtility1 == null)
				return -1;
			if (processUtility2 == null)
				return 1;
			return processUtility1.compareTo(processUtility2);

		case QOS_VALUE:
			return getQoSValue(servicesComposition1).compareTo(getQoSValue(servicesComposition2));

		default:
			return 0;
		}
	}

	private Double getQoSValue(BMServicesComposition servicesComposition) {

		List<BMQoSValue> serviceQoSInformation = servicesComposition.getQoSValues();

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
