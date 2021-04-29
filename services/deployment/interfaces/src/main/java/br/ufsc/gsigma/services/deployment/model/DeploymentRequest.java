package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import br.ufsc.gsigma.catalog.services.model.ITConfiguration;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

@XmlAccessorType(XmlAccessType.FIELD)
public class DeploymentRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private ITConfiguration itConfiguration;

	private br.ufsc.gsigma.catalog.services.model.Process businessProcess;

	private String businessProcessName;

	private ExecutionContext executionContext;

	public br.ufsc.gsigma.catalog.services.model.Process getBusinessProcess() {
		return businessProcess;
	}

	public void setBusinessProcess(br.ufsc.gsigma.catalog.services.model.Process businessProcess) {
		this.businessProcess = businessProcess;
	}

	public ITConfiguration getItConfiguration() {
		return itConfiguration;
	}

	public void setItConfiguration(ITConfiguration itConfiguration) {
		this.itConfiguration = itConfiguration;
	}

	public ServicesInformation getServicesInformation() {
		return this.itConfiguration != null ? this.itConfiguration.getServicesInformation() : null;
	}

	public String getBusinessProcessName() {
		return businessProcessName;
	}

	public void setBusinessProcessName(String businessProcessName) {
		this.businessProcessName = businessProcessName;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

}
