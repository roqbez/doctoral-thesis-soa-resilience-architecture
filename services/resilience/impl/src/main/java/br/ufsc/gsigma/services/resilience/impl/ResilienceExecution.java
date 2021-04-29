package br.ufsc.gsigma.services.resilience.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import br.ufsc.gsigma.binding.events.BindingConfigurationRequestEvent;
import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.binding.model.BindingConfigurationRequest;
import br.ufsc.gsigma.binding.model.NamespaceServices;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ServiceAssociation;
import br.ufsc.gsigma.catalog.services.model.ServiceConfig;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.util.thread.ExecutorUtil;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.ws.ServiceClient;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessContract;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessRequest;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.servicediscovery.util.DiscoveryUtil;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;
import br.ufsc.gsigma.services.deployment.locator.DeploymentServiceLocator;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedService;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedServiceDeploymentRequestItem;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedServicesDeploymentRequest;
import br.ufsc.gsigma.services.deployment.model.ServiceContainer;
import br.ufsc.gsigma.services.deployment.model.ServicesDeployment;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.interfaces.ResilienceService;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;
import br.ufsc.gsigma.services.resilience.model.ServicesCheckResult;
import br.ufsc.gsigma.services.resilience.support.InfinispanCaches;
import br.ufsc.gsigma.services.resilience.support.InfinispanSupport;
import br.ufsc.gsigma.services.resilience.support.ResilienceUtil;
import br.ufsc.gsigma.services.resilience.support.ResilienceUtil.DiscoveryRetryParams;
import br.ufsc.gsigma.services.resilience.support.SOAApplication;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationInstance;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationReconfiguration;
import br.ufsc.gsigma.services.resilience.support.SOAService;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;
import br.ufsc.services.core.util.json.JsonUtil;

