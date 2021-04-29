package br.ufsc.gsigma.services.resilience.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.util.thread.LinkedHashSetBlockingQueue;
import br.ufsc.gsigma.services.resilience.support.ResilienceUtil;
import br.ufsc.gsigma.services.resilience.support.SOAService;
import br.ufsc.gsigma.services.resilience.support.SOAServiceCheck;

public class ServiceMonitor implements Runnable {

	private static Logger logger = Logger.getLogger(ServiceMonitor.class);

	private ResilienceMonitoring monitoring;

	private ReadWriteLock lock = new ReentrantReadWriteLock(true); // must be fair

	private AtomicLong lastLogQueueContentRun = new AtomicLong();

	public ServiceMonitor(ResilienceMonitoring manager) {
		this.monitoring = manager;
	}

	void lock() {
		lock.writeLock().lock();
	}

	void unlock() {
		lock.writeLock().unlock();
	}

	@Override
	public void run() {

		if (!monitoring.getInfinispanSupport().isLeader()) {
			return;
		}

		Lock readLock = lock.readLock();

		readLock.lock();

		try {

			SOAServiceCheck serviceCheck = null;

			LinkedHashSetBlockingQueue<SOAServiceCheck> checkingQueue = monitoring.getCheckingQueue();

			if (checkingQueue == null) {
				return;
			}

			debugQueueIfNecessary(checkingQueue);

			serviceCheck = checkingQueue.poll();

			if (serviceCheck == null)
				return;

			SOAService service = serviceCheck.getService();

			logger.trace("Checking availability of service --> " + serviceCheck.getService().getServiceKey());

			boolean alive = ResilienceUtil.isServiceAlive(logger, service);

			try {

				Boolean wasAvailable = service.getAvailable();

				if (alive) {
					logger.trace("Service is AVAILABLE --> " + serviceCheck.getService().getServiceKey());
					monitoring.serviceAvailable(serviceCheck, wasAvailable);
					service.setAvailable(true);
				} else {
					logger.trace("Service is UNAVAILABLE --> " + serviceCheck.getService().getServiceKey());
					monitoring.serviceUnvailable(serviceCheck, wasAvailable);
					service.setAvailable(false);
				}

				service.setLastAvailabilityCheck(new Date());

			} finally {
//				service.updateCache();

				if (monitoring.shouldMonitorService(service)) {
					logger.trace("Adding in queue for next check --> " + serviceCheck.getService().getServiceKey());

					checkingQueue.put(serviceCheck);
				}
			}

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			readLock.unlock();
		}
	}

	private void debugQueueIfNecessary(LinkedHashSetBlockingQueue<SOAServiceCheck> checkingQueue) {

		if (logger.isDebugEnabled()) {

			long now = System.currentTimeMillis();

			if (lastLogQueueContentRun.updateAndGet(x -> now - x > 1000 ? now : x) == now) {

				int size = checkingQueue.size();

				if (size > 0) {

					StringBuilder sb = new StringBuilder("Monitoring checking queue contains " + size + " items:");

					List<SOAServiceCheck> services = new ArrayList<SOAServiceCheck>();

					checkingQueue.copyTo(services);

					for (SOAServiceCheck s : services) {
						sb.append("\n\t" + s.getService().getServiceEndpointURL());
					}

					logger.debug(sb.toString());
				}
			}
		}
	}

}
