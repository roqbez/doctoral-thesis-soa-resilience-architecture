package br.ufsc.gsigma.servicediscovery.support.struct;

import java.util.List;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;

public class CycleStructure extends Structure {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_REPEAT_COUNT = 2;

	private int repeatCount = DEFAULT_REPEAT_COUNT;

	public CycleStructure(List<ConnectableComponent> components) {
		this(components, DEFAULT_REPEAT_COUNT);
	}

	public CycleStructure(List<ConnectableComponent> components, int repeatCount) {
		for (ConnectableComponent c : components) {
			getChilds().add(c);
		}
		this.repeatCount = repeatCount;
	}

	public CycleStructure() {
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

}
