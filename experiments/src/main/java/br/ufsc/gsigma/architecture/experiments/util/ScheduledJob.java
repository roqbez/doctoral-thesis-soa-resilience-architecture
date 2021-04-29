package br.ufsc.gsigma.architecture.experiments.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScheduledJob<K> implements ScheduledFuture<K> {

	private ScheduledFuture<K> delegate;

	private CountDownLatch jobDone = new CountDownLatch(1);

	public ScheduledJob(ScheduledFuture<K> delegate) {
		this.delegate = delegate;
	}

	public ScheduledFuture<K> getDelegate() {
		return delegate;
	}

	public void waitJobDone() throws InterruptedException {
		jobDone.await();
	}

	public CountDownLatch getJobDone() {
		return jobDone;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return delegate.getDelay(unit);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return delegate.cancel(mayInterruptIfRunning);
	}

	@Override
	public int compareTo(Delayed o) {
		return delegate.compareTo(o);
	}

	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}

	@Override
	public boolean isDone() {
		return delegate.isDone();
	}

	@Override
	public K get() throws InterruptedException, ExecutionException {
		return delegate.get();
	}

	@Override
	public K get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return delegate.get(timeout, unit);
	}

}