package br.ufsc.gsigma.services.resilience.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;
import org.infinispan.AdvancedCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.thread.ExecutorUtil;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.services.execution.events.ProcessEvent;
import br.ufsc.gsigma.services.execution.events.ProcessTaskFinishedEvent;
import br.ufsc.gsigma.services.resilience.events.ServiceUnavailableEvent;
import br.ufsc.gsigma.services.resilience.support.AnalysisRequest;
import br.ufsc.gsigma.services.resilience.support.InfinispanCaches;
import br.ufsc.gsigma.services.resilience.support.InfinispanSupport;
import br.ufsc.gsigma.services.resilience.support.PlanningAction;
import br.ufsc.gsigma.services.resilience.support.SOAApplication;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationInstance;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationReconfiguration;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationServiceTracker;
import br.ufsc.gsigma.services.resilience.support.SOAService;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

@Component
public class ResilienceAnalysis implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = Logger.getLogger(ResilienceAnalysis.class);

	@Autowired
	private ResiliencePlanning planning;

	@Autowired
	private InfinispanCaches caches;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private InfinispanSupport infinispanSupport;

	@Autowired
	private ResilienceServiceInternal resilienceServiceInternal;

	private AdvancedCache<String, Integer> anaysisCache;

	private AdvancedCache<String, PlanningAction> planningCache;

	private ExecutorService pool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newFixedThreadPool(ResilienceParams.ANALYSIS_NUM_THREADS, new NamedThreadFactory("resilience-analysis")));

	private static ResilienceAnalysis INSTANCE;

	public static ResilienceAnalysis getInstance() {
		return INSTANCE;
	}

	@PostConstruct
	public void setup() {
		this.anaysisCache = caches.getAnalysisCache().getAdvancedCache();
		this.planningCache = caches.getPlanningCache().getAdvancedCache();
		INSTANCE = this;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		logger.info("Resilience Analysis ready");
	}

	public void analyzeSOAApplicationInstance(SOAApplicationInstance applicationInstance, ProcessEvent event) {

		if (logger.isDebugEnabled() && event instanceof ProcessTaskFinishedEvent) {

			ProcessTaskFinishedEvent evt = (ProcessTaskFinishedEvent) event;

			List<String> nextTasks = applicationInstance.getApplication().getNextTasks(evt.getServiceClassification());

			StringBuilder sb = new StringBuilder("Task '" + evt.getServiceClassification() + "' finished at applicationId=" + applicationInstance.getApplicationId() + ", applicationInstanceId=" + applicationInstance.getInstanceId() + ".");

			if (!CollectionUtils.isEmpty(nextTasks)) {
				sb.append("The next possible tasks are:");
				for (String t : nextTasks) {
					sb.append("\n\t" + t);
				}
			}

			logger.debug(sb.toString());
		}

		boolean deadlineCheck = false;

		if (deadlineCheck) {
			pool.submit(() -> {

				// TODO: improve
				Long deadline = applicationInstance.getProcessDeadline();

				if (deadline != null && !applicationInstance.isComplete()) {

					long sumDuration = 0;

					double sumDurationOverhead = 0;

					double overheadPercent = applicationInstance.getApplication().getEngineOverheadEstimative();

					for (SOAApplicationServiceTracker s : applicationInstance.getInvokedServices()) {
						if (s.isComplete()) {
							sumDuration += s.getDuration();
						}
					}

					sumDurationOverhead = sumDuration * (1 + overheadPercent) * 1.1d;

					if ((applicationInstance.getStartTime() + sumDurationOverhead) > deadline) {
						logger.debug("The processInstanceId=" + applicationInstance.getInstanceId() + " is likely to not meet the response time constraint (" //
								+ "responseTimeConstraint=" + applicationInstance.getApplication().getResponseTimeConstraint() + ", currentServicesDuration=" + sumDuration + ", currentServicesDurationWithOverhead=" + sumDurationOverhead + ", deadline=" + deadline + ")");
					}

				}
			});
		}

		if (logger.isDebugEnabled()) {
			BlockingQueue<Runnable> workQueue = ExecutorUtil.getWorkQueue(pool);
			if (workQueue.size() > 0) {
				logger.debug("Resilience Analysis working pool contains " + workQueue.size() + " item(s)");
			}
		}

	}

	public void analyzeServicesAvailable(SOAApplication application, Collection<SOAService> services) {
		analyzeInternal(new AnalysisRequest(application, services));
	}

	public void analyzeServiceUnavailable(SOAService service) {

		List<SOAApplicationInstance> applicationInstances = new LinkedList<SOAApplicationInstance>();

		for (SOAApplication a : service.getSOAApplications()) {

			for (SOAApplicationInstance i : resilienceServiceInternal.getSOAApplicationInstances(a.getApplicationId(), true)) {

				List<SOAApplicationServiceTracker> invokedServices = i.getInvokedServices();

				SOAApplicationServiceTracker lastInvokedService = !CollectionUtils.isEmpty(invokedServices) ? invokedServices.get(invokedServices.size() - 1) : null;

				boolean serviceCanBeInvoked = lastInvokedService == null;

				if (lastInvokedService != null) {
					for (String serviceClassification : a.getNextTasks(lastInvokedService.getServiceClassification())) {
						if (serviceClassification.equals(service.getServiceClassification())) {
							serviceCanBeInvoked = true;
							break;
						}
					}
				}

				if (serviceCanBeInvoked) {
					applicationInstances.add(i);
				} else {
					logger.warn("Service '" + service.getServiceEndpointURL() + "' can't be invoked by applicationId=" + a.getApplicationId() + ", applicationInstanceId=" + i.getInstanceId() + " so ignoring it for this instance");
				}
			}
		}

		boolean analyzeServicesWithoutRunningInstances = false;

		if (!applicationInstances.isEmpty() || analyzeServicesWithoutRunningInstances) {
			Collections.sort(applicationInstances);
			analyzeServiceUnavailableInternal(service, applicationInstances, null);
		} else {
			logger.warn("No need to act for service '" + service.getServiceEndpointURL() + "'. There are no process instances affected by it.");

		}
	}

	public void analyzeServiceUnavailableEvent(ServiceUnavailableEvent evt) {

		SOAService service = resilienceServiceInternal.getSOAService(evt.getService());

		SOAApplication application = resilienceServiceInternal.getSOAApplication(evt.getApplicationId());

		List<SOAApplicationInstance> applicationInstances = new LinkedList<SOAApplicationInstance>();

		for (String applicationInstanceId : evt.getApplicationInstanceIds()) {
			SOAApplicationInstance applicationInstance = resilienceServiceInternal.getSOAApplicationInstance(application, Long.valueOf(applicationInstanceId));
			applicationInstances.add(applicationInstance);
		}

		// if (service.getLastAvailabilityCheck() == null || event.getTimestamp() > service.getLastAvailabilityCheck().getTime()) {

		analyzeServiceUnavailableInternal(service, applicationInstances, evt);

		// }
	}

	private void analyzeServiceUnavailableInternal(SOAService service, List<SOAApplicationInstance> applicationInstances, ServiceUnavailableEvent evt) {

		Collection<SOAApplication> applications;

		synchronized (resilienceServiceInternal.getLockMonitor()) {
			applications = resilienceServiceInternal.getSOAApplications(service);
		}

		for (SOAApplication application : applications) {

			boolean monitoringEnabled = BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(application.getExecutionContext(), ExecutionFlags.FLAG_DISABLE_SERVICE_MONITORING));

			boolean externalEvent = evt != null;

			if (externalEvent || monitoringEnabled) {
				analyzeInternal(new AnalysisRequest(application, applicationInstances, service, evt));
			}
		}

	}

	public void analyzeSOAApplication(SOAApplication application) {
		analyzeInternal(new AnalysisRequest(application));
	}

	private void analyzeInternal(AnalysisRequest request) {

		pool.submit(() -> {

			SOAApplication application = request.getApplication();

			List<SOAApplicationInstance> applicationInstances = request.getApplicationInstances();

			List<Long> applicationInstanceIds = null;

			if (!CollectionUtils.isEmpty(applicationInstances)) {
				applicationInstanceIds = new ArrayList<Long>();
				for (SOAApplicationInstance i : applicationInstances) {
					applicationInstanceIds.add(i.getInstanceId());
				}
			}

			Collection<SOAService> services = request.getServices();

			final ExecutionContext executionContext = application.getExecutionContext();

			try {
				if (executionContext != null)
					executionContext.set();

				long s = System.currentTimeMillis();

				String applicationId = application.getApplicationId();

				// transactionTemplate.execute((txStatus) -> {

				// anaysisCache.lock(applicationId);

				// planningCache.lock(applicationId);

				boolean schedule = false;

				Collection<SOAService> requestAvailableServices = new LinkedList<SOAService>();
				Collection<SOAService> requestUnavailableServices = new LinkedList<SOAService>();

				for (SOAService service : services) {
					if (BooleanUtils.isTrue(service.getAvailable()))
						requestAvailableServices.add(service);
					else
						requestUnavailableServices.add(service);
				}

				boolean isExternalEvent = request.getServiceUnavailableEvent() != null;

				if (logger.isInfoEnabled()) {

					ExecutionLogMessage logMessage = new ExecutionLogMessage(LogConstants.MESSAGE_ID_RESILIENCE_ANALYSIS,
							"Analysis request for applicationId=" + application.getApplicationId() + ", applicationName=" + application.getName() + (applicationInstanceIds != null ? ", applicationInstanceIds=" + applicationInstanceIds : ""));

					logMessage.addTransientAttribute(LogConstants.APPLICATION_ID, application.getApplicationId());
					logMessage.addTransientAttribute(LogConstants.PROCESS_NAME, application.getName().replaceAll(" ", ""));
					logMessage.addTransientAttribute(LogConstants.RESILIENCE_SERVICES_NUMBER_OF_UNAVAILABLE_SERVICES, requestUnavailableServices.size());
					logMessage.addTransientAttribute(LogConstants.RESILIENCE_MONITORING_EXTERNAL_EVENT, isExternalEvent);

					logger.info(logMessage);

				}

				PlanningAction planningAction = planningCache.get(applicationId);

				boolean isRunning = planningAction != null && planningAction.isRunning();

				if (isRunning && !isExternalEvent) {
					logger.info("There is a reconfiguration for application " + applicationId + " (" + application.getName() + ") already running. Ignoring this request");
					return null;
				}

				SOAApplicationReconfiguration reconfiguration = null;

				int tstmp = application.getReconfigurationTimestamp();

				boolean create = false;

				if (planningAction == null) {
					reconfiguration = new SOAApplicationReconfiguration(application, applicationInstances);
					planningAction = new PlanningAction(infinispanSupport.getMyAddress().toString(), reconfiguration, s);
					schedule = true;
					create = true;
				} else {
					reconfiguration = planningAction.getReconfiguration();
					tstmp = reconfiguration.getApplication().getReconfigurationTimestamp();
					create = false;
					logger.info("There is a pending reconfiguration for application " + applicationId + " with timestamp " + tstmp + ". Only updating it");
				}

				reconfiguration.getAvailableServices().addAll(requestAvailableServices);
				reconfiguration.getUnavailableServices().addAll(requestUnavailableServices);

				if (create) {
					planningCache.put(applicationId, planningAction);
				} else {
					planningCache.replace(applicationId, planningAction);
				}

				anaysisCache.put(applicationId, tstmp);

				if (schedule || isExternalEvent) {

					final PlanningAction action = planningAction;

					// TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
					// @Override
					// public void afterCommit() {
					// planning.submitPlanningAction(action);
					planning.schedulePlanningAction(action);
					// }
					// });
				}

				return null;
				// });

			} finally {
				if (executionContext != null)
					executionContext.remove();
			}
		});

		if (logger.isDebugEnabled())

		{
			BlockingQueue<Runnable> workQueue = ExecutorUtil.getWorkQueue(pool);
			if (workQueue.size() > 0) {
				logger.debug("Resilience Analysis working pool contains " + workQueue.size() + " item(s)");
			}
		}

	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

}
