package br.ufsc.gsigma.servicediscovery.support.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.catalog.services.model.TaskParticipant;
import br.ufsc.gsigma.servicediscovery.model.QoSLevel;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;

public class TaskStructure extends Structure {

	private static final long serialVersionUID = 1L;

	private String taxonomyClassification;

	private Map<String, Double> qoSValues = new HashMap<String, Double>();

	private Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSLevels = new HashMap<String, NavigableMap<Integer, List<QoSLevel>>>();

	private Map<String, NavigableMap<Integer, QoSLevel>> qoSLevelsValue = new HashMap<String, NavigableMap<Integer, QoSLevel>>();

	private Map<String, Double> maxQoSValue = new HashMap<String, Double>();

	private Map<String, Double> minQoSValue = new HashMap<String, Double>();

	private int repeatCount;

	private int invokeSequence = 1;

	private List<TaskParticipant> participants = new ArrayList<TaskParticipant>();

	public TaskStructure(Task task) {
		this.id = task.getId();
		this.name = task.getName();

		setInputContactPoints(task.getInputContactPoints());
		setOutputContactPoints(task.getOutputContactPoints());

		// this.inputContactPoints = task.getInputContactPoints();
		// this.outputContactPoints = task.getOutputContactPoints();

		this.taxonomyClassification = task.getTaxonomyClassification();

		this.participants = task.getParticipants();
	}

	public TaskStructure(String name) {
		this.name = name;
	}

	public TaskStructure(String name, String taxonomyClassification) {
		this.name = name;
		this.taxonomyClassification = taxonomyClassification;
	}

	public TaskStructure(String name, Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSLevels) {
		this.name = name;
		this.qoSLevels = qoSLevels;
	}

	public int getInvokeSequence() {
		return invokeSequence;
	}

	public void setInvokeSequence(int invokeSequence) {
		this.invokeSequence = invokeSequence;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	public List<TaskParticipant> getParticipants() {
		return participants;
	}

	public Map<String, Double> getQoSValues() {
		return qoSValues;
	}

	public Double getQoSValue(String qoSKey) {

		if (qoSValues == null)
			return null;

		return qoSValues.get(qoSKey);
	}

	public void setQoSValue(String qoSKey, Double value) {

		if (qoSValues == null)
			qoSValues = new HashMap<String, Double>();

		qoSValues.put(qoSKey, value);
	}

	public void clearQoSValues() {

		if (qoSValues != null)
			qoSValues.clear();

		if (minQoSValue != null)
			minQoSValue.clear();

		if (maxQoSValue != null)
			maxQoSValue.clear();
	}

	public QoSLevel getChoosenQoSLevel(String qoSKey, int numberOfLevels) {

		NavigableMap<Integer, QoSLevel> m = qoSLevelsValue.get(qoSKey);

		if (m == null)
			return null;

		return m.get(numberOfLevels);
	}

	public List<QoSLevel> getChoosenQoSLevels() {
		List<QoSLevel> levels = new LinkedList<QoSLevel>();

		for (String qoSKey : qoSLevelsValue.keySet())
			levels.add(getChoosenQoSLevel(qoSKey));

		return levels;
	}

	public QoSLevel getChoosenQoSLevel(String qoSKey) {

		NavigableMap<Integer, QoSLevel> m = qoSLevelsValue.get(qoSKey);

		if (m == null)
			return null;

		return m.lastEntry().getValue();
	}

	public void setChoosenQoSLevel(QoSLevel level) {

		NavigableMap<Integer, QoSLevel> m = qoSLevelsValue.get(level.getQoSKey());

		if (m == null) {
			m = new TreeMap<Integer, QoSLevel>();
			qoSLevelsValue.put(level.getQoSKey(), m);
		}

		m.put(level.getTotalNumberOfLevels(), level);
	}

	public void addQoSLevel(QoSLevel level) {

		String qoSKey = level.getQoSKey();

		NavigableMap<Integer, List<QoSLevel>> m = qoSLevels.get(qoSKey);

		if (m == null) {
			m = new TreeMap<Integer, List<QoSLevel>>();
			qoSLevels.put(qoSKey, m);
		}

		List<QoSLevel> l = m.get(level.getTotalNumberOfLevels());

		if (l == null) {
			l = new SortedList<QoSLevel>(new BasicEventList<QoSLevel>(), new Comparator<QoSLevel>() {
				@Override
				public int compare(QoSLevel q1, QoSLevel q2) {
					return new Double(q1.getValue()).compareTo(q2.getValue());
				}
			});
			m.put(level.getTotalNumberOfLevels(), l);
		}

		l.add(level);

	}

	@SuppressWarnings("unchecked")
	public List<QoSLevel> getQoSLevels(String qoSKey, int numberOfLevels) {

		NavigableMap<Integer, List<QoSLevel>> m = qoSLevels.get(qoSKey);

		if (m == null)
			return Collections.EMPTY_LIST;

		List<QoSLevel> l = m.get(numberOfLevels);

		if (l == null)
			return Collections.EMPTY_LIST;

		return l;
	}

	public Map<String, NavigableMap<Integer, List<QoSLevel>>> getQoSLevels() {
		return qoSLevels;
	}

	public void setQoSLevels(Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSLevels) {
		this.qoSLevels = qoSLevels;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// public List<InputContactPoint> getInputContactPoints() {
	// return inputContactPoints;
	// }
	//
	// public void setInputContactPoints(List<InputContactPoint> inputContactPoints) {
	// this.inputContactPoints = inputContactPoints;
	// }
	//
	// public List<OutputContactPoint> getOutputContactPoints() {
	// return outputContactPoints;
	// }
	//
	// public void setOutputContactPoints(List<OutputContactPoint> outputContactPoints) {
	// this.outputContactPoints = outputContactPoints;
	// }

	public String getTaxonomyClassification() {
		return taxonomyClassification;
	}

	public void setTaxonomyClassification(String taxonomyClassification) {
		this.taxonomyClassification = taxonomyClassification;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && invokeSequence == ((TaskStructure) obj).invokeSequence;
	}

	@Override
	public String getLabel() {
		return (invokeSequence > 1 ? " (" + invokeSequence + ")" : "") + name;
	}

}
