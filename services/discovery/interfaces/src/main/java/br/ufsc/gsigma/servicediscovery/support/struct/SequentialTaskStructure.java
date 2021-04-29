package br.ufsc.gsigma.servicediscovery.support.struct;

import java.util.Collections;
import java.util.List;

public class SequentialTaskStructure extends SequentialStructure {

	private static final long serialVersionUID = 1L;

	public SequentialTaskStructure() {
	}

	public SequentialTaskStructure(TaskStructure task) {
		this(Collections.singletonList(task));
	}

	public SequentialTaskStructure(List<TaskStructure> tasks) {
		for (TaskStructure t : tasks)
			getChilds().add(t);
	}

	public void addTask(TaskStructure task) {
		getChilds().add(task);
	}

	public void addTask(SequentialTaskStructure s) {
		getChilds().addAll(s.getChilds());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TaskStructure> getTasks() {
		return (List) getChilds();
	}

	@Override
	protected void appendChilds(StringBuilder sb, Integer depth) {
		int i = 0;
		sb.append("[");
		for (TaskStructure t : getTasks()) {
			sb.append(t.getLabel());
			if (i++ < getTasks().size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
	}

}
