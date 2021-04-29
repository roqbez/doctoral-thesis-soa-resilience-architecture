package br.ufsc.gsigma.architecture.experiments.orderingprocess;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.architecture.experiments.AbstractProcessExperiment;

public abstract class AbstractOrderingProcessExperiment extends AbstractProcessExperiment {

	@Override
	public String getProcessName() {
		return "OrderingProcess";
	}

	@Override
	protected String getServicesPrimaryCategory() {
		return "ordering";
	}
	
	@Override
	public URL getDeployProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/deploymentService_deployApplication_OrderingProcess_Thesis1.xml");
	}

	@Override
	public URL getExecuteProcessRequestFile() throws Exception {
		return getExperimentResource("thesis1/processExecutionService_executeProcess_OrderingProcess_Thesis1.xml");
	}

	@Override
	protected List<String> getServicePaths() {
		List<String> servicePath = new LinkedList<String>();
		servicePath.add("/services/ubl/orderingprocess/buyerParty/placeOrder");
		servicePath.add("/services/ubl/orderingprocess/buyerParty/acceptOrder");
		servicePath.add("/services/ubl/orderingprocess/buyerParty/rejectOrder");
		servicePath.add("/services/ubl/orderingprocess/buyerParty/changeOrder");
		servicePath.add("/services/ubl/orderingprocess/buyerParty/cancelOrder");
		servicePath.add("/services/ubl/orderingprocess/buyerParty/receiveResponse");
		servicePath.add("/services/ubl/orderingprocess/sellerParty/receiveOrder");
		servicePath.add("/services/ubl/orderingprocess/sellerParty/processOrder");
		servicePath.add("/services/ubl/orderingprocess/sellerParty/acceptOrder");
		servicePath.add("/services/ubl/orderingprocess/sellerParty/rejectOrder");
		servicePath.add("/services/ubl/orderingprocess/sellerParty/addDetail");
		servicePath.add("/services/ubl/orderingprocess/sellerParty/cancelOrder");
		servicePath.add("/services/ubl/orderingprocess/sellerParty/changeOrder");
		return servicePath;
	}

}