@Component
public class ResilienceExecution implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = Logger.getLogger(ResilienceExecution.class);

	private Map<SOAApplication, ExecutorService> pools = new ConcurrentHashMap<SOAApplication, ExecutorService>();

	// private ExecutorService pool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newFixedThreadPool(ResilienceParams.EXECUTION_NUM_THREADS, new NamedThreadFactory("resilience-execution-pool")));

	@Autowired
	private InfinispanCaches caches;

	@Autowired
	private InfinispanSupport infinispanSupport;

	@Autowired
	private EventSender eventSender;

	@Autowired
	private ResilienceService resilienceService;

	@Autowired
	private ResilienceServiceInternal resilienceServiceInternal;

	private ServiceAccessContract serviceClient;

	private DeploymentService deploymentService;

	@PostConstruct
	public void setup() {
		this.serviceClient = ServiceClient.getClient(ServiceAccessContract.class, true);
		this.deploymentService = DeploymentServiceLocator.get();

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		logger.info("Resilience Execution ready");
		recoverExecutionActions();

	}

	private void recoverExecutionActions() {
		if (infinispanSupport.isLeader()) {
			for (SOAApplication application : caches.getSOAApplications().values()) {
				if (application.getBindingReconfigurationEvent() != null) {
					logger.info("Sending reconfiguration event for application --> " 
								+ application);
					eventSender.sendEvent(application.getBindingReconfigurationEvent());
				}
			}
		}
	}

	private ExecutorService getThreadPool(SOAApplication application) {
		return pools.computeIfAbsent(application, app -> createThreadPool(app));
	}

	private ExecutorService createThreadPool(SOAApplication application) {
		String name = application.getName().toLowerCase().replaceAll(" ", "-");
		logger.info("Creating cached thread pool for " + application.getName());
		return new ExecutionContextAwareExecutorServiceDecorator(Executors.newCachedThreadPool(new NamedThreadFactory("resilience-execution-" + name)));
	}

	public Future<Object> executeAction(SOAApplicationReconfiguration reconfiguration, long startTime, long delay) {

		final SOAApplication application = reconfiguration.getApplication();

		final String applicationId = application.getApplicationId();

		final List<SOAApplicationInstance> applicationInstances = reconfiguration.getApplicationInstances();

		final List<String> applicationInstanceIds = !CollectionUtils.isEmpty(applicationInstances) ? applicationInstances.stream().map(i -> String.valueOf(i.getInstanceId())).collect(Collectors.toList()) : null;

		if (logger.isInfoEnabled()) {
			logger.info("Submiting execution action for applicationId=" + applicationId + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : ""));
		}

		final ExecutorService pool = getThreadPool(application);

		Future<Object> f = pool.submit(() -> {

			int reconfigurationTimestamp = reconfiguration.getApplication().getReconfigurationTimestamp();

			// if (delay > 0) {
			// logger.info("Waiting " + delay + " ms for executing action in applicationId=" + applicationId + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : "")
			// + " and timestamp " + reconfigurationTimestamp);
			// Thread.sleep(delay);
			// }

			try {

				// else if (planningAction.isRunning()) {
				// logger.info("Planning action already running for application " + applicationId + ". Aborting");
				// abort = true;
				// return null;
				// }

				logger.info("Executing planning action for applicationId=" + applicationId + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : "") + ". The previous reconfiguration timestamp is "
						+ reconfigurationTimestamp);

				final ResilienceInfo resilienceInfo = new ResilienceInfo();

				final Process businessProcess = application.getBusinessProcess();
				final ServicesInformation servicesInformation = application.getServicesInformation();

				final String bpName = businessProcess.getName();

				if (logger.isInfoEnabled() && (!CollectionUtils.isEmpty(reconfiguration.getAvailableServices()) || !CollectionUtils.isEmpty(reconfiguration.getUnavailableServices()))) {
					StringBuilder sb = new StringBuilder();
					for (SOAService s : reconfiguration.getAvailableServices()) {
						sb.append("\n\tAVAILABLE: " + s.getServiceEndpointURL());
					}
					for (SOAService s : reconfiguration.getUnavailableServices()) {
						sb.append("\n\tUNAVAILABLE: " + s.getServiceEndpointURL());
					}
					logger.info("Processing reconfiguration with applicationId " + applicationId + " (" + bpName + ")" + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : "") + " and services at:" + sb.toString());
				}

				Map<String, Collection<ServiceEndpointInfo>> stillAliveServices = getStillAliveServices(application, reconfiguration);

				boolean containsAtLeastOneAliveService = true;

				BindingConfigurationRequest tmpBindingConfigurationRequest = new BindingConfigurationRequest(applicationId, application.getExecutionContext());
				tmpBindingConfigurationRequest.setApplicationInstanceIds(applicationInstanceIds);

				for (Entry<String, Collection<ServiceEndpointInfo>> e : stillAliveServices.entrySet()) {

					if (!e.getValue().isEmpty()) {

						for (ServiceEndpointInfo s : e.getValue()) {

							if (s.isAdhoc()) {
								logger.info("Adhoc " + s.getServiceEndpointURL() + " in applicationId " + applicationId + " (" + bpName + ")" + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : "") + " is alive");
							}

							tmpBindingConfigurationRequest.addServiceEndpoint(s);
						}
					} else {

						containsAtLeastOneAliveService = false;

						logger.warn("Service classification " + e.getKey() + " in applicationId " + applicationId + " (" + bpName + ")" + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : "")
								+ " don't have any alive service");
					}
				}

				long startTime2 = startTime;

				if (containsAtLeastOneAliveService) {
					logger.info("There is at least one available service for each classification in applicationId " + applicationId + " (" + bpName + ")" + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : "" //
							+ ". Returning a temporary configuration and continuing to process for a best one in background"));

					reconfigurationTimestamp = submitBindingConfigurationRequest(new SubmitBindingConfigurationRequestArgs(tmpBindingConfigurationRequest, reconfigurationTimestamp, false, startTime, true, application, applicationInstanceIds, resilienceInfo));
					startTime2 = System.currentTimeMillis();
				}

				final boolean checkTimestamp = false;

				final Map<String, Collection<ServiceEndpointInfo>> checkedDiscoveredServices = ResilienceUtil.checkAndDiscoverServices(resilienceService, businessProcess, servicesInformation, resilienceInfo, new DiscoveryRetryParams(1, 0L));

				final Set<String> tasksWithoutServices = checkedDiscoveredServices.entrySet().stream().filter(e -> e.getValue().size() == 0).map(e -> e.getKey()).collect(Collectors.toSet());

				final Map<String, Map<String, Collection<ServiceEndpointInfo>>> server2AdhocServices = deployAdhocServices(application, resilienceInfo, tasksWithoutServices);

				if (logger.isInfoEnabled()) {

					StringBuilder sb = new StringBuilder(
							"The following adhoc services are bound to applicationId " + applicationId + " (" + bpName + ")" + (applicationInstanceIds != null ? ", applicationName=" + application.getName() + ", applicationInstanceIds=" + applicationInstanceIds : "") + ":");

					boolean anyNonEmpty = false;

					for (Entry<String, Map<String, Collection<ServiceEndpointInfo>>> e : server2AdhocServices.entrySet()) {
						if (!MapUtils.isEmpty(e.getValue())) {
							sb.append("\n\tServer:" + e.getKey());

							for (Entry<String, Collection<ServiceEndpointInfo>> e2 : e.getValue().entrySet()) {
								if (!CollectionUtils.isEmpty(e2.getValue())) {
									sb.append("\n\t\t" + e2.getKey());
									for (ServiceEndpointInfo s : e2.getValue()) {
										sb.append("\n\t\t\t" + s.getServiceEndpointURL());
										anyNonEmpty = true;
									}
								}
							}
						}
					}

					if (anyNonEmpty) {
						logger.info(sb.toString());
					}
				}

				// Call BindingService to register binding configuration
				BindingConfigurationRequest bindingConfigurationRequest = new BindingConfigurationRequest(applicationId, application.getExecutionContext());

				bindingConfigurationRequest.setApplicationInstanceIds(applicationInstanceIds);

				// Service classifications explicitly not monitored
				for (Collection<ServiceEndpointInfo> services : getNotMonitoredServices(servicesInformation).values()) {
					bindingConfigurationRequest.addServiceEndpoints(services);
				}

				for (Collection<ServiceEndpointInfo> services : checkedDiscoveredServices.values()) {
					bindingConfigurationRequest.addServiceEndpoints(services);
				}

				for (Map<String, Collection<ServiceEndpointInfo>> adhocServices : server2AdhocServices.values()) {
					for (Collection<ServiceEndpointInfo> services : adhocServices.values()) {
						bindingConfigurationRequest.addServiceEndpoints(services);
					}
				}

				submitBindingConfigurationRequest(new SubmitBindingConfigurationRequestArgs(bindingConfigurationRequest, reconfigurationTimestamp, checkTimestamp, startTime2, false, application, applicationInstanceIds, resilienceInfo));

				return bindingConfigurationRequest;

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}

		});

		if (logger.isDebugEnabled()) {
			BlockingQueue<Runnable> workQueue = ExecutorUtil.getWorkQueue(pool);
			if (workQueue.size() > 0) {
				logger.debug("Resilience Execution working pool contains " + workQueue.size() + " item(s)");
			}
		}

		return f;
	}

	private int submitBindingConfigurationRequest(SubmitBindingConfigurationRequestArgs args) {

		List<String> classificationWithNoServices = new LinkedList<String>();

		for (Entry<String, Collection<ServiceEndpointInfo>> e : args.bindingConfigurationRequest.getServicesPerNamespace().entrySet()) {
			if (e.getValue().isEmpty()) {
				classificationWithNoServices.add(e.getKey());
			}
		}

		if (!classificationWithNoServices.isEmpty()) {
			logger.warn("Discarding configuration because it has empty services for classifications: " + classificationWithNoServices + " (currentReconfigurationTimestamp=" + args.currentReconfigurationTimestamp + ")");
			return -1;
		}

		int incrementedReconfigurationTimestamp;

		long reactionTime;

		BindingConfigurationRequestEvent bindingReconfigurationEvent;

		synchronized (args.application) {

			int lastTimestamp = args.application.getReconfigurationTimestamp();

			if (args.checkTimestamp && args.currentReconfigurationTimestamp < lastTimestamp) {
				logger.info("Ignoring resilience action because it is outdated (currentReconfigurationTimestamp=" + args.currentReconfigurationTimestamp + ", lastTimestamp=" + lastTimestamp + ")");
				return lastTimestamp;
			}

			reactionTime = System.currentTimeMillis() - args.startTime;

			args.resilienceInfo.setReactionTime(reactionTime);

			bindingReconfigurationEvent = new BindingConfigurationRequestEvent(args.bindingConfigurationRequest, args.resilienceInfo);
			args.application.setBindingReconfigurationEvent(bindingReconfigurationEvent);

			incrementedReconfigurationTimestamp = args.application.incrementReconfigurationTimestamp();
			args.application.updateCache();
			resilienceServiceInternal.monitorSOAApplication(args.application, args.bindingConfigurationRequest.getServiceEndpoints());
		}

		final String applicationId = args.application.getApplicationId();

		final String bpName = args.application.getBusinessProcess().getName();

		generateServiceAccessTokens(applicationId, args.bindingConfigurationRequest);

		if (logger.isInfoEnabled()) {
			logBoundServices(args.application, bpName, args.bindingConfigurationRequest);
		}

		eventSender.sendEvent(bindingReconfigurationEvent);

		ExecutionLogMessage logMessage = new ExecutionLogMessage(LogConstants.MESSAGE_ID_RESILIENCE_REACTION, (args.temporary ? "Temporary reconfiguration" : "Reconfiguration") + " done at applicationId=" + args.application.getApplicationId() + ", applicationName=" + args.application.getName()
				+ (args.applicationInstanceIds != null ? ", applicationInstanceIds=" + args.applicationInstanceIds : "") + " in " + reactionTime + " ms. The current reconfiguration timestamp is " + args.application.getReconfigurationTimestamp());

		logMessage.addTransientAttribute(LogConstants.RESILIENCE_RECONFIGURATION_DONE, true);
		logMessage.addTransientAttribute(LogConstants.RESILIENCE_TEMPORARY_RECONFIGURATION, args.temporary);
		logMessage.addTransientAttribute(LogConstants.APPLICATION_ID, args.application.getApplicationId());
		logMessage.addTransientAttribute(LogConstants.PROCESS_NAME, args.application.getName().replaceAll(" ", ""));
		logMessage.addTransientAttribute(LogConstants.RESILIENCE_REACTION_DURATION, args.resilienceInfo.getReactionTime());
		logMessage.addTransientAttribute(LogConstants.RESILIENCE_SERVICES_CHECK_DURATION, args.resilienceInfo.getServicesCheckTime());
		logMessage.addTransientAttribute(LogConstants.RESILIENCE_DISCOVERY_DURATION, args.resilienceInfo.getDiscoveryTime());
		logMessage.addTransientAttribute(LogConstants.RESILIENCE_DEPLOYMENT_DURATION, args.resilienceInfo.getDeploymentTime());
		logMessage.addTransientAttribute(LogConstants.MAX_BINDING_CONFIGURATION_VERSION, incrementedReconfigurationTimestamp);

		logger.info(logMessage);

		return incrementedReconfigurationTimestamp;
	}

	private void generateServiceAccessTokens(final String applicationId, BindingConfigurationRequest bindingConfigurationRequest) {
		ServiceAccessRequest serviceAccessRequest = new ServiceAccessRequest("EV 1", applicationId);

		for (ServiceEndpointInfo s : bindingConfigurationRequest.getServiceEndpoints()) {
			if (!s.isAdhoc() && s.getServiceAccessToken() == null) {
				try {

					String serviceAccessToken = ServiceEndpointInfo.getServiceAccessToken(s, serviceClient, serviceAccessRequest);
					s.setServiceAccessToken(serviceAccessToken);

				} catch (Exception ex) {
					logger.warn("Can't obtain access token for " + s.getServiceEndpointURL() + " --> " + ex.getMessage());
				}
			}

		}
	}

	private Map<String, Collection<ServiceEndpointInfo>> getStillAliveServices(final SOAApplication application, SOAApplicationReconfiguration reconfiguration) {

		ServicesInformation servicesInformation = application.getServicesInformation();

		Map<String, Collection<ServiceEndpointInfo>> stillAliveServices = new HashMap<String, Collection<ServiceEndpointInfo>>();

		BindingConfiguration previousBindingConfiguration = application.getBindingConfiguration();

		if (previousBindingConfiguration != null) {

			Map<String, ServiceEndpointInfo> servicesToCheck = new HashMap<String, ServiceEndpointInfo>();

			for (String classification : servicesInformation.getServicesClassifications()) {

				String namespace = DiscoveryUtil.serviceClassificationToNamespace(classification);

				NamespaceServices namespaceServices = previousBindingConfiguration.getNamespaceServices(namespace);

				if (namespaceServices != null) {
					for (ServiceEndpointInfo s : namespaceServices.getServices()) {
						// if (!reconfiguration.getAvailableServices().contains(s) && !reconfiguration.getUnavailableServices().contains(s)) {
						servicesToCheck.put(s.getServiceKey(), s);
						// }
					}
				}

			}

			ServicesCheckResult checkResult = resilienceService.checkServicesAvailable(new ArrayList<ServiceEndpointInfo>(servicesToCheck.values()));

			for (String serviceKey : checkResult.getAvailableServices()) {
				ServiceEndpointInfo s = servicesToCheck.get(serviceKey);
				String serviceClassification = s.getServiceClassification();
				Collection<ServiceEndpointInfo> services = stillAliveServices.get(serviceClassification);
				if (services == null) {
					services = new LinkedList<ServiceEndpointInfo>();
					stillAliveServices.put(serviceClassification, services);
				}
				services.add(s);
			}

			for (String classification : servicesInformation.getServicesClassifications()) {
				Collection<ServiceEndpointInfo> list = stillAliveServices.get(classification);
				if (list == null) {
					stillAliveServices.put(classification, new LinkedList<ServiceEndpointInfo>());
				}
			}
		}

		return stillAliveServices;
	}

	private void logBoundServices(final SOAApplication application, final String bpName, BindingConfigurationRequest bindingConfigurationRequest) {
		boolean hasAnyService = false;

		StringBuilder sb = new StringBuilder();

		int w = 0;

		for (Entry<String, Collection<ServiceEndpointInfo>> e : bindingConfigurationRequest.getServicesPerNamespace().entrySet()) {
			// sb.append("\n\t" + e.getKey());
			for (ServiceEndpointInfo s : e.getValue()) {
				sb.append("\n\t" + s.getServiceEndpointURL());
				w++;
				hasAnyService = true;
			}
		}

		if (hasAnyService) {

			if (logger.isDebugEnabled()) {
				logger.debug("The following services are now bound to application " + application.getApplicationId() + " (" + bpName + "):" + sb.toString());
			} else {
				logger.info(w + " service(s) are now bound to application " + application.getApplicationId() + " (" + bpName + ")");
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Map<String, Collection<ServiceEndpointInfo>>> deployAdhocServices(final SOAApplication application, final ResilienceInfo resilienceInfo, final Set<String> serviceClassifications) throws InterruptedException {

		final String applicationId = application.getApplicationId();

		final ExecutionContext executionContext = application.getExecutionContext();

		final Map<String, Map<String, Collection<ServiceEndpointInfo>>> server2AdhocServices = new HashMap<String, Map<String, Collection<ServiceEndpointInfo>>>();

		boolean useServiceDeployment = BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(executionContext, ExecutionFlags.FLAG_DISABLE_ADHOC_SERVICE_DEPLOYMENT));

		if (!useServiceDeployment || serviceClassifications.isEmpty()) {

			if (!useServiceDeployment) {
				logger.debug("Flag DISABLE_ADHOC_SERVICE_DEPLOYMENT set. Ignoring adhoc service deployment");
			}

			return server2AdhocServices;

		} else {

			final Map<String, Set<String>> server2ServicesClassificationsForAdhocDeploy = new HashMap<String, Set<String>>();

			Set<String> servicesClassificationsAlreadyDeployed = new LinkedHashSet<String>();

			long deploymentStartTime = System.currentTimeMillis();

			logger.info("The following classifications don't have any bound service: " + serviceClassifications + ". Trying to deploy adhoc services for applicationId " + applicationId);

			// Trying to deploy adhoc services

			Map<String, Collection<ServiceContainer>> mapServiceContainersPerManagerServer = new LinkedHashMap<String, Collection<ServiceContainer>>();

			Map<String, Map<String, PlatformManagedService>> mapManagedServicePerManagerServer = new HashMap<String, Map<String, PlatformManagedService>>();

			// Checking if services already have instances of adhoc services

			String coordinatorServer = DockerServers.SWARM_MANAGER;

			String defaultDeploymentsServers = JsonUtil.encode(Collections.singletonList(coordinatorServer));

			Collection<String> deploymentServers = (Collection<String>) JsonUtil.decode(ExecutionContext.getAttribute(executionContext, ExecutionAttributes.ATT_DEPLOYMENT_ORCHESTRATOR_SERVERS, String.class, defaultDeploymentsServers));

			logger.debug("Using server managers for deployment: " + deploymentServers);

			String managerServer = deploymentServers.iterator().next();

			List<String> serviceNames = serviceClassifications.stream().map(s -> getAdhocServiceNameFromServiceClassification(s)).collect(Collectors.toList());

			Collection<ServiceContainer> alreadyDeployedContainers = new HashSet<ServiceContainer>();
			Collection<PlatformManagedService> alreadyDeployedManagedServices = new HashSet<PlatformManagedService>();

			Map<String, PlatformManagedService> serviceClassificationToManagedService = new HashMap<String, PlatformManagedService>();

			List<PlatformManagedService> managedServices = deploymentService.getPlatformManagedServices(managerServer, serviceNames);

			if (!CollectionUtils.isEmpty(managedServices)) {
				for (PlatformManagedService pms : managedServices) {
					alreadyDeployedContainers.addAll(pms.getContainers());
					alreadyDeployedManagedServices.add(pms);
					serviceClassificationToManagedService.put(pms.getServiceClassification(), pms);
				}
			}

			mapServiceContainersPerManagerServer.put(managerServer, alreadyDeployedContainers);

			mapManagedServicePerManagerServer.put(managerServer, serviceClassificationToManagedService);

			// Only one server (Docker Swarm Manager)
			for (Entry<String, Collection<ServiceContainer>> e : mapServiceContainersPerManagerServer.entrySet()) {

				String deploymentServer = e.getKey();

				Map<String, Collection<ServiceEndpointInfo>> adhocServices = new HashMap<String, Collection<ServiceEndpointInfo>>();
				server2AdhocServices.put(deploymentServer, adhocServices);

				Set<String> servicesClassificationsForAdhocDeploy = new LinkedHashSet<String>();

				server2ServicesClassificationsForAdhocDeploy.put(deploymentServer, servicesClassificationsForAdhocDeploy);

				Map<String, List<ServiceContainer>> serviceClassificationServices = e.getValue().stream().collect(Collectors.groupingBy(ServiceContainer::getServiceClassification));

				for (Entry<String, List<ServiceContainer>> e2 : serviceClassificationServices.entrySet()) {

					String serviceClassification = e2.getKey();
					if (serviceClassifications.contains(serviceClassification)) {

						List<ServiceContainer> serviceContainers = e2.getValue();

						Collection<ServiceEndpointInfo> list = adhocServices.get(serviceClassification);

						if (list == null) {
							list = new LinkedList<ServiceEndpointInfo>();
							adhocServices.put(serviceClassification, list);
						}

						for (ServiceContainer c : serviceContainers) {
							ServiceEndpointInfo eInfo = getServiceEndpointInfoFromServiceContainer(c, deploymentServers, new ArrayList<ServiceContainer>(serviceContainers));
							list.add(eInfo);

						}

						if (!list.isEmpty() && logger.isInfoEnabled()) {
							StringBuilder sb = new StringBuilder("The server '" + deploymentServer + "' already has instances of service with classification '" + serviceClassification + "' at:");

							for (ServiceEndpointInfo info : list) {
								sb.append("\n\t" + info.getServiceEndpointURL());
							}
							logger.info(sb.toString());
						}
					}
				}

				for (String serviceClassification : serviceClassifications) {

					// Check number of desired replicas
					int numberOfReplicas = getNumberOfServiceReplicas(application, serviceClassification);

					boolean notContains = !adhocServices.containsKey(serviceClassification);

					int countReplicas = !notContains ? adhocServices.get(serviceClassification).size() : -1;

					boolean differentNumberOfReplicas = !notContains && countReplicas != numberOfReplicas;

					if (notContains || differentNumberOfReplicas) {

						if (differentNumberOfReplicas && logger.isInfoEnabled()) {
							logger.info("The service with classification " + serviceClassification + " has " + +countReplicas + "/" + numberOfReplicas + " replicas (less than desired)");
						}

						servicesClassificationsForAdhocDeploy.add(serviceClassification);
					} else {
						servicesClassificationsAlreadyDeployed.add(serviceClassification);
					}
				}
			}

			for (String deploymentServer : deploymentServers) {

				Set<String> servicesClassificationsForAdhocDeploy = server2ServicesClassificationsForAdhocDeploy.get(deploymentServer);

				if (!CollectionUtils.isEmpty(servicesClassificationsForAdhocDeploy)) {

					Map<String, PlatformManagedService> serviceClassificationManagedService = mapManagedServicePerManagerServer.get(deploymentServer);

					Map<String, Collection<ServiceEndpointInfo>> adhocServices = server2AdhocServices.get(deploymentServer);

					PlatformManagedServicesDeploymentRequest serviceDeploymentRequest = new PlatformManagedServicesDeploymentRequest();
					serviceDeploymentRequest.setManagerServer(deploymentServer);
					serviceDeploymentRequest.setWaitContainersStartup(true);
					serviceDeploymentRequest.setWaitOnlyFirstReplica(true);

					for (String serviceClassification : servicesClassificationsForAdhocDeploy) {

						PlatformManagedService existingPms = serviceClassificationManagedService.get(serviceClassification);

						if (existingPms == null) {

							int numberOfReplicas = getNumberOfServiceReplicas(application, serviceClassification);

							PlatformManagedServiceDeploymentRequestItem item = new PlatformManagedServiceDeploymentRequestItem();

							item.setServiceName(getAdhocServiceNameFromServiceClassification(serviceClassification));
							item.setServiceClassification(serviceClassification);
							item.setImageName("d-201603244.ufsc.br/ubl-services:latest");
							item.setServicePortMapping(new String[] { "*:8080" });
							item.setPlacementConstraints(Arrays.asList("node.labels.adhoc-ubl-services == 1"));
							item.setReplicas(numberOfReplicas);

							serviceDeploymentRequest.getItems().add(item);

						} else {
							logger.info("The service with classification " + serviceClassification + " already has a managed service installed in " + deploymentServer + " with id " + existingPms.getId());
						}
					}

					if (!serviceDeploymentRequest.getItems().isEmpty()) {

						ServicesDeployment resp = deploymentService.deployPlatformManagedServices(serviceDeploymentRequest);

						if (!resp.isError()) {

							long deploymentDuration = System.currentTimeMillis() - deploymentStartTime;

							logger.info("DeploymentService operation 'deployServices' at server '" + deploymentServer + "' successfully invoked in " + deploymentDuration + " ms");

							synchronized (resilienceInfo) {
								resilienceInfo.setDeploymentTime(resilienceInfo.getDeploymentTime() + deploymentDuration);
							}

							final List<ServiceEndpointInfo> endpointsForCheck = Collections.synchronizedList(new LinkedList<ServiceEndpointInfo>());

							for (PlatformManagedService pms : resp.getManagedServices()) {

								String serviceClassification = null;

								Collection<ServiceEndpointInfo> endpoints = new LinkedList<ServiceEndpointInfo>();

								for (ServiceContainer c : pms.getContainers()) {

									serviceClassification = c.getServiceClassification();

									Collection<ServiceEndpointInfo> list = adhocServices.get(serviceClassification);

									if (list == null) {
										list = new LinkedList<ServiceEndpointInfo>();
										adhocServices.put(serviceClassification, list);
									}

									ServiceEndpointInfo eInfo = getServiceEndpointInfoFromServiceContainer(c, deploymentServers, new ArrayList<ServiceContainer>(pms.getContainers()));
									list.add(eInfo);

									endpoints.add(eInfo);

									endpointsForCheck.add(eInfo);

								}

								if (!endpoints.isEmpty() && logger.isInfoEnabled()) {

									StringBuilder sb = new StringBuilder("Successfully deployed service in server '" + deploymentServer + "' with classification '" + serviceClassification + "'. The endpoint are:");

									for (ServiceEndpointInfo info : endpoints) {
										sb.append("\n\t" + info.getServiceEndpointURL());
									}
									logger.info(sb.toString());
								}

							}

							if (!endpointsForCheck.isEmpty() && BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(executionContext, ExecutionFlags.FLAG_IGNORE_DEPLOYED_SERVICES_CHECK))) {

								int minNumberOfServices = 1;

								Map<String, CountDownLatch> mapLatch = new HashMap<String, CountDownLatch>();

								for (String serviceClassification : servicesClassificationsForAdhocDeploy) {
									mapLatch.put(serviceClassification, new CountDownLatch(minNumberOfServices)); // waiting at least one service for each classification
								}

								logger.info("Checking if at least one deployed services for each of classifications " + servicesClassificationsForAdhocDeploy + " is alive");

								long s = System.currentTimeMillis();

								getThreadPool(application).submit(() -> {
									checkDeployedEndpoints(endpointsForCheck, resilienceInfo, mapLatch);
									return null;
								});

								for (CountDownLatch latch : mapLatch.values()) {
									latch.countDown();
								}

								long servicesCheckDuration = System.currentTimeMillis() - s;

								synchronized (resilienceInfo) {
									resilienceInfo.setServicesCheckTime(resilienceInfo.getServicesCheckTime() + servicesCheckDuration);
								}
							}

						} else {
							logger.error("Error deploying services with classifications '" + servicesClassificationsForAdhocDeploy + "' at deployment server " + deploymentServer + " --> " + resp.getErrorMessage());
						}
					} else {

						boolean waitStable = true;
						boolean checkOnlyZeroReplicas = true;

						if (logger.isInfoEnabled()) {
							logNoNeedToDeployManagedServices(deploymentServer, servicesClassificationsForAdhocDeploy, serviceClassificationManagedService, waitStable);
						}

						if (waitStable) {

							boolean stop = false;

							int maxAttempts = 5;
							int currentAttempt = 1;

							repeat_check: while (!stop) {

								List<PlatformManagedService> pmsToUpdate = new LinkedList<PlatformManagedService>();

								for (String serviceClassification : servicesClassificationsForAdhocDeploy) {

									PlatformManagedService existingPms = serviceClassificationManagedService.get(serviceClassification);

									if (existingPms != null) {

										int desiredNumberOfReplicas = getNumberOfServiceReplicas(application, serviceClassification);
										int currentNumberOfReplicas = ((Collection) ObjectUtils.defaultIfNull(existingPms.getContainers(), Collections.EMPTY_LIST)).size();

										if (checkOnlyZeroReplicas ? currentNumberOfReplicas == 0 : currentNumberOfReplicas < desiredNumberOfReplicas) {
											logger.info("Managed service " + existingPms.getName() + " doesn't have desired number of replicas (" + currentNumberOfReplicas + "/" + desiredNumberOfReplicas + "). Check " + currentAttempt + "/" + maxAttempts);
											pmsToUpdate.add(existingPms);
										}
									}
								}

								if (!pmsToUpdate.isEmpty()) {
									try {
										List<String> pmsToUpdateNames = pmsToUpdate.stream().map(PlatformManagedService::getName).collect(Collectors.toList());
										List<PlatformManagedService> list = deploymentService.getPlatformManagedServices(managerServer, pmsToUpdateNames);
										if (list != null) {
											for (PlatformManagedService pms : list) {
												serviceClassificationManagedService.put(pms.getServiceClassification(), pms);
											}
										}

										if (currentAttempt++ < maxAttempts) {
											Thread.sleep(1000L);
											continue repeat_check;
										}
									} catch (Exception e) {
										logger.error(e.getMessage(), e);
									}
								}

								stop = true;
							}

							long deploymentDuration = System.currentTimeMillis() - deploymentStartTime;

							synchronized (resilienceInfo) {
								resilienceInfo.setDeploymentTime(resilienceInfo.getDeploymentTime() + deploymentDuration);
							}

							List<ServiceEndpointInfo> newEndpoints = new LinkedList<ServiceEndpointInfo>();

							for (String serviceClassification : servicesClassificationsForAdhocDeploy) {

								Collection<ServiceEndpointInfo> currentBoundServices = adhocServices.get(serviceClassification);

								PlatformManagedService existingPms = serviceClassificationManagedService.get(serviceClassification);

								if (existingPms != null && existingPms.getContainers() != null) {
									for (ServiceContainer c : new ArrayList<ServiceContainer>(existingPms.getContainers())) {

										ServiceEndpointInfo s = getServiceEndpointInfoFromServiceContainer(c, deploymentServers, new ArrayList(existingPms.getContainers()));

										if (currentBoundServices != null) {

											Set<String> currentEndpointsUrls = currentBoundServices.stream().map(ServiceEndpointInfo::getServiceEndpointURL).collect(Collectors.toSet());

											if (!currentEndpointsUrls.contains(c.getServiceEndpointURL())) {
												logger.info("Adding adhoc service with endpoint " + c.getServiceEndpointURL() + " to adhoc services list for classification " + serviceClassification);
												currentBoundServices.add(s);
											}
										}
									}
								}
							}

							if (!newEndpoints.isEmpty() && BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(executionContext, ExecutionFlags.FLAG_IGNORE_DEPLOYED_SERVICES_CHECK))) {
								checkDeployedEndpoints(newEndpoints, resilienceInfo, null);
							}
						}
					}
				}
			}

		}
		return server2AdhocServices;

	}

	private void logNoNeedToDeployManagedServices(String deploymentServer, Set<String> servicesClassificationsForAdhocDeploy, Map<String, PlatformManagedService> serviceClassificationManagedService, boolean waitStable) {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("No need to deploy managed services at " + deploymentServer + (waitStable ? ", waiting associated containers to be stable" : ""));

			for (String serviceClassification : servicesClassificationsForAdhocDeploy) {

				PlatformManagedService existingPms = serviceClassificationManagedService.get(serviceClassification);

				if (existingPms != null) {
					sb.append("\n\t" + serviceClassification);

					if (!CollectionUtils.isEmpty(existingPms.getContainers())) {
						for (ServiceContainer c : existingPms.getContainers()) {
							sb.append("\n\t\t" + c.getServiceEndpointURL());
						}
					}
				}
			}

			logger.info(sb.toString());
		}
	}

	private int getNumberOfServiceReplicas(final SOAApplication application, String serviceClassification) {
		ServiceConfig servicesCfg = application.getServicesInformation().getServiceConfig(serviceClassification);
		int numberOfReplicas = Math.max((int) ObjectUtils.defaultIfNull(servicesCfg.getNumberOfReplicas(), 2), 1);
		return numberOfReplicas;
	}

	private static String getAdhocServiceNameFromServiceClassification(String serviceClassification) {

		String suffix = serviceClassification.toLowerCase().replaceAll("/", "-");
		String[] parts = suffix.split("-");
		suffix = parts[0] + "-" + parts[1] + "-" + StringUtils.join((String[]) ArrayUtils.subarray(parts, parts.length - 2, parts.length), "-");

		return "adhoc-" + suffix;
	}

	// @SuppressWarnings({ "unchecked", "unused" })
	// private Map<String, Map<String, Collection<ServiceEndpointInfo>>> deployAdhocServicesLegacy(final ExecutorService pool, final SOAApplication application, final ResilienceInfo resilienceInfo, final Set<String> serviceClassifications) throws
	// InterruptedException {
	//
	// final String applicationId = application.getApplicationId();
	//
	// final ExecutionContext executionContext = application.getExecutionContext();
	//
	// final Map<String, Map<String, Collection<ServiceEndpointInfo>>> server2AdhocServices = new HashMap<String, Map<String, Collection<ServiceEndpointInfo>>>();
	//
	// boolean useServiceDeployment = BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(executionContext, ExecutionFlags.FLAG_DISABLE_ADHOC_SERVICE_DEPLOYMENT));
	//
	// if (!useServiceDeployment || serviceClassifications.isEmpty()) {
	//
	// if (!useServiceDeployment) {
	// logger.debug("Flag DISABLE_ADHOC_SERVICE_DEPLOYMENT set. Ignoring adhoc service deployment");
	// }
	//
	// return server2AdhocServices;
	//
	// } else {
	//
	// final Map<String, Set<String>> server2ServicesClassificationsForAdhocDeploy = new HashMap<String, Set<String>>();
	//
	// final Map<String, Integer> server2InitialPort = new HashMap<String, Integer>();
	//
	// final Map<String, Future<ServicesDeployment>> servicesDeploymentFutures = new LinkedHashMap<String, Future<ServicesDeployment>>();
	//
	// Set<String> servicesClassificationsAlreadyDeployed = new LinkedHashSet<String>();
	//
	// long deploymentStartTime = System.currentTimeMillis();
	//
	// logger.info("The following classifications don't have any bound service: " + serviceClassifications + ". Trying to deploy adhoc services for applicationId " + applicationId);
	//
	// // Trying to deploy adhoc services
	//
	// Map<String, Future<List<ServiceContainer>>> serviceContainersFutures = new LinkedHashMap<String, Future<List<ServiceContainer>>>();
	//
	// // Checking if services already have instances of adhoc services
	//
	// String defaultDeploymentsServers = JsonUtil.encode(DockerServers.getServerForAdhocDeployment());
	//
	// Collection<String> deploymentServers = (Collection<String>) JsonUtil.decode(ExecutionContext.getAttribute(executionContext, ExecutionAttributes.ATT_DEPLOYMENT_SERVERS, String.class, defaultDeploymentsServers));
	//
	// logger.debug("Using servers for deployment: " + deploymentServers);
	//
	// for (String deploymentServer : deploymentServers) {
	// serviceContainersFutures.put(deploymentServer, pool.submit(new Callable<List<ServiceContainer>>() {
	// @Override
	// public List<ServiceContainer> call() throws Exception {
	// return deploymentService.getServiceContainersByDeploymentServer(deploymentServer); // TODO: make it generic;
	// }
	// }));
	// }
	//
	// for (Entry<String, Future<List<ServiceContainer>>> e : serviceContainersFutures.entrySet()) {
	//
	// String deploymentServer = e.getKey();
	// Future<List<ServiceContainer>> f = e.getValue();
	//
	// Map<String, Collection<ServiceEndpointInfo>> adhocServices = new HashMap<String, Collection<ServiceEndpointInfo>>();
	// Set<String> servicesClassificationsForAdhocDeploy = new LinkedHashSet<String>();
	//
	// server2AdhocServices.put(deploymentServer, adhocServices);
	// server2ServicesClassificationsForAdhocDeploy.put(deploymentServer, servicesClassificationsForAdhocDeploy);
	//
	// int initialPort = ServicesAddresses.ADHOC_SERVICES_INITIAL_PORT;
	//
	// List<ServiceContainer> serviceContainers = null;
	//
	// try {
	// serviceContainers = f.get();
	// } catch (Throwable ex) {
	// logger.error(ex.getMessage(), ex);
	// continue;
	// }
	//
	// if (serviceContainers != null) {
	// for (ServiceContainer c : serviceContainers) {
	//
	// initialPort = Math.max(c.getServicePort(), initialPort);
	//
	// String serviceClassification = c.getServiceClassification();
	//
	// if (serviceClassifications.contains(serviceClassification)) {
	//
	// Collection<ServiceEndpointInfo> list = adhocServices.get(serviceClassification);
	//
	// if (list == null) {
	// list = new LinkedList<ServiceEndpointInfo>();
	// adhocServices.put(serviceClassification, list);
	// }
	//
	// ServiceEndpointInfo eInfo = getServiceEndpointInfoFromServiceContainer(c, deploymentServers, Collections.EMPTY_LIST);
	// list.add(eInfo);
	//
	// logger.info("The server '" + deploymentServer + "' already has a instance of service with classification '" + eInfo.getServiceClassification() + "' at " + eInfo.getServiceEndpointURL() + ".");
	//
	// }
	// }
	// }
	//
	// for (String serviceClassification : serviceClassifications) {
	// if (!adhocServices.containsKey(serviceClassification)) {
	// servicesClassificationsForAdhocDeploy.add(serviceClassification);
	// } else {
	// servicesClassificationsAlreadyDeployed.add(serviceClassification);
	// }
	// }
	//
	// server2InitialPort.put(deploymentServer, initialPort);
	// }
	//
	// boolean needToWait = !servicesClassificationsAlreadyDeployed.containsAll(serviceClassifications);
	//
	// CountDownLatch firstDeployment = needToWait ? new CountDownLatch(1) : null;
	//
	// final AtomicInteger serverCount = new AtomicInteger(0);
	//
	// for (String deploymentServer : deploymentServers) {
	//
	// serverCount.incrementAndGet();
	//
	// Set<String> servicesClassificationsForAdhocDeploy = server2ServicesClassificationsForAdhocDeploy.get(deploymentServer);
	//
	// if (!CollectionUtils.isEmpty(servicesClassificationsForAdhocDeploy)) {
	//
	// servicesDeploymentFutures.put(deploymentServer, pool.submit(new Callable<ServicesDeployment>() {
	//
	// @Override
	// public ServicesDeployment call() throws Exception {
	//
	// try {
	//
	// Integer initialPort = server2InitialPort.get(deploymentServer);
	//
	// Map<String, Collection<ServiceEndpointInfo>> adhocServices = server2AdhocServices.get(deploymentServer);
	//
	// final AtomicInteger w = new AtomicInteger(initialPort);
	//
	// ServicesDeploymentRequest serviceDeploymentRequest = new ServicesDeploymentRequest();
	// serviceDeploymentRequest.setChangePortMappingIfNecessary(true);
	// serviceDeploymentRequest.setDeploymentServer(deploymentServer);
	//
	// for (String serviceClassification : servicesClassificationsForAdhocDeploy) {
	//
	// ServiceDeploymentRequestItem item = new ServiceDeploymentRequestItem();
	//
	// int c = serverCount.get();
	//
	// // TODO: more services instances per service classification
	// String containerName = "adhoc-" + serviceClassification.toLowerCase().replaceAll("/", "-") + "-1";
	// String serviceHost = "adhoc" + c + "-" + serviceClassification.toLowerCase().replaceAll("/", "-") + "-1";
	//
	// item.setContainerName(containerName);
	// item.setImageName("d-201603244.ufsc.br/ubl-services:latest");
	//
	// item.setServiceClassification(serviceClassification);
	//
	// // TODO: improve...
	// item.getContainerVariables().put(ServiceContainer.PARAM_SERVICE_HOST, serviceHost);
	// item.getContainerVariables().put(ServiceContainer.PARAM_SERVICE_PATH, serviceClassification);
	//
	// int port = w.getAndIncrement();
	//
	// item.setContainerPortMapping(new String[] { ServicesAddresses.DEFAULT_HTTP_PORT + ":" + port });
	// item.getContainerVariables().put(ServiceContainer.PARAM_SERVICE_PORT, String.valueOf(port));
	//
	// serviceDeploymentRequest.getItens().add(item);
	// }
	//
	// ServicesDeployment resp = deploymentService.deployServices(serviceDeploymentRequest);
	//
	// if (!resp.isError()) {
	//
	// long deploymentDuration = System.currentTimeMillis() - deploymentStartTime;
	//
	// logger.info("DeploymentService operation 'deployServices' at server '" + deploymentServer + "' successfully invoked in " + deploymentDuration + " ms");
	//
	// synchronized (resilienceInfo) {
	// resilienceInfo.setDeploymentTime(resilienceInfo.getDeploymentTime() + deploymentDuration);
	// }
	//
	// final List<ServiceEndpointInfo> endpointsForCheck = Collections.synchronizedList(new LinkedList<ServiceEndpointInfo>());
	//
	// for (ServiceContainer c : resp.getServiceContainers()) {
	//
	// String serviceClassification = c.getServiceClassification();
	//
	// Collection<ServiceEndpointInfo> list = adhocServices.get(serviceClassification);
	//
	// if (list == null) {
	// list = new LinkedList<ServiceEndpointInfo>();
	// adhocServices.put(serviceClassification, list);
	// }
	//
	// ServiceEndpointInfo eInfo = getServiceEndpointInfoFromServiceContainer(c, deploymentServers, Collections.EMPTY_LIST);
	// list.add(eInfo);
	//
	// endpointsForCheck.add(eInfo);
	//
	// logger.info("Successfully deployed service in server '" + deploymentServer + "' with classification '" + eInfo.getServiceClassification() + "'. The endpoint is " + eInfo.getServiceEndpointURL());
	// }
	//
	// if (!endpointsForCheck.isEmpty() && BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(executionContext, ExecutionFlags.FLAG_IGNORE_DEPLOYED_SERVICES_CHECK))) {
	// checkDeployedEndpoints(endpointsForCheck, resilienceInfo, null);
	// }
	//
	// } else {
	// logger.error("Error deploying services with classifications '" + servicesClassificationsForAdhocDeploy + "' at deployment server " + deploymentServer + " --> " + resp.getErrorMessage());
	// }
	//
	// return resp;
	//
	// } finally {
	// if (needToWait)
	// firstDeployment.countDown();
	// }
	// }
	// }));
	// }
	// }
	//
	// if (needToWait) {
	//
	// firstDeployment.await();
	//
	// // Waiting a little bit for remaining deployment servers..
	// for (
	//
	// Future<ServicesDeployment> f : servicesDeploymentFutures.values()) {
	// if (!f.isDone()) {
	// try {
	// f.get(500, TimeUnit.MILLISECONDS);
	// } catch (Throwable e) {
	// logger.warn("Erro waiting for service deployment job: " + e.getClass().getName());
	// }
	// }
	// }
	// }
	// }
	// return server2AdhocServices;
	// }

	private ServicesCheckResult checkDeployedEndpoints(final List<ServiceEndpointInfo> endpointsForCheck, final ResilienceInfo resilienceInfo, final Map<String, CountDownLatch> mapCountDownLatch) throws InterruptedException {

		try {

			logger.info("Checking if deployed services are alive");

			long s = System.currentTimeMillis();

			Map<String, List<ServiceEndpointInfo>> groupByServiceEndpoint = endpointsForCheck.stream().collect(Collectors.groupingBy(ServiceEndpointInfo::getServiceEndpointURL));

			List<ServiceEndpointInfo> endpoints = new ArrayList<ServiceEndpointInfo>(endpointsForCheck);

			Set<String> serviceClassifications = endpointsForCheck.stream().map(ServiceEndpointInfo::getServiceClassification).collect(Collectors.toSet());

			ServicesCheckResult checkResult = resilienceService.checkServicesAvailable(endpoints);

			int attempt = 0;

			boolean allServicesAvailable = checkResult.getUnavailableServices().size() == 0;

			boolean atLeastOneServiceAvailable = false;

			int maxAttempts = ResilienceParams.SERVICE_CHECK_MAX_ATTEMPTS;

			Set<String> allAvailableServiceEndpointURLs = new LinkedHashSet<String>();

			while (!atLeastOneServiceAvailable && (++attempt < maxAttempts)) {

				Thread.sleep(ResilienceParams.SERVICE_CHECK_ATTEMPT_DELAY);

				final List<String> availableServices = checkResult.getAvailableServices();

				endpoints = endpointsForCheck.stream().filter(x -> !availableServices.contains(x.getServiceKey())).collect(Collectors.toList());

				checkResult = resilienceService.checkServicesAvailable(endpoints);

				allServicesAvailable = checkResult.getUnavailableServices().size() == 0;

				allAvailableServiceEndpointURLs.addAll(checkResult.getAvailableServiceEndpointURLs());

				List<String> lServiceClassifications = new ArrayList<String>(serviceClassifications);

				stop_at_least_one_check: for (String url : allAvailableServiceEndpointURLs) {
					for (ServiceEndpointInfo se : groupByServiceEndpoint.get(url)) {
						lServiceClassifications.remove(se.getServiceClassification());

						if (lServiceClassifications.isEmpty()) {
							atLeastOneServiceAvailable = true;
							break stop_at_least_one_check;
						}

					}
				}

				if (!MapUtils.isEmpty(mapCountDownLatch)) {
					for (String url : checkResult.getAvailableServiceEndpointURLs()) {
						for (ServiceEndpointInfo se : groupByServiceEndpoint.get(url)) {
							CountDownLatch latch = mapCountDownLatch.get(se.getServiceClassification());
							if (latch != null) {
								latch.countDown();
							}
						}
					}
				}
			}

			long servicesCheckDuration = System.currentTimeMillis() - s;

			synchronized (resilienceInfo) {
				resilienceInfo.setServicesCheckTime(resilienceInfo.getServicesCheckTime() + servicesCheckDuration);
			}

			if (allServicesAvailable) {

				StringBuilder sb = new StringBuilder("Deployed services are ALIVE. The checking took " + servicesCheckDuration + " ms" + (attempt > 0 ? " in " + attempt + " attempt(s)" : "") + " for:");

				for (String servicesClassification : endpointsForCheck.stream().map(e -> e.getServiceClassification()).collect(Collectors.toList())) {
					sb.append("\n\t" + servicesClassification);
				}

				logger.info(sb.toString());

			} else {

				StringBuilder sb = new StringBuilder("Some deployed services are still UNAVAILABLE. The checking took " + servicesCheckDuration + " ms" + (attempt > 0 ? " in " + attempt + " attempt(s)" : "") + " for:");

				for (String serviceKey : checkResult.getUnavailableServices()) {
					sb.append("\n\t" + serviceKey);
				}

				logger.info(sb.toString());
			}

			return checkResult;

		} finally {
			if (!MapUtils.isEmpty(mapCountDownLatch)) {
				for (CountDownLatch latch : mapCountDownLatch.values()) {
					if (latch.getCount() == 0) {
						latch.countDown();
					}
				}
			}
		}
	}

	private Map<String, Collection<ServiceEndpointInfo>> getNotMonitoredServices(final ServicesInformation servicesInformation) {
		final Map<String, Collection<ServiceEndpointInfo>> notMonitoredServices = new HashMap<String, Collection<ServiceEndpointInfo>>();

		for (ServiceConfig serviceConfig : servicesInformation.getServicesConfig()) {
			if (ResilienceUtil.isServiceMonitoringDisabled(serviceConfig)) {

				String serviceClassification = serviceConfig.getClassification();

				Collection<ServiceEndpointInfo> services = new ArrayList<ServiceEndpointInfo>();

				for (ServiceAssociation s : serviceConfig.getServiceAssociations()) {
					services.add(ResilienceUtil.getServiceEndpointInfo(serviceClassification, serviceConfig.getNamespace(), s));
				}
				notMonitoredServices.put(serviceClassification, services);
			}
		}
		return notMonitoredServices;
	}

	private ServiceEndpointInfo getServiceEndpointInfoFromServiceContainer(ServiceContainer c, Collection<String> deploymentServers, List<ServiceContainer> hostContainers) {

		boolean isSwarm = c.getServiceId() != null;

		String serviceNamespace = DiscoveryUtil.serviceClassificationToNamespace(c.getServiceClassification());

		String serviceKey = null;

		String serviceProviderName = null;

		String serviceEndpointURL = null;

		String sanitizedDeploymentServer = c.getDeploymentServer();
		sanitizedDeploymentServer = StringUtils.replace(sanitizedDeploymentServer, "_", "-");
		sanitizedDeploymentServer = StringUtils.replace(sanitizedDeploymentServer, ".", "-");

		if (isSwarm) {

			Collections.sort(hostContainers, new Comparator<ServiceContainer>() {
				@Override
				public int compare(ServiceContainer s1, ServiceContainer s2) {
					return ObjectUtils.compare(s1.getServicePort(), s1.getServicePort());
				}
			});

			serviceKey = "adhoc:" + sanitizedDeploymentServer + ":" + (hostContainers.indexOf(c) + 1);
			serviceProviderName = "provider-" + sanitizedDeploymentServer;
			serviceEndpointURL = c.getServiceEndpointURL(c.getDeploymentServer());

		} else {

			String hostname = null;

			int i = 0;

			for (String dockerServer : deploymentServers) {
				if (dockerServer.equals(c.getDeploymentServer())) {
					break;
				}
				i++;
			}

			if (i > 0) {
				hostname = ServicesAddresses.ADHOC_SERVICES_SUBDOMAIN + (i + 1) + "." + ServicesAddresses.BASE_DOMAIN;
			} else {
				hostname = ServicesAddresses.ADHOC_SERVICES_SUBDOMAIN + "." + ServicesAddresses.BASE_DOMAIN;
			}

			serviceEndpointURL = c.getServiceEndpointURL(hostname);

			serviceKey = "adhoc:" + sanitizedDeploymentServer + ":" + c.getContainerName();

			serviceProviderName = "provider-" + sanitizedDeploymentServer;
		}

		// TODO: QoS and UDDI keys stuff...
		ServiceEndpointInfo eInfo = new ServiceEndpointInfo(serviceProviderName, c.getServiceClassification(), serviceNamespace, serviceKey, serviceEndpointURL, true);

		eInfo.getQoSValues().put("Availability.Availability", 0.98D);
		eInfo.getQoSValues().put("Cost.ExecutionCost", 330D);
		eInfo.getQoSValues().put("Performance.ResponseTime", 150D);
		eInfo.getQoSValues().put("Performance.Throughput", 85D);

		return eInfo;
	}

}
