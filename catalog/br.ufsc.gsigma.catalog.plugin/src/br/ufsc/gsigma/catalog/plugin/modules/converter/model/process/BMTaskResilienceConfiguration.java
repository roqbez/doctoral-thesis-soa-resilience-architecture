package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

@Root
public class BMTaskResilienceConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private boolean enableResilience = true;

	@Attribute
	private boolean enableAdhocServicesReplication = true;

	@Attribute
	private int serviceReplicas = 1;

	@ElementMap(attribute = true, key = "name", value = "value", required = false)
	private Map<String, String> params = new LinkedHashMap<String, String>();

	@ElementMap(attribute = true, key = "name", value = "value", required = false)
	private Map<String, Boolean> flags = new LinkedHashMap<String, Boolean>();

	public boolean isEnableResilience() {
		return enableResilience;
	}

	public void setEnableResilience(boolean enableResilience) {
		this.enableResilience = enableResilience;
	}

	public boolean isEnableAdhocServicesReplication() {
		return enableAdhocServicesReplication;
	}

	public void setEnableAdhocServicesReplication(boolean enableAdhocServicesReplication) {
		this.enableAdhocServicesReplication = enableAdhocServicesReplication;
	}

	public int getServiceReplicas() {
		return serviceReplicas;
	}

	public void setServiceReplicas(int serviceReplicas) {
		this.serviceReplicas = serviceReplicas;
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

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
