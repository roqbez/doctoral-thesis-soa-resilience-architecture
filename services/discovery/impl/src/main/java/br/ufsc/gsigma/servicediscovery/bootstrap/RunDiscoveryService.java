package br.ufsc.gsigma.servicediscovery.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;
import br.ufsc.gsigma.servicediscovery.impl.DiscoveryServiceImpl;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryAdminService;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryService;

public abstract class RunDiscoveryService {

	private final static Logger logger = LoggerFactory.getLogger(RunDiscoveryService.class);

	public static void main(String[] args) throws Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.DISCOVERY_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : String.valueOf(ServicesAddresses.DISCOVERY_SERVICE_HOSTNAME);
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.DISCOVERY_SERVICE_PORT);

		String urlDiscoveryService = "http://" + host + ":" + port + "/services/DiscoveryService";
		String urlDiscoveryAdminService = "http://" + host + ":" + port + "/services/DiscoveryAdminService";

		logger.info("Bootstrapping DiscoveryService in " + urlDiscoveryService);
		logger.info("Bootstrapping DiscoveryAdminService in " + urlDiscoveryAdminService);

		DiscoveryServiceImpl serviceImplementor = DiscoveryServiceImpl.INSTANCE;

		CxfUtil.createService(DiscoveryService.class, urlDiscoveryService, serviceImplementor);
		CxfUtil.createService(DiscoveryAdminService.class, urlDiscoveryAdminService, serviceImplementor);

		try {
			String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/DiscoveryService";
			UddiRegister.publishService(UddiLocator.getUDDITransport(), "root", "", //
					"uddi:gsigma.ufsc.br:repository", WebServiceLocator.DISCOVERY_SERVICE_UDDI_SERVICE_KEY, //
					"Discovery Service", //
					"Web Service supporting Service Discovery", //
					publishUrl + "?wsdl", "wsdlDeployment", //
					new TModelInstanceInfo[] {});
		} catch (Exception e) {
			logger.error("Error registering DiscoveryService in UDDI: " + e.getMessage());
		}
	}
}
