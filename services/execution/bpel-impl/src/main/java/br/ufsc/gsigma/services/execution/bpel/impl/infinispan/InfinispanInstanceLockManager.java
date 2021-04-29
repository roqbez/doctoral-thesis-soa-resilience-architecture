package br.ufsc.gsigma.services.execution.bpel.impl.infinispan;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.infinispan.lock.api.ClusteredLock;

public class InfinispanInstanceLockManager {

	public void lock(Long iid, int lockTimeout, TimeUnit lockTimeunit) throws InterruptedException, TimeoutException {

		ClusteredLock lock = InfinispanDatabase.getLock(getLockName(iid));

		if (lock != null) {

			CompletableFuture<Boolean> f = lock.tryLock(lockTimeout, lockTimeunit);

			Boolean r = false;

			try {
				r = f.get();
			} catch (InterruptedException e) {
				throw e;
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}

			if (!r) {
				throw new TimeoutException();
			}
		}
	}

	public void unlock(Long iid) {
		ClusteredLock lock = InfinispanDatabase.getLock(getLockName(iid));
		if (lock != null) {
			lock.unlock();
		}
	}

	public static String getLockName(Long iid) {
		return "ProcessInstance@" + iid;
	}

	public static final class TimeoutException extends Exception {
		private static final long serialVersionUID = 1L;
	}

}
