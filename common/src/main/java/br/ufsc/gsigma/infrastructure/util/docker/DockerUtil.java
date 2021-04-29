package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig.Builder;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;

import br.ufsc.gsigma.infrastructure.util.docker.ContainerLogPollSupport.ContainerLogClient;
import br.ufsc.gsigma.infrastructure.util.docker.ContainerLogPollSupport.ContainerLogPollJob;
import br.ufsc.gsigma.infrastructure.util.docker.ContainerLogPollSupport.ContainerLogScheduledThreadPoolExecutor;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;
import br.ufsc.gsigma.infrastructure.util.maven.Pom;

//https://github.com/docker-java/docker-java/wiki
public abstract class DockerUtil {

	private static final String IMAGE_ORACLE_JAVA8 = "oracle-java8:1.0.0";

	public static final String JAVA_APP_PATH_BASE = "/opt/java";

	private static final Logger logger = LoggerFactory.getLogger(DockerUtil.class);

	private static final DockerClient LOCALHOST_DOCKER_CLIENT = getDockerClient("tcp://127.0.0.1:2375");

	private static DockerClient docker = LOCALHOST_DOCKER_CLIENT;

	private static ContainerLogScheduledThreadPoolExecutor logPollScheduler;

	static {
		String dockerHost = "tcp://127.0.0.1:2375";

		getDockerClient(dockerHost);

	}

	public static String removeDockerStack(String dockerHost, String stackName) throws Exception {

		String path = DockerServers.CERT_PATH.getAbsolutePath();

		dockerHost = dockerHost.replaceAll("http://", "tcp://");

		Process p = Runtime.getRuntime().exec("cmd /c docker -H " + dockerHost + "  --tls --tlscacert=" + path + "/ca.pem --tlscert=" + path + "/cert.pem --tlskey=" + path + "/key.pem stack rm " + stackName);
		p.waitFor();

		StringBuilder sb = new StringBuilder();
		sb.append(IOUtils.toString(p.getInputStream(), "UTF-8"));
		sb.append(IOUtils.toString(p.getErrorStream(), "UTF-8"));

		return sb.toString();
	}

	public static void removeUnusedDockerContainers(String... dockerHosts) throws Exception {

		String path = DockerServers.CERT_PATH.getAbsolutePath();

		for (String dockerHost : dockerHosts) {

			dockerHost = dockerHost.replaceAll("http://", "tcp://");

			if (!dockerHost.startsWith("tcp://")) {
				dockerHost = "tcp://" + dockerHost;
			}

			Process p = Runtime.getRuntime().exec("cmd /c docker -H " + dockerHost + "  --tls --tlscacert=" + path + "/ca.pem --tlscert=" + path + "/cert.pem --tlskey=" + path + "/key.pem container prune -f");
			p.waitFor();

			StringBuilder sb = new StringBuilder();
			sb.append(IOUtils.toString(p.getInputStream(), "UTF-8"));
			sb.append(IOUtils.toString(p.getErrorStream(), "UTF-8"));

			System.out.println(sb.toString());
		}
	}

	public static void removeUnusedDockerImages(String... dockerHosts) throws Exception {

		String path = DockerServers.CERT_PATH.getAbsolutePath();

		for (String dockerHost : dockerHosts) {

			dockerHost = dockerHost.replaceAll("http://", "tcp://");

			if (!dockerHost.startsWith("tcp://")) {
				dockerHost = "tcp://" + dockerHost;
			}

			Process p = Runtime.getRuntime().exec("cmd /c docker -H " + dockerHost + "  --tls --tlscacert=" + path + "/ca.pem --tlscert=" + path + "/cert.pem --tlskey=" + path + "/key.pem image prune -af");
			p.waitFor();

			StringBuilder sb = new StringBuilder();
			sb.append(IOUtils.toString(p.getInputStream(), "UTF-8"));
			sb.append(IOUtils.toString(p.getErrorStream(), "UTF-8"));

			System.out.println(sb.toString());
		}
	}

