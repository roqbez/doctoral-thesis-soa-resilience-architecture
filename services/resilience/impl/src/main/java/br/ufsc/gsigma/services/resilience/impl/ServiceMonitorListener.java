package br.ufsc.gsigma.services.resilience.impl;

import br.ufsc.gsigma.services.resilience.support.SOAServiceCheck;

public interface ServiceMonitorListener {

	public void serviceAvailable(SOAServiceCheck service);

	public void serviceUnavailable(SOAServiceCheck service);

}
