package br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.architecture.experiments.AbstractProcessExperiment;

public abstract class AbstractFulfilmentWithReceiptAdviceProcessExperiment extends AbstractProcessExperiment {

	@Override
	public String getProcessName() {
		return "FulfilmentwithReceiptAdviceProcess";
	}

	@Override
	protected String getServicesPrimaryCategory() {
		return "fulfilment";
	}

	@Override
	public URL getDeployProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/deploymentService_deployApplication_FulfilmentWithReceiptAdviceProcess_Thesis1.xml");
	}

	@Override
	public URL getExecuteProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/processExecutionService_executeProcess_FulfilmentWithReceiptAdviceProcess_Thesis1.xml");
	}

	@Override
	protected List<String> getServicePaths() {

		List<String> servicePath = new LinkedList<String>();

		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/receiveOrderItems");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/checkStatusOfItems");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/adviseBuyerOfStatus");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/determineAction");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/acceptItems");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/adjustOrder");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/sendOrderCancellation");
		servicePath.add("/services/ubl/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus");

		return servicePath;
	}

}
