package br.ufsc.gsigma.services.bpelexport.model;

import java.util.ArrayList;
import java.util.List;

public class BPELAssign extends BPELConnectableComponent {

	private List<BPELCopy> copies = new ArrayList<BPELCopy>();

	public BPELAssign(String name) {
		this.name = name;
	}

	public List<BPELCopy> getCopies() {
		return copies;
	}

	public void setCopies(List<BPELCopy> copies) {
		this.copies = copies;
	}

	@Override
	public String toString() {
		return name;
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
		BPELAssign other = (BPELAssign) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
