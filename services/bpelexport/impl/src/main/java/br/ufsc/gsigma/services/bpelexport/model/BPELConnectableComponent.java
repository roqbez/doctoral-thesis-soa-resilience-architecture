package br.ufsc.gsigma.services.bpelexport.model;

import java.util.HashSet;
import java.util.Set;

public abstract class BPELConnectableComponent {

	protected String name;

	protected Set<BPELLink> sourceLinks = new HashSet<BPELLink>();

	protected Set<BPELLink> targetLinks = new HashSet<BPELLink>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<BPELLink> getSourceLinks() {
		return sourceLinks;
	}

	public void setSourceLinks(Set<BPELLink> sourceLinks) {
		this.sourceLinks = sourceLinks;
	}

	public Set<BPELLink> getTargetLinks() {
		return targetLinks;
	}

	public void setTargetLinks(Set<BPELLink> targetLinks) {
		this.targetLinks = targetLinks;
	}

}
