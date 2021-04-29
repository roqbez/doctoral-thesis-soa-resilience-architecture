package br.ufsc.gsigma.binding.support;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class RequestTracker implements Serializable, Comparable<RequestTracker> {

	private static final long serialVersionUID = 1L;

	private ExecutionContext executionContext;

	private RequestBindingContext requestBindingContext;

	private Map<String, Serializable> requestHeaders = new HashMap<String, Serializable>();

	private String serviceNamespace;

	private byte[] soapMessageBytes;

	private long requestTimestamp;

	private String requestCorrelationId;

	private int retryCount;

	private long expirarationDiffMillis = 5000L;

	private double retryMultiplier = 1.1;

	private int maxRetries = 10;

	private boolean completed;

	private boolean inFlight;

	private boolean replyInFlight;

	private String nodeId;

	private int sequence;

	public RequestTracker(String nodeId, int sequence, String requestCorrelationId, String serviceNamespace, long requestTimestamp, ExecutionContext executionContext, RequestBindingContext requestBindingContext) {
		this.nodeId = nodeId;
		this.sequence = sequence;
		this.requestCorrelationId = requestCorrelationId;
		this.serviceNamespace = serviceNamespace;

		this.requestTimestamp = requestTimestamp;

		this.executionContext = executionContext;
		this.requestBindingContext = requestBindingContext;

		if (requestTimestamp == -1L) {
			this.inFlight = true;
		}
	}

	public RequestTracker(String nodeId, int sequence, String requestCorrelationId, String serviceNamespace, boolean completed) {
		this.nodeId = nodeId;
		this.sequence = sequence;
		this.requestCorrelationId = requestCorrelationId;
		this.serviceNamespace = serviceNamespace;
		this.completed = completed;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isInFlight() {
		return inFlight;
	}

	public void setInFlight(boolean inFlight) {
		this.inFlight = inFlight;
	}

	public boolean isReplyInFlight() {
		return replyInFlight;
	}

	public void setReplyInFlight(boolean replyInFlight) {
		this.replyInFlight = replyInFlight;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public long getExpirarationDiffMillis() {
		return expirarationDiffMillis;
	}

	public void setExpirarationDiffMillis(long expirarationDiffMillis) {
		this.expirarationDiffMillis = expirarationDiffMillis;
	}

	public double getRetryMultiplier() {
		return retryMultiplier;
	}

	public void setRetryMultiplier(double retryMultiplier) {
		this.retryMultiplier = retryMultiplier;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public RequestBindingContext getRequestBindingContext() {
		return requestBindingContext;
	}

	public void setRequestBindingContext(RequestBindingContext requestBindingContext) {
		this.requestBindingContext = requestBindingContext;
	}

	public Map<String, Serializable> getRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(Map<String, Serializable> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public long getRequestTimestamp() {
		return requestTimestamp;
	}

	public void setRequestTimestamp(long requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}

	public String getRequestCorrelationId() {
		return requestCorrelationId;
	}

	public void setRequestCorrelationId(String requestCorrelationId) {
		this.requestCorrelationId = requestCorrelationId;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public byte[] getSoapMessageBytes() {
		return soapMessageBytes;
	}

	public void setSoapMessageBytes(byte[] soapMessageBytes) {
		this.soapMessageBytes = soapMessageBytes;
	}

	@Override
	public int compareTo(RequestTracker o) {
		return Comparator //
				.comparing((RequestTracker r) -> Integer.valueOf((String) ObjectUtils.defaultIfNull(r.getRequestBindingContext() != null ? r.getRequestBindingContext().getProcessInstanceId() : null, "0"))) //
				.thenComparing((RequestTracker r) -> r.requestTimestamp) //
				.compare(this, o);
	}

}
