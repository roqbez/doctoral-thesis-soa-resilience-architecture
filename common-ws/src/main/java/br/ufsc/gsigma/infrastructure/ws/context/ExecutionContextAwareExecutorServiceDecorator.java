package br.ufsc.gsigma.infrastructure.ws.context;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalExecutorServiceDecorator;

public class ExecutionContextAwareExecutorServiceDecorator extends ThreadLocalExecutorServiceDecorator {

	public ExecutionContextAwareExecutorServiceDecorator(ExecutorService delegate) {
		super(delegate);
	}

	@Override
	protected Runnable createRunnable(Runnable task) {
		return createExecutionContextAwareRunnable(task);
	}

	@Override
	protected <T> ThreadLocalCallable<T> createCallable(Callable<T> task) {
		return createExecutionContextAwareCallable(task);
	}

	static Runnable createExecutionContextAwareRunnable(Runnable task) {
		
		final ExecutionContext executionContext = ExecutionContext.get();

		return new ThreadLocalRunnable(task) {
			@Override
			public void run() {
				if (executionContext != null)
					ExecutionContext.set(executionContext);
				super.run();
			}
		};
	}

	static <T> ThreadLocalCallable<T> createExecutionContextAwareCallable(Callable<T> task) {

		final ExecutionContext executionContext = ExecutionContext.get();

		return new ThreadLocalCallable<T>(task) {
			@Override
			public T call() throws Exception {
				if (executionContext != null)
					ExecutionContext.set(executionContext);
				return super.call();
			}
		};
	}
}
