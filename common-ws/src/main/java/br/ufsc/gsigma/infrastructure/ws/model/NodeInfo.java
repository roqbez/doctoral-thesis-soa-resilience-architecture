package br.ufsc.gsigma.infrastructure.ws.model;

public class NodeInfo {

	private String nodeId;

	private String containerId;

	private String ip;

	private String hostname;

	private Boolean leader;

	public NodeInfo() {
	}

	public NodeInfo(String nodeId, String containerId, String ip, String hostname, Boolean leader) {
		this.nodeId = nodeId;
		this.containerId = containerId;
		this.ip = ip;
		this.hostname = hostname;
		this.leader = leader;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Boolean getLeader() {
		return leader;
	}

	public void setLeader(Boolean leader) {
		this.leader = leader;
	}

}
