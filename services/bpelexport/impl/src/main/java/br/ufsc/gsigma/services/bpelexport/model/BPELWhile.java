package br.ufsc.gsigma.services.bpelexport.model;

import java.util.ArrayList;
import java.util.List;

public class BPELWhile extends BPELConnectableComponent {

	private List<BPELFlow> flows = new ArrayList<BPELFlow>();

	private String condition = "true()";

	public BPELWhile(String name) {
		this.name = name;
	}

	public List<BPELFlow> getFlows() {
		return flows;
	}

	public void setFlows(List<BPELFlow> flows) {
		this.flows = flows;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

}
