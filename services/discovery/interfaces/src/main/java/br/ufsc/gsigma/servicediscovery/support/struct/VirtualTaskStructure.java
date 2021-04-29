package br.ufsc.gsigma.servicediscovery.support.struct;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;

import br.ufsc.gsigma.servicediscovery.model.QoSLevel;
import br.ufsc.gsigma.servicediscovery.support.ProcessStructureParser.ProcessStructureAggregator;

public class VirtualTaskStructure extends TaskStructure {

	private static final long serialVersionUID = 1L;

	public enum TaskAggregationType {
		SEQUENTIAL, PARALLEL, CONDITIONAL, CYCLE
	}

	private TaskAggregationType aggregationType;

	public VirtualTaskStructure(String name, TaskAggregationType aggregationType) {
		super(name);
		this.aggregationType = aggregationType;
		setTaxonomyClassification(name);
	}

	public VirtualTaskStructure(String name, TaskAggregationType aggregationType, List<TaskStructure> tasks) {
		this(name, aggregationType, tasks, 0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public VirtualTaskStructure(String name, TaskAggregationType aggregationType, List<TaskStructure> tasks, int repeatCount) {
		super(name);
		setChilds((List) tasks);
		sortChilds();
		this.aggregationType = aggregationType;
		setTaxonomyClassification(name);
		setRepeatCount(repeatCount);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public VirtualTaskStructure(String name, TaskAggregationType aggregationType, List<TaskStructure> tasks, Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSLevels) {
		super(name, qoSLevels);
		setChilds((List) tasks);
		sortChilds();
		this.aggregationType = aggregationType;
		setTaxonomyClassification(name);
	}

	@Override
	public String getLabel() {
		return aggregationType + "|" + super.getLabel();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TaskStructure> duplicateTasks(int multiplier) {

		if (multiplier > 0) {

			List<TaskStructure> result = new LinkedList<TaskStructure>();

			for (int i = 1; i <= multiplier; i++) {

				List<TaskStructure> childsClone = (List<TaskStructure>) SerializationUtils.clone((Serializable) getTasks());
				renameChilds((List) childsClone, i + "_");

				result.addAll(childsClone);
			}
			return result;
		}
		return Collections.EMPTY_LIST;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void renameChilds(List<Structure> childs, String prefix) {

		for (Structure s : childs) {

			s.setName(prefix + s.getName());

			if (s instanceof VirtualTaskStructure) {
				((VirtualTaskStructure) s).setTaxonomyClassification(s.getName());
			}

			renameChilds((List) s.getChilds(), prefix);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TaskStructure> getTasks() {
		return (List) getChilds();
	}

	public void addTask(TaskStructure task) {
		getChilds().add(task);
		sortChilds();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sortChilds() {
		Collections.sort(getChilds(), (Comparator) new Comparator<TaskStructure>() {
			@Override
			public int compare(TaskStructure t1, TaskStructure t2) {

				if (t1 instanceof VirtualTaskStructure && t2 instanceof VirtualTaskStructure)
					return t1.getName().compareTo(t2.getName());
				else if (t1 instanceof VirtualTaskStructure)
					return 1;
				else if (t2 instanceof VirtualTaskStructure)
					return -1;
				else
					return t1.getName().compareTo(t2.getName());
			}
		});
	}

	public TaskAggregationType getAggregationType() {
		return aggregationType;
	}

	public Set<TaskStructure> getLeafTasks() {
		return getLeafTasks(getTasks());
	}

	private Set<TaskStructure> getLeafTasks(List<TaskStructure> tasks) {

		Set<TaskStructure> result = new LinkedHashSet<TaskStructure>();

		for (TaskStructure t : tasks) {

			if (t instanceof VirtualTaskStructure) {
				for (TaskStructure ts : getLeafTasks(((VirtualTaskStructure) t).getTasks())) {
					result.add(ts);
				}
			} else {
				result.add(t);
			}
		}
		return result;
	}

	public void clearQoSValues() {

		super.clearQoSValues();

		for (TaskStructure t : getTasks()) {
			t.clearQoSValues();
		}
	}

	public void visitChilds(ProcessStructureAggregator visitor, Map<String, Boolean> params) {

		for (TaskStructure t : getTasks()) {
			if (t instanceof VirtualTaskStructure)
				((VirtualTaskStructure) t).visitChilds(visitor, params);
		}

		visitor.visitVirtualTask(this, params);
	}

}
