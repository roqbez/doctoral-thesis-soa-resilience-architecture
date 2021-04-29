package br.ufsc.gsigma.infrastructure.util.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadLocalScheduledExecutorServiceDecorator extends ThreadLocalExecutorServiceDecorator implements ScheduledExecutorService {

	public ThreadLocalScheduledExecutorServiceDecorator(ScheduledExecutorService delegate) {
		super(delegate);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return ((ScheduledExecutorService) delegate).schedule(createRunnable(command), delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return ((ScheduledExecutorService) delegate).schedule(createCallable(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return ((ScheduledExecutorService) delegate).scheduleAtFixedRate(createRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return ((ScheduledExecutorService) delegate).scheduleWithFixedDelay(createRunnable(command), initialDelay, delay, unit);
	}

}
