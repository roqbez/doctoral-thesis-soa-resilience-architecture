package br.ufsc.gsigma.catalog.services.bootstrap;

import org.apache.commons.configuration.ConfigurationException;

import br.ufsc.gsigma.catalog.services.impl.CatalogServiceImpl;
import br.ufsc.gsigma.catalog.services.persistence.bootstrap.RunCatalogPersistenceService;
import br.ufsc.gsigma.catalog.services.persistence.interfaces.CatalogPersistenceService;
import br.ufsc.gsigma.catalog.services.specifications.bootstrap.RunCatalogSpecificationService;
import br.ufsc.gsigma.catalog.services.specifications.interfaces.CatalogSpecificationService;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class BootstrapCatalogService {

	public static void main(String[] args) throws ConfigurationException, Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.CATALOG_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : ServicesAddresses.CATALOG_SERVICE_HOSTNAME;
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.CATALOG_SERVICE_PORT);

		CatalogPersistenceService catalogPersistenceService = RunCatalogPersistenceService.createService();
		CatalogSpecificationService catalogSpecificationService = RunCatalogSpecificationService.createService();

		CatalogServiceImpl catalogService = RunCatalogService.createService(catalogPersistenceService, catalogSpecificationService);

		RunCatalogService.bootstrap(host, port, publishHost, publishPort, catalogService);
	}
}
