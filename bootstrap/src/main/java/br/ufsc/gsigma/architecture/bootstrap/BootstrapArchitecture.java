package br.ufsc.gsigma.architecture.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.BootstrapUtil;
import br.ufsc.gsigma.infrastructure.util.DerbyUtil;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class BootstrapArchitecture {

	private final static Logger logger = LoggerFactory.getLogger(BootstrapArchitecture.class);

	public static void main(String[] args) throws Exception {

		DerbyUtil.silentLogging();

		// boolean bootstrapDiscoveryService = true;
		boolean bootstrapCatalogServices = true;
		// boolean bootstrapBpelExecution = true;

		// UDDI Federation Nodes
		// boolean bootstrapUddiFederation = true; // necessario para obter os atributos de QoS pelo discovery service
		// boolean populateUddis = false;
		// int uddiFederationNumberOfNodes = 0; // aumentar se necessario
		// int numberOfServicesPublishersPerNode = 1500;

		// boolean bootstrapUBLServices = false;

		// Transport mainUDDI = null;

		// Main UDDI
		// mainUDDI = BootstrapMainUDDI.bootstrap(ServicesAddresses.ALL_HOSTS, ServicesAddresses.SERVICE_REGISTRY_UDDI_PORT);

		// // Discovery Service
		// if (bootstrapDiscoveryService) {
		// BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.servicediscovery.bootstrap.RunDiscoveryService", //
		// MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_DISCOVERY), //
		// new String[] { ServicesAddresses.ALL_HOSTS, ServicesAddresses.DISCOVERY_SERVICE_PORT });
		// }

		if (bootstrapCatalogServices) {
			// Catalog Persistence Service
			BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.catalog.services.persistence.bootstrap.RunCatalogPersistenceService", //
					MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_CATALOG_PERSISTENCE), //
					new String[] { ServicesAddresses.ALL_HOSTS, "8084" });

			// Catalog Specification Service
			BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.catalog.services.specifications.bootstrap.RunCatalogSpecificationService", //
					MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_CATALOG_SPECIFICATION), //
					new String[] { ServicesAddresses.ALL_HOSTS, "8085" });

			// Catalog Service
			BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.catalog.services.bootstrap.RunCatalogService", //
					MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_CATALOG), //
					new String[] { ServicesAddresses.ALL_HOSTS, "8081" });
		}

		// // BPEL Export Service
		// BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.services.bpelexport.bootstrap.RunBPELExportService", //
		// MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_BPEL_EXPORTER), //
		// new String[] { ServicesAddresses.ALL_HOSTS, String.valueOf(ServicesAddresses.BPEL_EXPORTER_SERVICE_PORT) });

		// if (bootstrapBpelExecution) {
		// // Bpel Execution (ODE)
		// BootstrapBPELProcessExecutionService.bootstrap(ServicesAddresses.ALL_HOSTS, ServicesAddresses.EXECUTION_BPEL_SERVICE_PORT, mainUDDI);
		// }

		// if (bootstrapUddiFederation) {
		// // UDDI Federation
		// Transport uddiFederationTransport = BootstrapUDDIFederation.bootstrap(ServicesAddresses.ALL_HOSTS, ServicesAddresses.SERVICE_FEDERATION_UDDI_PORT,
		// ServicesAddresses.SERVICE_FEDERATION_UDDI_HOSTNAME, ServicesAddresses.SERVICE_FEDERATION_UDDI_PORT);
		//
		// for (int i = 1; i <= uddiFederationNumberOfNodes; i++)
		// BootstrapUDDIFederationNode.bootstrap(ServicesAddresses.ALL_HOSTS, 8079 - i, uddiFederationTransport, i, populateUddis, numberOfServicesPublishersPerNode);
		// }

		// if (bootstrapUBLServices) {
		// // UBL Services
		// BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.services.specifications.ubl.bootstrap.RunUBLServices", //
		// MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_UBL), //
		// new String[] { ServicesAddresses.ALL_HOSTS, ServicesAddresses.UBL_SERVICES_PORT });
		// }

		logger.info("Bootstrap complete");
	}
}
