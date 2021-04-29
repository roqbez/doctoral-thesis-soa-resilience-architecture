package br.ufsc.gsigma.services.resilience.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufsc.gsigma.binding.events.BindingConfiguredEvent;
import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.catalog.services.model.ServiceConfig;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;
import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.infrastructure.util.thread.NamedThreadFactory;
import br.ufsc.gsigma.infrastructure.ws.HostAddressUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.infrastructure.ws.model.NodeInfo;
import br.ufsc.gsigma.services.events.ServiceStartedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessDeployedEvent;
import br.ufsc.gsigma.services.execution.events.ProcessUndeployedEvent;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.interfaces.ResilienceService;
import br.ufsc.gsigma.services.resilience.model.ServicesCheckResult;
import br.ufsc.gsigma.services.resilience.support.InfinispanCaches;
import br.ufsc.gsigma.services.resilience.support.InfinispanManyToMany;
import br.ufsc.gsigma.services.resilience.support.InfinispanSupport;
import br.ufsc.gsigma.services.resilience.support.ResilienceUtil;
import br.ufsc.gsigma.services.resilience.support.SOAApplication;
import br.ufsc.gsigma.services.resilience.support.SOAApplicationInstance;
import br.ufsc.gsigma.services.resilience.support.SOAService;
import br.ufsc.gsigma.services.resilience.support.SOAServiceCheck;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

@Service
public class ResilienceServiceImpl implements ResilienceService, ResilienceServiceInternal {

	private static final Logger logger = Logger.getLogger(ResilienceServiceImpl.class);

	private static ResilienceServiceImpl INSTANCE;

	public static ResilienceServiceImpl getInstance() {
		return INSTANCE;
	}

	@Autowired
	private ResilienceMonitoring monitoring;

	@Autowired
	private InfinispanCaches caches;

	@Autowired
	private InfinispanSupport infinispanSupport;

	@Autowired
	private ResilienceAnalysis analysis;

