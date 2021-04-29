package br.ufsc.gsigma.services.uddi.bootstrap;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;

public class RunUDDIService {

	private static final Logger logger = Logger.getLogger(RunUDDIService.class);

	public static void main(String[] args) {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.DEFAULT_HTTP_PORT);

		String publishHost = args.length > 2 ? args[2] : null;
		String publishPort = args.length > 3 ? args[3] : null;

		bootstrap(host, port, publishHost, publishPort);
	}

	@SuppressWarnings("resource")
	public static void bootstrap(String host, String port, String publishHost, String publishPort) {

		new ClassPathXmlApplicationContext(new String[] { "/br/ufsc/gsigma/services/uddi/beans.xml" });

		if (publishHost != null && publishPort != null) {
			try {
				String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/inquiry";

				UddiRegister.publishService(UddiLocator.getUDDITransport(), "root", "", //
						"uddi:gsigma.ufsc.br:repository", WebServiceLocator.UDDI_FEDERATION_UDDI_SERVICE_KEY, //
						"UDDI Federation Service", //
						"UDDI Federation containing service providers", //
						publishUrl + "?wsdl", "wsdlDeployment", //
						new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:uddi.org:v3_inquiry") });
			} catch (Exception e) {
				logger.error("Error registering UDDIService in UDDI: " + e.getMessage());
			}
		}
	}

}
