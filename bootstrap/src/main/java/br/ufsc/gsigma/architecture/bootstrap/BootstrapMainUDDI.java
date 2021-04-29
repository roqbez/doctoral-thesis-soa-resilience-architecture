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

public class BootstrapMainUDDI {

	public static final String MAIN_CLASS = "br.ufsc.gsigma.services.uddi.bootstrap.RunUDDIService";

	public static final String SERVICE_REGISTRY_UDDI_PARTITION = "uddi:gsigma.ufsc.br";

	public static void main(String[] args) throws Exception {
		bootstrap(ServicesAddresses.SERVICE_REGISTRY_UDDI_HOSTNAME, ServicesAddresses.SERVICE_REGISTRY_UDDI_PORT);
	}

	public static Transport bootstrap(String host, int port) throws Exception {

		File basePathFile = new File("bootstrap/uddi_main");

		prepareFiles(host, port, basePathFile);

		File[] classPathBase = MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_UDDI);

		BootstrapUtil.bootstrapIsolated(MAIN_CLASS, "bootstrap", classPathBase, basePathFile, false);

		return UddiLocator.getUDDITransportByServicesBaseURL("http://" + host + ":" + port + "/services");

	}

	public static void prepareFiles(String host, int port, File basePathFile) throws IOException {

		FileUtils.buildFolder(new File[] { new File("templates/uddi/node"), new File("templates/uddi/main") }, basePathFile, //
				new String[] { "uddi.id=main", //
						"uddi.partition=" + SERVICE_REGISTRY_UDDI_PARTITION, //
						"uddi.host=" + host, //
						"uddi.port=" + port, //
						"uddi.db.connection.url=jdbc:derby:db/uddi/juddi-main-db;create=true" });
	}

}
