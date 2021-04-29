package br.ufsc.gsigma.services.resilience.support;

import java.util.concurrent.Callable;

import org.infinispan.lock.api.ClusteredLock;

public abstract class LockUtil {

	public static <T> T doInLock(ClusteredLock lock, Callable<T> job) throws Exception {
		try {
			lock.lock();
			return job.call();
		} finally {
			lock.unlock();
		}
	}
}
