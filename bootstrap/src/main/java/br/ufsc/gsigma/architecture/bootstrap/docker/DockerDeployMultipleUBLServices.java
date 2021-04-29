package br.ufsc.gsigma.architecture.bootstrap.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.util.Util;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public abstract class DockerDeployMultipleUBLServices {

	private static final int NUMBER_OF_REPLICAS = 5;

	private static final Logger logger = LoggerFactory.getLogger(DockerDeployMultipleUBLServices.class);

	public static void main(String[] args) throws Exception {

		DockerUtil.setDockerClient(DockerServers.DOCKER_CLIENT_150_162_6_133);

		try {

			DockerUBLServices dockerUBLServices = DockerUBLServices.INSTANCE;

			dockerUBLServices.createImage();

			for (int i = 1; i <= NUMBER_OF_REPLICAS; i++) {

				if (i == 1) {
					dockerUBLServices.deploy(DockerUBLServices.CONTAINER_NAME);
					continue;
				}

				String containerName = DockerUBLServices.CONTAINER_NAME + i;
				int port = DockerUBLServices.PORT + (i - 1);
				int debugPort = DockerUBLServices.DEBUG_PORT + (i - 1);
				int jmxPort = DockerUBLServices.JMX_PORT + (i - 1);
				String host = Util.suffixHost(DockerUBLServices.HOST, i);

				logger.info("Deploying UBL services at " + host + ":" + port + " (debugPort=" + debugPort + ", jmxPort=" + jmxPort + ")");

				dockerUBLServices.deploy(containerName, //
						new String[] { //
								ServicesAddresses.DEFAULT_DOCKER_HTTP_PORT + ":" + String.valueOf(port), //
								debugPort + ":" + String.valueOf(debugPort), //
								jmxPort + ":" + jmxPort //
						}, //
						new String[] { //
								DockerUBLServices.VARIABLE_HOST, host, //
								DockerUBLServices.VARIABLE_HTTP_PORT, String.valueOf(port), //
								DockerUBLServices.VARIABLE_DEBUG_PORT, String.valueOf(debugPort), //
								DockerUBLServices.VARIABLE_JMX_PORT, String.valueOf(jmxPort) //
						});
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			System.exit(0);
		}

	}

}
