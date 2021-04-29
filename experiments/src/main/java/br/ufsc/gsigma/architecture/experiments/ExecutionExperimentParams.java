package br.ufsc.gsigma.architecture.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import br.ufsc.gsigma.architecture.experiments.util.UnavailabilityCounter;
import br.ufsc.gsigma.catalog.services.model.ResilienceConfiguration;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;

public class ExecutionExperimentParams {

	public String executionId;

	public int repeats = 100;

	public int experimentIterations = 1;

	public boolean deployProcess;

	public boolean resetUBLServices = true;

	public boolean adhocServiceDeployment = false;

	public boolean simulateServicesUnvailability = true;

	public boolean simulateAdhocServicesUnvailability = true;

	//////

	public String protocolConverter;

	//////

	public int instanceCreationInitialDelay = 2000; // alternate in even numbers (0, 2, 4, 6 ...) , one instance at each 2 seconds for 5 processes

	public int instanceCreationInterval = 10000;

	//////

	public int serviceUnavailabilityStartOffset = 10;

	public int serviceUnavailabilityInitialDelay = 1000;

	public int serviceUnavailabilityInterval = 10000;

	//////

	public int serviceContainerUnavailabilityStartOffset = 20;

	public int serviceContainerUnavailabilityInitialDelay = 2000;

	public int serviceContainerUnavailabilityInterval = 60000;

	//////

	public int adhocUblServicesReplicas = 2;

	public int ublServicesReplicas = 5;

	public boolean disableMonitoring;

	public List<String> deploymentServers = new ArrayList<String>(DockerServers.getServerForAdhocDeployment());

	public List<Class<? extends AbstractProcessExperiment>> processes = new ArrayList<Class<? extends AbstractProcessExperiment>>();

	public ResilienceConfiguration resilienceConfiguration = new ResilienceConfiguration(1, 1, 1);

	public CountDownLatch waitProcessesDeployLatch;

	public List<ComponentUnavailabilityConfig> componentUnavailabilityConfigs = new ArrayList<ComponentUnavailabilityConfig>();

	public UnavailabilityCounter unavailabilityCounter;

	public Double cpuTime;

	public ExecutionExperimentParams() {

	}

	public ExecutionExperimentParams(List<Class<? extends AbstractProcessExperiment>> processes) {
		this.processes = processes;
	}

	public ExecutionExperimentParams(String executionId, int repeats, boolean deployProcess, boolean resetUBLServices, CountDownLatch waitProcessesDeployLatch, ResilienceConfiguration resilienceConfiguration) {
		this.executionId = executionId;
		this.repeats = repeats;
		this.deployProcess = deployProcess;
		this.resetUBLServices = resetUBLServices;
		this.waitProcessesDeployLatch = waitProcessesDeployLatch;
		this.resilienceConfiguration = resilienceConfiguration;
	}

	public ExecutionExperimentParams(ExecutionExperimentParams from) {
		this(from, from.executionId, from.resilienceConfiguration);
	}

	public ExecutionExperimentParams(ExecutionExperimentParams from, String executionId, int bindingServiceReplicas, int executionServiceReplicas, int resilienceServiceReplicas) {
		this(from, executionId, new ResilienceConfiguration(bindingServiceReplicas, executionServiceReplicas, resilienceServiceReplicas));
	}

	public ExecutionExperimentParams(ExecutionExperimentParams from, String executionId, int bindingServiceReplicas, int executionServiceReplicas, int resilienceServiceReplicas, Double cpuTime) {
		this(from, executionId, bindingServiceReplicas, executionServiceReplicas, resilienceServiceReplicas);
		this.cpuTime = cpuTime;
	}

	public ExecutionExperimentParams(ExecutionExperimentParams from, String executionId, ResilienceConfiguration resilienceConfiguration) {
		this.processes = from.processes;
		this.repeats = from.repeats;
		this.experimentIterations = from.experimentIterations;
		this.deployProcess = from.deployProcess;
		this.resetUBLServices = from.resetUBLServices;
		this.adhocServiceDeployment = from.adhocServiceDeployment;
		this.instanceCreationInitialDelay = from.instanceCreationInitialDelay;
		this.instanceCreationInterval = from.instanceCreationInterval;
		this.serviceUnavailabilityInitialDelay = from.serviceUnavailabilityInitialDelay;
		this.serviceUnavailabilityInterval = from.serviceUnavailabilityInterval;
		this.serviceContainerUnavailabilityInitialDelay = from.serviceContainerUnavailabilityInitialDelay;
		this.serviceContainerUnavailabilityInterval = from.serviceContainerUnavailabilityInterval;
		this.adhocUblServicesReplicas = from.adhocUblServicesReplicas;
		this.ublServicesReplicas = from.ublServicesReplicas;
		this.deploymentServers = from.deploymentServers;
		this.componentUnavailabilityConfigs = from.componentUnavailabilityConfigs;
		this.disableMonitoring = from.disableMonitoring;

		this.simulateServicesUnvailability = from.simulateServicesUnvailability;
		this.simulateAdhocServicesUnvailability = from.simulateAdhocServicesUnvailability;

		this.protocolConverter = from.protocolConverter;

		this.executionId = executionId;
		this.resilienceConfiguration = resilienceConfiguration;

		this.cpuTime = from.cpuTime;
	}

}