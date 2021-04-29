package br.ufsc.gsigma.architecture.experiments;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.architecture.experiments.billingwithcreditnote.BillingWithCreditNoteProcessExperiment1;
import br.ufsc.gsigma.architecture.experiments.billingwithcreditnote.BillingWithCreditNoteProcessExperiment2;
import br.ufsc.gsigma.architecture.experiments.billingwithcreditnote.BillingWithCreditNoteProcessExperiment4;
import br.ufsc.gsigma.architecture.experiments.billingwithcreditnote.BillingWithCreditNoteProcessExperiment5;
import br.ufsc.gsigma.architecture.experiments.billingwithcreditnote.BillingWithCreditNoteProcessExperiment6;
import br.ufsc.gsigma.architecture.experiments.createcatalogueprocess.CreateCatalogueProcessExperiment1;
import br.ufsc.gsigma.architecture.experiments.createcatalogueprocess.CreateCatalogueProcessExperiment2;
import br.ufsc.gsigma.architecture.experiments.createcatalogueprocess.CreateCatalogueProcessExperiment4;
import br.ufsc.gsigma.architecture.experiments.createcatalogueprocess.CreateCatalogueProcessExperiment5;
import br.ufsc.gsigma.architecture.experiments.createcatalogueprocess.CreateCatalogueProcessExperiment6;
import br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice.FulfilmentWithReceiptAdviceProcessExperiment1;
import br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice.FulfilmentWithReceiptAdviceProcessExperiment2;
import br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice.FulfilmentWithReceiptAdviceProcessExperiment4;
import br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice.FulfilmentWithReceiptAdviceProcessExperiment5;
import br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice.FulfilmentWithReceiptAdviceProcessExperiment6;
import br.ufsc.gsigma.architecture.experiments.orderingprocess.OrderingProcessExperiment1;
import br.ufsc.gsigma.architecture.experiments.orderingprocess.OrderingProcessExperiment2;
import br.ufsc.gsigma.architecture.experiments.orderingprocess.OrderingProcessExperiment4;
import br.ufsc.gsigma.architecture.experiments.orderingprocess.OrderingProcessExperiment5;
import br.ufsc.gsigma.architecture.experiments.orderingprocess.OrderingProcessExperiment6;
import br.ufsc.gsigma.architecture.experiments.paymentprocess.PaymentProcessExperiment1;
import br.ufsc.gsigma.architecture.experiments.paymentprocess.PaymentProcessExperiment2;
import br.ufsc.gsigma.architecture.experiments.paymentprocess.PaymentProcessExperiment4;
import br.ufsc.gsigma.architecture.experiments.paymentprocess.PaymentProcessExperiment5;
import br.ufsc.gsigma.architecture.experiments.paymentprocess.PaymentProcessExperiment6;
import br.ufsc.gsigma.architecture.experiments.util.BackupElasticSearch;
import br.ufsc.gsigma.architecture.experiments.util.CollectElasticSearchMetrics;
import br.ufsc.gsigma.architecture.experiments.util.CollectElasticSearchMetrics.MetricFiles;
import br.ufsc.gsigma.architecture.experiments.util.ExperimentsQueries;
import br.ufsc.gsigma.architecture.experiments.util.ExperimentsUtil;
import br.ufsc.gsigma.architecture.experiments.util.Job;
import br.ufsc.gsigma.architecture.experiments.util.ScheduledJob;
import br.ufsc.gsigma.architecture.experiments.util.UnavailabilityCounter;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.util.docker.GesellixDockerClient;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent.ComponentName;
import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.infrastructure.util.messaging.StandaloneMessageReceiver;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedService;
import br.ufsc.gsigma.services.deployment.model.ServiceContainer;
import br.ufsc.services.core.util.json.JsonUtil;
import de.gesellix.docker.client.stack.DeployStackConfig;
import de.gesellix.docker.client.stack.types.StackService;

public class ThesisExperimentsBootstrap {

	public static final ExecutionExperimentParams EXP1_PARAMS = new ExecutionExperimentParams(Arrays.asList( //
			BillingWithCreditNoteProcessExperiment1.class, //
			CreateCatalogueProcessExperiment1.class, //
			FulfilmentWithReceiptAdviceProcessExperiment1.class, //
			OrderingProcessExperiment1.class, //
			PaymentProcessExperiment1.class //
	));

