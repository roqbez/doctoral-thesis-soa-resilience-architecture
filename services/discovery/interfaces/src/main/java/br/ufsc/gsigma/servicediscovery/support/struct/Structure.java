package br.ufsc.gsigma.servicediscovery.support.struct;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;

public abstract class Structure extends ConnectableComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ConnectableComponent> childs = new LinkedList<ConnectableComponent>();

	private static ThreadLocal<Integer> toStringDepthThreadLocal = new ThreadLocal<Integer>();

	public List<ConnectableComponent> getChilds() {
		return childs;
	}

	public void setChilds(List<ConnectableComponent> childs) {
		this.childs = childs;
	}

	@Override
	public ConnectableComponent buildDefaultContactPoints() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Structure))
			return false;
		Structure other = (Structure) obj;
		if (childs == null) {
			if (other.childs != null)
				return false;
		} else if (!childs.equals(other.childs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	protected void toString(StringBuilder sb) {

		Integer depth = toStringDepthThreadLocal.get();

		boolean first = depth == null;

		depth = depth != null ? depth : 0;

		try {

			if (first) {
				sb.append("\n");
			}

			appendStr(sb, depth, getLabel());

			appendChilds(sb, depth);

		} finally {
			if (first) {
				toStringDepthThreadLocal.remove();
			}
		}
	}

	protected void appendChilds(StringBuilder sb, Integer depth) {
		if (childs != null && !childs.isEmpty()) {

			try {
				sb.append("[\n");

				toStringDepthThreadLocal.set(depth + 1);

				int i = 0;

				for (ConnectableComponent c : childs) {
					if (c instanceof Structure) {
						((Structure) c).toString(sb);

						if (i++ < childs.size() - 1) {
							sb.append(",");
						}

						sb.append("\n");
					}
				}
				appendStr(sb, depth, "]");
			} finally {
				toStringDepthThreadLocal.set(depth);
			}

		}
	}

	protected String getLabel() {
		return getClass().getSimpleName();
	}

	private void appendStr(StringBuilder sb, int depth, String str) {
		for (int i = 0; i < depth; i++) {
			sb.append('\t');
		}
		sb.append(str);
	}

}
