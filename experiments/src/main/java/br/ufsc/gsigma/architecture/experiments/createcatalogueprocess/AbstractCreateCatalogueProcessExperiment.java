package br.ufsc.gsigma.architecture.experiments.createcatalogueprocess;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.architecture.experiments.AbstractProcessExperiment;

public abstract class AbstractCreateCatalogueProcessExperiment extends AbstractProcessExperiment {

	@Override
	public String getProcessName() {
		return "CreateCatalogueProcess";
	}

	@Override
	protected String getServicesPrimaryCategory() {
		return "sourcing";
	}

	@Override
	public URL getDeployProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/deploymentService_deployApplication_CreateCatalogueProcess_Thesis1.xml");
	}

	@Override
	public URL getExecuteProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/processExecutionService_executeProcess_CreateCatalogueProcess_Thesis1.xml");
	}

	@Override
	protected List<String> getServicePaths() {

		List<String> servicePath = new LinkedList<String>();

		servicePath.add("/services/ubl/createcatalogueprocess/receiverParty/requestCatalogue");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/respondToRequest");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/processCatalogueRequest");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/sendRejection");
		servicePath.add("/services/ubl/createcatalogueprocess/receiverParty/receiveRejection");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/sendAcceptanceResponse");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/prepareCatalogueInformation");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/produceCatalogue");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/distributeCatalogue");
		servicePath.add("/services/ubl/createcatalogueprocess/receiverParty/receiveCatalogue");
		servicePath.add("/services/ubl/createcatalogueprocess/receiverParty/reviewCatalogueContent");
		servicePath.add("/services/ubl/createcatalogueprocess/receiverParty/acknowledgeAcceptance");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/receiveAcknowledgeAcceptance");
		servicePath.add("/services/ubl/createcatalogueprocess/receiverParty/acceptCatalogue");
		servicePath.add("/services/ubl/createcatalogueprocess/receiverParty/queryCatalogueContent");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/decideOnAction");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/cancelTransaction");
		servicePath.add("/services/ubl/createcatalogueprocess/providerParty/reviseContent");

		return servicePath;
	}

}
