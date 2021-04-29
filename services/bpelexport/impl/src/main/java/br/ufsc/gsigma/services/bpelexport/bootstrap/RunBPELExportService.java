package br.ufsc.gsigma.services.bpelexport.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;
import br.ufsc.gsigma.services.bpelexport.impl.BPELExporterServiceImpl;
import br.ufsc.gsigma.services.bpelexport.interfaces.BPELExporterService;

public class RunBPELExportService {

	private final static Logger logger = LoggerFactory.getLogger(RunBPELExportService.class);

	public static void main(String[] args) throws Exception {

		String host = args.length > 0 ? args[0] : ServicesAddresses.DEFAULT_DOCKER_HTTP_HOST;
		String port = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.BPEL_EXPORTER_SERVICE_PORT);

		String publishHost = args.length > 2 ? args[2] : String.valueOf(ServicesAddresses.BPEL_EXPORTER_SERVICE_HOSTNAME);
		String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.BPEL_EXPORTER_SERVICE_PORT);

		String url = "http://" + host + ":" + port + "/services/BPELExporterService";

		logger.info("Bootstrapping BPELExporterService in " + url);

		CxfUtil.createService(BPELExporterService.class, url, new BPELExporterServiceImpl());

		try {
			String publishUrl = "http://" + publishHost + ":" + publishPort + "/services/BPELExporterService";
			UddiRegister.publishService(UddiLocator.getUDDITransport("default"), "root", "", //
					"uddi:gsigma.ufsc.br:repository", WebServiceLocator.BPEL_EXPORT_SERVICE_UDDI_SERVICE_KEY, //
					"Catalog BPEL Exporter Service", //
					"Web Service supporting Business Processes Catalog BPEL Export", //
					publishUrl + "?wsdl", "wsdlDeployment", //
					new TModelInstanceInfo[] {});
		} catch (Exception e) {
			logger.error("Error registering BPELExporterService in UDDI: " + e.getMessage());
		}
	}

}
