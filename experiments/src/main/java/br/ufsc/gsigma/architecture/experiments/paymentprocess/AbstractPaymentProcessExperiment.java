package br.ufsc.gsigma.architecture.experiments.paymentprocess;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.architecture.experiments.AbstractProcessExperiment;

public abstract class AbstractPaymentProcessExperiment extends AbstractProcessExperiment {

	@Override
	public String getProcessName() {
		return "PaymentProcess";
	}

	@Override
	protected String getServicesPrimaryCategory() {
		return "payment";
	}

	@Override
	public URL getDeployProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/deploymentService_deployApplication_PaymentProcess_Thesis1.xml");
	}

	@Override
	public URL getExecuteProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/processExecutionService_executeProcess_PaymentProcess_Thesis1.xml");
	}

	@Override
	protected List<String> getServicePaths() {

		List<String> servicePath = new LinkedList<String>();

		servicePath.add("/services/ubl/paymentprocess/accountingCustomer/authorizePayment");
		servicePath.add("/services/ubl/paymentprocess/accountingCustomer/notifyOfPayment");
		servicePath.add("/services/ubl/paymentprocess/accountingSupplier/notifyPayee");
		servicePath.add("/services/ubl/paymentprocess/accountingSupplier/receiveAdvice");
		servicePath.add("/services/ubl/paymentprocess/payeeParty/receiveAdvice");

		return servicePath;
	}

}
