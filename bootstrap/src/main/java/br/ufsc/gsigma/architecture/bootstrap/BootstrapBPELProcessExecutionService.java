package br.ufsc.gsigma.architecture.bootstrap;

import java.io.File;
import java.io.IOException;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.BootstrapUtil;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class BootstrapBPELProcessExecutionService {

	public static void main(String[] args) throws Exception {

		String host = ServicesAddresses.ALL_HOSTS;
		int port = ServicesAddresses.EXECUTION_SERVICE_PORT;

		if (args != null) {
			if (args.length >= 1)
				host = args[0];
			if (args.length >= 2)
				port = Integer.parseInt(args[1]);
		}

		bootstrap(host, port);
	}

	public static void bootstrap(String host, int port) throws Exception {

		String basePath = "bootstrap/ode";

		prepareFiles(host, port, basePath);

		BootstrapUtil.bootstrapIsolated("br.ufsc.gsigma.services.execution.bpel.bootstrap.RunExecutionService", "bootstrap", new Class<?>[] { String.class, int.class, String.class },
				new Object[] { host, port, ServicesAddresses.EXECUTION_SERVICE_HOSTNAME, ServicesAddresses.EXECUTION_SERVICE_PORT, basePath },
				MavenUtil.getClassPathFromMavenPOM(MavenPOMLocation.POM_SERVICES_EXECUTION_BPEL), new File(basePath), true);

	}

	public static void prepareFiles(String host, int port, String basePath) throws IOException {
		prepareFiles(host, port, basePath, new File(basePath));
	}

	public static void prepareFiles(String host, int port, String basePath, File baseFile) throws IOException {
		prepareFiles(host, port, basePath, baseFile, "db/ode/ode-db1");
	}

	public static void prepareFiles(String host, int port, String basePath, File baseFile, String dbFile) throws IOException {
		FileUtils.buildFolder(new File[] { new File("templates/ode") }, baseFile, //
				new String[] { //
						"ode.host=" + host, //
						"ode.port=" + port, //
						"ode.basedir=" + basePath, //
						"ode.db.connection.url=jdbc:derby:" + dbFile, //
						"ode.db.connection.username=sa", //
						"ode.db.connection.password=" //
				}, false);
	}
}
