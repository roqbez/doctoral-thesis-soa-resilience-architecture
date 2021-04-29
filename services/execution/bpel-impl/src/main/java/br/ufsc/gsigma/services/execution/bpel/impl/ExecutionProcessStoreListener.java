package br.ufsc.gsigma.services.execution.bpel.impl;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreEvent.Type;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.store.ProcessConfImpl;
import org.apache.ode.store.ProcessStoreImpl;

public class ExecutionProcessStoreListener implements ProcessStoreListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExecutionProcessStoreListener.class);

	private static final Field f_pinfo;

	static {
		try {
			f_pinfo = ProcessConfImpl.class.getDeclaredField("_pinfo");
			f_pinfo.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private ExecutionODEServer executionODEServer;

	public ExecutionProcessStoreListener(ExecutionODEServer executionODEServer) {
		this.executionODEServer = executionODEServer;
	}

	@Override
	public void onProcessStoreEvent(ProcessStoreEvent event) {

		try {

			if (event.type == Type.ACTVIATED) {

				ProcessStoreImpl processStore = executionODEServer.getProcessStore();
				ProcessConf processConf = processStore.getProcessConfiguration(event.pid);

				ExecutionEventsListener.processDeployed(executionODEServer.getExecutorService(), processConf);

			} else if (event.type == Type.UNDEPLOYED) {

				ProcessStoreImpl processStore = executionODEServer.getProcessStore();
				ProcessConf processConf = processStore.getProcessConfiguration(event.pid);

				// if (executionODEServer.getBpelServer().hasActiveInstances(event.pid)) {
				// prepareProcess(executionODEServer.getExecutorService(), processConf);
				// }

				ExecutionEventsListener.processUndeployed(executionODEServer.getExecutorService(), processConf);

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
