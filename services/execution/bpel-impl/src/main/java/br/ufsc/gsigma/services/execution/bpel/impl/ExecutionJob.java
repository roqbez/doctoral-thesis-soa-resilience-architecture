package br.ufsc.gsigma.services.execution.bpel.impl;

import org.apache.ode.bpel.iapi.Scheduler.JobDetails;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.Job;

public class ExecutionJob extends Job {

	private ExecutionContext executionContext;

	public ExecutionJob(long when, boolean transacted, JobDetails jobDetail) {
		super(when, transacted, jobDetail);
		this.executionContext = ExecutionContext.get();
	}

	public ExecutionJob(long when, String jobId, boolean transacted, JobDetails jobDetail, ExecutionContext executionContext) {
		super(when, jobId, transacted, jobDetail);
		this.executionContext = executionContext;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

}
