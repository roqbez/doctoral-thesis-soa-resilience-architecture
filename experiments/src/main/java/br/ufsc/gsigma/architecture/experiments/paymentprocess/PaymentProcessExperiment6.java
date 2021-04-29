package br.ufsc.gsigma.architecture.experiments.paymentprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;

public class PaymentProcessExperiment6 extends AbstractPaymentProcessExperiment {

	private static final int REPEATS = 100;

	private static final String EXECUTION_ID = "exp4-bs1-exs1-rs1";

	public static void main(String[] args) throws Exception {
		new PaymentProcessExperiment6().executeInternal(new ExecutionExperimentParams(EXECUTION_ID, REPEATS, true, true, null, null));
	}

	@Override
	protected List<String> getServicesDeploymentServers() {
		return getDefaultServicesDeploymentServers();
	}

	protected Map<String, Integer> getServicesPathAvailability() {
		Map<String, Integer> servicePath = new HashMap<String, Integer>();
		servicePath.put("/services/ubl/paymentprocess/accountingCustomer/authorizePayment", 1);
		servicePath.put("/services/ubl/paymentprocess/accountingCustomer/notifyOfPayment", 0);
		servicePath.put("/services/ubl/paymentprocess/accountingSupplier/notifyPayee", 0);
		servicePath.put("/services/ubl/paymentprocess/accountingSupplier/receiveAdvice", 0);
		servicePath.put("/services/ubl/paymentprocess/payeeParty/receiveAdvice", 1);
		return servicePath;
	}

}
