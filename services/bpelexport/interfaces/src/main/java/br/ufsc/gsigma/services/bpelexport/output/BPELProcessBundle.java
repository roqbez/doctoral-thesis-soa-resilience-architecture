package br.ufsc.gsigma.services.bpelexport.output;

import java.io.Serializable;

public class BPELProcessBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private String targetRuntime = "ODE";

	private String processName;

	private byte[] zipContent;

	public BPELProcessBundle() {

	}

	public BPELProcessBundle(String processName, byte[] zipContent) {
		this.processName = processName;
		this.zipContent = zipContent;
	}

	public String getTargetRuntime() {
		return targetRuntime;
	}

	public void setTargetRuntime(String targetRuntime) {
		this.targetRuntime = targetRuntime;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public byte[] getZipContent() {
		return zipContent;
	}

	public void setZipContent(byte[] zipContent) {
		this.zipContent = zipContent;
	}

}
