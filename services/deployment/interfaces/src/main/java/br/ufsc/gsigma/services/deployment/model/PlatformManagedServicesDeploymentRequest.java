package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class PlatformManagedServicesDeploymentRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String managerServer;

	private boolean waitContainersStartup;

	private boolean waitOnlyFirstReplica;

	@XmlElementWrapper
	@XmlElement(name = "item")
	private List<PlatformManagedServiceDeploymentRequestItem> items = new LinkedList<PlatformManagedServiceDeploymentRequestItem>();

	public String getManagerServer() {
		return managerServer;
	}

	public void setManagerServer(String managerServer) {
		this.managerServer = managerServer;
	}

	public List<PlatformManagedServiceDeploymentRequestItem> getItems() {
		return items;
	}

	public void setItems(List<PlatformManagedServiceDeploymentRequestItem> items) {
		this.items = items;
	}

	public boolean isWaitContainersStartup() {
		return waitContainersStartup;
	}

	public void setWaitContainersStartup(boolean waitContainersStartup) {
		this.waitContainersStartup = waitContainersStartup;
	}

	public boolean isWaitOnlyFirstReplica() {
		return waitOnlyFirstReplica;
	}

	public void setWaitOnlyFirstReplica(boolean waitOnlyFirstReplica) {
		this.waitOnlyFirstReplica = waitOnlyFirstReplica;
	}

	@Override
	public String toString() {
		return "[managerServer=" + managerServer + ", waitContainersStartup=" + waitContainersStartup + ", waitOnlyFirstReplica=" + waitOnlyFirstReplica + ", items=" + items + "]";
	}

}