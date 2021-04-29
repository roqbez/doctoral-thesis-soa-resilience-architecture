package br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice;

import java.util.HashMap;
import java.util.Map;

import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;

public class FulfilmentWithReceiptAdviceProcessExperiment2 extends AbstractFulfilmentWithReceiptAdviceProcessExperiment {

	private static final int REPEATS = 100;

	private static final String EXECUTION_ID = "exp2-bs1-exs1-rs1";

	public static void main(String[] args) throws Exception {
		new FulfilmentWithReceiptAdviceProcessExperiment2().executeInternal(new ExecutionExperimentParams(EXECUTION_ID, REPEATS, true, true, null, null));
	}

	protected Map<String, Integer> getServicesPathAvailability() {
		Map<String, Integer> servicePath = new HashMap<String, Integer>();

		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/receiveOrderItems", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/checkStatusOfItems", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/adviseBuyerOfStatus", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/determineAction", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/acceptItems", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/buyerParty/sendOrderCancellation", 1);
		servicePath.put("/services/ubl/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", 1);

		return servicePath;
	}

}
