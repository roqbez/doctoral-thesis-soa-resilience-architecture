package br.ufsc.gsigma.services.execution.locator;

import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.services.execution.interfaces.ProcessExecutionService;

public abstract class ProcessExecutionServiceLocator {

	public static ProcessExecutionService get() {
		return WebServiceLocator.locateService(WebServiceLocator.BPEL_PROCESS_EXECUTION_SERVICE_KEY, ProcessExecutionService.class);
	}

}
