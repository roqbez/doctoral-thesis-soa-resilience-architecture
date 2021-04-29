package br.ufsc.gsigma.services.bpelexport.model;

import java.util.HashMap;
import java.util.Map;

import br.ufsc.gsigma.catalog.services.model.Task;

public class BPELInvoke extends BPELConnectableComponent {

	private BPELService service;

	private Map<BPELService, BPELAssign> mapSourceServiceToAssign = new HashMap<BPELService, BPELAssign>();

	private Task equivalentTask;

	public enum JoinCondition {
		AND, OR
	};

	private JoinCondition joinCondition = JoinCondition.AND;

	private String receiveName;

	private String replyName;

	private String callbackOperation;

	private boolean endEvent;

	public BPELInvoke(String name, String receiveName, String replyName, String callbackOperation, BPELService service) {
		this.name = name;
		this.receiveName = receiveName;
		this.replyName = replyName;
		this.callbackOperation = callbackOperation;
		this.service = service;
	}

	public String getJoinCondition() {
		if (targetLinks.size() < 2)
			return null;
		else if (joinCondition == JoinCondition.AND)
			return null;
		else {

			String joinCondition = "";

			int i = 0;
			for (BPELLink link : targetLinks) {
				if (i < targetLinks.size() - 1)
					joinCondition += "$" + link + " or ";
				else
					joinCondition += "$" + link;
				i++;
			}

			return joinCondition;

		}

	}

	public boolean isEndEvent() {
		return endEvent;
	}

	public void setEndEvent(boolean endEvent) {
		this.endEvent = endEvent;
	}

	public void setJoinCondition(JoinCondition joinCondition) {
		this.joinCondition = joinCondition;
	}

	public BPELService getService() {
		return service;
	}

	public void setService(BPELService service) {
		this.service = service;
	}

	// public Set<BPELAssign> getInputAssigns() {
	// return inputAssigns;
	// }
	//
	// public void setInputAssigns(Set<BPELAssign> inputAssigns) {
	// this.inputAssigns = inputAssigns;
	// }

	public Task getEquivalentTask() {
		return equivalentTask;
	}

	public void setEquivalentTask(Task equivalentTask) {
		this.equivalentTask = equivalentTask;
	}

	public Map<BPELService, BPELAssign> getMapSourceServiceToAssign() {
		return mapSourceServiceToAssign;
	}

	public void setMapSourceServiceToAssign(Map<BPELService, BPELAssign> mapSourceServiceToAssign) {
		this.mapSourceServiceToAssign = mapSourceServiceToAssign;
	}

	public String getReceiveName() {
		return receiveName;
	}

	public void setReceiveName(String receiveName) {
		this.receiveName = receiveName;
	}

	public String getReplyName() {
		return replyName;
	}

	public void setReplyName(String replyName) {
		this.replyName = replyName;
	}

	public String getCallbackOperation() {
		return callbackOperation;
	}

	public void setCallbackOperation(String callbackOperation) {
		this.callbackOperation = callbackOperation;
	}

	@Override
	public String toString() {
		return name;
	}

}
