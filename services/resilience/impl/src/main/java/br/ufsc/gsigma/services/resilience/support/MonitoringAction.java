package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.util.AppContext;
import br.ufsc.gsigma.services.resilience.impl.ResilienceMonitoring;

public class MonitoringAction implements Callable<Object>, Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MonitoringAction.class);

	private Collection<SOAService> servicesToMonitor;

	private Collection<SOAService> servicesToNotMonitor;

	public MonitoringAction(Collection<SOAService> servicesToMonitor, Collection<SOAService> servicesToNotMonitor) {
		this.servicesToMonitor = servicesToMonitor;
		this.servicesToNotMonitor = servicesToNotMonitor;
	}

	@Override
	public void run() {
		try {
			call();
		} catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		}
	}

	@Override
	public Object call() throws Exception {
		try {
			ResilienceMonitoring monitoring = AppContext.getBean(ResilienceMonitoring.class);
			monitoring.executeChangeServicesMonitoring(servicesToMonitor, servicesToNotMonitor);
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

}
