package br.ufsc.gsigma.infrastructure.util.thread;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ExecutorUtil {

	private static final Field FIELD_WORK_QUEUE;

	static {
		try {
			FIELD_WORK_QUEUE = ThreadPoolExecutor.class.getDeclaredField("workQueue");
			FIELD_WORK_QUEUE.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static BlockingQueue<Runnable> getWorkQueue(ExecutorService pool) {

		if (pool instanceof ThreadLocalExecutorServiceDecorator) {
			pool = ((ThreadLocalExecutorServiceDecorator) pool).getDelegate();
		}

		if (pool instanceof ThreadPoolExecutor) {
			try {
				return (BlockingQueue<Runnable>) FIELD_WORK_QUEUE.get(pool);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

}
