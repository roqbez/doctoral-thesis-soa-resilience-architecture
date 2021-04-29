package br.ufsc.gsigma.binding.bootstrap;

import java.io.File;

import org.apache.cxf.Bus;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.binding.support.LeaderServlet;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent.ComponentName;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;

public class RunBindingService {

	public static final String BINDING_SERVICE_PORT = "bindingservice.port";

	public static final String BINDING_SERVICE_HOSTNAME = "bindingservice.host";

	private static final Logger logger = LoggerFactory.getLogger(RunBindingService.class);

	public static String HOST;

	public static Integer PORT;

	public static void main(String[] args) throws Throwable {

		try {

			String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
			int port = args.length > 1 ? Integer.parseInt(args[1]) : ServicesAddresses.BINDING_SERVICE_PORT;

			String publishHost = args.length > 2 ? args[2] : String.valueOf(ServicesAddresses.BINDING_SERVICE_HOSTNAME);
			String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.BINDING_SERVICE_PORT);

			HOST = host;
			PORT = port;

			System.setProperty(BINDING_SERVICE_HOSTNAME, host);

			System.setProperty(BINDING_SERVICE_PORT, String.valueOf(port));

			bootstrap(host, port, publishHost, publishPort, "true".equals(System.getProperty("clean", "false")));

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public static void bootstrap(String host, int port, String publishHost, String publishPort) throws Exception {
		bootstrap(host, port, publishHost, publishPort, false);
	}

	@SuppressWarnings("resource")
	public static void bootstrap(String host, int port, String publishHost, String publishPort, boolean clean) throws Exception {

		System.setProperty("discovery.dns_query", "binding-service");

		System.setProperty("http.host", "0.0.0.0");
		System.setProperty("http.port", String.valueOf(port));

		String dataDir = System.getProperty("data.dir", null);

		if (dataDir != null) {
			System.setProperty("jms.data.dir", new File(dataDir, "jms").getAbsolutePath());
			System.setProperty("jms.data.kahadb.dir", new File(dataDir, "jms/kahadb").getAbsolutePath());
		}

		if (clean) {

			File dataDirFile = dataDir != null ? new File(dataDir) : null;

			if (dataDirFile != null && dataDirFile.exists()) {
				logger.info("Cleaning up directory --> " + dataDirFile.getAbsolutePath());
				FileUtils.recursiveDelete(dataDirFile);
			}
		}

		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] { "classpath:applicationContext.xml" });

		if (publishHost != null && publishPort != null) {
			try {
				String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/BindingService";

				UddiRegister.publishService(UddiLocator.getUDDITransport(), "root", "", //
						"uddi:gsigma.ufsc.br:repository", WebServiceLocator.BINDING_SERVICE_KEY, //
						"Binding Service", //
						"Binding Service", //
						publishUrl + "?wsdl", "wsdlDeployment", //
						new TModelInstanceInfo[0]);
			} catch (Exception e) {
				logger.error("Error registering BindingService in UDDI: " + e.getMessage());
			}
		}

		Bus cxfBus = applicationContext.getBean(Bus.class);

		JettyHTTPServerEngineFactory jettyFactory = cxfBus.getExtension(JettyHTTPServerEngineFactory.class);

		Server jettyServer = jettyFactory.createJettyHTTPServerEngine(port, "http").getServer();

		ContextHandlerCollection handler = (ContextHandlerCollection) jettyServer.getHandler();

		ServletContextHandler servletContextHandler = new ServletContextHandler(handler, "/leader");
		servletContextHandler.addServlet(LeaderServlet.class, "/*");
		servletContextHandler.start();

		EventSender eventSender = applicationContext.getBean(EventSender.class);
		eventSender.sendEvent(new BootstrapCompleteEvent(ComponentName.BINDING_SERVICE, eventSender.getSenderId()));
	}

	public static String getServiceHost() {
		return HOST;
	}

	public static Integer getServicePort() {
		return PORT;
	}

}
