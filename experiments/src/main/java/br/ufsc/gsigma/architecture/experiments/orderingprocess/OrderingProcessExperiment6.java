package br.ufsc.gsigma.architecture.experiments.orderingprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;

public class OrderingProcessExperiment6 extends AbstractOrderingProcessExperiment {

	private static final int REPEATS = 100;

	private static final String EXECUTION_ID = "exp4-bs1-exs1-rs1";

	public static void main(String[] args) throws Exception {
		new OrderingProcessExperiment6().executeInternal(new ExecutionExperimentParams(EXECUTION_ID, REPEATS, true, true, null, null));
	}

	@Override
	protected List<String> getServicesDeploymentServers() {
		return getDefaultServicesDeploymentServers();
	}

	protected Map<String, Integer> getServicesPathAvailability() {
		Map<String, Integer> servicePath = new HashMap<String, Integer>();

		// Coordinator
		servicePath.put("/services/ubl/orderingprocess/sellerParty/receiveOrder", 1);
		servicePath.put("/services/ubl/orderingprocess/sellerParty/processOrder", 0);
		servicePath.put("/services/ubl/orderingprocess/sellerParty/acceptOrder", 1);

		// Partner 2
		servicePath.put("/services/ubl/orderingprocess/sellerParty/addDetail", 0);
		servicePath.put("/services/ubl/orderingprocess/sellerParty/changeOrder", 1);

		// Partner 3
		servicePath.put("/services/ubl/orderingprocess/sellerParty/rejectOrder", 1);
		servicePath.put("/services/ubl/orderingprocess/sellerParty/cancelOrder", 1);

		servicePath.put("/services/ubl/orderingprocess/buyerParty/receiveResponse", 0);

		return servicePath;
	}

}
