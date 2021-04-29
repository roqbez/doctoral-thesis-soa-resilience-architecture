package br.ufsc.gsigma.services.bpelexport.model;

public class BPELLink {

	private String name;

	private String transitionCondition;

	private BPELConnectableComponent inputComponent;

	private BPELConnectableComponent outputComponent;

	public BPELLink(String name, BPELConnectableComponent outputComponent, BPELConnectableComponent inputComponent) {
		this.outputComponent = outputComponent;
		this.inputComponent = inputComponent;
		this.name = name;
	}

	public BPELLink(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTransitionCondition() {
		return transitionCondition;
	}

	public void setTransitionCondition(String transitionCondition) {
		this.transitionCondition = transitionCondition;
	}

	public BPELConnectableComponent getInputComponent() {
		return inputComponent;
	}

	public void setInputComponent(BPELConnectableComponent inputComponent) {
		this.inputComponent = inputComponent;
	}

	public BPELConnectableComponent getOutputComponent() {
		return outputComponent;
	}

	public void setOutputComponent(BPELConnectableComponent outputComponent) {
		this.outputComponent = outputComponent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BPELLink other = (BPELLink) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}