	public static String deployDockerStack(String dockerHost, String stackName, String yamlFile) throws Exception {

		String path = DockerServers.CERT_PATH.getAbsolutePath();

		dockerHost = dockerHost.replaceAll("http://", "tcp://");

		URL res = DockerUtil.class.getResource(yamlFile);

		String f = Paths.get(res.toURI()).toString();

		Process p = Runtime.getRuntime().exec("cmd /c docker -H " + dockerHost + "  --tls --tlscacert=" + path + "/ca.pem --tlscert=" + path + "/cert.pem --tlskey=" + path + "/key.pem stack deploy --prune --with-registry-auth -c " + f + " " + stackName);
		p.waitFor();

		StringBuilder sb = new StringBuilder();
		sb.append(IOUtils.toString(p.getInputStream(), "UTF-8"));
		sb.append(IOUtils.toString(p.getErrorStream(), "UTF-8"));

		return sb.toString();
	}

	public static DockerClient getDockerClient(String dockerHost) {
		return getDockerClient(dockerHost, null);
	}

	public static DockerClient getDockerClient(String dockerHost, File dockerCertPath) {

		Builder cfgb = DefaultDockerClientConfig.createDefaultConfigBuilder() //
				.withDockerHost(dockerHost) //
				.withDockerConfig(null);

		if (dockerCertPath != null && dockerCertPath.exists()) {
			cfgb.withDockerTlsVerify(true);
			cfgb.withDockerCertPath(dockerCertPath.getAbsolutePath());
		}

		DockerClientConfig config = cfgb.build();

		CustomJerseyDockerCmdExecFactory dockerCmdExecFactory = new CustomJerseyDockerCmdExecFactory();

		return new DockerClientDecorator(DockerClientBuilder.getInstance(config) //
				.withDockerCmdExecFactory(dockerCmdExecFactory) //
				.build(), config);
	}

	public static DockerClient getDockerClient() {
		return docker;
	}

	public static void setDockerClient(DockerClient docker) {
		DockerUtil.docker = docker;
	}

	private static boolean isImageExists(String tagName) {
		for (Image i : docker.listImagesCmd().exec()) {
			if (ArrayUtils.contains(i.getRepoTags(), tagName)) {
				return true;
			}
		}
		return false;
	}

	public static List<DockerImageResource> getDockerImageResourcesFromMavenPOM(String pomFile) throws Exception {
		return getDockerImageResourcesFromMavenPOM(MavenUtil.getPOMFromFile(pomFile));
	}

	public static List<DockerImageResource> getDockerImageResourcesFromMavenPOM(Pom pom) throws Exception {

		List<DockerImageResource> resources = new LinkedList<DockerImageResource>();

		final String prefix = "/classes/";

		for (File f : MavenUtil.getClassPathFromMavenPOM(pom)) {
			if (f.isDirectory()) {
				for (File f2 : FileUtils.listFiles(f, TrueFileFilter.TRUE, TrueFileFilter.TRUE)) {

					String name = br.ufsc.gsigma.infrastructure.util.FileUtils.adjustAbsolutePath(f2);

					int idx = name.indexOf(prefix);

					if (idx > 0)
						name = name.substring(idx + prefix.length());

					resources.add(new FileDockerImageResouce("bin/" + name, f2));
				}
			} else {
				resources.add(new FileDockerImageResouce("lib/" + f.getName(), f));
			}
		}

		return resources;
	}

	@SuppressWarnings("unchecked")
	public static InputStream createJavaApplicationImageFromPOM(String pomFile, String[] jreArgs, String mainClass, String[] appArgs, List<DockerImageResource> resources) throws Exception {
		return createJavaApplicationImageFromPOM(pomFile, jreArgs, mainClass, appArgs, resources, Collections.EMPTY_LIST);
	}

