package br.ufsc.gsigma.architecture.bootstrap;

import java.io.File;
import java.io.IOException;

import org.apache.juddi.v3.client.transport.Transport;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.BootstrapUtil;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;

public class BootstrapUDDIFederation {

	public static final String MAIN_CLASS = "br.ufsc.gsigma.services.uddi.bootstrap.RunUDDIService";

	public static void main(String[] args) throws Exception {
		bootstrap(ServicesAddresses.ALL_HOSTS, ServicesAddresses.SERVICE_FEDERATION_UDDI_PORT, ServicesAddresses.SERVICE_FEDERATION_UDDI_HOSTNAME, ServicesAddresses.SERVICE_FEDERATION_UDDI_PORT);
	}

	public static Transport bootstrap(String host, int port, String publishHost, int publishPort) throws Exception {

		File basePathFile = new File("bootstrap/uddi_federation");

		prepareFiles(host, port, basePathFile);

		File[] classPathBase = MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_UDDI);

		BootstrapUtil.bootstrapIsolated(MAIN_CLASS, "bootstrap", //
				new Class[] { String.class, String.class, String.class, String.class }, //
				new Object[] { host, String.valueOf(port), publishHost, String.valueOf(publishPort) }, //
				classPathBase, basePathFile, false);

		return UddiLocator.getUDDITransportByServicesBaseURL("http://" + host + ":" + port + "/services");

	}

	public static void prepareFiles(String host, int port, File basePathFile) throws IOException {
		String hibernateProperties = "";

		// String hibernateProperties =
		// "<property name=\"hibernate.hbm2ddl.auto\" value=\"update\" />";

		FileUtils.buildFolder(new File[] { new File("templates/uddi/node"), new File("templates/uddi/uddiFederation") }, //
				basePathFile, //
				new String[] { "uddi.id=uddiFederation", "uddi.partition=uddi:uddifederation", "uddi.host=" + host, //
						"uddi.port=" + port, //
						("uddi.db.hibernate.properties=" + hibernateProperties), "uddi.db.connection.url=jdbc:derby:db/uddi/juddi-federation-db;create=true" });
	}

}
