package br.ufsc.gsigma.architecture.experiments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.ufsc.gsigma.architecture.experiments.util.ExperimentsUtil;
import br.ufsc.gsigma.architecture.experiments.util.Job;
import br.ufsc.gsigma.architecture.experiments.util.ScheduledJob;
import br.ufsc.gsigma.architecture.experiments.util.UnavailabilityCounter;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.ws.ServiceClient;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedService;
import br.ufsc.gsigma.services.deployment.model.ServiceContainer;
import br.ufsc.gsigma.services.specifications.ubl.interfaces.UBLServicesAdminService;

public abstract class AbstractProcessExperiment {

	public static final String EXECUTION_SERVICE_URL = "http://executionservice.d-201603244.ufsc.br:7000/services/ProcessExecutionService";

	// public static final String EXECUTION_SERVICE_URL = "http://server2.d-201603244.ufsc.br:7000/services/ProcessExecutionService";

	public static final String DEPLOYMENT_SERVICE_URL = "http://deploymentservice.d-201603244.ufsc.br:6000/services/DeploymentService";

	protected final Logger logger = Logger.getLogger(getClass());
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
	protected List<ScheduledJob<?>> jobs = new CopyOnWriteArrayList<ScheduledJob<?>>();

	private static final UBLServicesAdminService ublServicesAdminService;

	static {
		ublServicesAdminService = ServiceClient.getClient(UBLServicesAdminService.class, //
				"http://" + ServicesAddresses.UBL_SERVICES_HOSTNAME + ":" + ServicesAddresses.UBL_SERVICES_PORT + "/services/UBLServicesAdminService", //
				CxfUtil.getClientFeatures());
	}

	protected void deployProcess(ExecutionExperimentParams params) throws Exception {
		logger.info("Deploying " + getProcessName());

		URL url = getDeployProcessRequestFile();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = null;

		try (InputStream in = url.openStream()) {
			doc = builder.parse(in);
		}

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		NodeList serviceConfigs = (NodeList) xpath.compile("//serviceConfig").evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < serviceConfigs.getLength(); i++) {
			((Element) serviceConfigs.item(i)).setAttribute("numberOfReplicas", String.valueOf(params.adhocUblServicesReplicas));
		}

