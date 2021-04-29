package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.scheduler;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

@Indexed
public class InfinispanSchedulerJob implements InfinispanObject<String> {

	private static final long serialVersionUID = 1L;

	@Field(analyze = Analyze.NO)
	private String jobId;

	@Field(analyze = Analyze.NO)
	private long schedDate;

	@Field(analyze = Analyze.NO, indexNullAs = "NULL", store = Store.YES)
	private String nodeId;

	private boolean transacted;

	private boolean loaded;

	private InfinispanSchedulerJobDetails detail;

	private ExecutionContext executionContext;

	@Override
	public String getId() {
		return jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public long getSchedDate() {
		return schedDate;
	}

	public void setSchedDate(long schedDate) {
		this.schedDate = schedDate;
	}

	public boolean isTransacted() {
		return transacted;
	}

	public void setTransacted(boolean transacted) {
		this.transacted = transacted;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public InfinispanSchedulerJobDetails getDetail() {
		return detail;
	}

	public void setDetail(InfinispanSchedulerJobDetails detail) {
		this.detail = detail;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	@Override
	public String toString() {
		return "InfinispanSchedulerJob [jobId=" + jobId + ", schedDate=" + schedDate + ", nodeId=" + nodeId + ", transacted=" + transacted + ", loaded=" + loaded + ", detail=" + detail + ", executionContext=" + executionContext + "]";
	}

}