	@SuppressWarnings("unchecked")
	public static InputStream createJavaApplicationImageFromPOM(String pomFile, String[] jreArgs, String mainClass, String[] appArgs, List<DockerImageResource> resources, List<String> volumes) throws Exception {

		Pom pom = MavenUtil.getPOMFromFile(pomFile);

		resources = new LinkedList<DockerImageResource>(resources != null ? resources : Collections.EMPTY_LIST);
		resources.addAll(getDockerImageResourcesFromMavenPOM(pom));

		return createJavaApplicationImage(pom.getArtifactId(), jreArgs, mainClass, appArgs, resources, volumes);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static InputStream createJavaApplicationImage(String appName, String[] jreArgs, String mainClass, String[] appArgs, List<DockerImageResource> resources, List<String> volumes) throws Exception {
		try {

			deployOracleJava8ImageIfNecessary();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(baos)))) {

				tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

				String path = JAVA_APP_PATH_BASE + "/" + appName;

				List<String> dockerFilesLines = new LinkedList( //
						Arrays.asList( //
								"FROM " + IMAGE_ORACLE_JAVA8, //
								"COPY java " + path + "/", //
								"WORKDIR " + path, //
								"RUN chmod +x " + path + "/startup.sh", //
								"CMD [\"" + path + "/startup.sh\"]")//
				);

				if (volumes != null && !volumes.isEmpty()) {

					StringBuilder sb = new StringBuilder();
					sb.append("VOLUME [");

					int i = 0;

					for (String volume : volumes) {

						sb.append("\"" + volume + "\"");

						if (i++ < volumes.size() - 1)
							sb.append(", ");
					}
					sb.append("]");

					dockerFilesLines.add(sb.toString());
				}

				writeEntry("Dockerfile", tar, dockerFilesLines.toArray(new String[dockerFilesLines.size()]));

				writeEntry("java/startup.sh", tar, //
						"#!/bin/bash", //
						"exec java" + (jreArgs != null ? " " + StringUtils.join(jreArgs, " ") : "") + " -cp bin:lib/* " + mainClass + (appArgs != null ? " " + StringUtils.join(appArgs, " ") : "") //
				);

				if (resources != null) {
					for (DockerImageResource i : resources) {
						i.process(tar, "java");
					}
				}
			}

			return new ByteArrayInputStream(baos.toByteArray());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	protected static void deployOracleJava8ImageIfNecessary() throws IOException, Exception {
		if (!isImageExists(IMAGE_ORACLE_JAVA8)) {
			deployImage(createOracleJava8Image(), IMAGE_ORACLE_JAVA8);
		}
	}

	public static boolean isImageExistsByTagName(String tagName) {
		return getImageIdByTagName(tagName) != null;
	}

	public static String getImageIdByTagName(String tagName) {
		return getImageIdByTagName(docker, tagName);
	}

	public static String getImageIdByTagName(DockerClient docker, String tagName) {

		List<Image> images = docker.listImagesCmd().exec();
		for (Image image : images) {
			if (image.getRepoTags() != null) {
				for (String tag : image.getRepoTags()) {
					if (tagName.contains(":") && tag.equals(tagName) || tag.startsWith(tagName + ":")) {
						return image.getId();
					}
				}
			}
		}
		return null;
	}

