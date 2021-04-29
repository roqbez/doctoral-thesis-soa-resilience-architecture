package br.ufsc.gsigma.infrastructure.ws.context;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalScheduledExecutorServiceDecorator;

public class ExecutionContextAwareScheduledExecutorServiceDecorator extends ThreadLocalScheduledExecutorServiceDecorator {

	public ExecutionContextAwareScheduledExecutorServiceDecorator(ScheduledExecutorService delegate) {
		super(delegate);
	}

	@Override
	protected <T> Callable<T> createCallable(Callable<T> task) {
		return ExecutionContextAwareExecutorServiceDecorator.createExecutionContextAwareCallable(task);
	}

	@Override
	protected Runnable createRunnable(Runnable task) {
		return ExecutionContextAwareExecutorServiceDecorator.createExecutionContextAwareRunnable(task);
	}

}
