package br.ufsc.gsigma.architecture.bootstrap.docker;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class DockerUBLServices extends AbstractDockerJavaApplication {

	public static final String POM_FILE = MavenPOMLocation.POM_SERVICES_UBL;

	public static final String HOST = ServicesAddresses.UBL_SERVICES_HOSTNAME;

	public static final int PORT = ServicesAddresses.UBL_SERVICES_PORT;

	public static final int DEBUG_PORT = ServicesAddresses.UBL_SERVICES_DEBUG_PORT;

	public static final int JMX_PORT = ServicesAddresses.UBL_SERVICES_JMX_PORT;

	public static final String MAIN_CLASS = "br.ufsc.gsigma.services.specifications.ubl.bootstrap.RunUBLServices";

	public static final String IMAGE_NAME = "ubl-services";

	public static final String CONTAINER_NAME = "ubl-services";

	public static final String VARIABLE_SERVICE_PATH = "SERVICE_PATH";

	public static final String[] APP_ARGS = new String[] { //
			ServicesAddresses.DEFAULT_DOCKER_HTTP_HOST, //
			String.valueOf(ServicesAddresses.DEFAULT_DOCKER_HTTP_PORT), //
			"${" + VARIABLE_HOST + ":-" + HOST + "}", //
			"${" + VARIABLE_HTTP_PORT + ":-" + PORT + "}", //
			"${" + VARIABLE_SERVICE_PATH + ":-" + "" + "}" //
			//
	};

	public static final DockerUBLServices INSTANCE = new DockerUBLServices();

	public DockerUBLServices() {
		super(POM_FILE, IMAGE_NAME, CONTAINER_NAME, HOST, PORT, DEBUG_PORT, JMX_PORT, MAIN_CLASS, DEFAULT_JRE_ARGS, APP_ARGS);
	}

	public DockerUBLServices(String containerName, String host, int port, int dubugPort, int jmxPort) {
		super(POM_FILE, IMAGE_NAME, containerName, host, port, dubugPort, jmxPort, MAIN_CLASS, DEFAULT_JRE_ARGS, APP_ARGS);
	}

	public static void main(String[] args) throws Exception {
		
		try {
//			DockerUtil.setDockerClient(DockerServers.DOCKER_CLIENT_150_162_6_131);
//			INSTANCE.deploy(true);

			DockerUtil.setDockerClient(DockerServers.DOCKER_CLIENT_150_162_6_133);
			INSTANCE.deploy(true);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
