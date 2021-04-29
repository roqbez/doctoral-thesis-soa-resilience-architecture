package br.ufsc.gsigma.architecture.experiments.orderingprocess;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;
import br.ufsc.gsigma.infrastructure.util.docker.DockerServers;

public class OrderingProcessExperiment3 extends AbstractOrderingProcessExperiment {

	private static final List<String> SERVERS = Collections.singletonList(DockerServers.SERVER_150_162_6_131);

	private static final int REPEATS = 100;

	private static final String EXECUTION_ID = "exp3-bs1-exs1-rs1";

	public static void main(String[] args) throws Exception {
		new OrderingProcessExperiment3().executeInternal(new ExecutionExperimentParams(EXECUTION_ID, REPEATS, true, true, null, null));
	}

	@Override
	protected List<String> getServicesDeploymentServers() {
		return SERVERS;
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
		servicePath.put("/services/ubl/orderingprocess/sellerParty/rejectOrder", 0);
		servicePath.put("/services/ubl/orderingprocess/sellerParty/cancelOrder", 1);

		return servicePath;
	}

}
