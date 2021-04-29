package br.ufsc.gsigma.services.bpelexport.support;

public class GraphCycle {

	private GraphNode sourceNode;
	private GraphNode targetNode;

	public GraphCycle(GraphNode sourceNode, GraphNode targetNode) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
	}

	public GraphNode getSourceNode() {
		return sourceNode;
	}

	public GraphNode getTargetNode() {
		return targetNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceNode == null) ? 0 : sourceNode.hashCode());
		result = prime * result + ((targetNode == null) ? 0 : targetNode.hashCode());
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
		GraphCycle other = (GraphCycle) obj;
		if (sourceNode == null) {
			if (other.sourceNode != null)
				return false;
		} else if (!sourceNode.equals(other.sourceNode))
			return false;
		if (targetNode == null) {
			if (other.targetNode != null)
				return false;
		} else if (!targetNode.equals(other.targetNode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return sourceNode.getObject() + "->" + targetNode.getObject();
	}

}
