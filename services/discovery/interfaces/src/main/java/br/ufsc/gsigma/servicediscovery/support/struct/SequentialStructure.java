package br.ufsc.gsigma.servicediscovery.support.struct;

import java.util.List;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;

public class SequentialStructure extends Structure {
	private static final long serialVersionUID = 1L;

	public SequentialStructure() {
	}

	public SequentialStructure(List<ConnectableComponent> childs) {
		setChilds(childs);
	}

	public ConnectableComponent getFirst() {
		return getChilds().get(0);
	}

	public ConnectableComponent getLast() {
		return getChilds().get(getChilds().size() - 1);
	}

}
