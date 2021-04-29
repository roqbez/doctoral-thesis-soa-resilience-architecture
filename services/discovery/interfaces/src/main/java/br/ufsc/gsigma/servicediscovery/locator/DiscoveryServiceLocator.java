package br.ufsc.gsigma.servicediscovery.locator;

import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryService;

public class DiscoveryServiceLocator {

	public static DiscoveryService get() {
		return WebServiceLocator.locateService(WebServiceLocator.DISCOVERY_SERVICE_UDDI_SERVICE_KEY, DiscoveryService.class);
	}

}
