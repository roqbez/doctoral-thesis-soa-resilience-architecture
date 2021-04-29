package br.ufsc.gsigma.catalog.services.locator;

import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;

public abstract class CatalogServiceLocator {

	public static CatalogService get() {
		return WebServiceLocator.locateService(WebServiceLocator.CATALOG_SERVICE_UDDI_SERVICE_KEY, CatalogService.class);
	}

}
