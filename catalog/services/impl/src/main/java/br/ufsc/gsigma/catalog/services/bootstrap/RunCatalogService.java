package br.ufsc.gsigma.catalog.services.bootstrap;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.catalog.services.impl.CatalogServiceImpl;
import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.catalog.services.persistence.interfaces.CatalogPersistenceService;
import br.ufsc.gsigma.catalog.services.specifications.interfaces.CatalogSpecificationService;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;

public class RunCatalogService {

	private final static Logger logger = LoggerFactory.getLogger(RunCatalogService.class);

	public static void main(String[] args) throws Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.CATALOG_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : ServicesAddresses.CATALOG_SERVICE_HOSTNAME;
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.CATALOG_SERVICE_PORT);

		bootstrap(host, port, publishHost, publishPort);
	}

	public static void bootstrap(String host, String port, String publishHost, String publishPort) throws Exception, ConfigurationException {
		CatalogServiceImpl catalogService = createService();
		bootstrap(host, port, publishHost, publishPort, catalogService);
	}

	public static void bootstrap(String host, String port, String publishHost, String publishPort, CatalogServiceImpl catalogService) throws Exception, ConfigurationException {
		String url = "http://" + host + ":" + port + "/services/CatalogService";

		logger.info("Bootstrapping CatalogService in " + url);

		CxfUtil.createService(CatalogService.class, url, catalogService);

		if (publishHost != null && publishPort != null) {

			String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/CatalogService";

			UddiRegister.publishService(UddiLocator.getUDDITransport(), "root", "", //
					"uddi:gsigma.ufsc.br:repository", WebServiceLocator.CATALOG_SERVICE_UDDI_SERVICE_KEY, //
					"Catalog Service", //
					"Web Service supporting Business Processes Catalog", //
					publishUrl + "?wsdl", "wsdlDeployment", //
					new TModelInstanceInfo[0]);
		}
	}

	public static CatalogServiceImpl createService() {
		return new CatalogServiceImpl();
	}

	public static CatalogServiceImpl createService(CatalogPersistenceService catalogPersistenceService, CatalogSpecificationService catalogSpecificationService) {
		return new CatalogServiceImpl(catalogPersistenceService, catalogSpecificationService);
	}

}
