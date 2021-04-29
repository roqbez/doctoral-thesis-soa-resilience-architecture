package br.ufsc.gsigma.architecture.bootstrap.docker;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class DockerDiscoveryService extends AbstractDockerJavaApplication {

	public static final String POM_FILE = MavenPOMLocation.POM_SERVICES_DISCOVERY;

	public static final String HOST = ServicesAddresses.DISCOVERY_SERVICE_HOSTNAME;

	public static final int PORT = ServicesAddresses.DISCOVERY_SERVICE_PORT;

	public static final int DEBUG_PORT = ServicesAddresses.DISCOVERY_SERVICE_DEBUG_PORT;

	public static final int JMX_PORT = ServicesAddresses.DISCOVERY_SERVICE_JMX_PORT;

	public static final String MAIN_CLASS = "br.ufsc.gsigma.servicediscovery.bootstrap.RunDiscoveryService";

	public static final String IMAGE_NAME = "discovery-service";

	public static final String CONTAINER_NAME = "discovery-service";

	public static final DockerDiscoveryService INSTANCE = new DockerDiscoveryService();

	public DockerDiscoveryService() {
		super(POM_FILE, IMAGE_NAME, CONTAINER_NAME, HOST, PORT, DEBUG_PORT, JMX_PORT, MAIN_CLASS);
		setVariables(new String[] { VARIABLE_JAVA_XMX, "2048m" });
		addVolume("db/discovery");
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