	public static final ExecutionExperimentParams EXP2_PARAMS = new ExecutionExperimentParams(Arrays.asList( //
			BillingWithCreditNoteProcessExperiment2.class, //
			CreateCatalogueProcessExperiment2.class, //
			FulfilmentWithReceiptAdviceProcessExperiment2.class, //
			OrderingProcessExperiment2.class, //
			PaymentProcessExperiment2.class //
	));

	public static final ExecutionExperimentParams EXP4_PARAMS = new ExecutionExperimentParams(Arrays.asList( //
			BillingWithCreditNoteProcessExperiment4.class, //
			CreateCatalogueProcessExperiment4.class, //
			FulfilmentWithReceiptAdviceProcessExperiment4.class, //
			OrderingProcessExperiment4.class, //
			PaymentProcessExperiment4.class //
	));

	public static final ExecutionExperimentParams EXP5_PARAMS = new ExecutionExperimentParams(Arrays.asList( //
			BillingWithCreditNoteProcessExperiment5.class, //
			CreateCatalogueProcessExperiment5.class, //
			FulfilmentWithReceiptAdviceProcessExperiment5.class, //
			OrderingProcessExperiment5.class, //
			PaymentProcessExperiment5.class //
	));

	public static final ExecutionExperimentParams EXP6_PARAMS = new ExecutionExperimentParams(Arrays.asList( //
			BillingWithCreditNoteProcessExperiment6.class, //
			CreateCatalogueProcessExperiment6.class, //
			FulfilmentWithReceiptAdviceProcessExperiment6.class, //
			OrderingProcessExperiment6.class, //
			PaymentProcessExperiment6.class //
	));

	public static final ExecutionExperimentParams EXP8_PARAMS = new ExecutionExperimentParams(EXP6_PARAMS);

	static {
		EXP1_PARAMS.repeats = 100;
		EXP1_PARAMS.simulateServicesUnvailability = false;
		// EXP1_PARAMS.protocolConverter = "br.ufsc.gsigma.binding.converters.SoapServiceProtocolConverter";
		// EXP1_PARAMS.protocolConverter = "br.ufsc.gsigma.binding.converters.JsonServiceProtocolConverter";

		EXP2_PARAMS.repeats = 100;
		EXP2_PARAMS.serviceUnavailabilityInterval = 10000;

		EXP4_PARAMS.repeats = 100;
		EXP4_PARAMS.adhocServiceDeployment = true;
		EXP4_PARAMS.adhocUblServicesReplicas = 1;

		EXP5_PARAMS.repeats = 100;
		EXP5_PARAMS.adhocServiceDeployment = true;
		EXP5_PARAMS.adhocUblServicesReplicas = 2;

		EXP6_PARAMS.repeats = 100;
		EXP6_PARAMS.adhocServiceDeployment = true;
		EXP6_PARAMS.adhocUblServicesReplicas = 3;

		EXP8_PARAMS.repeats = 100;
		EXP8_PARAMS.adhocServiceDeployment = true;
		EXP8_PARAMS.adhocUblServicesReplicas = 3;

		// EXP6_PARAMS.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("binding-service", 10000, 15 * 1000));
		// EXP6_PARAMS.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("resilience-service", 20000, 15 * 1000));
		// EXP6_PARAMS.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("execution-service", 30000, 15 * 1000));

		// EXP6_PARAMS.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("binding-service", 11000, 53000, 10));
		// EXP6_PARAMS.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("resilience-service", 13000, 57000, 10));
		// EXP6_PARAMS.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("execution-service", 19000, 62000, 10));

	}

	private static final Logger logger = Logger.getLogger(ThesisExperimentsBootstrap.class);

	private static final boolean USE_RANDOM_ARCH_COMPONENT_UNAVAILABILITY = true;

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

