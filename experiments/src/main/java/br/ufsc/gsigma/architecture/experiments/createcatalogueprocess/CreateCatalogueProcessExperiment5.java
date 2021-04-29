package br.ufsc.gsigma.architecture.experiments.createcatalogueprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;

public class CreateCatalogueProcessExperiment5 extends AbstractCreateCatalogueProcessExperiment {

	private static final int REPEATS = 100;

	private static final String EXECUTION_ID = "exp4-bs1-exs1-rs1";

	public static void main(String[] args) throws Exception {
		new CreateCatalogueProcessExperiment5().executeInternal(new ExecutionExperimentParams(EXECUTION_ID, REPEATS, true, true, null, null));
	}

	@Override
	protected List<String> getServicesDeploymentServers() {
		return getDefaultServicesDeploymentServers();
	}

	protected Map<String, Integer> getServicesPathAvailability() {
		Map<String, Integer> servicePath = new HashMap<String, Integer>();

		servicePath.put("/services/ubl/createcatalogueprocess/receiverParty/requestCatalogue", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/respondToRequest", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/processCatalogueRequest", 0);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/sendRejection", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/receiverParty/receiveRejection", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/sendAcceptanceResponse", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/prepareCatalogueInformation", 0);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/produceCatalogue", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/distributeCatalogue", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/receiverParty/receiveCatalogue", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/receiverParty/reviewCatalogueContent", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/receiverParty/acknowledgeAcceptance", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/receiveAcknowledgeAcceptance", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/receiverParty/acceptCatalogue", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/receiverParty/queryCatalogueContent", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/decideOnAction", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/cancelTransaction", 1);
		servicePath.put("/services/ubl/createcatalogueprocess/providerParty/reviseContent", 1);

		return servicePath;
	}

}
