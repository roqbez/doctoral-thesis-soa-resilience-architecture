package br.ufsc.gsigma.catalog.services.persistence.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.catalog.services.persistence.interfaces.CatalogPersistenceService;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;

public abstract class RunCatalogPersistenceService {

	private final static Logger logger = LoggerFactory.getLogger(RunCatalogPersistenceService.class);

	public static void main(String[] args) throws Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.CATALOG_PERSISTENCE_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : ServicesAddresses.CATALOG_PERSISTENCE_SERVICE_HOSTNAME;
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.CATALOG_PERSISTENCE_SERVICE_PORT);

		bootstrap(host, port, publishHost, publishPort);

	}

	public static void bootstrap(String host, String port, String publishHost, String publishPort) {

		CatalogPersistenceService catalogPersistenceService = createService();

		String url = "http://" + host + ":" + port + "/services/CatalogPersistenceService";

		logger.info("Bootstrapping CatalogPersistenceService in " + url);

		// Start WebService
		CxfUtil.createService(CatalogPersistenceService.class, url, catalogPersistenceService);

		if (publishHost != null && publishPort != null) {
			try {

				String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/CatalogPersistenceService";

				UddiRegister.publishService(UddiLocator.getUDDITransport("default"), "root", "", //
						"uddi:gsigma.ufsc.br:repository", WebServiceLocator.CATALOG_PERSISTENCE_SERVICE_UDDI_SERVICE_KEY, //
						"Catalog Persistence Service", //
						"Web Service supporting Business Processes Catalog Persistence", //
						publishUrl + "?wsdl", "wsdlDeployment", //
						new TModelInstanceInfo[0]);
			} catch (Exception e) {
				System.err.println("Error registering CatalogPersistenceService in UDDI: " + e.getMessage());
			}
		}
	}

	@SuppressWarnings("resource")
	public static CatalogPersistenceService createService() {

		// Start DB
		logger.info("Bootstrapping CatalogPersistenceService database");
		org.hsqldb.Server.main(new String[] { "-database.0", "file:db/catalog-persistence/database", "-dbname.0", "catalog" });

		// Start Spring

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] { "/br/ufsc/gsigma/catalog/services/persistence/applicationContext.xml" });

		CatalogPersistenceService catalogPersistenceService = (CatalogPersistenceService) applicationContext.getBean("catalogPersistenceServiceImpl", CatalogPersistenceService.class);

		return catalogPersistenceService;
	}
}
