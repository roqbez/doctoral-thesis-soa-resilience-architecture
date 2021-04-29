package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.engine.BpelEngineImpl;
import org.apache.ode.bpel.engine.Contexts;
import org.apache.ode.bpel.iapi.Scheduler;

public class ExecutionBpelEngine extends BpelEngineImpl {

	public static final int LOCK_TIMEOUT = 1;

	public static final TimeUnit LOCK_TIMEUNIT = TimeUnit.SECONDS;

	private static final Logger logger = Logger.getLogger(ExecutionBpelEngine.class);

	private ExecutorService executorService;

	private final ExecutionInstanceLockManager _instanceLockManager = new ExecutionInstanceLockManager();

	private Contexts contexts;

	public ExecutionBpelEngine(Contexts contexts, ExecutorService executorService) {
		super(contexts);
		this.contexts = contexts;
		this.executorService = executorService;
	}

	public void terminateProcessInstance(QName processId, Long instanceId) {
		ExecutionBpelProcess process = (ExecutionBpelProcess) getProcess(processId);
		process.terminateProcessInstance(instanceId);
	}

	Scheduler getScheduler() {
		return contexts.scheduler;
	}

	public void acquireInstanceLock(final Long iid) {
		// We lock the instance to prevent concurrent transactions and prevent unnecessary rollbacks,
		// Note that we don't want to wait too long here to get our lock, since we are likely holding
		// on to scheduler's locks of various sorts.
		try {
			_instanceLockManager.lock(iid, LOCK_TIMEOUT, LOCK_TIMEUNIT);
			this.contexts.scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
				public void afterCompletion(boolean success) {
					_instanceLockManager.unlock(iid);
				}

				public void beforeCompletion() {
				}
			});
		} catch (InterruptedException e) {
			// Retry later.
			logger.debug("Thread interrupted, job will be rescheduled");
			throw new Scheduler.JobProcessorException(true);
		} catch (ExecutionInstanceLockManager.TimeoutException e) {
			logger.debug("Instance " + iid + " is busy, rescheduling job.");
			throw new Scheduler.JobProcessorException(true);
		}
	}

	@Override
	public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
		logger.debug("Running job --> " + jobInfo);
		super.onScheduledJob(jobInfo);
	}

	ExecutorService getExecutorService() {
		return executorService;
	}

	public ExecutionInstanceLockManager getInstanceLockManager() {
		return _instanceLockManager;
	}

}
