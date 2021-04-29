package br.ufsc.gsigma.services.bpelexport.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BPELFlow {

	private String name;

	private BPELAssign initializeAssign;

	private Set<BPELInvoke> invokes = new HashSet<BPELInvoke>();

	private Set<BPELAssign> assigns = new HashSet<BPELAssign>();

	private Set<BPELLink> links = new HashSet<BPELLink>();

	private Set<BPELWhile> whiles = new HashSet<BPELWhile>();

	private Map<BPELInvoke, Set<BPELAssign>> mapInvokeToAssigns = new HashMap<BPELInvoke, Set<BPELAssign>>();

	public BPELFlow(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<BPELInvoke> getInvokes() {
		return invokes;
	}

	public void setInvokes(Set<BPELInvoke> invokes) {
		this.invokes = invokes;
	}

	public Set<BPELAssign> getAssigns() {
		return assigns;
	}

	public void setAssigns(Set<BPELAssign> assigns) {
		this.assigns = assigns;
	}

	public Set<BPELLink> getLinks() {
		return links;
	}

	public void setLinks(Set<BPELLink> links) {
		this.links = links;
	}

	public BPELAssign getInitializeAssign() {
		return initializeAssign;
	}

	public void setInitializeAssign(BPELAssign initializeAssign) {
		this.initializeAssign = initializeAssign;
	}

	public Map<BPELInvoke, Set<BPELAssign>> getMapInvokeToAssigns() {
		return mapInvokeToAssigns;
	}

	public void setMapInvokeToAssigns(Map<BPELInvoke, Set<BPELAssign>> mapInvokeToAssigns) {
		this.mapInvokeToAssigns = mapInvokeToAssigns;
	}

	public Set<BPELWhile> getWhiles() {
		return whiles;
	}

	public void setWhiles(Set<BPELWhile> whiles) {
		this.whiles = whiles;
	}

}
