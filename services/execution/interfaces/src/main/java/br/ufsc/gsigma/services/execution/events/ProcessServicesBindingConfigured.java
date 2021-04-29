package br.ufsc.gsigma.services.execution.events;

import javax.xml.namespace.QName;

import br.ufsc.gsigma.binding.model.BindingConfiguration;

public class ProcessServicesBindingConfigured extends ProcessEvent {

	private static final long serialVersionUID = 1L;

	private BindingConfiguration bindingConfiguration;

	public ProcessServicesBindingConfigured(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName, BindingConfiguration bindingConfiguration) {
		super(processExecutionServiceURL, applicationId, processURL, processId, processName);
		this.bindingConfiguration = bindingConfiguration;
	}

	public BindingConfiguration getBindingConfiguration() {
		return bindingConfiguration;
	}

}
