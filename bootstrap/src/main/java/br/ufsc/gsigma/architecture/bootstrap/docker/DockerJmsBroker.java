package br.ufsc.gsigma.architecture.bootstrap.docker;

import java.io.IOException;
import java.util.Map;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class DockerJmsBroker extends AbstractDockerJavaApplication {

	public static final String POM_FILE = MavenPOMLocation.POM_COMMON_MESSAGING;

	public static final String HOST = ServicesAddresses.JMS_BROKER_HOSTNAME;

	public static final int PORT = ServicesAddresses.JMS_BROKER_PORT;

	public static final int DEBUG_PORT = ServicesAddresses.JMS_BROKER_DEBUG_PORT;

	public static final int JMX_PORT = ServicesAddresses.JMS_BROKER_JMX_PORT;

	public static final String MAIN_CLASS = "br.ufsc.gsigma.infrastructure.util.messaging.BootstrapJmsBroker";

	public static final String IMAGE_NAME = "jms-broker";

	public static final String CONTAINER_NAME = "jms-broker";

	public static final DockerJmsBroker INSTANCE = new DockerJmsBroker();

	public DockerJmsBroker() {
		super(POM_FILE, IMAGE_NAME, CONTAINER_NAME, HOST, PORT, DEBUG_PORT, JMX_PORT, MAIN_CLASS);
		setVariables(new String[] { VARIABLE_JAVA_XMX, "512m" });
		addVolume("db/jms-broker");
	}

	@Override
	public void deploy(String containerName, String[] portMappings, Map<String, String> variables, boolean forceDeploy) throws Exception, IOException {

		portMappings = new String[] { //
				PORT + ":" + getPort(), //
				ServicesAddresses.DEFAULT_DOCKER_DEBUG_PORT + ":" + getDebugPort(), //
				getJmxPort() + ":" + getJmxPort() //
		};

		super.deploy(containerName, portMappings, variables, forceDeploy);
	}

	@Override
	protected String getServerVolumePath(String path) {
		return "/opt/docker/volumes/" + CONTAINER_NAME + "/" + path;
	}

	public static void main(String[] args) throws Exception {
		try {
			DockerUtil.setDockerClient(DockerServers.DOCKER_CLIENT_150_162_6_131);
			INSTANCE.deploy(true);
		} finally {
			System.exit(0);
		}
	}

}
