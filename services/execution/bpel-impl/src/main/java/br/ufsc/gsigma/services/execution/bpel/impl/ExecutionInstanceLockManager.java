package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExecutionInstanceLockManager {

	private static final Log __log = LogFactory.getLog(ExecutionInstanceLockManager.class);

	private final Lock _mutex = new java.util.concurrent.locks.ReentrantLock();

	private final Map<Long, LockInstanceInfo> _locks = new HashMap<Long, LockInstanceInfo>();

	private static ExecutionInstanceLockManager INSTANCE;

	public ExecutionInstanceLockManager() {
		INSTANCE = this;
	}

	public static ExecutionInstanceLockManager getInstance() {
		return INSTANCE;
	}

	public LockInstanceInfo getLockInfo(Long iid) {
		_mutex.lock();
		try {
			return _locks.get(iid);
		} finally {
			_mutex.unlock();
		}
	}

	public void lock(Long iid, int time, TimeUnit tu) throws InterruptedException, TimeoutException {
		if (iid == null)
			return;

		String thrd = Thread.currentThread().toString();
		if (__log.isDebugEnabled())
			__log.debug(thrd + ": lock (iid=" + iid + ", time=" + time + tu + ")");

		LockInstanceInfo li;

		_mutex.lock();
		try {

			while (true) {
				li = _locks.get(iid);
				if (li == null) {
					li = new LockInstanceInfo(iid, Thread.currentThread());
					_locks.put(iid, li);
					if (__log.isDebugEnabled())
						__log.debug(thrd + ": lock(iid=" + iid + ", time=" + time + tu + ")--> GRANTED");
					return;

				} else if (li.acquierer == Thread.currentThread()) {

					li.count++;

					if (__log.isDebugEnabled())
						__log.debug(thrd + ": lock(iid=" + iid + ", time=" + time + tu + ")--> ALREADY GRANTED");

					return;

				} else {
					if (__log.isDebugEnabled())
						__log.debug(thrd + ": lock(iid=" + iid + ", time=" + time + tu + ")--> WAITING (held by " + li.acquierer + ")");

					if (!li.available.await(time, tu)) {
						if (__log.isDebugEnabled())
							__log.debug(thrd + ": lock(iid=" + iid + ", time=" + time + tu + ")--> TIMEOUT (held by " + li.acquierer + ")");
						throw new TimeoutException();
					}
				}
			}

		} finally {
			_mutex.unlock();
		}

	}

	public void unlock(Long iid) {
		if (iid == null)
			return;

		String thrd = Thread.currentThread().toString();
		if (__log.isDebugEnabled())
			__log.debug(thrd + ": unlock(iid=" + iid + ")");

		_mutex.lock();
		try {
			LockInstanceInfo li = _locks.get(iid);
			if (li == null)
				throw new IllegalStateException("Instance not locked, cannot unlock!");

			if (--li.count == 0) {

				_locks.remove(iid);

				// Note, that we have to signall all threads, because new holder will create a new
				// instance of "available" condition variable, so all the waiters need to try again
				li.available.signalAll();
			}

		} finally {
			_mutex.unlock();
		}

	}

	@Override
	public String toString() {
		return "{InstanceLockManager: " + _locks + "}";
	}

	public class LockInstanceInfo {

		final long iid;

		/** Thread that acquired the lock. */
		final Thread acquierer;

		/** Condition-Variable indicating that the lock has become available. */
		Condition available = _mutex.newCondition();

		int count = 1;

		LockInstanceInfo(long iid, Thread t) {
			this.iid = iid;
			this.acquierer = t;
		}

		@Override
		public String toString() {
			return "{Lock for Instance #" + iid + ", acquired by " + acquierer + "}";
		}
	}

	/** Exception class indicating a time-out occured while obtaining a lock. */
	public static final class TimeoutException extends Exception {
		private static final long serialVersionUID = 7247629086692580285L;
	}
}
