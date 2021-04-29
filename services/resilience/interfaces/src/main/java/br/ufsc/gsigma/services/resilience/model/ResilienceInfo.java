package br.ufsc.gsigma.services.resilience.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResilienceInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private long reactionTime;

	private long servicesCheckTime;

	private long discoveryTime;

	private long deploymentTime;

	public ResilienceInfo() {
	}

	public long getReactionTime() {
		return reactionTime;
	}

	public void setReactionTime(long reactionTime) {
		this.reactionTime = reactionTime;
	}

	public long getServicesCheckTime() {
		return servicesCheckTime;
	}

	public void setServicesCheckTime(long servicesCheckTime) {
		this.servicesCheckTime = servicesCheckTime;
	}

	public long getDiscoveryTime() {
		return discoveryTime;
	}

	public void setDiscoveryTime(long discoveryTime) {
		this.discoveryTime = discoveryTime;
	}

	public long getDeploymentTime() {
		return deploymentTime;
	}

	public void setDeploymentTime(long deploymentTime) {
		this.deploymentTime = deploymentTime;
	}

	@Override
	public String toString() {
		return "[reactionTime=" + reactionTime + ", serviceCheckTime=" + servicesCheckTime + ", discoveryTime=" + discoveryTime + ", deploymentTime=" + deploymentTime + "]";
	}

}
