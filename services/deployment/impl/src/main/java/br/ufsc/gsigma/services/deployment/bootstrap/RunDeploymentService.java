package br.ufsc.gsigma.services.deployment.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;

public class RunDeploymentService {

	private static final Logger logger = LoggerFactory.getLogger(RunDeploymentService.class);

	private static String serviceHost;

	private static String servicePort;

	public static void main(String[] args) {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.DEPLOYMENT_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : ServicesAddresses.DEPLOYMENT_SERVICE_HOSTNAME;
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.DEPLOYMENT_SERVICE_PORT);

		bootstrap(host, port, publishHost, publishPort);

	}

	@SuppressWarnings("resource")
	public static void bootstrap(String host, String port, String publishHost, String publishPort) {

		serviceHost = host;
		servicePort = port;

		System.setProperty("http.host", serviceHost);
		System.setProperty("http.port", servicePort);

		new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });

		if (publishHost != null && publishPort != null) {
			try {
				String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/DeploymentService";

				UddiRegister.publishService(UddiLocator.getUDDITransport(), "root", "", //
						"uddi:gsigma.ufsc.br:repository", WebServiceLocator.DEPLOYMENT_SERVICE_KEY, //
						"Deployment Service", //
						"Deployment Service", //
						publishUrl + "?wsdl", "wsdlDeployment", //
						new TModelInstanceInfo[0]);
			} catch (Exception e) {
				logger.error("Error registering DeploymentService in UDDI: " + e.getMessage());
			}
		}
	}

	public static String getServiceHost() {
		return serviceHost;
	}

	public static String getServicePort() {
		return servicePort;
	}

}
