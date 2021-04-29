package br.ufsc.gsigma.services.execution.bpel.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.iapi.ProcessConf;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.services.execution.bpel.util.ProcessUtil;
import br.ufsc.gsigma.services.execution.events.ProcessDeployedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessInstanceFinishedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessInstanceStartedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessServicesBindingConfigured;
import br.ufsc.gsigma.services.execution.events.ProcessUndeployedEvent;
import br.ufsc.services.core.util.json.JsonUtil;

public abstract class ExecutionEventsListener {

	private static Logger logger = Logger.getLogger(ExecutionEventsListener.class);

	private static final ExecutorService pool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newFixedThreadPool(1, new NamedThreadFactory("ExecutionEventsListener")));

	private static final Set<Long> alreadyFinishedInstances = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

	public static void processDeployed(ExecutorService executorService, ProcessConf processConf) throws Exception {

		if (isSendEvent()) {

			ServicesInformation servicesInformation = ProcessUtil.getServicesInformation(processConf);

			Process businessProcess = ProcessUtil.getBusinessProcess(processConf);

			BindingServiceProcessCorrelator.getInstance().startCorrelate(processConf);

			String processName = processConf.getType().getLocalPart();

			String applicationId = ProcessUtil.getApplicationId(processConf);

			EventSender.getInstance()
					.sendEvent(new ProcessDeployedEvent( //
							ProcessUtil.getProcessExecutionServiceUrl(), //
							applicationId, //
							ProcessUtil.getProcessUrl(processName), //
							processConf.getProcessId(), //
							processName, //
							businessProcess, //
							servicesInformation //
			));

			// Run sync because binding can be not ready for process instances creation
			// executorService.submit(() -> {
			try {

				BindingConfiguration bindingConfiguration = BindingConfigurationHelper.configure(applicationId, processConf, businessProcess, servicesInformation, ExecutionContext.get(false));

				ProcessServicesBindingConfigured evt = new ProcessServicesBindingConfigured( //
						ProcessUtil.getProcessExecutionServiceUrl(), //
						applicationId, //
						ProcessUtil.getProcessUrl(processName), //
						processConf.getProcessId(), //
						processName, //
						bindingConfiguration //
				);
				EventSender.getInstance().sendEvent(evt, -1);

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				BindingServiceProcessCorrelator.getInstance().removeCorrelation(processConf);
			}
			// });
		}
	}

	private static boolean isSendEvent() {
		return !ThreadLocalHolder.isInitalized() || BooleanUtils.isNotTrue((Boolean) ThreadLocalHolder.getThreadLocalMap().get(ExecutionFlags.FLAG_IGNORE_EVENT_PUBLISHING));
	}

	public static void processUndeployed(ExecutorService executorService, ProcessConf processConf) throws Exception {

		if (isSendEvent()) {
			String processName = processConf.getType().getLocalPart();
			String applicationId = ProcessUtil.getApplicationId(processConf);

			// Removing binding configuration
			executorService.submit(() -> {
				try {
					BindingConfigurationHelper.remove(processConf);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				} finally {
					BindingServiceProcessCorrelator.getInstance().removeCorrelation(processConf);

				}
			});

			ProcessUndeployedEvent evt = new ProcessUndeployedEvent( //
					ProcessUtil.getProcessExecutionServiceUrl(), //
					applicationId, //
					ProcessUtil.getProcessUrl(processName), //
					processConf.getProcessId(), //
					processName //
			);
			EventSender.getInstance().sendEvent(evt, -1);
		}
	}

	public static void processInstanceCreated(ExecutorService executorService, ProcessConf processConf, ProcessInstanceDAO instance) throws Exception {

		if (isSendEvent()) {

			final String processName = instance.getProcess().getType().getLocalPart();
			// final String processNamespace = processConf.getProcessId().getNamespaceURI();
			final String applicationId = ProcessUtil.getApplicationId(processConf);
			final Long processInstanceId = instance.getInstanceId();
			final ServicesInformation servicesInformation = ProcessUtil.getServicesInformation(processConf);

			ProcessInstanceStartedEvent evt = new ProcessInstanceStartedEvent( //
					ProcessUtil.getProcessExecutionServiceUrl(), //
					applicationId, //
					ProcessUtil.getProcessUrl(processName), //
					processConf.getProcessId(), //
					processName, //
					processInstanceId, //
					instance.getCreateTime());

			EventSender.getInstance().sendEvent(evt, -1);

			pool.submit(() -> {
				try {

					// InfinispanDatabase.defineLock(InfinispanDatabase.getProcessInstanceLockKey(processNamespace, processName, String.valueOf(processInstanceId)));

					SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

					ExecutionLogMessage logMessage = new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_INSTANCE, "A new instance of process " + processName + " has been created with id '" + processInstanceId + "' at '" + df.format(instance.getCreateTime()) + "'") //
							.addAttribute(LogConstants.APPLICATION_ID, applicationId) //
							.addAttribute(LogConstants.PROCESS_NAME, processName) //
							.addAttribute(LogConstants.PROCESS_INSTANCE_ID, processInstanceId) //
							.addTransientAttribute(LogConstants.PROCESS_CREATION_TIME, instance.getCreateTime()) //
							.addTransientAttribute(LogConstants.PROCESS_STATE, ProcessState.stateToString(instance.getState())) //
							.addTransientAttribute(LogConstants.PROCESS_STARTED, true);

					populateQoSLogMessageAttributes(logMessage, servicesInformation, null, null);

					logger.info(logMessage);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			});
		}
	}

	public static void processInstanceFinished(ProcessConf processConf, ProcessInstanceDAO instance) throws Exception {

		final Long processInstanceId = instance.getInstanceId();

		if (alreadyFinishedInstances.add(processInstanceId)) {

			if (isSendEvent()) {

				final String processName = instance.getProcess().getType().getLocalPart();
				final String applicationId = ProcessUtil.getApplicationId(processConf);
				final Date finishTime = new Date(System.currentTimeMillis());

				final long duration = finishTime.getTime() - instance.getCreateTime().getTime();

				final ServicesInformation servicesInformation = ProcessUtil.getServicesInformation(processConf);

				short processStateCode = getProcessStateCode(instance, servicesInformation, duration);

				final String processState = ExecutionProcessState.stateToString(processStateCode);

				ProcessInstanceFinishedEvent evt = new ProcessInstanceFinishedEvent( //
						ProcessUtil.getProcessExecutionServiceUrl(), //
						applicationId, //
						ProcessUtil.getProcessUrl(processName), //
						processConf.getProcessId(), //
						processName, //
						processInstanceId, //
						instance.getCreateTime(), //
						finishTime, //
						processStateCode, //
						processState //
				);

				EventSender.getInstance().sendEvent(evt, -1);

				pool.submit(() -> {
					try {

						// final Date finishTime = instance.getLastActiveTime();

						SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

						ExecutionContext executionContext = ExecutionContext.get();

						Map<String, InvokedServiceInfo> invokedServicesMap = executionContext.getAttributes(LogConstants.PREFIX_INVOKED_SERVICE, InvokedServiceInfo.class);

						String json = executionContext.getAttribute(LogConstants.INVOKED_SERVICES_FORKS, String.class);

						final List<String> invokedServicesForks = json != null ? (List<String>) JsonUtil.getTypedListValue(json, String.class) : null;

						final Collection<InvokedServiceInfo> invokedServices = invokedServicesMap.values();

						final List<String> invokedServicesStr = invokedServices.stream() //
								.map(x -> x.getServiceEndpointInfo().getServiceProviderName() + ":" + x.getServiceLabel() + ":" + getInvokedTaskStr(x)) //
								.collect(Collectors.toList());

						int distinctNumberOfInvokedServices = new HashSet<String>(invokedServicesStr).size();

						final List<String> invokedTasksStr = new ArrayList<String>(invokedServices.size());

						for (InvokedServiceInfo s : invokedServices) {
							String taskClassificationSuffix = getInvokedTaskStr(s);
							invokedTasksStr.add(taskClassificationSuffix);
						}

						final Set<String> invokedServiceProviders = new LinkedHashSet<String>();

						for (InvokedServiceInfo s : invokedServices) {
							invokedServiceProviders.add(s.getServiceEndpointInfo().getServiceProviderName());
						}

						int numberOfInvokedServices = invokedServices.size();

						int numberOfInvokedServiceProviders = invokedServiceProviders.size();

						TreeSet<Long> bindingReconfigurationsVersions = new TreeSet<Long>();

						RequestBindingContext requestBindingContext = RequestBindingContext.get();
						if (requestBindingContext != null) {
							long v = requestBindingContext.getOriginalBindingEngineConfigurationVersion();
							if (v > 0) {
								bindingReconfigurationsVersions.add(v);
							}
						}

						DescriptiveStatistics resilienceReactionTimeStats = new DescriptiveStatistics();
						DescriptiveStatistics resilienceCheckServicesTimeStats = new DescriptiveStatistics();
						DescriptiveStatistics resilienceDiscoveryTimeStats = new DescriptiveStatistics();
						DescriptiveStatistics resilienceDeploymentTimeStats = new DescriptiveStatistics();

						invokedServices.stream().forEach(s -> {
							bindingReconfigurationsVersions.add(s.getBindingConfigurationVersion());
							if (s.getResilienceInfo() != null) {
								resilienceReactionTimeStats.addValue(s.getResilienceInfo().getReactionTime());
								resilienceCheckServicesTimeStats.addValue(s.getResilienceInfo().getServicesCheckTime());
								resilienceDiscoveryTimeStats.addValue(s.getResilienceInfo().getDiscoveryTime());
								resilienceDeploymentTimeStats.addValue(s.getResilienceInfo().getDeploymentTime());
							}
						});

						int numberOfBindingReconfigurations = Math.max(0, bindingReconfigurationsVersions.size() - 1);

						ExecutionLogMessage logMessage = new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_INSTANCE, "The instance '" + processInstanceId + "' of process " + processName + " finished at '" + df.format(finishTime) + "' with state " + processState);

						logMessage.addAttribute(LogConstants.APPLICATION_ID, applicationId);
						logMessage.addAttribute(LogConstants.PROCESS_NAME, processName);
						logMessage.addAttribute(LogConstants.PROCESS_INSTANCE_ID, processInstanceId);
						logMessage.addTransientAttribute(LogConstants.PROCESS_CREATION_TIME, instance.getCreateTime());
						logMessage.addTransientAttribute(LogConstants.PROCESS_FINISH_TIME, finishTime);
						logMessage.addTransientAttribute(LogConstants.PROCESS_DURATION, duration);
						logMessage.addTransientAttribute(LogConstants.PROCESS_STATE, processState);
						logMessage.addTransientAttribute(LogConstants.PROCESS_STATE_CODE, processStateCode);
						logMessage.addTransientAttribute(LogConstants.PROCESS_FINISHED, true);
						logMessage.addTransientAttribute(LogConstants.SERVICE_INVOKED_SERVICES, invokedServicesStr);

						logMessage.addTransientAttribute(LogConstants.SERVICE_INVOKED_TASKS, invokedTasksStr);

						logMessage.addTransientAttribute(LogConstants.SERVICE_INVOKED_SERVICE_PROVIDERS, invokedServiceProviders);

						logMessage.addTransientAttribute(LogConstants.NUMBER_OF_INVOKED_SERVICES, numberOfInvokedServices);
						logMessage.addTransientAttribute(LogConstants.DISTINCT_NUMBER_OF_INVOKED_SERVICES, distinctNumberOfInvokedServices);

						logMessage.addTransientAttribute(LogConstants.NUMBER_OF_INVOKED_SERVICE_PROVIDERS, numberOfInvokedServiceProviders);
						logMessage.addTransientAttribute(LogConstants.NUMBER_OF_BINDING_RECONFIGURATIONS, numberOfBindingReconfigurations);

						if (numberOfBindingReconfigurations > 0) {
							logMessage.addTransientAttribute(LogConstants.MAX_BINDING_CONFIGURATION_VERSION, bindingReconfigurationsVersions.last());
						}

						logMessage.addTransientAttribute(LogConstants.RESILIENCE_REACTION_MEAN_TIME, numberOfBindingReconfigurations > 0 && resilienceReactionTimeStats.getN() > 0 ? resilienceReactionTimeStats.getMean() : 0);
						logMessage.addTransientAttribute(LogConstants.RESILIENCE_SERVICES_CHECK_MEAN_TIME, numberOfBindingReconfigurations > 0 && resilienceCheckServicesTimeStats.getN() > 0 ? resilienceCheckServicesTimeStats.getMean() : 0);
						logMessage.addTransientAttribute(LogConstants.RESILIENCE_DISCOVERY_MEAN_TIME, numberOfBindingReconfigurations > 0 && resilienceDiscoveryTimeStats.getN() > 0 ? resilienceDiscoveryTimeStats.getMean() : 0);
						logMessage.addTransientAttribute(LogConstants.RESILIENCE_DEPLOYMENT_MEAN_TIME, numberOfBindingReconfigurations > 0 && resilienceDeploymentTimeStats.getN() > 0 ? resilienceDeploymentTimeStats.getMean() : 0);

						populateQoSLogMessageAttributes(logMessage, servicesInformation, invokedServices, invokedServicesForks);

						logger.info(logMessage);
					} catch (Throwable e) {
						logger.error(e.getMessage(), e);
					}
				});
			}
		}
	}

	private static short getProcessStateCode(ProcessInstanceDAO instance, final ServicesInformation servicesInformation, final long duration) {
		short processStateCode = instance.getState();

		if (processStateCode == ProcessState.STATE_COMPLETED_OK) {

			Double desiredResponseTime = null;

			if (servicesInformation != null && servicesInformation.getQoSCriterions() != null) {
				for (QoSCriterion qCrit : servicesInformation.getQoSCriterions()) {
					if ("Performance.ResponseTime".equals(qCrit.getQoSKey())) {
						desiredResponseTime = qCrit.getQoSValue();
						break;
					}
				}
				if (desiredResponseTime != null && duration > desiredResponseTime) {
					processStateCode = ExecutionProcessState.STATE_COMPLETED_WITH_QOS_VIOLATION;
				}
			}
		}
		return processStateCode;
	}

	private static String getInvokedTaskStr(InvokedServiceInfo s) {
		String c = s.getServiceEndpointInfo().getServiceEndpointURL();
		int idx = c.lastIndexOf('/', c.lastIndexOf('/') - 1);
		String taskClassificationSuffix = c.substring(idx + 1);
		return taskClassificationSuffix;
	}

	private static void populateQoSLogMessageAttributes(ExecutionLogMessage logMessage, ServicesInformation servicesInformation, Collection<InvokedServiceInfo> invokedServices, List<String> invokedServicesForks) {
		if (servicesInformation != null && servicesInformation.getQoSCriterions() != null) {

			List<String> constraints = new LinkedList<String>();

			for (QoSCriterion qCrit : servicesInformation.getQoSCriterions()) {

				// logMessage.addAttribute("QoS.Constraint." + qCrit.getQoSKey(), qCrit.getQoSValue());

				//@formatter:off
				String op = "LT".equalsIgnoreCase(qCrit.getComparisionType()) ? "<" : //
							"LE".equalsIgnoreCase(qCrit.getComparisionType()) ? "<=" : //
							"EQ".equalsIgnoreCase(qCrit.getComparisionType()) ? "=" : //
							"GT".equalsIgnoreCase(qCrit.getComparisionType()) ? ">" : //
							"GE".equalsIgnoreCase(qCrit.getComparisionType()) ? ">=" : "";
				//@formatter:on

				constraints.add(qCrit.getQoSKey() + " " + op + " " + qCrit.getQoSValue());

				if ("Performance.ResponseTime".equals(qCrit.getQoSKey())) {
					logMessage.addAttribute("QoS.Constraints." + qCrit.getQoSKey(), qCrit.getQoSValue());
				}

			}
			// logMessage.addAttribute("QoS.Constraints", constraints);
		}

		if (invokedServices != null) {

			double declaredEndToEndResponseTime = 0D;
			double observedEndToEndResponseTime = 0D;

			double observedCost = 0D;

			// ResponseTime
			// for (InvokedServiceInfo iv : adjustInvokedServicesForResponseTime(invokedServices)) {

			for (InvokedServiceInfo iv : invokedServices) {
				if (!iv.isError()) {
					Map<String, Double> qoSValues = iv.getServiceEndpointInfo().getQoSValues();

					if (declaredEndToEndResponseTime >= 0) {
						Number responseTime = (Number) qoSValues.get("Performance.ResponseTime");
						if (responseTime != null)
							declaredEndToEndResponseTime += responseTime.doubleValue();
						else
							declaredEndToEndResponseTime = -1;
					}

					observedEndToEndResponseTime += iv.getInvokeFinishTime() - iv.getInvokeStartTime();
				}
			}

			// Cost
			for (InvokedServiceInfo iv : invokedServices) {
				if (!iv.isError()) {
					Map<String, Double> qoSValues = iv.getServiceEndpointInfo().getQoSValues();

					if (observedCost >= 0) {
						Number cost = (Number) qoSValues.get("Cost.ExecutionCost");
						if (cost != null)
							observedCost += cost.doubleValue();
						else
							observedCost = -1;
					}
				}
			}

			logMessage.addAttribute("QoS.Declared.EndToEnd.Performance.ResponseTime", declaredEndToEndResponseTime);
			logMessage.addAttribute("QoS.Declared.EndToEnd.Cost.ExecutionCost", observedCost);

			if (observedEndToEndResponseTime > 0)
				logMessage.addAttribute("QoS.Observed.EndToEnd.Performance.ResponseTime", observedEndToEndResponseTime);

			if (observedCost > 0)
				logMessage.addAttribute("QoS.Observed.EndToEnd.Cost.ExecutionCost", observedCost);

			// logMessage.addAttribute("QoS.Declared.EndToEnd",
			// Arrays.asList( //
			// ImmutableMap.of("Performance.ResponseTime", declaredEndToEndResponseTime), //
			// ImmutableMap.of("Cost.ExecutionCost", observedCost) //
			// ) //
			// );
			//
			// List<Map<String, Double>> list = new LinkedList<Map<String, Double>>();
			//
			// if (observedEndToEndResponseTime > 0)
			// list.add(ImmutableMap.of("Performance.ResponseTime", observedEndToEndResponseTime));
			//
			// if (observedCost > 0)
			// list.add(ImmutableMap.of("Cost.ExecutionCost", observedCost));
			//
			// if (!list.isEmpty())
			// logMessage.addAttribute("QoS.Observed.EndToEnd", list);

		}
	}

	@SuppressWarnings("unused")
	private static List<InvokedServiceInfo> adjustInvokedServicesForResponseTime(Collection<InvokedServiceInfo> invokedServices) {

		List<InvokedServiceInfo> response = new LinkedList<InvokedServiceInfo>(invokedServices);

		ListIterator<InvokedServiceInfo> it = response.listIterator();

		while (it.hasNext()) {

			InvokedServiceInfo s = it.next();

			String serviceClassification = s.getServiceEndpointInfo().getServiceClassification();

			// very dummy approach for billing process which has a fork
			if ( //
			"ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/acceptCredit".equals(serviceClassification) || //
					"ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/acceptCharges".equals(serviceClassification)) {

				it.remove();
			}
		}

		return response;

	}

}
