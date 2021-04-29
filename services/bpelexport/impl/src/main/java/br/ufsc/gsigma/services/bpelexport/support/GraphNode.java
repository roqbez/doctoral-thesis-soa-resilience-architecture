package br.ufsc.gsigma.services.bpelexport.support;

import java.util.HashSet;
import java.util.Set;

public class GraphNode {

	private Object object;

	private Set<GraphNode> inputs = new HashSet<GraphNode>();
	private Set<GraphNode> outputs = new HashSet<GraphNode>();

	public GraphNode(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	public Set<GraphNode> getInputs() {
		return inputs;
	}

	public Set<GraphNode> getOutputs() {
		return outputs;
	}

	@Override
	public String toString() {
		return object.toString();
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return object.equals(((GraphNode) obj).object);
	}

}
