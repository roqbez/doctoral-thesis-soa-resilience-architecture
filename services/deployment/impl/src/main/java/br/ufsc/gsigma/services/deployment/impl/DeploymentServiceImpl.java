package br.ufsc.gsigma.services.deployment.impl;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Striped;

import br.ufsc.gsigma.catalog.services.model.DeploymentServer;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider.InfrastructureServerType;
import br.ufsc.gsigma.catalog.services.model.ResilienceConfiguration;
import br.ufsc.gsigma.infrastructure.util.docker.DockerClientConfigAccessor;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.util.docker.GesellixDockerClient;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent.ComponentName;
import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.infrastructure.util.messaging.StandaloneMessageReceiver;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContextAwareExecutorServiceDecorator;
import br.ufsc.gsigma.services.bpelexport.interfaces.BPELExporterService;
import br.ufsc.gsigma.services.bpelexport.locator.BPELExporterServiceLocator;
import br.ufsc.gsigma.services.bpelexport.output.BPELProcessBundle;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;
import br.ufsc.gsigma.services.deployment.model.Deployment;
import br.ufsc.gsigma.services.deployment.model.DeploymentRequest;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedService;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedServiceDeploymentRequestItem;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedServiceDeploymentRequestItem.PortPublishMode;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedServicesDeploymentRequest;
import br.ufsc.gsigma.services.deployment.model.ServiceContainer;
import br.ufsc.gsigma.services.deployment.model.ServiceDeploymentRequestItem;
import br.ufsc.gsigma.services.deployment.model.ServicesDeployment;
import br.ufsc.gsigma.services.deployment.model.ServicesDeploymentRequest;
import br.ufsc.gsigma.services.execution.dto.ProcessDeploymentInfo;
import br.ufsc.gsigma.services.execution.interfaces.ProcessExecutionService;
import br.ufsc.gsigma.services.execution.locator.ProcessExecutionServiceLocator;
import br.ufsc.services.core.util.json.JsonUtil;
import de.gesellix.docker.client.stack.DeployStackConfig;
import de.gesellix.docker.client.stack.types.StackService;

@Service
public class DeploymentServiceImpl implements DeploymentService {

	private static final Logger logger = Logger.getLogger(DeploymentServiceImpl.class);

	private ExecutorService pool = new ExecutionContextAwareExecutorServiceDecorator(Executors.newCachedThreadPool());

	// private Striped<Lock> deploymentServerLocker = Striped.lock(512);

	private Striped<Lock> platformManagedServiceLocker = Striped.lock(512);

	private Map<String, InfrastructureProvider> providers = new LinkedHashMap<String, InfrastructureProvider>();

	private Map<String, Map<String, DeploymentServer>> nodesCache = new HashMap<String, Map<String, DeploymentServer>>();

	public DeploymentServiceImpl() {
		addInfrastructureProvider(createInfrastructureProvider("GSigma Provider"));
		addInfrastructureProvider(createInfrastructureProvider("XYZ Provider"));
		addInfrastructureProvider(createInfrastructureProvider("ABC Provider"));
	}

	private InfrastructureProvider createInfrastructureProvider(String name) {
		return createInfrastructureProvider(null, name);
	}

	private InfrastructureProvider createInfrastructureProvider(String id, String name) {

		List<DeploymentServer> servers = Arrays.asList( //
				new DeploymentServer(DockerServers.SERVER_150_162_6_133, "150.162.6.133"), //
				new DeploymentServer(DockerServers.SERVER_150_162_6_194, "150.162.6.194") //
		);

		List<DeploymentServer> orchestratorServers = Arrays.asList(new DeploymentServer(DockerServers.SERVER_150_162_6_63, "150.162.6.63"));

		InfrastructureProvider provider = new InfrastructureProvider(name, InfrastructureServerType.DOCKER, servers, orchestratorServers);

		if (id == null) {
			provider.setId(DigestUtils.sha1Hex(provider.getName()));
		}

		return provider;
	}

	private void addInfrastructureProvider(InfrastructureProvider provider) {
		providers.put(provider.getId(), provider);
	}

	@Override
	public List<InfrastructureProvider> getInfrastructureProviders() {
		return new ArrayList<InfrastructureProvider>(providers.values());
	}

	@Override
	public InfrastructureProvider getInfrastructureProvider(String id) {
		return providers.get(id);
	}

	@Override
	public Deployment deployApplication(DeploymentRequest request) {

		String processName = request.getBusinessProcessName();

		if (request == null || processName == null || request.getBusinessProcess() == null || request.getItConfiguration() == null || request.getServicesInformation() == null)
			throw new IllegalArgumentException("Invalid arguments");

		try {

			ExecutionContext executionContext = ExecutionContext.get();

			if (executionContext == null) {
				executionContext = new ExecutionContext();
				executionContext.set();
			}

			InfrastructureProvider infrastructureProvider = request.getItConfiguration().getInfrastructureProvider();

			String dockerUSwarmURL = "http://150.162.6.63:2376";

			if (infrastructureProvider != null) {

				if (infrastructureProvider.getId() != null) {
					executionContext.addAttribute(ExecutionAttributes.ATT_INFRASTRUCTURE_PROVIDER_ID, infrastructureProvider.getId());
				}

				List<DeploymentServer> deploymentServers = infrastructureProvider.getDeploymentServers();

				if (executionContext.getAttribute(ExecutionAttributes.ATT_DEPLOYMENT_SERVERS) == null && !CollectionUtils.isEmpty(deploymentServers)) {
					String json = JsonUtil.encode(deploymentServers.stream().map(s -> s.getAddress()).collect(Collectors.toList()));
					executionContext.addAttribute(ExecutionAttributes.ATT_DEPLOYMENT_SERVERS, json);
				}

				List<DeploymentServer> deploymentOrchestratorServers = infrastructureProvider.getOrchestratorServers();

				if (executionContext.getAttribute(ExecutionAttributes.ATT_DEPLOYMENT_ORCHESTRATOR_SERVERS) == null && !CollectionUtils.isEmpty(deploymentOrchestratorServers)) {

					dockerUSwarmURL = "http://" + deploymentOrchestratorServers.get(0) + ":2376";

					String json = JsonUtil.encode(deploymentOrchestratorServers.stream().map(s -> s.getAddress()).collect(Collectors.toList()));

					executionContext.addAttribute(ExecutionAttributes.ATT_DEPLOYMENT_ORCHESTRATOR_SERVERS, json);
				}
			}

			final BPELExporterService bpelExporterService = BPELExporterServiceLocator.get();

			logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_DEPLOYMENT, "Converting process to BPEL") //
					.addAttribute(LogConstants.PROCESS_NAME, processName) //
			);

