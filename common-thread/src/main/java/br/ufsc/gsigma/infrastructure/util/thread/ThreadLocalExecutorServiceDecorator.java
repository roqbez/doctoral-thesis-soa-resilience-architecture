package br.ufsc.gsigma.infrastructure.util.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ThreadLocalExecutorServiceDecorator implements ExecutorService {

	protected ExecutorService delegate;

	public ThreadLocalExecutorServiceDecorator(ExecutorService delegate) {
		this.delegate = delegate;
	}

	public ExecutorService getDelegate() {
		return delegate;
	}

	public void execute(Runnable command) {
		delegate.execute(createRunnable(command));
	}

	public void shutdown() {
		delegate.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return delegate.shutdownNow();
	}

	public boolean isShutdown() {
		return delegate.isShutdown();
	}

	public boolean isTerminated() {
		return delegate.isTerminated();
	}

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return delegate.awaitTermination(timeout, unit);
	}

	public <T> Future<T> submit(Callable<T> task) {
		return delegate.submit(createCallable(task));
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return delegate.submit(createRunnable(task), result);
	}

	public Future<?> submit(Runnable task) {
		return delegate.submit(createRunnable(task));
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return delegate.invokeAll(threadLocalRunnables(tasks));
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return delegate.invokeAll(threadLocalRunnables(tasks), timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return delegate.invokeAny(threadLocalRunnables(tasks));
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return delegate.invokeAny(threadLocalRunnables(tasks), timeout, unit);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> Collection<? extends Callable<T>> threadLocalRunnables(Collection<? extends Callable<T>> list) {
		Collection result = new ArrayList(list.size());
		for (Callable<T> c : list) {
			result.add(new ThreadLocalCallable(c));
		}
		return result;
	}

	protected Runnable createRunnable(Runnable task) {
		return new ThreadLocalRunnable(task);
	}

	protected <T> Callable<T> createCallable(Callable<T> task) {
		return new ThreadLocalCallable<T>(task);
	}

	public static class ThreadLocalRunnable implements Runnable {

		private Runnable delegate;

		public ThreadLocalRunnable(Runnable delegate) {
			this.delegate = delegate;
		}

		@Override
		public void run() {
			try {
				this.delegate.run();
			} finally {
				ThreadLocalHolder.clearThreadLocal();
			}
		}
	}

	public static class ThreadLocalCallable<T> implements Callable<T> {

		private Callable<T> delegate;

		public ThreadLocalCallable(Callable<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T call() throws Exception {
			try {
				return this.delegate.call();
			} finally {
				ThreadLocalHolder.clearThreadLocal();
			}
		}
	}

}
