package br.ufsc.gsigma.services.resilience.locator;

import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.services.resilience.interfaces.ResilienceService;

public abstract class ResilienceServiceLocator {

	public static ResilienceService get() {
		return WebServiceLocator.locateService(WebServiceLocator.RESILIENCE_SERVICE_KEY, ResilienceService.class);
	}

}