	public static void main(String[] args) throws Exception {

		// System.out.println(JsonUtil.encode(EXP5_PARAMS).replaceAll("\"", "`"));
		// System.out.println(JsonUtil.encode(EXP6_PARAMS).replaceAll("\"", "`"));
		//
		// if (true) {
		// return;
		// }

		String json = StringUtils.join(args);

		if (!StringUtils.isEmpty(json)) {
			json = json.replaceAll("`", "\"");
		}

		ExecutionExperimentParams providedExp = null;

		if (args.length > 0 && JsonUtil.isJson(json)) {
			providedExp = (ExecutionExperimentParams) JsonUtil.decode(json, ExecutionExperimentParams.class);
			logger.info("Using provided exp --> " + ReflectionToStringBuilder.toString(providedExp));
		}

		String swarmServer = DockerServers.TCP_SWARM_MANAGER;

		GesellixDockerClient dockerClient = new GesellixDockerClient(swarmServer);

		boolean deployArch = true;

		boolean resetUBLServices = true;

		if (deployArch) {

			ExperimentsUtil.clearAllLogs();

			String resp = DockerUtil.removeDockerStack(swarmServer, "ubl");
			logger.info("Remove stack (ubl) --> \n\t" + resp.replaceAll("\n", "\n\t"));
			Thread.sleep(2000L);

			resp = DockerUtil.removeDockerStack(swarmServer, "exp");
			logger.info("Remove stack (exp) --> \n\t" + resp.replaceAll("\n", "\n\t"));

			Thread.sleep(10000L);

			logger.info("Clearing logs --> resilience-service");
			ExperimentsUtil.clearProgramLogs("resilience-service");

			logger.info("Clearing logs --> binding-service");
			ExperimentsUtil.clearProgramLogs("binding-service");

			logger.info("Clearing logs --> discovery-service");
			ExperimentsUtil.clearProgramLogs("discovery-service");

			logger.info("Clearing logs --> ubl-services");
			ExperimentsUtil.clearProgramLogs("ubl-services");

			resp = DockerUtil.removeDockerStack(swarmServer, "jms");
			logger.info("Remove stack (jms) --> \n\t" + resp.replaceAll("\n", "\n\t"));

			logger.info("Deploying JMS Broker");

			DockerUtil.deployDockerStack(swarmServer, "jms", "/docker-compose-jms-broker.yml");

			Thread.sleep(15000L);
		}

		StandaloneMessageReceiver messageReceiver = StandaloneMessageReceiver.create("tcp://jmsbroker.d-201603244.ufsc.br:61616", ThesisExperimentsBootstrap.class.getName());

		ReadyToRunListener listener = new ReadyToRunListener();
		messageReceiver.registerEventListener(listener);

		if (providedExp != null) {
			runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, providedExp);

		} else {

			// exp1
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP1_PARAMS, "exp1-bs1-exs1-rs0", 1, 1, 0));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP1_PARAMS, "exp1-bs2-exs1-rs0", 2, 1, 0));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP1_PARAMS, "exp1-bs3-exs2-rs0", 2, 2, 0));

			// exp2
