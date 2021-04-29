package br.ufsc.gsigma.services.bpelexport.model;

public class BPELCopy {

	private BPELFrom from = new BPELFrom();

	private BPELTo to = new BPELTo();

	public BPELCopy() {

	}

	public BPELCopy(String fromVariable, String fromQuery, String toVariable, String toQuery) {
		from.setVariable(fromVariable);
		from.setQuery(fromQuery);
		to.setVariable(toVariable);
		to.setQuery(toQuery);
	}

	public BPELFrom getFrom() {
		return from;
	}

	public void setFrom(BPELFrom from) {
		this.from = from;
	}

	public BPELTo getTo() {
		return to;
	}

	public void setTo(BPELTo to) {
		this.to = to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		BPELCopy other = (BPELCopy) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

}
