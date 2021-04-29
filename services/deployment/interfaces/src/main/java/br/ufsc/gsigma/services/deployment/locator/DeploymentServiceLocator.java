package br.ufsc.gsigma.services.deployment.locator;

import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;

public abstract class DeploymentServiceLocator {

	public static DeploymentService get() {
		return WebServiceLocator.locateService(WebServiceLocator.DEPLOYMENT_SERVICE_KEY, DeploymentService.class);
	}

}
