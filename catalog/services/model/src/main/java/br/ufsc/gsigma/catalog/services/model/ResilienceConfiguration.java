package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResilienceConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private int bindingServiceReplicas = 1;

	@XmlAttribute
	private int executionServiceReplicas = 1;

	@XmlAttribute
	private int resilienceServiceReplicas = 1;

	private Map<String, String> params = new LinkedHashMap<String, String>();

	private Map<String, Boolean> flags = new LinkedHashMap<String, Boolean>();

	public ResilienceConfiguration() {
	}

	public ResilienceConfiguration(int bindingServiceReplicas, int executionServiceReplicas, int resilienceServiceReplicas) {
		this.bindingServiceReplicas = bindingServiceReplicas;
		this.executionServiceReplicas = executionServiceReplicas;
		this.resilienceServiceReplicas = resilienceServiceReplicas;
	}

	public int getBindingServiceReplicas() {
		return bindingServiceReplicas;
	}

	public void setBindingServiceReplicas(int bindingServiceReplicas) {
		this.bindingServiceReplicas = bindingServiceReplicas;
	}

	public int getExecutionServiceReplicas() {
		return executionServiceReplicas;
	}

	public void setExecutionServiceReplicas(int executionServiceReplicas) {
		this.executionServiceReplicas = executionServiceReplicas;
	}

	public int getResilienceServiceReplicas() {
		return resilienceServiceReplicas;
	}

	public void setResilienceServiceReplicas(int resilienceServiceReplicas) {
		this.resilienceServiceReplicas = resilienceServiceReplicas;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public Map<String, Boolean> getFlags() {
		return flags;
	}

	public void setFlags(Map<String, Boolean> flags) {
		this.flags = flags;
	}

}