	private ExecutorService pool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newFixedThreadPool(8, new NamedThreadFactory("resilience-servicescheck-pool")));

	private Map<String, SOAApplication> soaApplications;

	private Map<String, SOAService> soaServices;

	private InfinispanManyToMany<String, String> servicesToApplications;

	@Override
	public Object getLockMonitor() {
		return servicesToApplications;
	}

	@PostConstruct
	public void setup() throws Exception {

		INSTANCE = this;

		this.soaApplications = caches.getSOAApplications();
		this.soaServices = caches.getSOAServices();

		this.monitoring.addListener(new ServiceMonitorListener() {
			@Override
			public void serviceAvailable(SOAServiceCheck serviceCheck) {
				handleServiceAvailable(serviceCheck);
			}

			@Override
			public void serviceUnavailable(SOAServiceCheck service) {
				handleServiceUnavailable(service);
			}
		});

		this.servicesToApplications = new InfinispanManyToMany<>(caches.getServiceToApplicationsMultimap(), caches.getApplicationToServicesMultimap());
	}

	@Override
	public String getPublicIp() {
		return HostAddressUtil.getPublicIp();
	}

	@Override
	public NodeInfo getNodeInfo() {
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		return new NodeInfo(infinispanSupport.getMyAddress().toString(), DockerContainerUtil.getContainerId(), getPublicIp(), hostName, infinispanSupport.isLeader());
	}

	@Override
	public Collection<SOAApplication> getSOAApplications(SOAService service) {

		Collection<String> ids = getSOAApplicationIds(service);

		List<SOAApplication> result = new ArrayList<>(ids.size());

		for (String id : ids) {
			result.add(soaApplications.get(id));
		}

		return result;
	}

	@Override
	public Collection<String> getSOAApplicationIds(SOAService service) {
		return servicesToApplications.getValues(service.getServiceKey());
	}

	@Override
	public SOAApplication getSOAApplication(String applicationId) {
		return soaApplications.get(applicationId);
	}

	@Override
	public List<String> getSOAApplicationIds() {
		return new ArrayList<String>(new TreeSet<String>(soaApplications.keySet()));
	}

	public Collection<SOAService> getSOAServices(SOAApplication application) {

		Collection<String> ids = servicesToApplications.getKeys(application.getApplicationId());

		List<SOAService> result = new ArrayList<>(ids.size());

		for (String id : ids) {
			SOAService s = soaServices.get(id);
			if (s != null) {
				result.add(s);
			}
		}

		return result;
	}

	private void handleServiceAvailable(SOAServiceCheck serviceCheck) {
		handleServicesAvailable(Collections.singleton(serviceCheck.getService()), true);
	}

	private void handleServicesAvailable(Collection<SOAService> services, boolean isFromMonitoring) {

		if (infinispanSupport.isLeader()) {

			Map<String, List<SOAService>> groupedByServiceNamespace = services.stream().collect(Collectors.groupingBy(SOAService::getServiceNamespace, Collectors.toList()));

			for (SOAApplication application : soaApplications.values()) {

				boolean monitoringEnabled = BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(application.getExecutionContext(), ExecutionFlags.FLAG_DISABLE_SERVICE_MONITORING));

				if (!isFromMonitoring || monitoringEnabled) {

					for (Entry<String, List<SOAService>> e : groupedByServiceNamespace.entrySet()) {
						final String serviceNamespace = e.getKey();
						final List<SOAService> namespaceServices = e.getValue();
						if (ResilienceUtil.isApplicationWithoutBoundServices(application, serviceNamespace)) {
							analysis.analyzeServicesAvailable(application, namespaceServices);
						}
					}
				}
			}
		}
	}

	private void handleServiceUnavailable(SOAServiceCheck serviceCheck) {
		analysis.analyzeServiceUnavailable(serviceCheck.getService());
	}

	@Override
	public void receiveBusEvent(Event event) throws Exception {

		try {

			if (event instanceof ProcessDeployedEvent) {

				if (infinispanSupport.isLeader()) {

					ProcessDeployedEvent processDeployedEvent = (ProcessDeployedEvent) event;

					String applicationId = processDeployedEvent.getApplicationId();

					SOAApplication application = soaApplications.get(applicationId);

					ExecutionContext executionContext = event.getExecutionContext();

					if (application == null) {
						application = new SOAApplication(applicationId, executionContext);
						logger.info("SOA Application created --> " + application //
								+ "\n\texecutionContext=" + ReflectionToStringBuilder.toString(executionContext));
					} else {
						logger.info("SOA Application updated --> " + application //
								+ "\n\texecutionContext=" + ReflectionToStringBuilder.toString(executionContext));
					}

					ServicesInformation servicesInformation = processDeployedEvent.getServicesInformation();

					application.setBusinessProcess(processDeployedEvent.getBusinessProcess());
					application.setServicesInformation(servicesInformation);
					application.setExecutionContext(executionContext);

					boolean monitoringEnabled = BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(executionContext, ExecutionFlags.FLAG_DISABLE_SERVICE_MONITORING));

					if (!monitoringEnabled) {
						logger.info("Monitoring is disabled for SOA Application --> " + application);
					}

					soaApplications.put(applicationId, application);

					// Trying to discover initial services if there aren't any bound
					if (!servicesInformation.hasAnyBoundService()) {
						analysis.analyzeSOAApplication(application);
					}

				}

				// Stopping monitoring application
			} else if (event instanceof ProcessUndeployedEvent) {

				if (infinispanSupport.isLeader()) {

					ProcessUndeployedEvent processUndeployedEvent = (ProcessUndeployedEvent) event;

					String applicationId = processUndeployedEvent.getApplicationId();

					SOAApplication application = soaApplications.get(applicationId);

					if (application != null) {

						stopMonitoringSOAApplication(application);

						soaApplications.remove(applicationId);

						logger.info("SOA Application removed --> " + application);
					}

				}

				// } else if (event instanceof ProcessServiceBindingConfigured) {
				//
				// ProcessServiceBindingConfigured bindingEvent = (ProcessServiceBindingConfigured) event;
				// String applicationId = bindingEvent.getApplicationId();
				//
				// SOAApplication application = soaApplications.get(applicationId);
				//
				// if (application != null) {
				// synchronized (application) {
				// application.setBindingConfiguration(bindingEvent.getBindingConfiguration());
				// application.updateCache();
				// }
				// logger.info("SOA Application binding configured at process deployment --> " + application);
				// monitorSOAApplication(application);
				// }

			} else if (event instanceof BindingConfiguredEvent) {

				BindingConfiguredEvent bindingEvent = (BindingConfiguredEvent) event;

				BindingConfiguration bindingConfiguration = bindingEvent.getConfiguration();

				String applicationId = bindingConfiguration.getApplicationId();

				SOAApplication application = soaApplications.get(applicationId);

				if (application != null) {
					synchronized (application) {
						application.setBindingConfiguration(bindingConfiguration);
						if (infinispanSupport.isLeader()) {
							application.updateCacheAsync();
						}
					}
					logger.info("SOA Application binding configured --> " + application);
					monitorSOAApplication(application);
				}

			} else if (event instanceof ServiceStartedEvent) {

				if (infinispanSupport.isLeader()) {

					for (SOAApplication application : soaApplications.values()) {
						final String serviceNamespace = ((ServiceStartedEvent) event).getServiceNamespace();
						if (ResilienceUtil.isApplicationWithoutBoundServices(application, serviceNamespace)) {
							analysis.analyzeSOAApplication(application);
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public SOAApplicationInstance getSOAApplicationInstance(SOAApplication application, Long processInstanceId) {

		Cache<String, SOAApplicationInstance> cache = caches.getSOAApplicationInstances();

		String key = SOAApplicationInstance.getKey(application.getApplicationId(), processInstanceId);

		SOAApplicationInstance applicationInstance = cache.get(key);

		if (applicationInstance == null) {

			synchronized (application) {

				applicationInstance = cache.get(key);

				if (applicationInstance == null) {
					applicationInstance = new SOAApplicationInstance(application.getApplicationId(), processInstanceId);
					cache.put(key, applicationInstance);
				}
			}
		}

		return applicationInstance;
	}

	@Override
	public List<SOAApplicationInstance> getSOAApplicationInstances(String applicationId, boolean onlyRunning) {

		Cache<String, SOAApplicationInstance> cache = caches.getSOAApplicationInstances();

		List<SOAApplicationInstance> list = new LinkedList<SOAApplicationInstance>();

		for (SOAApplicationInstance instance : cache.values()) {

			if (instance.getApplicationId().equals(applicationId) && (!onlyRunning || !instance.isComplete())) {
				list.add(instance);
			}
		}

		return list;
	}

	@Override
	public void monitorSOAApplications(Collection<SOAApplication> applications) {
		synchronized (servicesToApplications) {
			for (SOAApplication application : applications) {
				monitorSOAApplication(application, application.getBindingConfiguration());
			}
		}
	}

	@Override
	public void monitorSOAApplication(SOAApplication application) {
		monitorSOAApplication(application, application.getBindingConfiguration());
	}

	public void monitorSOAApplication(SOAApplication application, BindingConfiguration bindingConfiguration) {
		monitorSOAApplication(application, bindingConfiguration != null ? bindingConfiguration.getAllServices() : null);
	}

	@Override
	public void monitorSOAApplication(SOAApplication application, Collection<ServiceEndpointInfo> boundServices) {
		handleMonitoringSOAApplication(application, boundServices, false);
	}

	@Override
	public void stopMonitoringSOAApplication(SOAApplication application) {
		handleMonitoringSOAApplication(application, application.getBindingConfiguration() != null ? application.getBindingConfiguration().getAllServices() : null, true);
	}

	private void handleMonitoringSOAApplication(SOAApplication application, Collection<ServiceEndpointInfo> boundServices, boolean applicationRemoved) {

		Collection<SOAService> currentBoundServices = new LinkedHashSet<SOAService>();

		Collection<SOAService> noLongerUsedServices = new LinkedHashSet<SOAService>();

		synchronized (servicesToApplications) {

			if (boundServices != null) {

				Collection<String> ignoreMonitoringServiceClassifications = new HashSet<String>();

				for (ServiceConfig cfg : application.getServicesInformation().getServicesConfig()) {
					if (ResilienceUtil.isServiceMonitoringDisabled(cfg)) {
						ignoreMonitoringServiceClassifications.add(cfg.getClassification());
					}
				}

				if (!ignoreMonitoringServiceClassifications.isEmpty()) {
					logger.info("Ignoring service monitoring for classifications: " + ignoreMonitoringServiceClassifications);
				}

				if (!applicationRemoved) {

					List<ServiceEndpointInfo> allServices = new LinkedList<ServiceEndpointInfo>(boundServices);

					ListIterator<ServiceEndpointInfo> it = allServices.listIterator();

					while (it.hasNext()) {
						ServiceEndpointInfo service = it.next();
						if (!ignoreMonitoringServiceClassifications.isEmpty() && ignoreMonitoringServiceClassifications.contains(service.getServiceClassification())) {
							it.remove();
						}
					}

					currentBoundServices.addAll(getSOAServices(allServices));

					if (!isSOAApplicationMonitored(application)) {
						logger.info("Monitoring SOA Application --> " + application //
								+ (application.getExecutionContext() != null ? "\n\tattributes=" + application.getExecutionContext().getAttributes() : "") + (application.getExecutionContext() != null ? "\n\tflags=" + application.getExecutionContext().getFlags() : "") //
						);
					}
				}

				// Getting no longer bound services

				Collection<SOAService> previousBoundServices = getSOAServices(application);

				for (SOAService service : previousBoundServices) {
					if (!currentBoundServices.contains(service)) {

						logger.info("The service at " + service.getServiceEndpointURL() + " is no longer bound to " + application);
						servicesToApplications.remove(service.getServiceKey(), application.getApplicationId());

						// The service is no longer used in any application, so remove it from monitoring
						if (getSOAApplicationIds(service).isEmpty()) {
							noLongerUsedServices.add(service);
						}
					}
				}

				// Adding new ones
				for (SOAService service : currentBoundServices) {
					servicesToApplications.put(service.getServiceKey(), application.getApplicationId());
				}
			}
		}

		if (!currentBoundServices.isEmpty() || !noLongerUsedServices.isEmpty()) {
			monitoring.submitChangeServicesMonitoring(currentBoundServices, noLongerUsedServices);
		}
	}

	private boolean isSOAApplicationMonitored(SOAApplication application) {
		synchronized (servicesToApplications) {
			return !servicesToApplications.getKeys(application.getApplicationId()).isEmpty();
		}
	}

	@Override
	public ServicesCheckResult checkServicesEndpointsAvailable(List<String> serviceEndpointURLs, String serviceNamespace) {

		Collection<SOAService> soaServices = new ArrayList<SOAService>(serviceEndpointURLs.size());

		for (String serviceEndpointURL : serviceEndpointURLs) {
			soaServices.add(new SOAService(serviceEndpointURL, serviceNamespace));
		}

		return checkServicesAvailableInternal(soaServices);
	}

	@Override
	public ServicesCheckResult checkServicesAvailable(List<ServiceEndpointInfo> services) {
		return checkServicesAvailableInternal(getSOAServices(services));
	}

	private ServicesCheckResult checkServicesAvailableInternal(Collection<SOAService> soaServices) {

		ServicesCheckResult result = new ServicesCheckResult();

		List<Future<Void>> futures = new LinkedList<Future<Void>>();

		final int connectionTimeout = ExecutionContext.getAttribute(ExecutionAttributes.ATT_RESILIENCE_SERVICE_CHECK_HTTP_CONNECTION_TIMEOUT, Integer.class, ResilienceParams.DEFAULT_HTTP_CONNECTION_TIMEOUT);

		final int readTimeout = ExecutionContext.getAttribute(ExecutionAttributes.ATT_RESILIENCE_SERVICE_CHECK_HTTP_READ_TIMEOUT, Integer.class, ResilienceParams.DEFAULT_HTTP_READ_TIMEOUT);

		Map<String, List<SOAService>> groupByEndpoint = soaServices.stream().collect(Collectors.groupingBy(SOAService::getServiceEndpointURL));

		for (Entry<String, List<SOAService>> e : groupByEndpoint.entrySet()) {

			String endpoint = e.getKey();

			List<SOAService> listSoaServices = e.getValue();

			boolean someToCheck = false;

			for (SOAService s : listSoaServices) {

				Boolean wasUnavailable = s.getAvailable() != null && !s.getAvailable();

				if (wasUnavailable && (System.currentTimeMillis() - s.getLastAvailabilityCheck().getTime()) < ResilienceParams.DEFAULT_SERVICE_UNAVAILABLE_CHECK_DIFF_MILLIS) {
					result.getUnavailableServices().add(s.getServiceKey());
					continue;
				}

				someToCheck = true;

			}

			if (someToCheck) {

				SOAService s = listSoaServices.get(0);

				Future<Void> f = pool.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {

						if (ResilienceUtil.isServiceAlive(logger, endpoint, s.getServiceNamespace(), connectionTimeout, readTimeout)) {
							List<String> availableServices = result.getAvailableServices();
							synchronized (availableServices) {
								for (SOAService s : listSoaServices) {
									if (s.getServiceKey() != null) {
										availableServices.add(s.getServiceKey());
									}
									if (!result.getAvailableServiceEndpointURLs().contains(s.getServiceEndpointURL())) {
										result.getAvailableServiceEndpointURLs().add(s.getServiceEndpointURL());
									}
								}
							}
						} else {
							List<String> unavailableServices = result.getUnavailableServices();
							synchronized (unavailableServices) {
								for (SOAService s : listSoaServices) {
									if (s.getServiceKey() != null) {
										unavailableServices.add(s.getServiceKey());
									}
									if (!result.getUnavailableServiceEndpointURLs().contains(s.getServiceEndpointURL())) {
										result.getUnavailableServiceEndpointURLs().add(s.getServiceEndpointURL());
									}
								}
							}
						}
						return null;
					}
				});

				futures.add(f);
			}

		}

		for (Future<Void> f : futures) {
			try {
				f.get((connectionTimeout + readTimeout) * 2, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				f.cancel(true);
			}
		}

		Collections.sort(result.getAvailableServices());
		Collections.sort(result.getUnavailableServices());

		return result;
	}

	@Override
	public Collection<SOAService> getSOAServices(Collection<ServiceEndpointInfo> services) {

		Collection<SOAService> result = new HashSet<SOAService>(services.size());

		for (ServiceEndpointInfo service : services) {
			SOAService soaService = getSOAService(service);
			result.add(soaService);
		}

		return result;
	}

	@Override
	public SOAService getSOAService(ServiceEndpointInfo e) {

		synchronized (soaServices) {

			SOAService soaService = soaServices.get(e.getServiceKey());
			if (soaService == null) {
				soaService = new SOAService(e.getServiceClassification(), e.getServiceNamespace(), e.getServiceKey(), e.getBindingTemplateKey(), e.getServiceEndpointURL());
				soaServices.put(e.getServiceKey(), soaService);
			} else {
				if (!soaService.getServiceEndpointURL().equals(e.getServiceEndpointURL())) {
					soaService.setServiceEndpointURL(e.getServiceEndpointURL());
					soaServices.put(e.getServiceKey(), soaService);
				}
			}
			return soaService;
		}
	}
}
