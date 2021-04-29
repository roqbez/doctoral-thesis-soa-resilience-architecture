package br.ufsc.gsigma.architecture.experiments.billingwithcreditnote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;

public class BillingWithCreditNoteProcessExperiment6 extends AbstractBillingWithCreditNoteProcessExperiment {

	private static final int REPEATS = 100;

	private static final String EXECUTION_ID = "exp4-bs1-exs1-rs1";

	public static void main(String[] args) throws Exception {
		new BillingWithCreditNoteProcessExperiment6().executeInternal(new ExecutionExperimentParams(EXECUTION_ID, REPEATS, true, true, null, null));
	}

	@Override
	protected List<String> getServicesDeploymentServers() {
		return getDefaultServicesDeploymentServers();
	}

	protected Map<String, Integer> getServicesPathAvailability() {
		Map<String, Integer> servicePath = new HashMap<String, Integer>();

		servicePath.put("/services/ubl/billingwithcreditnote/accountingCustomer/acceptCharges", 1);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingCustomer/acceptCredit", 1);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingCustomer/receiveCreditNote", 1);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingCustomer/receiveInvoice", 1);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingCustomer/reconcileCharges", 0);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingCustomer/sendAccountResponse", 1);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingSupplier/raiseCreditNote", 1);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingSupplier/raiseInvoice", 0);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingSupplier/receiveAccountResponse", 1);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingSupplier/reconcileCharges", 0);
		servicePath.put("/services/ubl/billingwithcreditnote/accountingSupplier/validateResponse", 1);

		return servicePath;
	}

}
