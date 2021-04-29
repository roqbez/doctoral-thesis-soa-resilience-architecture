package br.ufsc.gsigma.binding.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;

@XmlAccessorType(XmlAccessType.FIELD)
public class InvokedServiceInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private long bindingConfigurationVersion;

	private ServiceEndpointInfo serviceEndpointInfo;

	private String serviceLabel;

	private boolean error;

	private long invokeStartTime;

	private long invokeFinishTime;

	private int invokeAttempt;

	private int repeatCount;

	private ResilienceInfo resilienceInfo;

	private Map<String, String> attributes = new LinkedHashMap<String, String>();

	public InvokedServiceInfo() {
	}

	public InvokedServiceInfo(ServiceEndpointInfo serviceEndpointInfo, long bindingConfigurationVersion, ResilienceInfo resilienceInfo, int repeatCount, int invokeAttempt) {
		this.serviceEndpointInfo = serviceEndpointInfo;
		this.bindingConfigurationVersion = bindingConfigurationVersion;

		String serviceKey = serviceEndpointInfo.getServiceKey();
		this.serviceLabel = (serviceEndpointInfo.isAdhoc() ? "adhoc:" : "") + serviceKey.substring(serviceKey.lastIndexOf('-', serviceKey.lastIndexOf('-') - 1) + 1);
		this.resilienceInfo = resilienceInfo;
		this.repeatCount = repeatCount;
		this.invokeAttempt = invokeAttempt;
	}

	public int getInvokeAttempt() {
		return invokeAttempt;
	}

	public void setInvokeAttempt(int invokeAttempt) {
		this.invokeAttempt = invokeAttempt;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	public long getInvokeStartTime() {
		return invokeStartTime;
	}

	public void setInvokeStartTime(long invokeStartTime) {
		this.invokeStartTime = invokeStartTime;
	}

	public long getInvokeFinishTime() {
		return invokeFinishTime;
	}

	public void setInvokeFinishTime(long invokeFinishTime) {
		this.invokeFinishTime = invokeFinishTime;
	}

	public long getBindingConfigurationVersion() {
		return bindingConfigurationVersion;
	}

	public void setBindingConfigurationVersion(long bindingConfigurationVersion) {
		this.bindingConfigurationVersion = bindingConfigurationVersion;
	}

	public ServiceEndpointInfo getServiceEndpointInfo() {
		return serviceEndpointInfo;
	}

	public void setServiceEndpointInfo(ServiceEndpointInfo serviceEndpointInfo) {
		this.serviceEndpointInfo = serviceEndpointInfo;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getServiceLabel() {
		return serviceLabel;
	}

	public void setServiceLabel(String serviceLabel) {
		this.serviceLabel = serviceLabel;
	}

	public ResilienceInfo getResilienceInfo() {
		return resilienceInfo;
	}

	public void setResilienceInfo(ResilienceInfo resilienceInfo) {
		this.resilienceInfo = resilienceInfo;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return serviceLabel;
	}

}