			BPELProcessBundle bundle = bpelExporterService.convertToBPEL(request.getBusinessProcess(), request.getItConfiguration());

			if (bundle == null || bundle.getZipContent() == null)
				throw new RuntimeException("BPEL Process binary is null");

			ResilienceConfiguration resilienceConfiguration = request.getItConfiguration().getResilienceConfiguration();

			if (resilienceConfiguration != null) {

				int bindingServiceReplicas = resilienceConfiguration.getBindingServiceReplicas();
				int executionServiceReplicas = resilienceConfiguration.getExecutionServiceReplicas();
				int resilienceServiceReplicas = resilienceConfiguration.getResilienceServiceReplicas();

				logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_DEPLOYMENT, "Deploying support services (execution, binding and resilience)") //
						.addAttribute(LogConstants.PROCESS_NAME, processName));

				deploySupportServices(dockerUSwarmURL, bindingServiceReplicas, executionServiceReplicas, resilienceServiceReplicas);
			}

			final ProcessExecutionService processExecutionService = ProcessExecutionServiceLocator.get();

			logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_DEPLOYMENT, "Deploying to execution service") //
					.addAttribute(LogConstants.PROCESS_NAME, processName));

			final ProcessDeploymentInfo processDeploymentInfo = processExecutionService.deployProcess(processName, request.getExecutionContext(), bundle.getZipContent());

			Deployment result = new Deployment();

			if (processDeploymentInfo.isError()) {
				result.setError(true);

				logger.error(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_DEPLOYMENT, "Error deploying process " + processName) //
						.addAttribute(LogConstants.PROCESS_NAME, processDeploymentInfo) //
				);

			} else {
				result.setBusinessProcessName(processDeploymentInfo.getProcessName());
				result.setBusinessProcessId(processDeploymentInfo.getProcessId());
				// TODO: result.setBusinessProcessEndpoint(businessProcessEndpoint);

				logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_DEPLOYMENT, "Application deployed: " + processDeploymentInfo.getProcessName()) //
						.addAttribute(LogConstants.PROCESS_NAME, processDeploymentInfo) //
				);

			}

			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("Error deploying application: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean deploySupportServices(String dockerUSwarmURL, int bindingServiceReplicas, int executionServiceReplicas, int resilienceServiceReplicas) throws URISyntaxException, IOException {

		// TODO: check if services already exist

		// TODO: deploy jms broker

		GesellixDockerClient dockerClient = new GesellixDockerClient(dockerUSwarmURL);

		URL supportServicesYamlUrl = DeploymentServiceImpl.class.getResource("/docker-compose-support-services.yml");

		DeployStackConfig deployStackConfig = dockerClient.getDeployStackConfig("EBR", supportServicesYamlUrl);

		StackService bindingService = deployStackConfig.getServices().get("binding-service");
		((Map<String, Object>) ((Map<String, Object>) bindingService.getMode()).get("replicated")).put("replicas", bindingServiceReplicas);

		StackService executionService = deployStackConfig.getServices().get("execution-service");
		((Map<String, Object>) ((Map<String, Object>) executionService.getMode()).get("replicated")).put("replicas", executionServiceReplicas);

		StackService resilienceService = deployStackConfig.getServices().get("resilience-service");
		((Map<String, Object>) ((Map<String, Object>) resilienceService.getMode()).get("replicated")).put("replicas", resilienceServiceReplicas);

		StandaloneMessageReceiver messageReceiver = StandaloneMessageReceiver.create("tcp://jmsbroker.d-201603244.ufsc.br:61616", DeploymentService.class.getName());

		try {
			ReadyToRunListener listener = new ReadyToRunListener();
			messageReceiver.registerEventListener(listener);

			listener.init(bindingServiceReplicas, executionServiceReplicas, resilienceServiceReplicas);

			dockerClient.deployStack("EBR", deployStackConfig);

			try {
				listener.awaitReady(1000 * 60 * 5);
			} catch (InterruptedException e) {
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				messageReceiver.shutdown();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return true;

	}

	@Override
	public Map<String, String> pullImage(List<String> deploymentServers, String image) {

		Map<String, String> result = new HashMap<String, String>();

		for (String server : deploymentServers) {
			DockerClient client = DockerServers.getDockerClient(server);
			if (client != null) {
				String imageId = DockerUtil.pullImage(client, image);
				if (imageId != null) {
					result.put(server, imageId);
				}
			}
		}

		return result;
	}

	@Override
	public List<DeploymentServer> getPlatformManagedDeploymentServers(String managerServer) {
		Map<String, DeploymentServer> result = getPlatformManagedDeploymentServersInternal(managerServer);
		return new ArrayList<DeploymentServer>(result.values());
	}

	private Map<String, DeploymentServer> getPlatformManagedDeploymentServersCache(String managerServer) {
		return nodesCache.computeIfAbsent(managerServer, s -> getPlatformManagedDeploymentServersInternal(s));
	}

	@SuppressWarnings("unchecked")
	private Map<String, DeploymentServer> getPlatformManagedDeploymentServersInternal(String managerServer) {
		GesellixDockerClient client = getGesellixDockerClientFromStr(managerServer);

		Map<String, DeploymentServer> managedServers = new LinkedHashMap<String, DeploymentServer>();

		if (client != null) {

			List<Map<String, Object>> nodes = client.httpGetToListMap("/nodes", Collections.EMPTY_MAP);

			for (Map<String, Object> node : nodes) {

				String nodeId = (String) node.get("ID");

				Map<String, Object> status = (Map<String, Object>) node.get("Status");

				String addr = (String) status.get("Addr");

				DeploymentServer server = new DeploymentServer(nodeId, DockerServers.getDockerServerNameFromAddress(addr), addr);

				managedServers.put(nodeId, server);

			}

		}
		return managedServers;
	}

	@Override
	public List<PlatformManagedService> removePlatformManagedServices(String managerServer, List<String> serviceNames) {

		List<PlatformManagedService> result = new LinkedList<PlatformManagedService>();

		try {
			GesellixDockerClient dockerClient = getGesellixDockerClientFromStr(managerServer);

			if (dockerClient != null) {
				for (PlatformManagedService pms : getPlatformManagedServices(managerServer, serviceNames)) {
					dockerClient.rmService(pms.getName());
					result.add(pms);
				}
			}
			return result;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<PlatformManagedService> getPlatformManagedServices(String managerServer, List<String> serviceNames) {
		return getPlatformManagedServicesInternal(managerServer, serviceNames, null);
	}

	@Override
	public PlatformManagedService getPlatformManagedServiceById(String managerServer, String serviceId) {
		List<PlatformManagedService> l = getPlatformManagedServicesInternal(managerServer, null, Collections.singletonList(serviceId));
		return !CollectionUtils.isEmpty(l) ? l.get(0) : null;
	}

	@Override
	public List<PlatformManagedService> getPlatformManagedServiceByIds(String managerServer, List<String> serviceIds) {
		return getPlatformManagedServicesInternal(managerServer, null, serviceIds);
	}

	@SuppressWarnings({ "unchecked" })
	private List<PlatformManagedService> getPlatformManagedServicesInternal(String managerServer, List<String> serviceNames, List<String> serviceIds) {

		try {
			GesellixDockerClient client = getGesellixDockerClientFromStr(managerServer);

			Map<String, PlatformManagedService> managedServices = new LinkedHashMap<String, PlatformManagedService>();

			if (client != null) {

				Map<String, DeploymentServer> nodes = getPlatformManagedDeploymentServersCache(managerServer);

				Map<String, String> queryParams = !CollectionUtils.isEmpty(serviceIds) ? ImmutableMap.of("filters", JsonUtil.encode(ImmutableMap.of("id", serviceIds))) : Collections.EMPTY_MAP;

				List<Map<String, Object>> services = client.httpGetToListMap("/services", queryParams);

				if (services != null) {

					for (Map<String, Object> m : services) {

						Map<String, Object> spec = (Map<String, Object>) m.get("Spec");

						String serviceName = (String) spec.get("Name");

						if (!CollectionUtils.isEmpty(serviceNames)) {
							if (!matchServiceName(serviceName, serviceNames)) {
								continue;
							}
						}

						String id = (String) m.get("ID");

						PlatformManagedService svc = new PlatformManagedService(id, serviceName);

						Map<String, Object> taskTemplate = (Map<String, Object>) spec.get("TaskTemplate");

						Map<String, Object> containerSpec = (Map<String, Object>) taskTemplate.get("ContainerSpec");

						List<String> env = (List<String>) containerSpec.get("Env");

						if (!CollectionUtils.isEmpty(env)) {
							svc.getVariables().putAll(env.stream().map(v -> v.split("=")).collect(Collectors.toMap(p -> p[0], p -> p[1])));
						}

						Map<String, Object> mode = (Map<String, Object>) spec.get("Mode");

						Map<String, Object> replicated = mode != null ? (Map<String, Object>) mode.get("Replicated") : null;

						if (replicated != null) {
							Number replicas = (Number) replicated.get("Replicas");
							if (replicas != null) {
								svc.setReplicas(replicas.intValue());
							}
						}

						managedServices.put(id, svc);

					}

					if (!managedServices.isEmpty()) {

						List<Map<String, Object>> tasks = new LinkedList<Map<String, Object>>();

						for (String serviceId : managedServices.keySet()) {
							try {
								tasks.addAll(client.httpGetToListMap("/tasks", ImmutableMap.of("filters", JsonUtil.encode(ImmutableMap.of("service", Collections.singleton(serviceId))))));
							} catch (Exception e) {
							}
						}

						for (Map<String, Object> task : (List<Map<String, Object>>) tasks) {

							String taskId = (String) task.get("ID");

							String serviceId = (String) task.get("ServiceID");

							String nodeId = (String) task.get("NodeID");

							DeploymentServer server = nodes.get(nodeId);

							Map<String, Object> status = (Map<String, Object>) task.get("Status");

							String state = (String) status.get("State");

							boolean isRunning = "running".equalsIgnoreCase(state);

							Map<String, Object> portStatus = (Map<String, Object>) status.get("PortStatus");

							List<Map<String, Object>> ports = portStatus != null ? (List<Map<String, Object>>) portStatus.get("Ports") : null;

							Map<String, Object> spec = (Map<String, Object>) task.get("Spec");

							Map<String, Object> containerSpec = (Map<String, Object>) spec.get("ContainerSpec");

							List<String> env = (List<String>) containerSpec.get("Env");

							Map<String, Object> containerStatus = (Map<String, Object>) status.get("ContainerStatus");

							String containerId = containerStatus != null ? (String) containerStatus.get("ContainerID") : null;

							if (containerId != null && isRunning) {

								ServiceContainer container = new ServiceContainer();
								container.setContainerId(containerId);
								container.setImageName((String) containerSpec.get("Image"));
								container.setDeploymentServer(server != null ? server.getAddress() : null);
								container.setServiceId(serviceId);
								container.setTaskId(taskId);

								Map<String, String> containerVariables = new HashMap<String, String>();
								container.setContainerVariables(containerVariables);

								if (!CollectionUtils.isEmpty(env)) {
									containerVariables.putAll(env.stream().map(v -> v.split("=")).collect(Collectors.toMap(p -> p[0], p -> p[1])));
									container.setServiceClassification(containerVariables != null ? containerVariables.get(ServiceContainer.PARAM_SERVICE_CLASSIFICATION) : null);
								}

								if (!CollectionUtils.isEmpty(ports)) {
									String[] containerPortMapping = new String[ports.size()];
									int i = 0;
									for (Map<String, Object> portMapping : ports) {
										containerPortMapping[i++] = portMapping.get("PublishedPort") + ":" + portMapping.get("TargetPort");
										containerVariables.put(ServiceContainer.PARAM_SERVICE_PORT, String.valueOf(portMapping.get("PublishedPort")));
									}
									container.setContainerPortMapping(containerPortMapping);
								}
								PlatformManagedService managedService = managedServices.get(serviceId);
								managedService.getContainers().add(container);
							}

						}

					}
				}
			}

			return new ArrayList<PlatformManagedService>(managedServices.values());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private boolean matchServiceName(String serviceName, List<String> serviceNames) {
		boolean matchAny = false;
		for (String s : serviceNames) {
			if (serviceName.contains(s)) {
				matchAny = true;
				break;
			}
		}
		return matchAny;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ServicesDeployment deployPlatformManagedServices(PlatformManagedServicesDeploymentRequest request) {

		String managerServer = request.getManagerServer();

		// Lock lock = deploymentServerLocker.get(deploymentServer);

		ServicesDeployment result = new ServicesDeployment();

		try {

			// lock.lock();

			long s = System.currentTimeMillis();

			GesellixDockerClient dockerClient = getGesellixDockerClientFromStr(managerServer);

			if (dockerClient == null)
				throw new IllegalArgumentException("Unable to find any deployment server with name " + managerServer);

			List<String> serviceNames = new ArrayList<String>(request.getItems().size());

			int totalContainers = 0;

			for (PlatformManagedServiceDeploymentRequestItem item : request.getItems()) {

				String serviceName = item.getServiceName();

				Lock lock = platformManagedServiceLocker.get(serviceName);

				try {

					lock.lock();

					serviceNames.add(serviceName);

					Map<String, Object> config = new HashMap<String, Object>();
					config.put("Name", item.getServiceName());

					Map<String, Object> taskTemplate = new HashMap<String, Object>();
					config.put("TaskTemplate", taskTemplate);

					Map<String, Object> containerSpec = new HashMap<String, Object>();
					taskTemplate.put("ContainerSpec", containerSpec);

					containerSpec.put("Image", item.getImageName());
					containerSpec.put("Hostname", "{{.Service.Name}}{{.Task.Slot}}");

					Map<String, String> containerVariables = item.getContainerVariables();

					if (containerVariables == null) {
						containerVariables = new HashMap<String, String>();
						item.setContainerVariables(containerVariables);
					}

					if (item.getServiceClassification() != null) {

						String servicePath = getServicePathFromServiceClassification(item.getServiceClassification());

						containerVariables.put(ServiceContainer.PARAM_SERVICE_CLASSIFICATION, item.getServiceClassification());
						containerVariables.put(ServiceContainer.PARAM_SERVICE_PATH, servicePath);
						containerVariables.put("JAVA_ARGS_EXTRA", servicePath);
					}

					if (!MapUtils.isEmpty(containerVariables)) {
						List<String> env = item.getContainerVariables().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList());
						containerSpec.put("Env", env);
					}

					if (!ArrayUtils.isEmpty(item.getServicePortMapping())) {

						Map<String, Object> endpointSpec = new HashMap<String, Object>();
						config.put("EndpointSpec", endpointSpec);

						List<Map<String, Object>> ports = new ArrayList<Map<String, Object>>();
						endpointSpec.put("Ports", ports);

						for (String map : item.getServicePortMapping()) {
							Map<String, Object> portMap = new HashMap<String, Object>();

							String[] sp = map.split(":");

							if (sp.length == 1) {
								portMap.put("TargetPort", Integer.valueOf(sp[0]));
							} else {
								if (!"*".equals(sp[0])) {
									portMap.put("PublishedPort", Integer.valueOf(sp[0]));
								}
								portMap.put("TargetPort", Integer.valueOf(sp[1]));

								String publishMode = ObjectUtils.defaultIfNull(item.getPortPublishMode(), PortPublishMode.HOST).toString();

								portMap.put("PublishMode", publishMode);
							}

							portMap.put("Protocol", "tcp");

							ports.add(portMap);
						}
					}

					Map<String, Object> updateConfig = new HashMap<String, Object>();
					config.put("UpdateConfig", updateConfig);

					updateConfig.put("Parallelism", 0);

					Map<String, Object> mode = new HashMap<String, Object>();
					config.put("Mode", mode);

					Map<String, Object> replicated = new HashMap<String, Object>();
					mode.put("Replicated", replicated);

					int replicas = Math.max(item.getReplicas(), 1);

					totalContainers += replicas;

					replicated.put("Replicas", replicas);

					if (!CollectionUtils.isEmpty(item.getPlacementConstraints())) {
						Map<String, Object> placement = new HashMap<String, Object>();
						taskTemplate.put("Placement", placement);
						placement.put("Constraints", item.getPlacementConstraints());
					}

					Map<String, Object> createOptions = new HashMap<String, Object>();

					// String authToken = dockerClient.getManageAuthentication().retrieveEncodedAuthTokenForImage(item.getImageName());

					String authToken = "eyJ1c2VybmFtZSI6ImRvY2tlci1jbGllbnQiLCJwYXNzd29yZCI6IkRvY2tlckNsaWVudDJrMTgiLCJlbWFpbCI6bnVsbCwic2VydmVyYWRkcmVzcyI6ImQtMjAxNjAzMjQ0LnVmc2MuYnIifQ==";

					createOptions.put("EncodedRegistryAuth", authToken);

					List<Map<String, Object>> existingServices = (List<Map<String, Object>>) dockerClient.services(getFiltersMap(ImmutableMap.of("name", Collections.singletonList(item.getServiceName())))).getContent();

					if (!CollectionUtils.isEmpty(existingServices)) {
						dockerClient.rmService(item.getServiceName());
					}

					dockerClient.createService(config, createOptions);

				} finally {
					lock.unlock();
				}
			}

			List<PlatformManagedService> deployedServices = null;

			boolean stop = false;

			int retryAttempt = 0;

			int maxRetries = 15;

			while (!stop) {

				deployedServices = getPlatformManagedServices(managerServer, serviceNames);

				int countContainers = 0;

				for (PlatformManagedService ps : deployedServices) {
					countContainers += ps.getContainers().size();
				}

				if (!request.isWaitContainersStartup() || (request.isWaitOnlyFirstReplica() && countContainers == 1) || countContainers == totalContainers || (retryAttempt++ == maxRetries)) {
					stop = true;
				} else {
					Thread.sleep(1000L);
				}
			}

			result.getManagedServices().addAll(deployedServices);

			long d = System.currentTimeMillis() - s;

			StringBuilder sb = new StringBuilder("Platform managed services were deployed at server " + managerServer + " in " + d + " ms");

			for (PlatformManagedService ps : deployedServices) {
				sb.append("\n\t" + ps.getName() + " (" + ps.getReplicas() + ")");
				for (ServiceContainer c : ps.getContainers()) {
					sb.append("\n\t\t" + c.getContainerId() + "\t at " + c.getDeploymentServer());
				}
			}

			logger.info(sb.toString());

		} catch (Throwable e) {
			logger.error("Error deploying managed services with request: " + request + " --> " + e.getMessage(), e);

			result.setError(true);
			result.setErrorMessage(e.getMessage());

		} finally {
			// lock.unlock();
		}

		return result;
	}

	@Override
	public List<ServiceContainer> getServiceContainersByDeploymentServer(String deploymentServer) {

		// Lock lock = deploymentServerLocker.get(deploymentServer);

		try {

			// lock.lock();

			DockerClient dockerClient = getDockerClientFromStr(deploymentServer);

			if (dockerClient == null)
				throw new IllegalArgumentException("Unable to find any deployment server with name " + deploymentServer);

			List<ServiceContainer> result = new LinkedList<ServiceContainer>();

			List<Container> containers = dockerClient.listContainersCmd() //
					.withShowAll(true) //
					.exec();

			for (Container c : containers) {

				ServiceContainer serviceContainer = mapServiceContainer(deploymentServer, dockerClient, c);

				if (serviceContainer != null)
					result.add(serviceContainer);
			}

			return result;

		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			// lock.unlock();
		}
	}

	@Override
	public ServiceContainer getServiceContainerByName(String deploymentServer, String containerName) {

		// Lock lock = deploymentServerLocker.get(deploymentServer);

		try {

			// lock.lock();

			DockerClient dockerClient = getDockerClientFromStr(deploymentServer);

			if (dockerClient == null)
				throw new IllegalArgumentException("Unable to find any deployment server with name " + deploymentServer);

			Container container = DockerUtil.getContainerByName(dockerClient, containerName);

			if (container != null) {

				ServiceContainer serviceContainer = mapServiceContainer(deploymentServer, dockerClient, container);

				if (serviceContainer != null)
					return serviceContainer;
			}

			return null;

		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			// lock.unlock();
		}
	}

	@Override
	public List<ServiceContainer> getServiceContainersByServicePath(String deploymentServer, String servicePath) {
		return getServiceContainersByServicePaths(deploymentServer, Collections.singletonList(servicePath));
	}

	@Override
	public List<ServiceContainer> getServiceContainersByServicePaths(String deploymentServer, List<String> servicePaths) {

		// Lock lock = deploymentServerLocker.get(deploymentServer);

		try {

			// lock.lock();

			List<ServiceContainer> containers = getServiceContainersByDeploymentServer(deploymentServer);

			List<ServiceContainer> result = new ArrayList<ServiceContainer>(containers.size());

			for (ServiceContainer c : containers) {
				for (String servicePath : servicePaths) {
					if (c.getServicePath().endsWith(servicePath)) {
						result.add(c);
					}
				}
			}
			return result;

		} finally {
			// lock.unlock();
		}
	}

	@Override
	public List<ServiceContainer> getServiceContainersByLabelValues(List<String> deploymentServers, String labelName, List<String> labelValues) {

		List<ServiceContainer> result = new LinkedList<ServiceContainer>();

		for (String server : deploymentServers) {

			DockerClient dockerClient = getDockerClientFromStr(server);

			if (dockerClient == null) {
				continue;
			}

			List<Container> containers = dockerClient.listContainersCmd() //
					.withShowAll(true) //
					.exec();

			next_container: for (Container c : containers) {
				String value = c.getLabels().get(labelName);
				if (value != null) {
					for (String v : labelValues) {
						if (value.endsWith(v)) {
							InspectContainerResponse info = dockerClient.inspectContainerCmd(c.getId()).exec();
							ServiceContainer container = mapServiceContainer(server, dockerClient, info);
							result.add(container);
							continue next_container;
						}
					}
				}
			}

		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ServiceContainer> removeRandomServiceContainers(String deploymentServer, int numberOfContainers) {

		// Lock lock = deploymentServerLocker.get(deploymentServer);

		try {

			// lock.lock();

			DockerClient dockerClient = getDockerClientFromStr(deploymentServer);

			List<ServiceContainer> result = new ArrayList<ServiceContainer>(numberOfContainers);

			List<ServiceContainer> containers = getServiceContainersByDeploymentServer(deploymentServer);

			if (containers.isEmpty())
				return Collections.EMPTY_LIST;

			Set<Integer> randoms = new HashSet<Integer>();

			for (int i = 0; i < numberOfContainers; i++) {

				int r = new Random().nextInt(containers.size());

				while (randoms.contains(r)) {
					r = new Random().nextInt(containers.size());
				}

				randoms.add(r);

				ServiceContainer c = containers.get(r);

				execRemoveContainerCmd(dockerClient, c);

				result.add(c);

			}

			return result;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		} finally {
			// lock.unlock();
		}

	}

	@Override
	public List<ServiceContainer> removeServiceContainers(List<ServiceContainer> containers) {

		List<ServiceContainer> result = new LinkedList<ServiceContainer>();

		for (ServiceContainer c : containers) {
			DockerClient dockerClient = getDockerClientFromStr(c.getDeploymentServer());
			if (c != null) {
				try {
					execRemoveContainerCmd(dockerClient, c);
					result.add(c);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return result;
	}

	@Override
	public ServiceContainer removeServiceContainerByName(String deploymentServer, String containerName) {

		// Lock lock = deploymentServerLocker.get(deploymentServer);

		try {

			// lock.lock();

			DockerClient dockerClient = getDockerClientFromStr(deploymentServer);

			ServiceContainer container = getServiceContainerByName(deploymentServer, containerName);

			if (container != null) {
				execRemoveContainerCmd(dockerClient, container);
			}

			return container;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		} finally {
			// lock.unlock();
		}

	}

	@Override
	public List<ServiceContainer> removeServiceContainersByServicePath(String deploymentServer, String servicePath) {
		return removeServiceContainersByServersServicePaths(Collections.singletonList(deploymentServer), Collections.singletonList(servicePath));
	}

	@Override
	public List<ServiceContainer> removeServiceContainersByServersServicePaths(List<String> deploymentServers, List<String> servicePaths) {

		List<ServiceContainer> result = new LinkedList<ServiceContainer>();

		for (String deploymentServer : deploymentServers) {

			// Lock lock = deploymentServerLocker.get(deploymentServer);

			try {

				// lock.lock();

				DockerClient dockerClient = getDockerClientFromStr(deploymentServer);

				List<ServiceContainer> containers = getServiceContainersByServicePaths(deploymentServer, servicePaths);

				for (ServiceContainer c : containers) {
					execRemoveContainerCmd(dockerClient, c);
				}

				result.addAll(containers);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
			} finally {
				// lock.unlock();
			}
		}
		return result;
	}

	@Override
	public List<ServiceContainer> removeAllServiceContainers(List<String> deploymentServers) {

		List<ServiceContainer> allContainers = new LinkedList<ServiceContainer>();

		for (String deploymentServer : deploymentServers) {

			// Lock lock = deploymentServerLocker.get(deploymentServer);

			try {

				// lock.lock();

				long s = System.currentTimeMillis();

				DockerClient dockerClient = getDockerClientFromStr(deploymentServer);

				List<ServiceContainer> containers = getServiceContainersByDeploymentServer(deploymentServer);

				for (ServiceContainer c : containers) {
					execRemoveContainerCmd(dockerClient, c);
				}

				long d = System.currentTimeMillis() - s;

				logger.info("All services containers were removed from " + deploymentServer + " in " + d + " ms");

				allContainers.addAll(containers);

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);

			} finally {
				// lock.unlock();
			}
		}

		return allContainers;
	}

	@Override
	public ServicesDeployment deployServices(ServicesDeploymentRequest request) {

		String deploymentServer = request.getDeploymentServer();

		// Lock lock = deploymentServerLocker.get(deploymentServer);

		ServicesDeployment result = new ServicesDeployment();

		try {

			// lock.lock();

			long s = System.currentTimeMillis();

			DockerClient dockerClient = getDockerClientFromStr(deploymentServer);

			if (dockerClient == null)
				throw new IllegalArgumentException("Unable to find any deployment server with name " + deploymentServer);

			String deploymentHost = ((DockerClientConfigAccessor) dockerClient).getDockerClientConfig().getDockerHost().getHost();

			Set<Integer> usedPorts = ConcurrentHashMap.newKeySet();

			Map<String, String> containerNameToId = new HashMap<String, String>();

			Map<String, List<Integer>> containerPorts = new HashMap<String, List<Integer>>();

			for (Container c : dockerClient.listContainersCmd().withShowAll(true).exec()) {

				String containerName = c.getNames()[0].substring(1);

				containerNameToId.put(containerName, c.getId());

				for (ContainerPort p : c.getPorts()) {
					Integer port = p.getPublicPort();
					if (port != null) {
						usedPorts.add(port);

						List<Integer> l = containerPorts.get(containerName);
						if (l == null) {
							l = new ArrayList<Integer>();
							containerPorts.put(containerName, l);
						}
						l.add(port);
					}
				}
			}

			List<ServiceDeploymentRequestItem> deploymentServicesItens = new ArrayList<ServiceDeploymentRequestItem>(request.getItens());

			// Trying to order services by service classification to preserve the same port mapping assignment
			Collections.sort(deploymentServicesItens, new Comparator<ServiceDeploymentRequestItem>() {
				@Override
				public int compare(ServiceDeploymentRequestItem d1, ServiceDeploymentRequestItem d2) {
					if (d1.getServiceClassification() == null && d2.getServiceClassification() == null)
						return 0;
					else if (d1.getServiceClassification() == null)
						return 1;
					else if (d2.getServiceClassification() == null)
						return -1;
					else
						return d1.getServiceClassification().compareTo(d2.getServiceClassification());
				}
			});

			final List<Future<ServiceContainer>> futures = new ArrayList<Future<ServiceContainer>>(deploymentServicesItens.size());

			final CountDownLatch waitForContainerRemove = new CountDownLatch(deploymentServicesItens.size());

			for (ServiceDeploymentRequestItem item : deploymentServicesItens) {

				Future<ServiceContainer> f = pool.submit(new Callable<ServiceContainer>() {

					@Override
					public ServiceContainer call() throws Exception {

						try {

							String imageId = DockerUtil.getImageIdByTagName(dockerClient, item.getImageName());

							if (imageId == null) {
								imageId = DockerUtil.pullImage(dockerClient, item.getImageName());
							}

							String containerName = item.getContainerName();

							Map<String, String> variables = item.getContainerVariables();

							if (variables == null)
								variables = new HashMap<String, String>();

							// TODO: make this code more generic
							String sc = item.getServiceClassification();
							String servicePath = getServicePathFromServiceClassification(sc);
							variables.put(ServiceContainer.PARAM_SERVICE_PATH, servicePath);
							variables.put(ServiceContainer.PARAM_SERVICE_CLASSIFICATION, sc);

							String cId = containerNameToId.get(containerName);

							if (cId != null) {
								dockerClient.removeContainerCmd(cId) //
										.withForce(true) //
										.exec();
								List<Integer> l = containerPorts.get(containerName);
								if (l != null) {
									usedPorts.removeAll(l);
								}
							}
							waitForContainerRemove.countDown();

							waitForContainerRemove.await();

							String[] portMapping = item.getContainerPortMapping();

							synchronized (usedPorts) {

								if (portMapping != null) {
									for (int i = 0; i < portMapping.length; i++) {
										String p = portMapping[i];

										int idx = p.lastIndexOf(':');

										if (idx > 0) {

											int port = Integer.valueOf(p.substring(idx + 1));

											if (request.isChangePortMappingIfNecessary()) {
												int w = 0;

												boolean available = false;

												while (!(available = !usedPorts.contains(port) && portAvailable(deploymentHost, port)) && (w++ < 1000)) {
													port++;
												}

												if (!available) {
													throw new IllegalStateException("Unable to find an available port at server " + deploymentServer + " for service container '" + containerName + "'");
												}

											}

											p = p.substring(0, idx) + ":" + port;
											portMapping[i] = p;

											if (p.startsWith("8080:")) {
												variables.put(ServiceContainer.PARAM_SERVICE_PORT, String.valueOf(port));
											}
										}

									}
								}

								for (String p : portMapping) {

									int idx = p.lastIndexOf(':');

									if (idx > 0) {
										int port = Integer.valueOf(p.substring(idx + 1));
										usedPorts.add(port);
									}
								}
							}

							variables.put("JAVA_ARGS", "0.0.0.0 8080 " + variables.get(ServiceContainer.PARAM_SERVICE_HOST) + " " + variables.get(ServiceContainer.PARAM_SERVICE_PORT) + " " + variables.get(ServiceContainer.PARAM_SERVICE_PATH));

							logger.info("Deploying container " + containerName + " at server " + deploymentServer + " with port mapping " + ArrayUtils.toString(portMapping) + " and variables " + variables);

							CreateContainerCmd cmd = DockerUtil.createContainerCmd(dockerClient, //
									imageId, //
									containerName, //
									portMapping, //
									null, //
									variables, //
									false, false
							//
							);

							cmd.withImage(item.getImageName());

							Map<String, String> labels = new HashMap<String, String>();
							labels.put("serviceClassification", sc);
							cmd.withLabels(labels);

							cmd.withNetworkMode("net");

							String containerId = cmd.exec().getId();

							DockerUtil.startContainer(dockerClient, containerId);

							InspectContainerResponse info = dockerClient.inspectContainerCmd(containerId).exec();

							return mapServiceContainer(deploymentServer, dockerClient, info);

						} finally {
							waitForContainerRemove.countDown();
						}
					}
				});
				futures.add(f);
			}

			for (Future<ServiceContainer> f : futures) {
				result.getServiceContainers().add(f.get());
			}

			long d = System.currentTimeMillis() - s;

			logger.info("All services were deployed at server " + deploymentServer + " in " + d + " ms");

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			result.setError(true);
			result.setErrorMessage(e.getMessage());

		} finally {
			// lock.unlock();
		}

		return result;
	}

	private ServiceContainer mapServiceContainer(String deploymentServer, DockerClient dockerClient, Container c) {

		String serviceClassification = c.getLabels().get("serviceClassification");

		if (serviceClassification != null) {
			InspectContainerResponse info = dockerClient.inspectContainerCmd(c.getId()).exec();
			return mapServiceContainer(deploymentServer, dockerClient, info);
		}

		return null;
	}

	private void execRemoveContainerCmd(DockerClient dockerClient, ServiceContainer c) {
		RemoveContainerCmd cmd = dockerClient.removeContainerCmd(c.getContainerId());
		cmd.withForce(true);
		cmd.exec();
	}

	private ServiceContainer mapServiceContainer(String deploymentServer, DockerClient dockerClient, InspectContainerResponse info) {
		String containerName = info.getName();

		containerName = containerName.startsWith("/") ? containerName.substring(1) : containerName;

		ServiceContainer serviceContainer = new ServiceContainer();
		serviceContainer.setContainerId(info.getId());

		serviceContainer.setContainerName(containerName);
		serviceContainer.setDeploymentServer(deploymentServer);

		String imageId = info.getImageId();

		List<String> tags = dockerClient.inspectImageCmd(imageId).exec().getRepoTags();

		String imageName = tags.size() > 0 ? tags.get(0) : null;

		serviceContainer.setImageName(imageName != null ? imageName : imageId);

		Map<String, String> variables = new LinkedHashMap<String, String>();

		String[] envs = info.getConfig().getEnv();

		if (envs != null) {
			for (String e : envs) {
				String[] s = e.split("=");
				variables.put(s[0], s[1]);
			}
		}

		serviceContainer.setContainerVariables(variables);

		Map<String, String> containerVariables = serviceContainer.getContainerVariables();
		serviceContainer.setServiceClassification(containerVariables != null ? containerVariables.get(ServiceContainer.PARAM_SERVICE_CLASSIFICATION) : null);

		return serviceContainer;
	}

	private static String getServicePathFromServiceClassification(String sc) {
		int idx1 = sc.indexOf('/');
		int idx2 = sc.indexOf('/', idx1 + 1);
		String servicePath = sc.substring(0, idx1) + "/" + sc.substring(idx2 + 1);

		String[] split = servicePath.split("/");

		// Ugly hack for ubl/sourcing/catalogueprovision/createcatalogueprocess/
		if (split.length == 5) {
			servicePath = StringUtils.join(ArrayUtils.remove(split, 1), "/");
		}

		return servicePath;
	}

	private static boolean portAvailable(String host, int port) {

		try {
			(new Socket(host, port)).close();
			return false;
		} catch (SocketException e) {
			return true;
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private static Map<String, Object> getFiltersMap(Object value) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("filters", value);
		return map;
	}

	// Other ugly hack...
	private DockerClient getDockerClientFromStr(String deploymentServer) {

		DockerClient client = DockerServers.getDockerClient(deploymentServer);

		if (client == null) {
			String serverName = DockerServers.getDockerServerNameFromAddress(deploymentServer);
			client = DockerServers.getDockerClient(serverName);
		}

		return client;
	}

	private GesellixDockerClient getGesellixDockerClientFromStr(String managerServer) {

		GesellixDockerClient client = DockerServers.getGesellixDockerClient(managerServer);

		if (client == null) {
			String serverName = DockerServers.getDockerServerNameFromAddress(managerServer);
			client = DockerServers.getGesellixDockerClient(serverName);
		}

		return client;
	}

	private static class ReadyToRunListener implements br.ufsc.gsigma.infrastructure.util.messaging.EventListener {

		private CountDownLatch bindingServiceReady;

		private CountDownLatch executionServiceReady;

		private CountDownLatch resilienceServiceReady;

		ReadyToRunListener() {
		}

		void init(int bindingServiceReplicas, int executionServiceReplicas, int resilienceServiceReplicas) {
			bindingServiceReady = new CountDownLatch(bindingServiceReplicas);
			executionServiceReady = new CountDownLatch(executionServiceReplicas);
			resilienceServiceReady = new CountDownLatch(resilienceServiceReplicas);
		}

		@Override
		public void onEvent(Event event) {

			if (event instanceof BootstrapCompleteEvent) {

				BootstrapCompleteEvent evt = (BootstrapCompleteEvent) event;

				if (evt.getComponentName().equals(ComponentName.BINDING_SERVICE)) {
					bindingServiceReady.countDown();

				} else if (evt.getComponentName().equals(ComponentName.EXECUTION_SERVICE)) {
					executionServiceReady.countDown();

				} else if (evt.getComponentName().equals(ComponentName.RESILIENCE_SERVICE)) {
					resilienceServiceReady.countDown();

				}
			}
		}

		public void awaitReady(long timeout) throws InterruptedException {

			bindingServiceReady.await(timeout, TimeUnit.MILLISECONDS);
			logger.info("BindingService is ready");

			executionServiceReady.await(timeout, TimeUnit.MILLISECONDS);
			logger.info("ExecutionService is ready");

			resilienceServiceReady.await(timeout, TimeUnit.MILLISECONDS);
			logger.info("ResilienceService is ready");
		}
	}

}
