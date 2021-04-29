package br.ufsc.gsigma.architecture.bootstrap;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.juddi.v3.client.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.BusinessService;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.BootstrapUtil;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UBLUddiUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;

public class BootstrapUDDIFederationNode {

	private final static Logger logger = LoggerFactory.getLogger(BootstrapUDDIFederationNode.class);

	private final static ExecutorService pool = Executors.newCachedThreadPool();

	public static void main(String[] args) {

	}

	public static Transport bootstrap(String host, int port, Transport uddiFederationTransport, final int i, boolean populateUddis, final int numberOfPublishers) throws Exception {

		String hibernateProperties = "";

		String uddiName = "uddi_federation_node" + i;

		String partition = "uddi:uddifederation-node" + i;

		FileUtils.buildFolder(new File[] { new File("templates/uddi/node") }, //
				new File("bootstrap/" + uddiName), //
				new String[] { "uddi.id=uddifederation-node" + i, //
						"uddi.partition=" + partition, "uddi.host=" + host, //
						"uddi.port=" + port, ("uddi.db.hibernate.properties=" + hibernateProperties), "uddi.db.connection.url=jdbc:derby:db/uddi/juddi-federation-node" + i + "-db;create=true" });

		File[] classPathBase = MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_UDDI);

		boolean parallel = !populateUddis;

		BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.services.uddi.bootstrap.RunUDDIService", "bootstrap", classPathBase, new File("bootstrap/" + uddiName), parallel);

		// Registering UDDI in UDDI Federation
		final String baseURL = "http://" + host + ":" + port;

		PublishUDDIFederationNode.publishUDDIFederationNode(uddiFederationTransport, baseURL, i);

		final Transport transport = UddiLocator.getUDDITransportByServicesBaseURL(baseURL + "/services");

		// Populating example services
		if (populateUddis) {

			final String nodeName = "node" + i;

			pool.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {

					try {

						UBLUddiUtil.createPublishers(transport, numberOfPublishers);

						Map<String, Integer> mapServiceKeyPrefixCount = new HashMap<String, Integer>();

						int count = 0;

						for (int z = 1; z <= numberOfPublishers; z++) {

							// logger.info("Populating UDDI '" + baseURL + "' with example services -> iteration=" + z);
							// String publisher = "publisher" + (new Random().nextInt(numberOfPublishers) + 1);

							String publisher = "publisher" + z;

							List<BusinessService> businessServices = UBLUddiUtil.populateServices(transport, publisher, "", null, "uddi:" + publisher, "UBL Services " + i, nodeName, mapServiceKeyPrefixCount);

							int numberOfSavedServices = businessServices.size();

							count += numberOfSavedServices;

							logger.info(count + " services saved in UDDI '" + baseURL + "' -> iteration=" + z);

							// for (BusinessService s : businessServices) {
							// logger.info("publishService(" + baseURL + ") -> serviceKey: " + s.getServiceKey());
							// }
						}

						logger.info(count + " services populated in UDDI '" + baseURL + "'");

						return null;

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						throw e;
					}
				}
			});

		}

		return transport;
	}

}
