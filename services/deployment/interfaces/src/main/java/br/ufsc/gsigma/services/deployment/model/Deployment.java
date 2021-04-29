package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;

public class Deployment implements Serializable {

	private static final long serialVersionUID = 1L;

	private String businessProcessName;

	private String businessProcessId;

	private String businessProcessEndpoint;

	private boolean error;

	public String getBusinessProcessName() {
		return businessProcessName;
	}

	public void setBusinessProcessName(String businessProcessName) {
		this.businessProcessName = businessProcessName;
	}

	public String getBusinessProcessId() {
		return businessProcessId;
	}

	public void setBusinessProcessId(String businessProcessId) {
		this.businessProcessId = businessProcessId;
	}

	public String getBusinessProcessEndpoint() {
		return businessProcessEndpoint;
	}

	public void setBusinessProcessEndpoint(String businessProcessEndpoint) {
		this.businessProcessEndpoint = businessProcessEndpoint;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

}
