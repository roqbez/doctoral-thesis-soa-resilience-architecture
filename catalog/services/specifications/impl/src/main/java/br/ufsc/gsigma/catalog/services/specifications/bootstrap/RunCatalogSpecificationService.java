package br.ufsc.gsigma.catalog.services.specifications.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.catalog.services.specifications.impl.CatalogSpecificationServiceImpl;
import br.ufsc.gsigma.catalog.services.specifications.interfaces.CatalogSpecificationService;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;

public class RunCatalogSpecificationService {

	private final static Logger logger = LoggerFactory.getLogger(RunCatalogSpecificationService.class);

	public static void main(String[] args) throws Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.CATALOG_SPECIFICATION_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : ServicesAddresses.CATALOG_SPECIFICATION_SERVICE_HOSTNAME;
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.CATALOG_SPECIFICATION_SERVICE_PORT);

		bootstrap(host, port, publishHost, publishPort);
	}

	public static void bootstrap(String host, String port, String publishHost, String publishPort) throws Exception {

		String url = "http://" + host + ":" + port + "/services/CatalogSpecificationService";

		logger.info("Bootstrapping CatalogSpecificationService in " + url);

		CxfUtil.createService(CatalogSpecificationService.class, url, createService());

		if (publishHost != null && publishPort != null) {
			try {
				String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/CatalogSpecificationService";

				UddiRegister.publishService(UddiLocator.getUDDITransport("default"), "root", "", //
						"uddi:gsigma.ufsc.br:repository", WebServiceLocator.CATALOG_SPECIFICATION_SERVICE_UDDI_SERVICE_KEY, //
						"Catalog Specification Service", //
						"Web Service supporting Business Processes Catalog Specifications", //
						publishUrl + "?wsdl", "wsdlDeployment", //
						new TModelInstanceInfo[] {});
			} catch (Exception e) {
				System.err.println("Error registering CatalogPersistenceService in UDDI: " + e.getMessage());
			}
		}
	}

	public static CatalogSpecificationServiceImpl createService() {
		return new CatalogSpecificationServiceImpl();
	}

}
