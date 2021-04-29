package br.ufsc.gsigma.servicediscovery.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;

public class ServiceClassificationInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, Double> maxQoSValue = new HashMap<String, Double>();

	private Map<String, Double> minQoSValue = new HashMap<String, Double>();

	private Map<String, Double> avgQoSValue = new HashMap<String, Double>();

	private Map<String, Double> sdQoSValue = new HashMap<String, Double>();

	private Map<String, NavigableMap<Double, Integer>> qoSValuesDistribution = new HashMap<String, NavigableMap<Double, Integer>>();

	private transient Map<String, List<Double>> qoSValues = new HashMap<String, List<Double>>();

	private int totalNumberOfServices;

	public Map<Double, Integer> getQoSValuesDistribution(String serviceClassification, String qoSKey) {

		NavigableMap<Double, Integer> qoSAttDist = qoSValuesDistribution.get(qoSKey);

		if (qoSAttDist == null)
			return null;

		return Collections.unmodifiableMap(qoSAttDist);
	}

	public Integer getCountLEQoSValue(String qoSKey, Double value) {

		NavigableMap<Double, Integer> qoSAttDist = qoSValuesDistribution.get(qoSKey);

		if (qoSAttDist == null)
			return null;

		int count = 0;

		for (Integer c : qoSAttDist.headMap(value, true).values()) {
			count += c;
		}

		return count;
	}

	public Integer getCountGEQoSValue(String qoSKey, Double value) {

		NavigableMap<Double, Integer> qoSAttDist = qoSValuesDistribution.get(qoSKey);

		if (qoSAttDist == null)
			return null;

		int count = 0;

		for (Integer c : qoSAttDist.tailMap(value, true).values()) {
			count += c;
		}

		return count;
	}

	public Double getQoSMinValue(String qoSKey) {
		return minQoSValue.get(qoSKey);
	}

	public Double getQoSMaxValue(String qoSKey) {
		return maxQoSValue.get(qoSKey);
	}

	public void postProcess() {

		for (Entry<String, List<Double>> e : qoSValues.entrySet()) {

			String qosKey = e.getKey();
			List<Double> values = e.getValue();

			int count = values.size();

			// Sum
			double sum = 0;
			for (double v : values)
				sum += v;

			// Avg
			double avg = sum / count;
			avgQoSValue.put(qosKey, avg);

			// Variance
			double t = 0;

			for (double v : values)
				t += (avg - v) * (avg - v);

			double variance = t / values.size();

			// Standard Deviation
			double sd = Math.sqrt(variance);
			sdQoSValue.put(qosKey, sd);

		}

	}

	public void collect(DiscoveredService s) {

		synchronized (this) {

			this.totalNumberOfServices++;

			for (ServiceQoSInformationItem q : s.getQoSInformation()) {

				String qosKey = q.getQoSKey();
				Double qoSValue = q.getQoSValue();

				// QoS Values
				List<Double> values = qoSValues.get(qosKey);
				if (values == null) {
					values = new LinkedList<Double>();
					qoSValues.put(qosKey, values);
				}
				values.add(qoSValue);

				// MinMax
				Double min = minQoSValue.get(qosKey);
				Double max = maxQoSValue.get(qosKey);

				if (min == null) {
					min = qoSValue;
				} else {
					min = Math.min(min, qoSValue);
				}

				if (max == null) {
					max = qoSValue;
				} else {
					max = Math.max(max, qoSValue);
				}

				minQoSValue.put(qosKey, min);
				maxQoSValue.put(qosKey, max);

				// ValueDist
				NavigableMap<Double, Integer> valueDist = qoSValuesDistribution.get(qosKey);
				if (valueDist == null) {
					valueDist = new TreeMap<Double, Integer>();
					qoSValuesDistribution.put(qosKey, valueDist);
				}

				Integer c = valueDist.get(qoSValue);
				if (c == null)
					c = 0;

				valueDist.put(qoSValue, c + 1);
			}
		}
	}

	public Map<String, Double> getMaxQoSValue() {
		return maxQoSValue;
	}

	public Map<String, Double> getMinQoSValue() {
		return minQoSValue;
	}

	public Map<String, Double> getAvgQoSValue() {
		return avgQoSValue;
	}

	public Map<String, Double> getSdQoSValue() {
		return sdQoSValue;
	}

	public int getTotalNumberOfServices() {
		return totalNumberOfServices;
	}

}
