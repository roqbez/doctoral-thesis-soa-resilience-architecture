package br.ufsc.gsigma.services.execution.dto;

import java.io.Serializable;

public class ProcessInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String processName;

	private String processId;

	private String version;

	private String deploymentPackage;

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDeploymentPackage() {
		return deploymentPackage;
	}

	public void setDeploymentPackage(String deploymentPackage) {
		this.deploymentPackage = deploymentPackage;
	}

}
