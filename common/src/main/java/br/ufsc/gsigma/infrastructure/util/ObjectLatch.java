package br.ufsc.gsigma.infrastructure.util;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectLatch<R> implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The object. */
	/*
	 * ==> object is set and got on different threads should be volatile,to get rid of caching issues
	 */
	private volatile R object = null;

	/** The latch counter created and set to 1. */
	private final CountDownLatch latch = new SerializableCountDownLatch(1);

	/** lock for set invariant */
	private final Lock setLock = new ReentrantLock();

	/**
	 * Checks if the object is already available (has been already set).
	 *
	 * @return true, if the object is already available (has been already set)
	 */
	public boolean isAvailable() {
		/*
		 * ==> this forms an invariant with set(..) should be locked
		 */
		setLock.lock();
		try {
			return latch.getCount() == 0;
		} finally {
			setLock.unlock();
		}
	}

	/**
	 * Sets the object if it is not already set. Otherwise ignore this request.
	 *
	 * @param object
	 *            the object
	 */
	public void set(R object) {
		// ==> forms an invariant with isAvailable(..)
		setLock.lock();
		try {
			if (!isAvailable()) {
				this.object = object;
				latch.countDown();
			}
		} finally {
			setLock.unlock();
		}
	}

	/**
	 * Get the object if it is already available (has already been set).
	 * <p>
	 * If it is not available, wait until it is or until an interrupt (InterruptedException) terminates the wait.
	 * 
	 * @return the object if it is already available (has already been set)
	 *
	 * @throws InterruptedException
	 */
	public R get() throws InterruptedException {
		latch.await();
		// not part of any invariant
		// no need to lock/synchronize
		return object;
	}

}
