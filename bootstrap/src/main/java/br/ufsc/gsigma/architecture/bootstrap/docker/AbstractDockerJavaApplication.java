package br.ufsc.gsigma.architecture.bootstrap.docker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;

import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.docker.DockerImageResource;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;
import br.ufsc.gsigma.infrastructure.util.maven.Pom;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import edu.emory.mathcs.backport.java.util.Arrays;

public abstract class AbstractDockerJavaApplication implements DockerApplication {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final String VARIABLE_HOST = "HOST";

	public static final String VARIABLE_HTTP_PORT = "HTTP_PORT";

	public static final String VARIABLE_DEBUG_PORT = "DEBUG_PORT";

	public static final String VARIABLE_JMX_PORT = "JMX_PORT";

	public static final String VARIABLE_JAVA_XMX = "JAVA_XMX";

	protected static final String[] DEFAULT_JRE_ARGS = new String[] { //
			"-Xmx${" + VARIABLE_JAVA_XMX + ":-512m}", //
			"-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${" + VARIABLE_DEBUG_PORT + ":-" + ServicesAddresses.DEFAULT_DOCKER_DEBUG_PORT + "}", //
			"-Dcom.sun.management.jmxremote", //
			"-Dcom.sun.management.jmxremote.local.only=false", //
			"-Dcom.sun.management.jmxremote.authenticate=false", //
			"-Dcom.sun.management.jmxremote.ssl=false", //
			"-Dcom.sun.management.jmxremote.port=${" + VARIABLE_JMX_PORT + ":-" + ServicesAddresses.DEFAULT_DOCKER_JMX_PORT + "}", //
			"-Dcom.sun.management.jmxremote.rmi.port=${" + VARIABLE_JMX_PORT + ":-" + ServicesAddresses.DEFAULT_DOCKER_JMX_PORT + "}", //
			"-Djava.rmi.server.hostname=${" + VARIABLE_HOST + ":-localhost}" //
	};

	protected static final String[] DEFAULT_APP_ARGS = new String[] { //
			ServicesAddresses.DEFAULT_DOCKER_HTTP_HOST, //
			String.valueOf(ServicesAddresses.DEFAULT_DOCKER_HTTP_PORT) //
	};

	private String pomFile;

	private String imageName;

	private String containerName;

	private String host;

	private int port;

	private int debugPort;

	private int jmxPort;

	private String mainClass;

	private String[] variables;

	private List<String> volumes;

	private String[] jreArgs = DEFAULT_JRE_ARGS;

	private String[] appArgs = DEFAULT_APP_ARGS;

	private Pom pom;

	public AbstractDockerJavaApplication(String pomFile, String imageName, String containerName, String host, int port, int debugPort, int jmxPort, String mainClass) {
		this.pomFile = pomFile;
		this.imageName = imageName;
		this.containerName = containerName;
		this.host = host;
		this.port = port;
		this.debugPort = debugPort;
		this.jmxPort = jmxPort;
		this.mainClass = mainClass;
	}

	public AbstractDockerJavaApplication(String pomFile, String imageName, String containerName, String host, int port, int debugPort, int jmxPort, String mainClass, String[] jreArgs, String[] appArgs) {
		this.pomFile = pomFile;
		this.imageName = imageName;
		this.containerName = containerName;
		this.host = host;
		this.port = port;
		this.debugPort = debugPort;
		this.jmxPort = jmxPort;
		this.mainClass = mainClass;
		this.jreArgs = jreArgs != null ? jreArgs : this.jreArgs;
		this.appArgs = appArgs != null ? appArgs : this.appArgs;
	}

	@SuppressWarnings("unchecked")
	public AbstractDockerJavaApplication(String pomFile, String imageName, String containerName, String host, int port, int debugPort, int jmxPort, String mainClass, String[] jreArgs, String[] appArgs, String[] variables, String[] volumes) {
		this.pomFile = pomFile;
		this.imageName = imageName;
		this.containerName = containerName;
		this.host = host;
		this.port = port;
		this.debugPort = debugPort;
		this.jmxPort = jmxPort;
		this.mainClass = mainClass;
		this.jreArgs = jreArgs != null ? jreArgs : this.jreArgs;
		this.appArgs = appArgs != null ? appArgs : this.appArgs;
		this.variables = variables;
		this.volumes = Arrays.asList(volumes);
	}

	protected Map<String, String> enrichVariables(Map<String, String> variables) {

		variables = variables != null ? new TreeMap<String, String>(variables) : new TreeMap<String, String>();

		if (!variables.containsKey(VARIABLE_HOST) && host != null)
			variables.put(VARIABLE_HOST, host);

		if (!variables.containsKey(VARIABLE_HTTP_PORT))
			variables.put(VARIABLE_HTTP_PORT, String.valueOf(port));

		if (!variables.containsKey(VARIABLE_DEBUG_PORT))
			variables.put(VARIABLE_DEBUG_PORT, String.valueOf(debugPort));

		if (!variables.containsKey(VARIABLE_JMX_PORT))
			variables.put(VARIABLE_JMX_PORT, String.valueOf(jmxPort));

		return variables;
	}

