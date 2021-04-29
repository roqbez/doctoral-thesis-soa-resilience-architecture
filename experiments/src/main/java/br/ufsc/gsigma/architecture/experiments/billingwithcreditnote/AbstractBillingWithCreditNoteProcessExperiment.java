package br.ufsc.gsigma.architecture.experiments.billingwithcreditnote;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.architecture.experiments.AbstractProcessExperiment;

public abstract class AbstractBillingWithCreditNoteProcessExperiment extends AbstractProcessExperiment {

	@Override
	public String getProcessName() {
		return "BillingwithCreditNoteProcess";
	}

	@Override
	protected String getServicesPrimaryCategory() {
		return "billing";
	}

	@Override
	public URL getDeployProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/deploymentService_deployApplication_BillingWithCreditNoteProcess_Thesis1.xml");
	}

	@Override
	public URL getExecuteProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/processExecutionService_executeProcess_BillingWithCreditNoteProcess_Thesis1.xml");
	}

	@Override
	protected List<String> getServicePaths() {

		List<String> servicePath = new LinkedList<String>();

		servicePath.add("/services/ubl/billingwithcreditnote/accountingCustomer/acceptCharges");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingCustomer/acceptCredit");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingCustomer/receiveCreditNote");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingCustomer/receiveInvoice");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingCustomer/reconcileCharges");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingCustomer/sendAccountResponse");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingSupplier/raiseCreditNote");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingSupplier/raiseInvoice");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingSupplier/receiveAccountResponse");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingSupplier/reconcileCharges");
		servicePath.add("/services/ubl/billingwithcreditnote/accountingSupplier/validateResponse");

		return servicePath;
	}

}
