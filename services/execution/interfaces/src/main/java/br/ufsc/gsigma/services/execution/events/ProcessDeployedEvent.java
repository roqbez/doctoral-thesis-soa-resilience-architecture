package br.ufsc.gsigma.services.execution.events;

import javax.xml.namespace.QName;

import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;

public class ProcessDeployedEvent extends ProcessEvent {

	private static final long serialVersionUID = 1L;

	private Process businessProcess;

	private ServicesInformation servicesInformation;

	public ProcessDeployedEvent(String processExecutionServiceURL, String applicationId, String processURL, QName processId, String processName, Process businessProcess,
			ServicesInformation servicesInformation) {
		super(processExecutionServiceURL, applicationId, processURL, processId, processName);
		this.businessProcess = businessProcess;
		this.servicesInformation = servicesInformation;
	}

	public Process getBusinessProcess() {
		return businessProcess;
	}

	public ServicesInformation getServicesInformation() {
		return servicesInformation;
	}

}
