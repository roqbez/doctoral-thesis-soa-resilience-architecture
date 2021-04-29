package br.ufsc.gsigma.services.execution.bpel.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.cxf.Bus;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.infrastructure.util.DerbyUtil;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.ZipUtils;
import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent.ComponentName;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;
import br.ufsc.gsigma.services.execution.bpel.impl.ExecutionServiceImpl;
import br.ufsc.gsigma.services.execution.bpel.ode.ODEAxisServlet;
import br.ufsc.gsigma.services.execution.bpel.util.LeaderServlet;
import br.ufsc.gsigma.services.execution.interfaces.ProcessExecutionService;

public class RunExecutionService {

	public static final String EXECUTION_SERVICE_PORT = "executionservice.port";

	public static final String EXECUTION_SERVICE_HOSTNAME = "executionservice.host";

	private static final Logger logger = Logger.getLogger(RunExecutionService.class);

	public static String PROCESS_EXECUTION_SERVICE_URL;

	public static void main(String[] args) throws Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		int port = args.length > 1 ? Integer.parseInt(args[1]) : ServicesAddresses.EXECUTION_SERVICE_PORT;

		String publishHost = args.length > 2 ? args[2] : String.valueOf(ServicesAddresses.EXECUTION_SERVICE_HOSTNAME);
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.EXECUTION_SERVICE_PORT);

		String basePath = args.length > 4 ? args[4] : "src/main/webapp";

		System.setProperty(EXECUTION_SERVICE_HOSTNAME, publishHost);

		System.setProperty(EXECUTION_SERVICE_PORT, String.valueOf(publishPort));

		bootstrap(host, port, publishHost, publishPort, basePath, "true".equals(System.getProperty("clean")));
	}

	public static void bootstrap() throws Exception {

		Properties props = new Properties();
		props.load(RunExecutionService.class.getResourceAsStream("/WEB-INF/application.properties"));

		bootstrap(props.getProperty("ode.host"), Integer.parseInt(props.getProperty("ode.port")), null, null, props.getProperty("ode.basedir"));
	}

	public static void bootstrap(String host, int port, String publishHost, String publishPort, String basePath) throws Exception {
		bootstrap(host, port, publishHost, publishPort, basePath, false);
	}

	@SuppressWarnings("serial")
	public static void bootstrap(String host, int port, String publishHost, String publishPort, String basePath, boolean clean) throws Exception {

		System.setProperty("discovery.dns_query", "execution-service");

		String dataDir = System.getProperty("data.dir", null);

		if (dataDir == null && DockerContainerUtil.isRunningInContainer()) {
			dataDir = "data";
			System.setProperty("data.dir", dataDir);
		}

		if (dataDir != null) {
			System.setProperty("jms.data.dir", new File(dataDir, "jms").getAbsolutePath());
			System.setProperty("jms.data.kahadb.dir", new File(dataDir, "jms/kahadb").getAbsolutePath());
		}

		if (clean) {

			// DBUtil.clearDatabase();

			File dbDir = dataDir != null ? new File(dataDir) : new File("db/execution");

			if (dbDir.exists()) {
				logger.info("Cleaning up directory --> " + dbDir.getAbsolutePath());
				FileUtils.recursiveDelete(dbDir);
			}
			dbDir.mkdirs();

			// unzipDerbyDb(dbDir);
		}

		DerbyUtil.silentLogging();

		String odePath = "ode";

		Server server = new Server(new InetSocketAddress(host, port));

		WebAppContext webApp = new WebAppContext();
		webApp.setDescriptor(basePath + "/WEB-INF/web.xml");
		webApp.setResourceBase(basePath);
		webApp.setContextPath("/" + odePath);

		final ExecutionServiceImpl processExecutionService = new ExecutionServiceImpl(host, port, odePath);

		final Bus cxfBus = CxfUtil.createBus();

		final CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet() {
			{
				this.bus = cxfBus;
			}

			@Override
			public void init(ServletConfig sc) throws ServletException {
				super.init(sc);
			}
		};

		ServletContextHandler cxfContext = new ServletContextHandler();
		cxfContext.setContextPath("/services");
		cxfContext.addServlet(new ServletHolder(cxfServlet), "/*");

		ServletContextHandler leaderServlet = new ServletContextHandler();
		leaderServlet.setContextPath("/leader");
		leaderServlet.addServlet(LeaderServlet.class, "/*");

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { webApp, cxfContext, leaderServlet });

		server.setHandler(contexts);

		server.start();

		processExecutionService.setOdeAxisServlet((ODEAxisServlet) webApp.getServletHandler().getServlet("AxisServlet").getServlet());

		CxfUtil.createService(cxfBus, ProcessExecutionService.class, "/" + ProcessExecutionService.class.getSimpleName(), processExecutionService);

		if (publishHost != null && publishPort != null) {
			try {

				PROCESS_EXECUTION_SERVICE_URL = "http://" + publishHost + ":" + publishPort + "/services/ProcessExecutionService";

				UddiRegister.publishService(UddiLocator.getUDDITransport(), "root", "", //
						"uddi:gsigma.ufsc.br:repository", WebServiceLocator.BPEL_PROCESS_EXECUTION_SERVICE_KEY, //
						"BPEL Process Execution Service", //
						"BPEL Process Execution Service", //
						PROCESS_EXECUTION_SERVICE_URL + "?wsdl", "wsdlDeployment", //
						new TModelInstanceInfo[] { //
								UddiRegister.getTModelInstanceInfo("uddi:gsigma.ufsc.br:uddi:processexecutionservice"), //
								UddiRegister.getTModelInstanceInfo("uddi:gsigma.ufsc.br:uddi:processexecutionservice-bpel") });

			} catch (Exception e) {
				logger.error("Error registering BPELProcessExecutionService in UDDI: " + e.getMessage());
			}
		}

		EventSender eventSender = EventSender.getInstance();
		eventSender.sendEvent(new BootstrapCompleteEvent(ComponentName.EXECUTION_SERVICE, eventSender.getSenderId()));

		server.join();

	}

	@SuppressWarnings("unused")
	private static void unzipDerbyDb(File dbDir) throws IOException, FileNotFoundException {
		try (FileInputStream in = new FileInputStream(new File("ode-db.zip"))) {
			ZipUtils.unzip(in, dbDir);
		}
	}
}
