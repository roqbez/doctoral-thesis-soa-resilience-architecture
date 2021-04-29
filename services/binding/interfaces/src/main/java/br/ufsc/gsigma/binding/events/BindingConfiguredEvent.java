package br.ufsc.gsigma.binding.events;

import br.ufsc.gsigma.binding.model.BindingConfiguration;

public class BindingConfiguredEvent extends BindingServiceEvent {

	private static final long serialVersionUID = 1L;

	private BindingConfiguration configuration;

	public BindingConfiguredEvent(BindingConfiguration configuration) {
		this.configuration = configuration;
	}

	public BindingConfiguration getConfiguration() {
		return configuration;
	}

}
