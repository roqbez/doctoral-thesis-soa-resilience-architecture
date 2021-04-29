package br.ufsc.gsigma.servicediscovery.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSLevel;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;

public class QoSLevels implements Serializable {

	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(QoSLevels.class);

	private Map<String, Map<Integer, Collection<QoSLevel>>> qosLevelsMapByAtt = new HashMap<String, Map<Integer, Collection<QoSLevel>>>();

	private String serviceClassification;

	public QoSLevels(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public Collection<QoSLevel> getQoSLevels(String qoSKey, int numberOfLevels) {

		Map<Integer, Collection<QoSLevel>> qosLevelsMap = qosLevelsMapByAtt.get(qoSKey);

		if (qosLevelsMap == null)
			return null;

		return qosLevelsMap.get(numberOfLevels);
	}

	@SuppressWarnings("rawtypes")
	public synchronized int calculateQoSLevels(Iterator services, Map<String, QoSAttribute> qoSAttributes, QoSMinMax qoSMinMax,
			ServiceClassificationInfo serviceClassificationInfo, final Integer... numbersOfLevels) throws Exception {

		logger.info("Starting to calculate QoS Levels for serviceClassification=" + serviceClassification + ", numbersOfLevels=" + ArrayUtils.toString(numbersOfLevels));

		long start = System.currentTimeMillis();

		final Map<String, List<ServiceQoSInformationItem>> mapServicesByQosAtt = new LinkedHashMap<String, List<ServiceQoSInformationItem>>();

		final Map<String, NavigableMap<Double, Double>> mapServiceQoSUtility = new HashMap<String, NavigableMap<Double, Double>>();

		Double highestUtility = null;

		int count = 0;

		try {

			while (services.hasNext()) {

				DiscoveredService s = (DiscoveredService) services.next();

				if (!serviceClassification.equals(s.getServiceClassification()))
					throw new IllegalArgumentException("All services must be of the same service classification!");

				Double serviceUtility = ServiceQoSUtil.calculateServiceUtility(s, qoSAttributes, qoSMinMax, null);

				// Highest Utility
				if (highestUtility == null) {
					highestUtility = serviceUtility;
				} else {
					highestUtility = Math.max(highestUtility, serviceUtility);
				}

				for (ServiceQoSInformationItem q : s.getQoSInformation()) {

					String qoSKey = q.getQoSKey();

					QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

					if (isQoSAttributeNotConfigured(qoSAtt))
						continue;

					q.setService(s);

					Double qoSValue = q.getQoSValue();

					// Utility Map
					NavigableMap<Double, Double> qoSMap = mapServiceQoSUtility.get(qoSKey);

					if (qoSMap == null) {
						qoSMap = new TreeMap<Double, Double>();
						mapServiceQoSUtility.put(qoSKey, qoSMap);
					}

					Double utility = qoSMap.get(qoSValue);

					if (utility == null)
						qoSMap.put(qoSValue, serviceUtility);
					else
						qoSMap.put(qoSValue, Math.max(serviceUtility, utility));

					List<ServiceQoSInformationItem> l = mapServicesByQosAtt.get(qoSKey);
					if (l == null) {

						l = new SortedList<ServiceQoSInformationItem>(new BasicEventList<ServiceQoSInformationItem>(), new Comparator<ServiceQoSInformationItem>() {
							@Override
							public int compare(ServiceQoSInformationItem q1, ServiceQoSInformationItem q2) {
								return q1.getQoSValue().compareTo(q2.getQoSValue());
							}
						});

						mapServicesByQosAtt.put(qoSKey, l);
					}
					l.add(q);

				}
				count++;
			}

		} finally {
			if (services instanceof AutoCloseable)
				try {
					((AutoCloseable) services).close();
				} catch (Exception e) {
				}
		}

		calculateQoS(mapServicesByQosAtt, qoSAttributes, mapServiceQoSUtility, serviceClassificationInfo, highestUtility, numbersOfLevels);

		long d = System.currentTimeMillis() - start;
		logger.info("QoS Levels for serviceClassification=" + serviceClassification + ", numbersOfLevels=" + ArrayUtils.toString(numbersOfLevels) + " calculated in " + d + " ms");

		return count;

	}

	private boolean isQoSAttributeNotConfigured(QoSAttribute qoSAtt) {
		return qoSAtt == null || qoSAtt.getSequentialAggregationType() == null || qoSAtt.getConditionalAggregationType() == null || qoSAtt.getParallelAggregationType() == null
				|| qoSAtt.getLoopAggregationType() == null;
	}

	private void calculateQoS(Map<String, List<ServiceQoSInformationItem>> mapServicesByQosAtt, Map<String, QoSAttribute> qoSAttributes,
			Map<String, NavigableMap<Double, Double>> mapServiceQoSUtility, ServiceClassificationInfo serviceClassificationInfo, Double highestUtility, Integer... numbersOfLevels) {

		for (Entry<String, List<ServiceQoSInformationItem>> e : mapServicesByQosAtt.entrySet()) {

			String qoSKey = e.getKey();

			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			// Not configured QoS Attribute
			if (qoSAtt.getValueUtilityDirection() == null)
				continue;

			List<ServiceQoSInformationItem> servicesQoS = e.getValue();

			NavigableMap<Double, Double> qoSUtility = mapServiceQoSUtility.get(qoSKey);

			Map<Integer, Collection<QoSLevel>> levelsMap = qosLevelsMapByAtt.get(qoSKey);
			if (levelsMap == null) {
				levelsMap = new TreeMap<Integer, Collection<QoSLevel>>();
				qosLevelsMapByAtt.put(qoSKey, levelsMap);
			}

			int l = servicesQoS.size();

			Collection<QoSLevelsGroupInternal> groups = new ArrayList<QoSLevelsGroupInternal>(numbersOfLevels.length);

			for (int numberOfLevels : numbersOfLevels) {
				if (numberOfLevels <= l) {
					int groupSize = (l - 2) / (numberOfLevels - 2);

					QoSLevelsGroupInternal g = new QoSLevelsGroupInternal(numberOfLevels, groupSize);

					QoSLevel currentLevel = new QoSLevel(serviceClassification, qoSKey, 2, numberOfLevels);
					g.currentLevel = currentLevel;

					Double first = servicesQoS.get(0).getQoSValue();
					Double last = servicesQoS.get(l - 1).getQoSValue();

					g.levelValues.add(first);
					g.levelValues.add(last);

					QoSLevel firstLevel = new QoSLevel(serviceClassification, qoSKey, 1, numberOfLevels, first);
					firstLevel.setNegativeUtility(calculateUtility(firstLevel, qoSAtt, qoSUtility, serviceClassificationInfo, highestUtility, true));
					firstLevel.setPositiveUtility(calculateUtility(firstLevel, qoSAtt, qoSUtility, serviceClassificationInfo, highestUtility, false));
					g.levels.add(firstLevel);

					g.levels.add(currentLevel);
					QoSLevel lastLevel = new QoSLevel(serviceClassification, qoSKey, numberOfLevels, numberOfLevels, last);
					lastLevel.setNegativeUtility(calculateUtility(lastLevel, qoSAtt, qoSUtility, serviceClassificationInfo, highestUtility, true));
					lastLevel.setPositiveUtility(calculateUtility(lastLevel, qoSAtt, qoSUtility, serviceClassificationInfo, highestUtility, false));
					g.levels.add(lastLevel);
					groups.add(g);

					levelsMap.put(numberOfLevels, g.levels);
				}
			}

			int i = 0;

			for (ServiceQoSInformationItem q : servicesQoS) {

				if (i > 0 && i < l - 1) {

					for (QoSLevelsGroupInternal g : groups) {

						List<Double> values = g.currentLevel.getValues();

						values.add(q.getQoSValue());

						if (i % g.offset == 0 || i == l - 2) {

							double v = values.get(new Random().nextInt(values.size()));

							// nao pode escolher um mesmo valor q ja foi escolhido antes
							int k = 0;
							while (g.levelValues.contains(v) && k++ < values.size()) {
								v = values.get(new Random().nextInt(values.size()));
							}
							g.levelValues.add(v);

							g.currentLevel.setValue(v);

							g.currentLevel.setNegativeUtility(calculateUtility(g.currentLevel, qoSAtt, qoSUtility, serviceClassificationInfo, highestUtility, true));
							g.currentLevel.setPositiveUtility(calculateUtility(g.currentLevel, qoSAtt, qoSUtility, serviceClassificationInfo, highestUtility, false));

							if (g.levels.size() < g.numberOfLevels) {
								QoSLevel currentLevel = new QoSLevel(serviceClassification, q.getQoSKey(), g.currentLevel.getNumber() + 1, g.numberOfLevels);
								g.currentLevel = currentLevel;
								g.levels.add(g.levels.size() - 1, currentLevel);
							}
						}
					}
				}
				i++;
			}
		}
	}

	private double calculateUtility(QoSLevel q, QoSAttribute qoSAtt, NavigableMap<Double, Double> qoSUtility, ServiceClassificationInfo serviceClassificationInfo,
			Double highestUtility, boolean isNegative) {

		String qoSKey = q.getQoSKey();
		double v = q.getValue();

		int totalNumberOfServices = serviceClassificationInfo.getTotalNumberOfServices();

		int h = isNegative ? serviceClassificationInfo.getCountLEQoSValue(qoSKey, v) : serviceClassificationInfo.getCountGEQoSValue(qoSKey, v);

		Double us = 0D;

		Collection<Double> servicesUtilities = isNegative ? qoSUtility.headMap(v, true).values() : qoSUtility.tailMap(v, true).values();

		for (Double vu : servicesUtilities)
			us = Math.max(us, vu);

		double u = ((double) h / (double) totalNumberOfServices) * (us / highestUtility);

		return u;

	}

	private class QoSLevelsGroupInternal {

		int offset;

		int numberOfLevels;

		List<QoSLevel> levels;

		QoSLevel currentLevel;

		Set<Double> levelValues = new HashSet<Double>();

		QoSLevelsGroupInternal(int numberOfLevels, int offset) {
			this.numberOfLevels = numberOfLevels;
			this.offset = offset;
			this.levels = new LinkedList<QoSLevel>();
		}

	}

}
