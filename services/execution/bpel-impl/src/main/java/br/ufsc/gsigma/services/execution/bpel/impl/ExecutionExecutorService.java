package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.services.execution.bpel.impl.ExecutionScheduler.SchedulerTaskCallable;

public class ExecutionExecutorService extends ExecutionContextAwareExecutorServiceDecorator {

	private static final Logger logger = Logger.getLogger(ExecutionExecutorService.class);

	public ExecutionExecutorService(ExecutorService delegate) {
		super(delegate);
	}

	@Override
	protected <T> ThreadLocalCallable<T> createCallable(Callable<T> task) {

		if (logger.isDebugEnabled() && !(task instanceof SchedulerTaskCallable)) {
			logger.debug("Creating callable --> " + task);
		}

		final ExecutionContext executionContext = ExecutionContext.get();

		final RequestBindingContext requestBindingContext = RequestBindingContext.get();

		return new ThreadLocalCallable<T>(task) {
			@Override
			public T call() throws Exception {

				if (executionContext != null)
					ExecutionContext.set(executionContext);

				if (requestBindingContext != null)
					RequestBindingContext.set(requestBindingContext);

				String originalThreadName = Thread.currentThread().getName();

				try {

					if (requestBindingContext != null) {
						Thread.currentThread().setName(originalThreadName + (requestBindingContext.getProcessInstanceId() != null ? "-pid:" + requestBindingContext.getProcessInstanceId() : ""));
					}

					return super.call();

				} finally {
					if (requestBindingContext != null) {
						Thread.currentThread().setName(originalThreadName);
					}
				}
			}
		};
	}

	@Override
	protected Runnable createRunnable(Runnable task) {

		if (logger.isDebugEnabled()) {
			logger.debug("Creating runnable --> " + task);
		}

		final ExecutionContext executionContext = ExecutionContext.get();

		final RequestBindingContext requestBindingContext = RequestBindingContext.get();

		return new ThreadLocalRunnable(task) {
			@Override
			public void run() {

				if (executionContext != null)
					ExecutionContext.set(executionContext);

				if (requestBindingContext != null)
					RequestBindingContext.set(requestBindingContext);

				String originalThreadName = Thread.currentThread().getName();

				try {

					if (requestBindingContext != null) {
						Thread.currentThread().setName(originalThreadName + (requestBindingContext.getProcessInstanceId() != null ? "-pid:" + requestBindingContext.getProcessInstanceId() : ""));
					}

					super.run();

				} finally {
					if (requestBindingContext != null) {
						Thread.currentThread().setName(originalThreadName);
					}
				}
			}
		};
	}

}
