package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.scheduler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InfinispanSchedulerJobDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long instanceId;

	private String mexId;

	private String processId;

	private String type;

	private String channel;

	private String correlatorId;

	private String correlationKeySet;

	private Integer retryCount;

	private Boolean inMem;

	private Map<String, Object> detailsExt = new HashMap<String, Object>();

	public Long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(Long instanceId) {
		this.instanceId = instanceId;
	}

	public String getMexId() {
		return mexId;
	}

	public void setMexId(String mexId) {
		this.mexId = mexId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCorrelatorId() {
		return correlatorId;
	}

	public void setCorrelatorId(String correlatorId) {
		this.correlatorId = correlatorId;
	}

	public String getCorrelationKeySet() {
		return correlationKeySet;
	}

	public void setCorrelationKeySet(String correlationKeySet) {
		this.correlationKeySet = correlationKeySet;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public Boolean getInMem() {
		return inMem;
	}

	public void setInMem(Boolean inMem) {
		this.inMem = inMem;
	}

	public Map<String, Object> getDetailsExt() {
		return detailsExt;
	}

	public void setDetailsExt(Map<String, Object> detailsExt) {
		this.detailsExt = detailsExt;
	}

	@Override
	public String toString() {
		return "[instanceId=" + instanceId + ", mexId=" + mexId + ", processId=" + processId + ", type=" + type + ", channel=" + channel + ", correlatorId=" + correlatorId + ", correlationKeySet=" + correlationKeySet + ", retryCount=" + retryCount
				+ ", inMem=" + inMem + "]";
	}

}
