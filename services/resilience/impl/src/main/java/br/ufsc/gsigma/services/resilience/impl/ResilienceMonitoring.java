package br.ufsc.gsigma.services.resilience.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.infinispan.CacheCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import br.ufsc.gsigma.binding.events.ServiceInvokedEvent;
import br.ufsc.gsigma.binding.events.ServiceReplyEvent;
import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.infrastructure.util.thread.LinkedHashSetBlockingQueue;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareScheduledExecutorServiceDecorator;
import br.ufsc.gsigma.services.execution.events.ProcessEvent;
import br.ufsc.gsigma.services.execution.events.ProcessInstanceFinishedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessInstanceStartedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessTaskFinishedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessTaskStartedEvent;
import br.ufsc.gsigma.services.resilience.events.EmptyServiceListEvent;
import br.ufsc.gsigma.services.resilience.events.ServiceUnavailableEvent;
import br.ufsc.gsigma.services.resilience.support.InfinispanCaches;
import br.ufsc.gsigma.services.resilience.support.InfinispanSupport;
import br.ufsc.gsigma.services.resilience.support.MonitoringAction;
import br.ufsc.gsigma.services.resilience.support.SOAApplication;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationInstance;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationServiceTracker;
import br.ufsc.gsigma.services.resilience.support.SOAService;
import br.ufsc.gsigma.services.resilience.support.SOAServiceCheck;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

