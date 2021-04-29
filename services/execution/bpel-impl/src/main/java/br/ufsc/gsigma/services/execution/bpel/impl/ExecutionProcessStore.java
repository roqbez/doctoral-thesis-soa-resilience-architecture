package br.ufsc.gsigma.services.execution.bpel.impl;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.store.ConfStoreConnectionFactory;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfImpl;
import org.apache.ode.store.ProcessStoreImpl;

import br.ufsc.gsigma.infrastructure.util.ZipUtils;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store.InfinispanDeploymentUnitDAO;

public class ExecutionProcessStore extends ProcessStoreImpl {

	private static final Logger logger = Logger.getLogger(ExecutionProcessStore.class);

	public ExecutionProcessStore() {
		super();
	}

	public ExecutionProcessStore(EndpointReferenceContext eprContext, DataSource ds, String persistenceType, OdeConfigProperties props, boolean createDatamodel) {
		super(eprContext, ds, persistenceType, props, createDatamodel);
	}

	public ExecutionProcessStore(EndpointReferenceContext eprContext, DataSource inMemDs) {
		super(eprContext, inMemDs);
	}

	public void setConfStoreConnectionFactory(ConfStoreConnectionFactory connectionFactory) {
		try {
			Field f = ProcessStoreImpl.class.getDeclaredField("_cf");
			f.setAccessible(true);
			f.set(this, connectionFactory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected File findDeployDir(DeploymentUnitDAO dudao) {

		if (dudao instanceof InfinispanDeploymentUnitDAO) {
			try {

				InfinispanDeploymentUnitDAO ispnDuDao = (InfinispanDeploymentUnitDAO) dudao;

				File f = new File(_deployDir, dudao.getName());

				if (!f.exists()) {
					logger.info("Creating directory for process --> " + f.getAbsolutePath());
					f.mkdirs();

					ZipUtils.unzip(ispnDuDao.getDeploymentUnitBinary(), f);
					File deployedMarker = new File(f.getParent(), dudao.getName() + ".deployed");
					deployedMarker.createNewFile();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
			}
		}

		return super.findDeployDir(dudao);
	}

	public List<ProcessConfImpl> loadDeploymentUnit(DeploymentUnitDAO dudao) {

		List<ProcessConfImpl> processes = super.load(dudao);

		// Dispatch DISABLED, RETIRED and ACTIVE events in that order
		Collections.sort(processes, new Comparator<ProcessConf>() {
			public int compare(ProcessConf o1, ProcessConf o2) {
				return stateValue(o1.getState()) - stateValue(o2.getState());
			}

			int stateValue(ProcessState state) {
				if (ProcessState.DISABLED.equals(state))
					return 0;
				if (ProcessState.RETIRED.equals(state))
					return 1;
				if (ProcessState.ACTIVE.equals(state))
					return 2;
				throw new IllegalStateException("Unexpected process state: " + state);
			}
		});

		for (ProcessConfImpl p : processes) {
			try {
				logger.info("Loading process " + p.getProcessId());
				fireStateChange(p.getProcessId(), p.getState(), dudao.getName());
			} catch (Exception except) {
				logger.error("Error while activating process: pid=" + p.getProcessId() + " package=" + dudao.getName(), except);
			}
		}

		return processes;
	}

	private void fireStateChange(QName processId, ProcessState state, String duname) {
		switch (state) {
		case ACTIVE:
			fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTVIATED, processId, duname));
			break;
		case DISABLED:
			fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DISABLED, processId, duname));
			break;
		case RETIRED:
			fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.RETIRED, processId, duname));
			break;
		}
	}

}