//			runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP2_PARAMS, "exp2-bs1-exs1-rs0", 1, 1, 0));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP2_PARAMS, "exp2-bs1-exs1-rs2", 1, 1, 2));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP2_PARAMS, "exp2-bs2-exs1-rs1", 2, 1, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP2_PARAMS, "exp2-bs2-exs2-rs1", 2, 2, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP2_PARAMS, "exp2-bs2-exs2-rs2", 2, 2, 2));

			// exp4
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP4_PARAMS, "exp4-bs1-exs1-rs1", 1, 1, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP4_PARAMS, "exp4-bs1-exs1-rs2", 1, 1, 2));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP4_PARAMS, "exp4-bs2-exs1-rs1", 2, 1, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP4_PARAMS, "exp4-bs2-exs2-rs1", 2, 2, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP4_PARAMS, "exp4-bs2-exs2-rs2", 2, 2, 2));

			// exp5
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP5_PARAMS, "exp5-bs1-exs1-rs1", 1, 1, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP5_PARAMS, "exp5-bs1-exs1-rs2", 1, 1, 2));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP5_PARAMS, "exp5-bs2-exs1-rs1", 2, 1, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP5_PARAMS, "exp5-bs2-exs2-rs1", 2, 2, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP5_PARAMS, "exp5-bs2-exs2-rs2", 2, 2, 2));

			// exp6
			 runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs1-exs1-rs1", 1, 1, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs1-exs1-rs2", 1, 1, 2));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs2-exs1-rs1", 2, 1, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs2-exs2-rs1", 2, 2, 1));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs2-exs2-rs2", 2, 2, 2));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs3-exs3-rs3", 3, 3, 3));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs4-exs4-rs4", 4, 4, 4));
			// runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP6_PARAMS, "exp6-bs4-exs1-rs4", 4, 1, 4));

			//runExperiment(swarmServer, dockerClient, listener, deployArch, resetUBLServices, new ExecutionExperimentParams(EXP8_PARAMS, "exp8-bs1-exs1-rs1", 1, 1, 1, 2D));
		}
		System.exit(0);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void runExperiment(String swarmManager, GesellixDockerClient dockerClient, ReadyToRunListener listener, boolean deployArch, boolean resetUBLServices, ExecutionExperimentParams params) throws Exception {

		long s = System.currentTimeMillis();

		logger.info("Running experiment '" + params.executionId + "'");

		UnavailabilityCounter uCounter = new UnavailabilityCounter();

		List<AbstractProcessExperiment> exps = params.processes.stream().map(t -> {
			try {
				return t.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());

		if (deployArch) {

			String[] serverAddresses = getServerAddresses();

			String resp = DockerUtil.removeDockerStack(swarmManager, "ubl");
			logger.info("Remove stack (ubl) --> \n\t" + resp.replaceAll("\n", "\n\t"));
			Thread.sleep(2000L);

			resp = DockerUtil.removeDockerStack(swarmManager, "exp");
			logger.info("Remove stack (exp) --> \n\t" + resp.replaceAll("\n", "\n\t"));

			Thread.sleep(5000L);

			logger.info("Clearing logs --> resilience-service");
			ExperimentsUtil.clearProgramLogs("resilience-service");

			logger.info("Clearing logs --> binding-service");
			ExperimentsUtil.clearProgramLogs("binding-service");

			logger.info("Clearing logs --> discovery-service");
			ExperimentsUtil.clearProgramLogs("discovery-service");

			logger.info("Clearing logs --> ubl-services");
			ExperimentsUtil.clearProgramLogs("ubl-services");

			DeploymentService deploymentService = ExperimentsUtil.getDeploymentService();

			for (AbstractProcessExperiment exp : exps) {
				if (params.adhocServiceDeployment && !CollectionUtils.isEmpty(exp.getServicesDeploymentServers())) {
					String server = exp.getServicesDeploymentServers().get(0);
					List<PlatformManagedService> removed = deploymentService.removePlatformManagedServices(server, Collections.singletonList(exp.getServicesPrimaryCategory()));
					if (!CollectionUtils.isEmpty(removed)) {
						logger.info("Removed adhoc managed services --> " + removed);
					}
				}
			}

			Thread.sleep(5000L);

			int bindingServiceReplicas = params.resilienceConfiguration.getBindingServiceReplicas();
			int executionServiceReplicas = params.resilienceConfiguration.getExecutionServiceReplicas();
			int resilienceServiceReplicas = params.resilienceConfiguration.getResilienceServiceReplicas();

			listener.init(bindingServiceReplicas, executionServiceReplicas, resilienceServiceReplicas, params.ublServicesReplicas);

			logger.info("Running executionId " + params.executionId);

			{
				logger.info("Deploying stack 'ubl'");
				DeployStackConfig deployStackConfig = dockerClient.getDeployStackConfig("ubl", "/docker-compose-ubl-services.yml");
				dockerClient.deployStack("ubl", deployStackConfig);
			}

			DeployStackConfig deployStackConfig = dockerClient.getDeployStackConfig("exp", "/docker-compose-exp.yml");

			StackService bindingService = deployStackConfig.getServices().get("binding-service");
			((Map<String, Object>) ((Map<String, Object>) bindingService.getMode()).get("replicated")).put("replicas", bindingServiceReplicas);
			setCpuLimit(bindingService, params.cpuTime);

			StackService executionService = deployStackConfig.getServices().get("execution-service");
			((Map<String, Object>) ((Map<String, Object>) executionService.getMode()).get("replicated")).put("replicas", executionServiceReplicas);
			setCpuLimit(executionService, params.cpuTime);

			StackService resilienceService = deployStackConfig.getServices().get("resilience-service");
			((Map<String, Object>) ((Map<String, Object>) resilienceService.getMode()).get("replicated")).put("replicas", resilienceServiceReplicas);
			setCpuLimit(resilienceService, params.cpuTime);

			logger.info("Deploying stack 'exp'");
			dockerClient.deployStack("exp", deployStackConfig);

			logger.info("Waiting components bootstrap");
			listener.awaitReady();

			Thread.sleep(15000L);

			logger.info("Removing unused containers and images");
			DockerUtil.removeUnusedDockerContainers(serverAddresses);
			DockerUtil.removeUnusedDockerImages(serverAddresses);
		}

		ExecutorService pool = Executors.newCachedThreadPool();

		List<Future<Object>> futures = new ArrayList<Future<Object>>();

		for (AbstractProcessExperiment exp : exps) {
			exp.init(params);
			exp.deployProcess(params);
		}

		int m = 1;

		params.waitProcessesDeployLatch = new CountDownLatch(exps.size());
		params.deployProcess = false;

		for (AbstractProcessExperiment exp : exps) {

			ExecutionExperimentParams expParams = new ExecutionExperimentParams(params);

			expParams.unavailabilityCounter = uCounter;

			expParams.instanceCreationInitialDelay = expParams.instanceCreationInitialDelay * m++;

			Future<Object> f = pool.submit(() -> {

				for (int i = 1; i <= expParams.experimentIterations; i++) {
					logger.info("[" + exp.getProcessName() + "] Stage: " + i + " - Clearing previous logs for " + expParams.executionId);

					ExperimentsUtil.clearLogs(expParams.executionId, exp.getProcessName());

					logger.info("[" + exp.getProcessName() + "] Stage: " + i + " - Executing process for " + expParams.executionId);

					exp.execute(expParams);
				}

				return null;
			});

			futures.add(f);
		}

		params.waitProcessesDeployLatch.countDown();

		List<ScheduledJob> archUnavailabilityJobs = new ArrayList<ScheduledJob>();

		for (ComponentUnavailabilityConfig cfg : params.componentUnavailabilityConfigs) {
			archUnavailabilityJobs.add(simulateArchitectureComponentUnavailability(swarmManager, cfg, uCounter));

		}

		for (Future<Object> f : futures) {
			f.get();
		}

		for (ScheduledJob job : archUnavailabilityJobs) {
			job.cancel(false);
			job.getJobDone().countDown();
		}

		uCounter.stopCounter();

		Thread.sleep(60 * 1000L);

		Date date = new Date();

		String dateLabel = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss").format(date);

		File dataFolder = new File("data");

		File baseFolder = new File(new File(dataFolder, params.executionId), dateLabel);

		baseFolder.mkdirs();

		File reportFile = new File(baseFolder, dateLabel + "-" + params.executionId + "_report.txt");

		try (PrintWriter pw = new PrintWriter(reportFile)) {

			List<Integer> duplicatedInstances = new ArrayList<Integer>();

			List<Map<String, Object>> notFinishedProcessInstances = ExperimentsQueries.getNotFinishedProcessInstances(duplicatedInstances);

			if (!notFinishedProcessInstances.isEmpty()) {
				System.out.println("Not finished process instances: ");
			}

			List<String> notFinishedStr = new ArrayList<String>();

			for (Map<String, Object> l : notFinishedProcessInstances) {
				Map<String, Object> data = (Map<String, Object>) l.get("_source");
				String p = data.get("processInstanceId") + " - " + data.get("processName");
				System.err.println(p);
				notFinishedStr.add(p);
			}

			pw.println("Not finished process instances (" + notFinishedProcessInstances.size() + "): " + StringUtils.join(notFinishedStr, ", "));
			pw.println("Duplicated (" + duplicatedInstances.size() + "): " + StringUtils.join(duplicatedInstances, ", "));

			if (uCounter.hasAnyCount()) {
				pw.println("Unavailability Counter:");
				pw.println(uCounter.toString());
			}

			System.out.println("Total not finished: " + notFinishedProcessInstances.size());

		}

		File statsFile = new File(baseFolder, dateLabel + "-" + params.executionId + "_stats.txt");

		List<Map<String, Object>> statsList = new ArrayList<Map<String, Object>>();

		try (PrintWriter pw = new PrintWriter(statsFile)) {
			for (AbstractProcessExperiment exp : exps) {
				MetricFiles files = CollectElasticSearchMetrics.collect(params.executionId, exp.getProcessName(), dataFolder, date);
				Map<String, Object> stats = CollectElasticSearchMetrics.getProcessStats(files);
				pw.println(stats.toString());
				statsList.add(stats);
			}

			pw.println();
			pw.println();
			CollectElasticSearchMetrics.printExpTable(statsList, pw);
		}

		BackupElasticSearch.backupElasticSearch(new File(baseFolder, dateLabel + "-" + params.executionId + "_esdata.tar.gz"));

		long d = (System.currentTimeMillis() - s) / 60000;

		logger.info("Experiment '" + params.executionId + "' finished in " + d + " minutes");

	}

	@SuppressWarnings("unchecked")
	private static void setCpuLimit(StackService stackService, Double cpuTime) {

		if (cpuTime != null) {

			Map<String, Object> taskTemplate = ((Map<String, Object>) stackService.getTaskTemplate());
			Map<String, Object> resources = (Map<String, Object>) taskTemplate.get("resources");

			Map<String, Object> limits = new HashMap<String, Object>();
			limits.put("nanoCpus", new BigDecimal(cpuTime * (double) Math.pow(10, 9)));

			resources.put("limits", limits);
		}
	}

	private static String[] getServerAddresses() {
		return ArrayUtils.addAll(DockerServers.getServerAddresses(), "tcp://150.162.1.46:2376", "tcp://150.162.1.88:2376");
	}

	private static ScheduledJob<?> simulateArchitectureComponentUnavailability(String swarmServer, ComponentUnavailabilityConfig cfg, UnavailabilityCounter uCounter) {

		AtomicInteger currentServer = new AtomicInteger();

		AtomicInteger currentNumberOfServers = new AtomicInteger(-1);

		Job job = new Job() {

			public void run(int count, int total) throws Exception {

				List<PlatformManagedService> platformManagedServices = ExperimentsUtil.getDeploymentService().getPlatformManagedServices(swarmServer, Collections.singletonList(cfg.componentName));

				if (platformManagedServices == null || platformManagedServices.isEmpty()) {
					return;
				}

				Map<String, List<ServiceContainer>> groupedByServer = new TreeMap<String, List<ServiceContainer>>();

				for (PlatformManagedService m : platformManagedServices) {
					if (m.getName().endsWith(cfg.componentName)) {
						for (ServiceContainer c : m.getContainers()) {

							String server = c.getDeploymentServer();

							List<ServiceContainer> containers = groupedByServer.get(server);

							if (containers == null) {
								containers = new LinkedList<ServiceContainer>();
								groupedByServer.put(server, containers);
							}

							containers.add(c);
						}
					}
				}

				List<String> deploymentServers = new ArrayList<String>(groupedByServer.keySet());

				int previousCount = currentNumberOfServers.get();

				if (previousCount == -1 || deploymentServers.size() != previousCount) {
					currentNumberOfServers.set(deploymentServers.size());
					currentServer.set(0);
				}

				int index;

				if (USE_RANDOM_ARCH_COMPONENT_UNAVAILABILITY) {
					index = new Random().nextInt(deploymentServers.size());
				} else {
					index = currentServer.getAndIncrement() % currentNumberOfServers.get();
				}

				String server = deploymentServers.get(index);

				try {

					List<ServiceContainer> containers = groupedByServer.get(server);

					ServiceContainer container = containers.get(new Random().nextInt(containers.size()));

					List<ServiceContainer> r = ExperimentsUtil.getDeploymentService().removeServiceContainers(Collections.singletonList(container));

					if (uCounter != null && r != null) {
						uCounter.incrementArchComponentUnavailability(cfg.componentName, r.size());
					}

					logger.info("Changing architecture component '" + cfg.componentName + "' container availability at " + server + " (" + count + "/" + total + ") --> " + r);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		};

		return ExperimentsUtil.scheduleAtFixedRate(scheduler, job, cfg.initialDelay, cfg.period, cfg.repeats);
	}

	private static class ReadyToRunListener implements br.ufsc.gsigma.infrastructure.util.messaging.EventListener {

		private CountDownLatch bindingServiceReady;

		private CountDownLatch executionServiceReady;

		private CountDownLatch resilienceServiceReady;

		private CountDownLatch ublServicesReady;

		ReadyToRunListener() {
		}

		void init(int bindingServiceReplicas, int executionServiceReplicas, int resilienceServiceReplicas, int ublServicesReplicas) {
			bindingServiceReady = new CountDownLatch(bindingServiceReplicas);
			executionServiceReady = new CountDownLatch(executionServiceReplicas);
			resilienceServiceReady = new CountDownLatch(resilienceServiceReplicas);
			ublServicesReady = new CountDownLatch(ublServicesReplicas);
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

				} else if (evt.getComponentName().equals(ComponentName.UBL_SERVICES)) {
					ublServicesReady.countDown();
				}
			}
		}

		public void awaitReady() throws InterruptedException {

			bindingServiceReady.await();
			logger.info("BindingService is ready");

			executionServiceReady.await();
			logger.info("ExecutionService is ready");

			resilienceServiceReady.await();
			logger.info("ResilienceService is ready");

			ublServicesReady.await();
			logger.info("UBL Services is ready");
		}
	}

}
