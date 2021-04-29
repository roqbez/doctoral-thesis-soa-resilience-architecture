package br.ufsc.gsigma.services.resilience.bootstrap;

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

import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent.ComponentName;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;
import br.ufsc.gsigma.services.resilience.support.LeaderServlet;

public class RunResilienceService {

	private static final Logger logger = LoggerFactory.getLogger(RunResilienceService.class);

	private static String serviceHost;

	private static String servicePort;

	public static void main(String[] args) throws Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.RESILIENCE_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : ServicesAddresses.RESILIENCE_SERVICE_HOSTNAME;
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.RESILIENCE_SERVICE_PORT);

		bootstrap(host, port, publishHost, publishPort, "true".equals(System.getProperty("clean", "false")));

	}

	@SuppressWarnings("resource")
	public static void bootstrap(String host, String port, String publishHost, String publishPort, boolean clean) throws Exception {

		System.setProperty("discovery.dns_query", "resilience-service");

		serviceHost = host;
		servicePort = port;

		System.setProperty("http.host", serviceHost);
		System.setProperty("http.port", servicePort);

		String dataDir = System.getProperty("data.dir", "db/resilience");

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

		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });

		if (publishHost != null && publishPort != null) {
			try {
				String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/ResilienceService";

				UddiRegister.publishService(UddiLocator.getUDDITransport(), "root", "", //
						"uddi:gsigma.ufsc.br:repository", WebServiceLocator.RESILIENCE_SERVICE_KEY, //
						"Resilience Service", //
						"Resilience Service", //
						publishUrl + "?wsdl", "wsdlDeployment", //
						new TModelInstanceInfo[0]);
			} catch (Exception e) {
				logger.error("Error registering ResilienceService in UDDI: " + e.getMessage());
			}
		}

		Bus cxfBus = applicationContext.getBean(Bus.class);

		JettyHTTPServerEngineFactory jettyFactory = cxfBus.getExtension(JettyHTTPServerEngineFactory.class);

		Server jettyServer = jettyFactory.createJettyHTTPServerEngine(Integer.valueOf(port), "http").getServer();

		ContextHandlerCollection handler = (ContextHandlerCollection) jettyServer.getHandler();

		ServletContextHandler servletContextHandler = new ServletContextHandler(handler, "/leader");
		servletContextHandler.addServlet(LeaderServlet.class, "/*");
		servletContextHandler.start();

		EventSender eventSender = applicationContext.getBean(EventSender.class);
		eventSender.sendEvent(new BootstrapCompleteEvent(ComponentName.RESILIENCE_SERVICE, eventSender.getSenderId()));
	}

	public static String getServiceHost() {
		return serviceHost;
	}

	public static String getServicePort() {
		return servicePort;
	}

}