	public static String pullImage(DockerClient docker, String imageName) {

		String tag = null;

		String registry = imageName;

		if (registry.contains(":")) {
			String[] sp = registry.split(":");
			registry = sp[0];
			tag = sp[1];
		}

		PullImageCmd cmd = docker.pullImageCmd(registry);

		if (tag != null) {
			cmd.withTag(tag);
		}

		cmd.withAuthConfig(DockerRegistries.DEFAULT_REGISTRY_AUTH_CONFIG);

		PullImageResultCallback callback = null;

		try {
			callback = cmd.exec(new PullImageResultCallback() {
				@Override
				public void onNext(PullResponseItem object) {
					System.out.print(object);
				}
			});

			callback.awaitCompletion();

			return getImageIdByTagName(docker, imageName);

		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			if (callback != null) {
				try {
					callback.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public static String deployImage(File f, String tagName) throws IOException {
		try (InputStream in = new FileInputStream(f)) {
			return deployImage(in, tagName);
		}
	}

	public static String deployImage(InputStream inputStream, String tagName) throws IOException {

		BuildImageResultCallback callback = new BuildImageResultCallback() {
			@Override
			public void onNext(BuildResponseItem item) {
				if (item.getStream() != null)
					System.out.print(item.getStream());
				super.onNext(item);
			}
		};

		try (InputStream tarInputStream = inputStream) {
			return docker.buildImageCmd(tarInputStream) //
					.withTags(Collections.singleton(tagName)) //
					.exec(callback).awaitImageId();
		}
	}

	public static String getContainerIdByName(DockerClient docker, String containerName) {
		Container c = getContainerByName(docker, containerName);
		return c != null ? c.getId() : null;
	}

	public static boolean isContainerRunningById(DockerClient docker, String containerId) {
		try {
			InspectContainerResponse resp = docker.inspectContainerCmd(containerId).exec();
			return BooleanUtils.isTrue(resp.getState().getRunning());
		} catch (NotFoundException e) {
			return false;
		}
	}

	public static boolean isContainerExists(String containerId) {
		try {
			InspectContainerResponse resp = docker.inspectContainerCmd(containerId).exec();
			return resp.getId() != null;
		} catch (NotFoundException e) {
			return false;
		}
	}

	public static Container getContainerByName(DockerClient docker, String containerName) {

		List<Container> containers = docker.listContainersCmd() //
				.withShowAll(true) //
				.exec();

		for (Container c : containers) {
			if (ArrayUtils.contains(c.getNames(), "/" + containerName)) {
				return c;
			}
		}
		return null;
	}

	public static String createContainer(DockerClient docker, String imageId, String containerName, int... ports) {

		String[] portMappings = new String[ports != null ? ports.length : 0];

		if (ports != null) {
			for (int i = 0; i < ports.length; i++) {
				portMappings[i] = String.valueOf(ports[i]);
			}
		}
		return createContainer(docker, imageId, containerName, portMappings);
	}

	public static String createContainer(DockerClient docker, String imageId, String containerName, String... portMappings) {
		return createContainer(docker, imageId, containerName, portMappings, null, null);
	}

	public static String createContainer(DockerClient docker, String imageId, String containerName, String[] portMappings, String[] volumeMappings) {
		return createContainer(docker, imageId, containerName, portMappings, volumeMappings, null);
	}

	public static String createContainer(DockerClient docker, String imageId, String containerName, String[] portMappings, String[] volumeMappings, Map<String, String> variables) {

		CreateContainerCmd cmd = createContainerCmd(docker, imageId, containerName, portMappings, volumeMappings, variables, false, false);

		String containerId = cmd.exec().getId();

		logger.info("Container successfully created with id " + containerId + " from image with name " + containerName + (variables != null && !variables.isEmpty() ? " and variables " + variables : ""));

		return containerId;
	}

	public static CreateContainerCmd createContainerCmd(DockerClient docker, String imageId, String containerName, String[] portMappings, String[] volumeMappings) {
		return createContainerCmd(docker, imageId, containerName, portMappings, volumeMappings, (Map<String, String>) null, false, false);
	}

	public static CreateContainerCmd createContainerCmd(DockerClient docker, String imageId, String containerName, String[] portMappings, String[] volumeMappings, String[] variables) {
		return createContainerCmd(docker, imageId, containerName, portMappings, volumeMappings, parseVariables(variables), false, false);
	}

	public static CreateContainerCmd createContainerCmd(DockerClient docker, String imageId, String containerName, String[] portMappings, String[] volumeMappings, Map<String, String> variables, boolean removeContainer, boolean forceRemove) {

		if (removeContainer) {
			Container container = getContainerByName(docker, containerName);

			if (container != null) {

				String containerId = container.getId();

				if (!forceRemove && isContainerRunning(container)) {
					logger.info("Stopping container " + containerId + " with name " + containerName);
					docker.stopContainerCmd(containerId).exec();
				}

				logger.info("Removing container " + containerId + " with name " + containerName);
				docker.removeContainerCmd(containerId) //
						.withForce(forceRemove) //
						.exec();
			}
		}

		logger.info("Creating container from image " + imageId + " with name " + containerName);

		CreateContainerCmd cmd = docker.createContainerCmd(imageId).withName(containerName);

		if (portMappings != null && portMappings.length > 0) {

			List<ExposedPort> exposedPorts = new LinkedList<ExposedPort>();

			Ports portBindings = new Ports();

			for (String portMapping : portMappings) {

				boolean isUDP = false;

				if (portMapping.startsWith("TCP:")) {
					portMapping = portMapping.substring(4);
				} else if (portMapping.startsWith("UDP:")) {
					portMapping = portMapping.substring(4);
					isUDP = true;
				}

				String[] s = portMapping.split(":");

				int containerPort = s.length > 1 ? Integer.valueOf(s[0]) : Integer.valueOf(s[0]);
				int hostPort = s.length > 1 ? Integer.valueOf(s[1]) : Integer.valueOf(s[0]);

				ExposedPort exposed = isUDP ? ExposedPort.udp(containerPort) : ExposedPort.tcp(containerPort);

				exposedPorts.add(exposed);

				portBindings.bind(exposed, Binding.bindPort(hostPort));

			}
			cmd.withExposedPorts(exposedPorts);
			cmd.withPortBindings(portBindings);
		}

		if (volumeMappings != null && volumeMappings.length > 0) {

			List<Volume> volumes = new ArrayList<Volume>(volumeMappings.length);

			List<Bind> binds = new ArrayList<Bind>(volumeMappings.length);

			for (String vStr : volumeMappings) {

				int idx = vStr.lastIndexOf(':');

				if (idx == -1)
					throw new IllegalArgumentException("Invalid volume bind definition: " + vStr);

				String hostPath = vStr.substring(0, idx);
				String vName = vStr.substring(idx + 1);

				Volume v = new Volume(vName);
				volumes.add(v);

				binds.add(new Bind(hostPath, v, AccessMode.rw));
			}
			cmd.withVolumes(volumes);
			cmd.withBinds(binds);
		}

		boolean hasVariables = variables != null && !variables.isEmpty();

		if (hasVariables) {
			cmd.withEnv(variables.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList()));
		}
		return cmd;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> parseVariables(String... variables) {

		if (variables == null)
			return Collections.EMPTY_MAP;

		Map<String, String> variablesMap = new LinkedHashMap<String, String>();

		String key = null;
		String value = null;

		for (String s : variables) {

			if (key == null)
				key = s;

			else if (value == null)
				value = s;

			if (key != null && value != null) {
				variablesMap.put(key, value);
				key = null;
				value = null;
			}
		}
		return variablesMap;
	}

	private static boolean isContainerRunning(Container container) {
		return container.getStatus().toLowerCase().startsWith("up ");
	}

	public static void startContainer(DockerClient docker, String containerId) {
		docker.startContainerCmd(containerId).exec();
	}

	public static void stopContainer(DockerClient docker, String containerId) {
		docker.stopContainerCmd(containerId).exec();
	}

	public static void pollContainerLogs(String containerId) throws Exception {
		pollContainerLogs(containerId, System.out, System.err);
	}

	public static void pollContainerLogs(String containerId, PrintStream stdout, PrintStream stderr) throws Exception {

		if (logPollScheduler == null) {
			synchronized (DockerUtil.class) {
				if (logPollScheduler == null) {
					logPollScheduler = new ContainerLogScheduledThreadPoolExecutor(1);
				}
			}
		}

		ContainerLogPollJob job = logPollScheduler.getContainerLogPollJob(containerId);

		if (job == null) {
			job = new ContainerLogPollJob(docker, containerId);
			logPollScheduler.scheduleAtFixedRate(job, 0, 100, TimeUnit.MILLISECONDS);
		}

		job.addClient(new ContainerLogClient(stdout, stderr));
	}

	private static InputStream createOracleJava8Image() throws Exception {

		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(baos)))) {

				tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

				byte[] dockerFileBytes = null;

				try (InputStream in = DockerUtil.class.getResourceAsStream("/br/ufsc/gsigma/architecture/bootstrap/docker/images/oracle-java8/Dockerfile")) {
					dockerFileBytes = IOUtils.toByteArray(in);
				}

				TarArchiveEntry entry = new TarArchiveEntry("Dockerfile");
				entry.setSize(dockerFileBytes.length);

				tar.putArchiveEntry(entry);
				tar.write(dockerFileBytes);
				tar.closeArchiveEntry();

			}

			return new ByteArrayInputStream(baos.toByteArray());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private static void writeEntry(String filename, TarArchiveOutputStream tar, String... lines) throws IOException {

		StringWriter sw = new StringWriter();

		PrintWriter pw = new PrintWriter(sw);

		for (String line : lines) {
			pw.print(line);
			pw.print('\n');
		}

		byte[] bytes = sw.toString().getBytes(StandardCharsets.UTF_8);

		TarArchiveEntry entry = new TarArchiveEntry(filename);
		entry.setSize(bytes.length);

		tar.putArchiveEntry(entry);
		tar.write(bytes);
		tar.closeArchiveEntry();
	}
}