	protected Pom getPom() {
		if (pom == null) {
			try {
				pom = MavenUtil.getPOMFromFile(pomFile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return pom;
	}

	public File createImageFile() throws Exception {
		return createImageFile(true, new AtomicBoolean());
	}

	private File createImageFile(boolean overwrite, AtomicBoolean alreadyExists) throws Exception {

		File f = getImageFile();

		if (!f.exists()) {
			alreadyExists.set(false);
		} else {
			alreadyExists.set(true);
		}

		f = !f.exists() || overwrite ? createImage(f) : f;

		return f;
	}

	@Override
	public void deploy() throws IOException, Exception {
		deploy(true);
	}

	@SuppressWarnings("unchecked")
	public void deploy(boolean forceCreateImage) throws Exception, IOException {

		createImage(forceCreateImage);

		if (this.variables == null || this.variables.length == 0)
			deploy(containerName, Collections.EMPTY_MAP, forceCreateImage);
		else
			deploy(containerName, this.variables, forceCreateImage);
	}

	public void deploy(String containerName, String[] variables) throws Exception, IOException {
		deploy(containerName, variables, true);
	}

	public void deploy(String containerName, String[] variables, boolean forceDeploy) throws Exception, IOException {
		Map<String, String> variablesMap = DockerUtil.parseVariables(variables);
		deploy(containerName, null, variablesMap, forceDeploy);
	}

	public void deploy(String containerName, String[] portMappings, String[] variables) throws Exception, IOException {
		deploy(containerName, portMappings, variables, true);
	}

	public void deploy(String containerName, String[] portMappings, String[] variables, boolean forceDeploy) throws Exception, IOException {
		Map<String, String> variablesMap = DockerUtil.parseVariables(variables);
		deploy(containerName, portMappings, variablesMap, forceDeploy);
	}

	public void deploy(String containerName) throws Exception, IOException {
		deploy(containerName, null, variables, false);
	}

	public void deploy(String containerName, Map<String, String> variables) throws Exception, IOException {
		deploy(containerName, null, variables, false);
	}

	public void deploy(String containerName, Map<String, String> variables, boolean forceDeploy) throws Exception, IOException {
		deploy(containerName, null, variables, forceDeploy);
	}

	public void deploy(String containerName, String[] portMappings, Map<String, String> variables) throws Exception, IOException {
		deploy(containerName, portMappings, variables, false);
	}

	public void deploy(String containerName, String[] portMappings, Map<String, String> variables, boolean forceDeploy) throws Exception, IOException {

		variables = enrichVariables(variables);

		logger.info("Deploying container " + containerName + " with image " + imageName + (variables != null && !variables.isEmpty() ? " with variables " + variables : ""));

		AtomicBoolean alreadyExists = new AtomicBoolean(false);

		File f = createImageFile(forceDeploy, alreadyExists);

		boolean redeploy = forceDeploy || !alreadyExists.get();

		String tagName = imageName + ":" + getPom().getVersion();

		String imageId = DockerUtil.getImageIdByTagName(tagName);

		if (imageId == null || redeploy) {
			imageId = DockerUtil.deployImage(f, tagName);
			redeploy = true;
		}

		DockerClient docker = DockerUtil.getDockerClient();

		String containerId = DockerUtil.getContainerIdByName(docker, containerName);

		if (containerId == null || redeploy) {

			if (portMappings == null || portMappings.length == 0) {
				portMappings = new String[] { //
						ServicesAddresses.DEFAULT_DOCKER_HTTP_PORT + ":" + port, //
						debugPort + ":" + debugPort, //
						jmxPort + ":" + jmxPort //
				};
			}

			containerId = DockerUtil.createContainer(docker, //
					imageId, //
					containerName, //
					portMappings, //
					volumes != null ? volumes.toArray(new String[volumes.size()]) : null, //
					variables //
			);
		}

		if (DockerUtil.isContainerRunningById(docker, containerId))
			DockerUtil.stopContainer(docker, containerId);

		DockerUtil.startContainer(docker, containerId);
	}

	@Override
	public File createImage() throws Exception {
		return createImage(true);
	}

	@Override
	public File createImageIfNecessary() throws Exception {
		return createImage(false);
	}

	private File createImage(boolean force) throws Exception {
		File imageFile = getImageFile();

		if (imageFile.exists() && !force)
			return imageFile;

		return createImage(imageFile);
	}

	@Override
	public File createImage(File imageFileName) throws Exception {

		logger.info("Creating image " + imageName);

		List<DockerImageResource> resources = new LinkedList<DockerImageResource>();

		InputStream imageStream = DockerUtil.createJavaApplicationImageFromPOM( //
				getPom().getMavenProject().getFile().getAbsolutePath(), //
				jreArgs, //
				mainClass, //
				appArgs, //
				resources, //
				(volumes != null ? volumes.stream().map(x -> x.indexOf(':') > 0 ? x.split(":")[1] : x).collect(Collectors.toList()) : null) //
		);

		try (InputStream in = imageStream) {
			try (FileOutputStream out = new FileOutputStream(imageFileName)) {
				IOUtils.copy(in, out);
			}
		}

		return imageFileName;

	}

	@Override
	public File getImageFile() {
		return new File("docker_images", "docker-" + imageName + "-" + getPom().getVersion() + ".tar.gz");
	}

	protected void addVolume(String path) {
		if (this.volumes == null)
			this.volumes = new LinkedList<String>();

		this.volumes.add(getServerVolumePath(path) + ":" + DockerUtil.JAVA_APP_PATH_BASE + "/" + getPom().getArtifactId() + "/" + path);
	}

	protected String getServerVolumePath(String path) {
		return FileUtils.adjustAbsolutePath(new File(path));
	}

	public String[] getVariables() {
		return variables;
	}

	public void setVariables(String[] variables) {
		this.variables = variables;
	}

	public List<String> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<String> volumes) {
		this.volumes = volumes;
	}

	@SuppressWarnings("unchecked")
	public void setVolumes(String... volumes) {
		this.volumes = Arrays.asList(volumes);
	}

	public int getDebugPort() {
		return debugPort;
	}

	public int getPort() {
		return port;
	}

	public int getJmxPort() {
		return jmxPort;
	}

}
