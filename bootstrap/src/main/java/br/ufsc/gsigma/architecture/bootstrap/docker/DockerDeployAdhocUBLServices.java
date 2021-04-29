package br.ufsc.gsigma.architecture.bootstrap.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public abstract class DockerDeployAdhocUBLServices {

	private static final Logger logger = LoggerFactory.getLogger(DockerDeployAdhocUBLServices.class);

	public static void main(String[] args) throws Exception {

		DockerUtil.setDockerClient(DockerServers.DOCKER_CLIENT_150_162_6_131);

		try {

			DockerUBLServices dockerUBLServices = DockerUBLServices.INSTANCE;

			dockerUBLServices.createImage();

			int i = 1;

			int offset = 500;

			String servicePath = "ubl/paymentprocess/accountingCustomer/notifyOfPayment";

			String containerSuffix = servicePath.substring("ubl/".length()).replaceAll("/", "-").toLowerCase();
			String containerName = "adhoc-" + DockerUBLServices.CONTAINER_NAME + (i > 1 ? "-" + i : "") + "-" + containerSuffix;
			int port = DockerUBLServices.PORT + offset + (i - 1);
			int debugPort = DockerUBLServices.DEBUG_PORT + offset + (i - 1);
			int jmxPort = DockerUBLServices.JMX_PORT + offset + (i - 1);
			String host = "adhoc-ublservices" + (i > 1 ? i : "");
			// Util.suffixHost(DockerUBLServices.HOST, i);

			logger.info("Deploying adhoc UBL services at " + host + ":" + port + " (debugPort=" + debugPort + ", jmxPort=" + jmxPort + ")");

			dockerUBLServices.deploy(containerName, //
					new String[] { //
							ServicesAddresses.DEFAULT_DOCKER_HTTP_PORT + ":" + String.valueOf(port), //
							ServicesAddresses.DEFAULT_DOCKER_DEBUG_PORT + ":" + String.valueOf(debugPort), //
							jmxPort + ":" + jmxPort //
					}, //
					new String[] { //
							DockerUBLServices.VARIABLE_HOST, host, //
							DockerUBLServices.VARIABLE_HTTP_PORT, String.valueOf(port), //
							DockerUBLServices.VARIABLE_DEBUG_PORT, String.valueOf(debugPort), //
							DockerUBLServices.VARIABLE_JMX_PORT, String.valueOf(jmxPort), //
							DockerUBLServices.VARIABLE_SERVICE_PATH, servicePath //
					});

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			System.exit(0);
		}

	}

}
