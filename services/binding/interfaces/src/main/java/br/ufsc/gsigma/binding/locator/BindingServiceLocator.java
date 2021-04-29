package br.ufsc.gsigma.binding.locator;

import br.ufsc.gsigma.binding.interfaces.BindingService;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;

public abstract class BindingServiceLocator {

	public static BindingService get() {
		return WebServiceLocator.locateService(WebServiceLocator.BINDING_SERVICE_KEY, BindingService.class);
	}

}