@Component
public class ResilienceMonitoring implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = Logger.getLogger(ResilienceMonitoring.class);

	private Map<String, LinkedHashSetBlockingQueue<SOAServiceCheck>> mapCheckingQueue = new ConcurrentHashMap<String, LinkedHashSetBlockingQueue<SOAServiceCheck>>();

	private ScheduledExecutorService scheduler = new ExecutionContextAwareScheduledExecutorServiceDecorator(Executors.newScheduledThreadPool(ResilienceParams.MONITOR_NUM_THREADS, new NamedThreadFactory("resilience-monitoring")));

	private Set<String> stopMonitoringServices = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	private List<ServiceMonitorListener> listeners = new CopyOnWriteArrayList<ServiceMonitorListener>();

	private ServiceMonitor monitor;

	@Autowired
	private InfinispanCaches caches;

	@Autowired
	private ResilienceServiceInternal resilienceServiceInternal;

	@Autowired
	private ResilienceAnalysis analysis;

	@Autowired
	private InfinispanSupport infinispanSupport;

	private List<ScheduledFuture<?>> checkJobs = new LinkedList<ScheduledFuture<?>>();

	ResilienceServiceInternal getResilienceServiceInternal() {
		return resilienceServiceInternal;
	}

	public InfinispanSupport getInfinispanSupport() {
		return infinispanSupport;
	}

	@PostConstruct
	public void setup() {

		this.monitor = new ServiceMonitor(this);

		for (int i = 0; i < ResilienceParams.MONITOR_NUM_THREADS; i++) {
			ScheduledFuture<?> checkJob = scheduler.scheduleAtFixedRate(monitor, 0, ResilienceParams.SERVICE_MONITOR_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
			checkJobs.add(checkJob);
		}

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		monitoringComponentStarted();

	}

	private void monitoringComponentStarted() {
		
		logger.info("Resilience Monitoring ready");

		CacheCollection<SOAApplication> applications = caches.getSOAApplications().values();
		
		resilienceServiceInternal.monitorSOAApplications(caches.getSOAApplications().values());

		for (SOAApplication application : applications) {
			logger.info("Monitoring SOA Application --> " 
						+ application.getApplicationId() + " - " + application.getName());
		}
	}

	public void receiveBusEvent(Event event) throws Exception {

		if (!infinispanSupport.isLeader()) {
			return;
		}

		try {

			SOAApplicationServiceTracker s = null;

			if (event instanceof ProcessInstanceStartedEvent) {

				ProcessInstanceStartedEvent processInstanceStartedEvent = (ProcessInstanceStartedEvent) event;
				Long processInstanceId = processInstanceStartedEvent.getProcessInstanceId();

				SOAApplication application = resilienceServiceInternal.getSOAApplication(processInstanceStartedEvent.getApplicationId());

				if (application != null) {
					logger.debug("SOA Application instance '" + processInstanceId + "' started [processInstanceId=" + processInstanceId + ", responseTimeConstraint=" + application.getResponseTimeConstraint());
					SOAApplicationInstance applicationInstance = resilienceServiceInternal.getSOAApplicationInstance(application, processInstanceId);
					applicationInstance.setProcessInstanceStartedEvent(processInstanceStartedEvent);
				}

			} else if (event instanceof ProcessInstanceFinishedEvent) {

				ProcessInstanceFinishedEvent processInstanceFinishedEvent = (ProcessInstanceFinishedEvent) event;
				Long processInstanceId = processInstanceFinishedEvent.getProcessInstanceId();

				SOAApplication application = resilienceServiceInternal.getSOAApplication(processInstanceFinishedEvent.getApplicationId());

				if (application != null) {

					SOAApplicationInstance applicationInstance = resilienceServiceInternal.getSOAApplicationInstance(application, processInstanceId);

					applicationInstance.setProcessInstanceFinishedEvent(processInstanceFinishedEvent);

					applicationInstance.setComplete(true);

					logger.info("Process " + application.getBusinessProcess().getName() + " with processInstanceId=" + processInstanceId + " completed [status=" + processInstanceFinishedEvent.getProcessStateName() + ", servicesDuration=" + applicationInstance.getServicesDuration()
							+ " ms, instanceDuration=" + applicationInstance.getProcessInstanceDuration() + ", invokedServices=" + applicationInstance.getInvokedServices().size() + ", engineOverhead=" + applicationInstance.getEngineOverhead() + ", responseTimeConstraint="
							+ application.getResponseTimeConstraint() + "]");

					applicationInstance.updateCacheAsync();
				}

			} else if (event instanceof ProcessTaskStartedEvent) {
				s = handleProcessTaskStartedEvent((ProcessTaskStartedEvent) event);

			} else if (event instanceof ProcessTaskFinishedEvent) {
				s = handleProcessTaskFinishedEvent((ProcessTaskFinishedEvent) event);

			} else if (event instanceof ServiceInvokedEvent) {
				s = handleServiceInvokedEvent((ServiceInvokedEvent) event);

			} else if (event instanceof ServiceReplyEvent) {
				s = handleServiceReplyEvent((ServiceReplyEvent) event);

			} else if (event instanceof EmptyServiceListEvent) {

				if (infinispanSupport.isLeader()) {

					logger.info("Received empty service list event --> " + event);

					String applicationId = ((EmptyServiceListEvent) event).getApplicationId();

					SOAApplication application = resilienceServiceInternal.getSOAApplication(applicationId);

					if (application != null) {
						analysis.analyzeSOAApplication(application);
					}
				}

			} else if (event instanceof ServiceUnavailableEvent) {

				if (infinispanSupport.isLeader()) {

					ServiceUnavailableEvent evt = (ServiceUnavailableEvent) event;

					SOAService service = resilienceServiceInternal.getSOAService(evt.getService());

					logger.info("Received service unavailable event --> " + service.getServiceEndpointURL());
					analysis.analyzeServiceUnavailableEvent(evt);

				}
			}

			if (s != null) {
				analysis.analyzeSOAApplicationInstance(s.getApplicationInstance(), event instanceof ProcessEvent ? (ProcessEvent) event : null);
			}

			if (s != null && !s.wasComplete() && s.isComplete()) {
				logger.debug("Task '" + s.getBusinessTask().getName() + "' of processInstanceId=" + s.getApplicationInstance().getInstanceId() + " completed in " + s.getDuration() + " ms");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private SOAApplicationServiceTracker handleProcessTaskFinishedEvent(ProcessTaskFinishedEvent processTaskFinishedEvent) {

		int invocationNumber = processTaskFinishedEvent.getInvocationNumber();
		String serviceClassification = processTaskFinishedEvent.getServiceClassification();
		Long processInstanceId = processTaskFinishedEvent.getProcessInstanceId();

		String applicationId = processTaskFinishedEvent.getApplicationId();

		SOAApplication application = resilienceServiceInternal.getSOAApplication(applicationId);

		if (application != null) {

			SOAApplicationInstance applicationInstance = resilienceServiceInternal.getSOAApplicationInstance(application, processInstanceId);

			SOAApplicationServiceTracker service = getSOAApplicationServiceTracker(application, applicationInstance, serviceClassification, invocationNumber);
			service.setProcessTaskFinishedEvent(processTaskFinishedEvent);
			applicationInstance.updateCacheAsync();

			return service;
		} else {
			return null;
		}
	}

	private SOAApplicationServiceTracker handleProcessTaskStartedEvent(ProcessTaskStartedEvent processTaskStartedEvent) {

		int invocationNumber = processTaskStartedEvent.getInvocationNumber();
		String serviceClassification = processTaskStartedEvent.getServiceClassification();
		Long processInstanceId = processTaskStartedEvent.getProcessInstanceId();

		String applicationId = processTaskStartedEvent.getApplicationId();

		SOAApplication application = resilienceServiceInternal.getSOAApplication(applicationId);

		if (application != null) {

			SOAApplicationInstance applicationInstance = resilienceServiceInternal.getSOAApplicationInstance(application, processInstanceId);

			SOAApplicationServiceTracker service = getSOAApplicationServiceTracker(application, applicationInstance, serviceClassification, invocationNumber);
			service.setProcessTaskStartedEvent(processTaskStartedEvent);

			applicationInstance.updateCacheAsync();

			return service;
		} else {
			return null;
		}
	}

	private SOAApplicationServiceTracker handleServiceInvokedEvent(ServiceInvokedEvent serviceInvokedEvent) {

		int invocationNumber = serviceInvokedEvent.getInvocationNumber();
		String serviceClassification = serviceInvokedEvent.getServiceClassification();
		Long processInstanceId = Long.valueOf(serviceInvokedEvent.getProcessInstanceId());

		String applicationId = serviceInvokedEvent.getApplicationId();

		SOAApplication application = resilienceServiceInternal.getSOAApplication(applicationId);

		if (application != null) {

			SOAApplicationInstance applicationInstance = resilienceServiceInternal.getSOAApplicationInstance(application, processInstanceId);

			SOAApplicationServiceTracker service = getSOAApplicationServiceTracker(application, applicationInstance, serviceClassification, invocationNumber);
			service.setServiceInvokedEvent(serviceInvokedEvent);

			applicationInstance.updateCacheAsync();

			return service;
		} else {
			return null;
		}
	}

	private SOAApplicationServiceTracker handleServiceReplyEvent(ServiceReplyEvent serviceReplyEvent) {

		int invocationNumber = serviceReplyEvent.getInvocationNumber();
		String serviceClassification = serviceReplyEvent.getServiceClassification();
		Long processInstanceId = Long.valueOf(serviceReplyEvent.getProcessInstanceId());

		String applicationId = serviceReplyEvent.getApplicationId();

		SOAApplication application = resilienceServiceInternal.getSOAApplication(applicationId);

		if (application != null) {
			SOAApplicationInstance applicationInstance = resilienceServiceInternal.getSOAApplicationInstance(application, processInstanceId);

			SOAApplicationServiceTracker service = getSOAApplicationServiceTracker(application, applicationInstance, serviceClassification, invocationNumber);
			service.setServiceReplyEvent(serviceReplyEvent);

			applicationInstance.updateCacheAsync();

			return service;
		} else {
			return null;
		}
	}

	private SOAApplicationServiceTracker getSOAApplicationServiceTracker(SOAApplication application, SOAApplicationInstance applicationInstance, String serviceClassification, int invocationNumber) {

		synchronized (applicationInstance) {
			SOAApplicationServiceTracker service = applicationInstance.getInvokedService(serviceClassification, invocationNumber);
			if (service == null) {

				Task businessTask = null;

				for (Task t : application.getBusinessProcess().getTasks()) {
					if (serviceClassification.equals(t.getTaxonomyClassification())) {
						businessTask = t;
					}
				}

				service = new SOAApplicationServiceTracker(applicationInstance, businessTask, serviceClassification, invocationNumber);
				applicationInstance.getInvokedServices().add(service);
			}
			return service;
		}
	}

	void addListener(ServiceMonitorListener listener) {
		this.listeners.add(listener);
	}

	void submitChangeServicesMonitoring(Collection<SOAService> servicesToMonitor, Collection<SOAService> servicesToNotMonitor) {
		infinispanSupport.getClusterExecution() //
				.allNodeSubmission() //
				.submit(new MonitoringAction(servicesToMonitor, servicesToNotMonitor));
	}

	public void executeChangeServicesMonitoring(Collection<SOAService> servicesToMonitor, Collection<SOAService> servicesToNotMonitor) {

		if (CollectionUtils.isNotEmpty(servicesToMonitor) || CollectionUtils.isNotEmpty(servicesToMonitor)) {
			monitor.lock();
			try {
				stopMonitoringServices(servicesToNotMonitor);
				monitorServices(servicesToMonitor);
			} finally {
				monitor.unlock();
			}
		}
	}

	private void monitorServices(Collection<SOAService> services) {
		monitor.lock();
		try {

			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("Going to monitor " + services.size() + " service(s)");

				if (logger.isDebugEnabled()) {
					for (SOAService s : services) {
						sb.append("\n\t" + s.getServiceKey());
					}
				}

				logger.info(sb.toString());
			}

			List<SOAServiceCheck> serviceChecks = services.stream().map(s -> new SOAServiceCheck(s)).collect(Collectors.toList());

			if (!serviceChecks.isEmpty()) {

				Map<String, List<SOAServiceCheck>> mapServiceToSOAApplications = new HashMap<String, List<SOAServiceCheck>>();

				for (SOAServiceCheck s : serviceChecks) {

					Collection<String> soaApplicationIds = resilienceServiceInternal.getSOAApplicationIds(s.getService());

					for (String appId : soaApplicationIds) {
						List<SOAServiceCheck> list = mapServiceToSOAApplications.get(appId);
						if (list == null) {
							list = new LinkedList<SOAServiceCheck>();
							mapServiceToSOAApplications.put(appId, list);
						}
						list.add(s);
					}

				}

				for (Entry<String, List<SOAServiceCheck>> e : mapServiceToSOAApplications.entrySet()) {
					String applicationId = e.getKey();
					getCheckingQueue(applicationId).replaceAll(e.getValue());
				}
			}

		} finally {
			monitor.unlock();
		}
	}

	private void stopMonitoringServices(Collection<SOAService> services) {
		int n = services.size();
		if (n > 0) {
			monitor.lock();
			try {
				logger.info("Stopping monitoring " + n + " service(s)");
				stopMonitoringServices.addAll(services.stream().map(s -> s.getServiceKey()).collect(Collectors.toList()));
			} finally {
				monitor.unlock();
			}
		}
	}

	boolean shouldMonitorService(SOAService service) {
		return !stopMonitoringServices.remove(service.getServiceKey());
	}

	void serviceAvailable(SOAServiceCheck serviceCheck, Boolean wasAvailable) {
		if (wasAvailable == null || !wasAvailable) {
			SOAService service = serviceCheck.getService();
			logger.info("Service is [ALIVE] \tat " + service.getServiceEndpointURL() + " --> " + service);

			for (ServiceMonitorListener l : listeners) {
				l.serviceAvailable(serviceCheck);
			}
		}
	}

	void serviceUnvailable(SOAServiceCheck serviceCheck, Boolean wasAvailable) {
		if (wasAvailable == null || wasAvailable) {
			SOAService service = serviceCheck.getService();
			logger.info("Service is [NOT ALIVE] \tat " + service.getServiceEndpointURL() + " --> " + service);

			for (ServiceMonitorListener l : listeners) {
				l.serviceUnavailable(serviceCheck);
			}
		}
	}

	private AtomicInteger currentQueue = new AtomicInteger();

	// Round robin checking queue for each SOA application
	LinkedHashSetBlockingQueue<SOAServiceCheck> getCheckingQueue() {

		synchronized (mapCheckingQueue) {

			List<String> soaApplicationKeys = new ArrayList<String>(new TreeSet<String>(mapCheckingQueue.keySet()));

			int n = soaApplicationKeys.size();

			if (n == 0) {
				return null;
			}

			int posi = currentQueue.getAndUpdate(x -> x == n - 1 ? 0 : x + 1);

			String appKey = soaApplicationKeys.get(posi);

			return getCheckingQueue(appKey);

		}
	}

	private LinkedHashSetBlockingQueue<SOAServiceCheck> getCheckingQueue(String appKey) {

		synchronized (mapCheckingQueue) {

			LinkedHashSetBlockingQueue<SOAServiceCheck> queue = mapCheckingQueue.get(appKey);

			if (queue == null) {
				queue = new LinkedHashSetBlockingQueue<SOAServiceCheck>();
				mapCheckingQueue.put(appKey, queue);
			}

			return queue;
		}
	}

}
