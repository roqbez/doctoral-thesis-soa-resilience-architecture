package br.ufsc.gsigma.services.resilience.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.infinispan.AdvancedCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.util.concurrent.Striped;

import br.ufsc.gsigma.infrastructure.util.thread.ExecutorUtil;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareScheduledExecutorServiceDecorator;
import br.ufsc.gsigma.services.resilience.support.InfinispanCaches;
import br.ufsc.gsigma.services.resilience.support.InfinispanSupport;
import br.ufsc.gsigma.services.resilience.support.PlanningAction;
import br.ufsc.gsigma.services.resilience.support.SOAApplication;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationReconfiguration;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

@Component
public class ResiliencePlanning implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = Logger.getLogger(ResiliencePlanning.class);

	@Autowired
	private InfinispanCaches caches;

	@Autowired
	private InfinispanSupport infinispanSupport;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private ResilienceExecution resilienceExecution;

	// private Map<SOAApplication, ScheduledExecutorService> schedulers = new ConcurrentHashMap<SOAApplication, ScheduledExecutorService>();

	ScheduledExecutorService scheduler = new ExecutionContextAwareScheduledExecutorServiceDecorator(Executors.newScheduledThreadPool(ResilienceParams.PLANNING_NUM_THREADS, new NamedThreadFactory("resilience-planning-scheduler")));

	private ExecutorService pool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newCachedThreadPool(new NamedThreadFactory("resilience-planning")));

	private AdvancedCache<String, PlanningAction> planningCache;

	private Striped<Lock> locker = Striped.lock(512);

	private static ResiliencePlanning INSTANCE;

	public static ResiliencePlanning getInstance() {
		return INSTANCE;
	}

	@PostConstruct
	public void setup() {
		this.planningCache = caches.getPlanningCache().getAdvancedCache();
		INSTANCE = this;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		logger.info("Resilience Planning ready");
		recoverPlanningActions();
	}

	private void recoverPlanningActions() {
		if (infinispanSupport.isLeader()) {
			for (PlanningAction action : this.planningCache.values()) {
				logger.info("Recovering planning action for application --> " 
							+ action.getReconfiguration().getApplication());
				schedulePlanningAction(action);
			}
		}
	}

	// private ScheduledExecutorService getScheduler(SOAApplication application) {
	// return schedulers.computeIfAbsent(application, app -> createScheduler(app));
	// }

	// private ExecutionContextAwareScheduledExecutorServiceDecorator createScheduler(SOAApplication application) {
	// int planningNumThreads = ResilienceParams.PLANNING_NUM_THREADS;
	// String name = application.getName().toLowerCase().replaceAll(" ", "-");
	// logger.info("Creating scheduled executor for " + application.getName());
	// return new ExecutionContextAwareScheduledExecutorServiceDecorator(Executors.newScheduledThreadPool(planningNumThreads, new NamedThreadFactory("resilience-planning-scheduler-" + name)));
	// }

	public PlanningAction schedulePlanningAction(PlanningAction action) {

		int delay = ExecutionContext.getAttribute(ExecutionAttributes.ATT_RESILIENCE_PLANNING_SCHEDULER_DELAY, Integer.class, ResilienceParams.DEFAULT_PLANNING_SCHEDULER_DELAY);

		SOAApplication application = action.getReconfiguration().getApplication();
		logger.info("Scheduling planning action with delay of " + delay + " ms for application " + application.getApplicationId() + " and reconfigurationTimestamp=" + application.getReconfigurationTimestamp());

		// ScheduledExecutorService scheduler = getScheduler(action.getReconfiguration().getApplication());

		scheduler.schedule((Callable<Object>) action, delay, TimeUnit.MILLISECONDS);

		if (logger.isDebugEnabled()) {
			BlockingQueue<Runnable> workQueue = ExecutorUtil.getWorkQueue(scheduler);
			if (workQueue.size() > 0) {
				logger.debug("Resilience Planning scheduler working pool contains " + workQueue.size() + " item(s)");
			}
		}

		return action;
	}

	public void submitPlanningAction(PlanningAction action) {

		int delay = ExecutionContext.getAttribute(ExecutionAttributes.ATT_RESILIENCE_PLANNING_SCHEDULER_DELAY, Integer.class, ResilienceParams.DEFAULT_PLANNING_SCHEDULER_DELAY);

		action.setDelay(delay);
		action.setExecutionContext(ExecutionContext.get());

		// SOAApplication application = action.getReconfiguration().getApplication();
		// logger.info("Submiting planning action with delay of " + delay + " ms for application " + application.getApplicationId() + " and reconfigurationTimestamp=" +
		// application.getReconfigurationTimestamp());
		//
		// infinispanSupport.getClusterExecution() //
		// .singleNodeSubmission(8) //
		// .submit(action);

		SOAApplicationReconfiguration reconfiguration = action.getReconfiguration();

		pool.submit(() -> {

			final SOAApplication application = reconfiguration.getApplication();

			final String applicationId = application.getApplicationId();

			Lock lock = locker.get(applicationId);

			try {

				lock.lock();

				String myNode = infinispanSupport.getMyAddress().toString();

				PlanningAction planningAction = planningCache.get(applicationId);

				if (planningAction != null) {

					boolean sameNode = myNode.equals(planningAction.getNodeId());

					if (sameNode && planningAction.isRunning()) {
						logger.info("Planning action already executing for application " + application + ". Aborting");
						return null;
					} else if (!sameNode) {
						logger.info("Recovering interrupted action from node " + planningAction.getNodeId() + " for application " + application);
					}
				}

				planningAction.setRunning(true);
				planningAction.setNodeId(myNode);

				planningCache.replace(applicationId, planningAction);

			} finally {
				lock.unlock();
			}

			try {
				Future<Object> f = resilienceExecution.executeAction(reconfiguration, action.getStartTime(), (long) delay);

				f.get();

			} finally {
				planningCache.remove(applicationId);
			}

			return null;
		});

		if (logger.isDebugEnabled()) {
			BlockingQueue<Runnable> workQueue = ExecutorUtil.getWorkQueue(pool);
			if (workQueue.size() > 0) {
				logger.debug("Resilience Planning working pool contains " + workQueue.size() + " item(s)");
			}
		}
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

}
