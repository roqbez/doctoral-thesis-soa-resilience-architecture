package br.ufsc.gsigma.servicediscovery.support.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;

public class SplitJoinStructure extends Structure {

	private static final long serialVersionUID = 1L;

	private boolean conditional;

	private ConnectableComponent splitComponent;

	private ConnectableComponent joinComponent;

	private Collection<Collection<List<ConnectableComponent>>> branches;

	public SplitJoinStructure(Collection<Collection<List<ConnectableComponent>>> branches, boolean conditional, ConnectableComponent splitComponent, ConnectableComponent joinComponent) {
		this.branches = branches;
		this.conditional = conditional;
		this.splitComponent = splitComponent;
		this.joinComponent = joinComponent;
		updateBranches();
	}

	public void updateBranches() {
		getChilds().clear();
		for (Collection<List<ConnectableComponent>> b : branches) {
			getChilds().add(new GroupStructure(b));
		}
	}

	public boolean isConditional() {
		return conditional;
	}

	public ConnectableComponent getSplitComponent() {
		return splitComponent;
	}

	public ConnectableComponent getJoinComponent() {
		return joinComponent;
	}

	@SuppressWarnings("unchecked")
	public Collection<List<ConnectableComponent>> getBranches() {

		Collection<List<ConnectableComponent>> l = new ArrayList<List<ConnectableComponent>>(getChilds().size());

		for (Object o : getChilds()) {
			List<List<ConnectableComponent>> b = (List<List<ConnectableComponent>>) o;
			if (b.size() == 1)
				l.add(b.get(0));
		}

		return l;
	}

}
