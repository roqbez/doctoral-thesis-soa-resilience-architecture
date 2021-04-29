package br.ufsc.gsigma.services.execution.bpel.impl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.ProcessAndInstanceManagementImpl;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessStore;

import br.ufsc.gsigma.services.execution.bpel.ode.ODEManagementService;

public class ExecutionODEManagementService extends ODEManagementService {

	@SuppressWarnings("unused")
	private BpelDAOConnectionFactory persistentDaoConnectionFactory;

	// private BpelDAOConnectionFactory inMemDaoConnectionFactory;

	@Override
	protected ProcessAndInstanceManagementImpl createProcessAndInstanceManagement(BpelServer server, ProcessStore _store) {
		this.persistentDaoConnectionFactory = ((ExecutionBpelServer) server).getOdeServer().getBpelDAOConnectionFactory();
		// this.inMemDaoConnectionFactory = ((ExecutionBpelServer) server).getInMemDaoConnectionFactory();
		return super.createProcessAndInstanceManagement(server, _store);
	}

	@Override
	protected <T> DynamicMessageReceiver<T> createDynamicMessageReceiver(T service) {
		return new DynamicMessageReceiver<T>(service) {
			@Override
			public void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {
				try {
					// SwitchableBpelDAOConnectionFactory.setThreadLocalBpelDAOConnectionFactory(inMemDaoConnectionFactory);
					super.invokeBusinessLogic(messageContext);
				} finally {
					// SwitchableBpelDAOConnectionFactory.removeThreadLocalBpelDAOConnectionFactory();
				}
			}
		};
	}

}
