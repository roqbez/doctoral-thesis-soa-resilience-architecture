package br.ufsc.gsigma.binding.events;

import br.ufsc.gsigma.binding.model.BindingConfigurationRequest;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;

public class BindingConfigurationRequestEvent extends BindingServiceEvent implements RequestEvent {

	private static final long serialVersionUID = 1L;

	private BindingConfigurationRequest configurationRequest;

	public BindingConfigurationRequestEvent(BindingConfigurationRequest configurationRequest) {
		this.configurationRequest = configurationRequest;
	}

	public BindingConfigurationRequestEvent(BindingConfigurationRequest configurationRequest, ResilienceInfo resilienceInfo) {
		this.configurationRequest = configurationRequest;
		this.configurationRequest.setResilienceInfo(resilienceInfo);
	}

	public BindingConfigurationRequest getConfigurationRequest() {
		return configurationRequest;
	}

}
