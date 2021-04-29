package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.ode.bpel.iapi.ContextException;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.DatabaseDelegate;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.Job;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.SimpleScheduler;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.Task;

public class ExecutionScheduler extends SimpleScheduler {

	public ExecutionScheduler(String nodeId, DatabaseDelegate del, Properties conf) {
		super(nodeId, del, conf);
	}

	public <T> T execTransaction(final Callable<T> transaction, int timeout) throws Exception, ContextException {
		return super.execTransaction(new Callable<T>() {

			@Override
			public T call() throws Exception {
				try {
					return transaction.call();
				} catch (Exception e) {
					__log.error(e.getMessage(), e);
					throw e;
				}
			}

		}, timeout);
	}

	@Override
	protected Job createJob(boolean transacted, JobDetails jobDetail, Date when) {
		return new ExecutionJob(when.getTime(), transacted, jobDetail);
	}

	@Override
	protected Job createJob(Date when, JobDetails jobDetails) {
		return new ExecutionJob(when.getTime(), true, jobDetails);
	}

	@Override
	protected void runJob(Job job) {

		ExecutionContext executionContext = ((ExecutionJob) job).getExecutionContext();
		if (executionContext != null)
			ExecutionContext.set(executionContext);

		super.runJob(job);
	}

	@Override
	protected void runPolledRunnable(Job job) {

		ExecutionContext executionContext = ((ExecutionJob) job).getExecutionContext();
		if (executionContext != null)
			ExecutionContext.set(executionContext);

		super.runPolledRunnable(job);
	}

	@Override
	protected RunJob createRunJob(final Job job, final JobProcessor processor) {

		final RequestBindingContext requestBindingContext = RequestBindingContext.get();

		return new RunJob(job, processor) {
			@Override
			public Void call() throws Exception {

				String threadName = null;

				if (requestBindingContext != null) {
					String threadSuffix = requestBindingContext.getInvokedServiceInfo() != null ? "-" + requestBindingContext.getInvokedServiceInfo().getServiceEndpointInfo().getServiceClassification().replaceAll("/", "_").toLowerCase() : "";
					threadName = Thread.currentThread().getName() + threadSuffix;
				}

				String previousThreadName = null;

				try {

					if (threadName != null) {
						previousThreadName = Thread.currentThread().getName();
						Thread.currentThread().setName(threadName);
					}

					return super.call();

				} finally {
					if (previousThreadName != null) {
						Thread.currentThread().setName(previousThreadName);
					}
				}
			}
		};
	}

	@Override
	public void runTask(final Task task) {
		if (task instanceof Job) {
			Job job = (Job) task;
			if (job.getDetail().getDetailsExt().get("runnable") != null) {
				runPolledRunnable(job);
			} else {
				runJob(job);
			}
		} else if (task instanceof SchedulerTask) {
			_exec.submit(new SchedulerTaskCallable(task));
		}
	}

	public final class SchedulerTaskCallable implements Callable<Void> {

		private final Task task;

		private SchedulerTaskCallable(Task task) {
			this.task = task;
		}

		public Void call() throws Exception {
			try {
				((SchedulerTask) task).run();
			} catch (Exception ex) {
				__log.error("Error during SchedulerTask execution", ex);
			}
			return null;
		}

		@Override
		public String toString() {
			return task.toString();
		}
	}

}