		NodeList adhocFlag = (NodeList) xpath.compile("//flags/entry[@key='DISABLE_ADHOC_SERVICE_DEPLOYMENT']").evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < adhocFlag.getLength(); i++) {
			((Element) adhocFlag.item(i)).setAttribute("value", String.valueOf(!params.adhocServiceDeployment));
		}

		NodeList disableServiceMonitoringFlag = (NodeList) xpath.compile("//flags/entry[@key='DISABLE_SERVICE_MONITORING']").evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < disableServiceMonitoringFlag.getLength(); i++) {
			((Element) disableServiceMonitoringFlag.item(i)).setAttribute("value", String.valueOf(params.disableMonitoring));
		}

		boolean forceProtocolConverter = !StringUtils.isBlank(params.protocolConverter);

		NodeList forceProtocolConverterFlag = (NodeList) xpath.compile("//flags/entry[@key='FORCE_PROTOCOL_CONVERTER']").evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < forceProtocolConverterFlag.getLength(); i++) {
			((Element) forceProtocolConverterFlag.item(i)).setAttribute("value", String.valueOf(forceProtocolConverter));
		}

		NodeList forceProtocolConverterAtt = (NodeList) xpath.compile("//attributes/entry[@key='PROTOCOL_CONVERTER']").evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < forceProtocolConverterAtt.getLength(); i++) {
			((Element) forceProtocolConverterAtt.item(i)).setAttribute("value", StringUtils.defaultIfBlank(params.protocolConverter, ""));
		}

		File f = File.createTempFile("deploy-", "-" + getProcessName() + ".xml");

		try {
			f.deleteOnExit();

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(f);

			transformer.transform(source, result);

			String resp = postSoap(DEPLOYMENT_SERVICE_URL, f.toURI().toURL());
			logger.info("Deployed " + getProcessName() + " -- > " + resp);

		} finally {
			f.delete();
		}

	}

	protected Map<String, Integer> getServicesPathAvailability() {
		Map<String, Integer> servicePath = new HashMap<String, Integer>();

		for (String path : getServicePaths()) {
			servicePath.put(path, 1);
		}

		return servicePath;
	}

	protected void waitLatchIfNecessary(ExecutionExperimentParams parameterObject) throws InterruptedException {
		if (parameterObject.deployProcess && parameterObject.waitProcessesDeployLatch != null) {

			logger.info("Latch countDown");
			parameterObject.waitProcessesDeployLatch.countDown();

			logger.info("Waiting latch");
			parameterObject.waitProcessesDeployLatch.await();
			logger.info("Latch done, continuing...");

		}
	}

	protected List<String> getUBLServicesServers() {
		return Arrays.asList( //
				"http://ublservices.d-201603244.ufsc.br:11000", //
				"http://ublservices2.d-201603244.ufsc.br:11001", //
				"http://ublservices3.d-201603244.ufsc.br:11002", //
				"http://ublservices4.d-201603244.ufsc.br:11003", //
				"http://ublservices5.d-201603244.ufsc.br:11004" //
		//
		);
	}

	protected List<String> getServicesPathsSuffixes() {

		List<String> servicesClassifications = getServicePaths();

		List<String> result = new ArrayList<String>(servicesClassifications.size());

		for (String s : servicesClassifications) {

			int idx = s.lastIndexOf('/');
			idx = s.lastIndexOf('/', idx - 1);
			idx = s.lastIndexOf('/', idx - 1);

			result.add(s.substring(idx + 1));

		}

		return result;
	}

	@SuppressWarnings("unchecked")
	protected List<String> getServicesDeploymentServers() {
		return Collections.EMPTY_LIST;
	}

	protected List<String> getDefaultServicesDeploymentServers() {
		return Collections.singletonList(DockerServers.SWARM_MANAGER);
	}

	protected List<String> getDefaultTargetNodesServers() {
		return Arrays.asList( //
				DockerServers.SERVER_150_162_6_63, //
				DockerServers.SERVER_150_162_6_210, //
				DockerServers.SERVER_150_162_6_210 //
		);
	}

	protected String getServicesDockerImage() {
		return "d-201603244.ufsc.br/ubl-services:latest";
	}

	protected void init(ExecutionExperimentParams params) {

		List<String> deploymentServers = getServicesDeploymentServers();

		if (params.adhocServiceDeployment && !deploymentServers.isEmpty()) {

			String image = getServicesDockerImage();
			List<String> servers = getDefaultTargetNodesServers();

			Map<String, String> r = ExperimentsUtil.getDeploymentService().pullImage(servers, image);

			logger.info("Pulling image " + getServicesDockerImage() + " in servers " + servers + " --> " + r);
		}
	}

	protected void resetEnv() {

		List<String> servicesPath = getServicePaths();
		logger.info("Setting services " + servicesPath + " availability to 'available'");
		getUblServicesAdminService().setHostsServicesAvailability(servicesPath, getUBLServicesServers(), true);

		List<String> deploymentServers = getServicesDeploymentServers();

		if (!deploymentServers.isEmpty()) {
			// List<ServiceContainer> removedContainers = getDeploymentService().removeServiceContainersByServersServicePaths(deploymentServers, getServicesPathsSuffixes());

			List<PlatformManagedService> removedPMServices = ExperimentsUtil.getDeploymentService().removePlatformManagedServices(deploymentServers.get(0), Collections.singletonList(getServicesPrimaryCategory()));

			logger.info("Removing managed service containers --> " + removedPMServices);
		}
	}

	protected void simulateServiceUnavailability(int initialDelay, int period, int numberOfServiceReplicas, UnavailabilityCounter uCounter) {

		// Random changing service availability

		Map<String, Integer> servicePath = getServicesPathAvailability();

		List<String> hosts = getUBLServicesServers();

		// Simulating service unavailability
		scheduleAtFixedRate(initialDelay, period, -1, (count, total) -> {
			try {
				List<String> r = getUblServicesAdminService().setRandomServiceAvailabilityCombination(servicePath, hosts);

				if (uCounter != null) {
					for (Entry<String, Integer> s : servicePath.entrySet()) {
						uCounter.incrementProcessServicesUnavailability(s.getKey(), numberOfServiceReplicas - s.getValue());
					}
				}

				logger.info("Changing service availability... (" + count + "/" + total + ") --> " + r);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	protected void simulateServiceContainerUnavailability(List<String> servers, int initialDelay, int period, int repeats, UnavailabilityCounter uCounter) {

		AtomicInteger currentServer = new AtomicInteger();

		AtomicInteger currentNumberOfServers = new AtomicInteger(-1);

		// Simulating service unavailability
		scheduleAtFixedRate(initialDelay, period, repeats, (count, total) -> {

			List<PlatformManagedService> platformManagedServices = ExperimentsUtil.getDeploymentService().getPlatformManagedServices(servers.get(0), Collections.singletonList(getServicesPrimaryCategory()));

			if (platformManagedServices == null || platformManagedServices.isEmpty()) {
				return;
			}

			Map<String, List<ServiceContainer>> groupedByServer = new TreeMap<String, List<ServiceContainer>>();

			for (PlatformManagedService m : platformManagedServices) {
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

			List<String> deploymentServers = new ArrayList<String>(groupedByServer.keySet());

			int previousCount = currentNumberOfServers.get();

			if (previousCount == -1 || deploymentServers.size() != previousCount) {
				currentNumberOfServers.set(deploymentServers.size());
				currentServer.set(0);
			}

			int index = new Random().nextInt(deploymentServers.size());

			// int index = currentServer.getAndIncrement() % currentNumberOfServers.get();

			String server = deploymentServers.get(index);

			try {
				// List<ServiceContainer> r = getDeploymentService().removeServiceContainersByServersServicePaths(Collections.singletonList(server), getServicesPathsSuffixes());

				List<ServiceContainer> r = ExperimentsUtil.getDeploymentService().removeServiceContainers(groupedByServer.get(server));

				if (uCounter != null && r != null) {
					for (ServiceContainer s : r) {
						uCounter.incrementAdhocProcessServicesUnavailability(s.getServiceClassification(), 1);
					}
				}

				logger.info("Changing service deployment container availability at " + server + " (" + count + "/" + total + ") --> " + r);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	protected URL getExperimentResource(String file) throws MalformedURLException {
		return AbstractProcessExperiment.class.getResource("/br/ufsc/gsigma/architecture/experiments/files/" + file);
	}

	public void execute(ExecutionExperimentParams parameterObject) throws Exception {

		this.jobs.clear();
		this.scheduler = Executors.newScheduledThreadPool(5);

		try {
			executeInternal(parameterObject);
		} finally {
			scheduler.shutdownNow();
			scheduler = null;
		}
	}

	protected abstract String getServicesPrimaryCategory();

	public abstract String getProcessName();

	public abstract URL getDeployProcessRequestFile() throws Exception;

	public abstract URL getExecuteProcessRequestFile() throws Exception;

	protected abstract List<String> getServicePaths();

	protected ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	protected void executeInternal(ExecutionExperimentParams params) throws Exception {

		int offset = 0;

		if (params.resetUBLServices) {
			resetEnv();
		}

		if (params.deployProcess) {
			deployProcess(params);
		}

		waitLatchIfNecessary(params);

		// Simulating creation of service instances
		ScheduledJob<?> f1 = scheduleAtFixedRate(params.instanceCreationInitialDelay, params.instanceCreationInterval, params.repeats + offset, (count, total) -> {
			logger.info("Starting process instance... (" + count + "/" + total + ")");
			try {
				postSoap(EXECUTION_SERVICE_URL, getExecuteProcessRequestFile(), params.executionId);
			} catch (Throwable e) {
				e.printStackTrace();
			}

			if (params.simulateServicesUnvailability && count == params.serviceUnavailabilityStartOffset) {

				int delay = params.serviceUnavailabilityInitialDelay;

				simulateServiceUnavailability(delay, params.serviceUnavailabilityInterval, params.ublServicesReplicas, params.unavailabilityCounter);

			} else if (params.simulateAdhocServicesUnvailability && params.adhocServiceDeployment && count == params.serviceContainerUnavailabilityStartOffset) {

				int delay = params.serviceContainerUnavailabilityInitialDelay;

				simulateServiceContainerUnavailability(getServicesDeploymentServers(), delay, params.serviceContainerUnavailabilityInterval, params.repeats, params.unavailabilityCounter);
			}

		});

		f1.waitJobDone();

		cancelJobs();

		// waitJobs();
	}

	public static UBLServicesAdminService getUblServicesAdminService() {
		return ublServicesAdminService;
	}

	protected ScheduledJob<?> scheduleAtFixedRate(long initialDelay, long period, final int repeats, final Job job) {
		ScheduledJob<?> resp = ExperimentsUtil.scheduleAtFixedRate(scheduler, job, initialDelay, period, repeats);
		jobs.add(resp);
		return resp;
	}

	protected void cancelJobs() throws InterruptedException {
		for (ScheduledJob<?> j : jobs) {
			j.cancel(true);
			j.getJobDone().countDown();
		}
	}

	protected void waitJobs() throws InterruptedException {

		// int n = 0;
		//
		// while (n != jobs.size()) {
		//
		// for (ScheduledJob<?> j : jobs) {
		// if (!j.isDone())
		// j.waitJobDone();
		// }
		//
		// n = jobs.size();
		// }

	}

	protected String postSoap(String url, URL content, String executionId) throws Exception {

		String xml = IOUtils.toString(content, Charset.forName("UTF-8"));

		return postSoap(url, xml, executionId);
	}

	protected String postSoap(String url, String xml, String executionId) throws Exception {
		return postSoap(url, xml, executionId, 0);
	}

	protected String postSoap(String url, String xml, String executionId, int repeat) throws Exception {

		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

		conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		conn.setRequestMethod("POST");

		conn.setDoOutput(true);

		xml = xml.replaceFirst("(?s)<ectx:executionId[^>]*>.*?</ectx:executionId>", //
				"<ectx:executionId>" + executionId + "</ectx:executionId>");

		IOUtils.write(xml, conn.getOutputStream(), Charset.forName("UTF-8"));

		int respCode = conn.getResponseCode();

		String response = null;

		try (InputStream in = respCode == 200 ? conn.getInputStream() : conn.getErrorStream();) {
			try {
				response = IOUtils.toString(in, StandardCharsets.UTF_8);
			} catch (IOException e) {
				if (repeat++ < 5) {
					Thread.sleep(3000L);
					return postSoap(url, xml, executionId, repeat);
				} else {
					throw e;
				}
			}
		}

		if (repeat++ < 5 && (respCode == 500 || respCode == 503)) {
			Thread.sleep(3000L);
			return postSoap(url, xml, executionId, repeat);
		}

		if (respCode != 200) {

			String errorMessage = null;

			try (InputStream in = conn.getErrorStream()) {
				try {
					errorMessage = IOUtils.toString(in, StandardCharsets.UTF_8);
				} catch (IOException e) {
				}
			}

			throw new Exception("Error executing request: " + respCode + " - " + conn.getResponseMessage() + " --> " + errorMessage);
		}

		return response;
	}

	protected String postSoap(String url, URL content) throws Exception {

		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

		conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		conn.setRequestMethod("POST");

		conn.setDoOutput(true);

		try (InputStream in = content.openStream()) {
			try (OutputStream out = conn.getOutputStream()) {
				IOUtils.copy(in, conn.getOutputStream());
			}
		}

		int respCode = conn.getResponseCode();

		String response;

		try (InputStream in = respCode == 200 ? conn.getInputStream() : conn.getErrorStream();) {
			response = IOUtils.toString(in, StandardCharsets.UTF_8);
		}

		if (respCode != 200) {
			throw new Exception("Error executing request: " + respCode + " - " + conn.getResponseMessage());
		}

		return response;
	}

}