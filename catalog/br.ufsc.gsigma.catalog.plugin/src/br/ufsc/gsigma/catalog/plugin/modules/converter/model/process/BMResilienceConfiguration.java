package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

@Root
public class BMResilienceConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private boolean enableBindingServiceReplication;

	@Attribute
	private boolean enableExecutionServiceReplication;

	@Attribute
	private boolean enableResilienceServiceReplication;

	@Attribute
	private int bindingServiceReplicas = 1;

	@Attribute
	private int executionServiceReplicas = 1;

	@Attribute
	private int resilienceServiceReplicas = 1;

	@Attribute
	private int servicesCheckInterval = ResilienceParams.SERVICE_MONITOR_CHECK_INTERVAL;

	@Attribute
	private int servicesCheckTimeout = ResilienceParams.DEFAULT_HTTP_READ_TIMEOUT;

	@Attribute
	private int waitNewConfigurationTimeout = (int) ResilienceParams.WAIT_FOR_NEW_BINDING_CONFIGURATION_TIMEOUT;

	@ElementMap(attribute = true, key = "name", value = "value", required = false)
	private Map<String, String> params = new LinkedHashMap<String, String>();

	@ElementMap(attribute = true, key = "name", value = "value", required = false)
	private Map<String, Boolean> flags = new LinkedHashMap<String, Boolean>();

	public boolean isEnableBindingServiceReplication() {
		return enableBindingServiceReplication;
	}

	public void setEnableBindingServiceReplication(boolean enableBindingServiceReplication) {
		this.enableBindingServiceReplication = enableBindingServiceReplication;
	}

	public boolean isEnableExecutionServiceReplication() {
		return enableExecutionServiceReplication;
	}

	public void setEnableExecutionServiceReplication(boolean enableExecutionServiceReplication) {
		this.enableExecutionServiceReplication = enableExecutionServiceReplication;
	}

	public boolean isEnableResilienceServiceReplication() {
		return enableResilienceServiceReplication;
	}

	public void setEnableResilienceServiceReplication(boolean enableResilienceServiceReplication) {
		this.enableResilienceServiceReplication = enableResilienceServiceReplication;
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

	public int getServicesCheckInterval() {
		return servicesCheckInterval;
	}

	public void setServicesCheckInterval(int servicesCheckInterval) {
		this.servicesCheckInterval = servicesCheckInterval;
	}

	public int getServicesCheckTimeout() {
		return servicesCheckTimeout;
	}

	public void setServicesCheckTimeout(int servicesCheckTimeout) {
		this.servicesCheckTimeout = servicesCheckTimeout;
	}

	public int getWaitNewConfigurationTimeout() {
		return waitNewConfigurationTimeout;
	}

	public void setWaitNewConfigurationTimeout(int waitNewConfigurationTimeout) {
		this.waitNewConfigurationTimeout = waitNewConfigurationTimeout;
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
