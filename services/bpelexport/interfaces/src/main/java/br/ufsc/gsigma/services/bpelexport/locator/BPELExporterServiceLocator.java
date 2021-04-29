package br.ufsc.gsigma.services.bpelexport.locator;

import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.services.bpelexport.interfaces.BPELExporterService;

public abstract class BPELExporterServiceLocator {

	public static BPELExporterService get() {
		return WebServiceLocator.locateService(WebServiceLocator.BPEL_EXPORT_SERVICE_UDDI_SERVICE_KEY, BPELExporterService.class);
	}

}
