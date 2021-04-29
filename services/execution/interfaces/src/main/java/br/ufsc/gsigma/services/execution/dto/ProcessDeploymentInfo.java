package br.ufsc.gsigma.services.execution.dto;

import java.io.Serializable;

public class ProcessDeploymentInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String processName;

	private String processId;

	private String deploymentPackage;

	private boolean error;

	private String errorMessage;

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

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getDeploymentPackage() {
		return deploymentPackage;
	}

	public void setDeploymentPackage(String deploymentPackage) {
		this.deploymentPackage = deploymentPackage;
	}

	@Override
	public String toString() {
		return "ProcessDeploymentInfo [processName=" + processName + ", processId=" + processId + ", deploymentPackage=" + deploymentPackage
				+ (error ? ", error=" + error + ", errorMessage=" + errorMessage : "") + "]";
	}

}
