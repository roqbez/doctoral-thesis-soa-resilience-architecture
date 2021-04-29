package br.ufsc.gsigma.services.execution.bpel.impl;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelEngineImpl;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.Contexts;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.ProcessConf;

public class ExecutionBpelServer extends BpelServerImpl {

	private ExecutionBpelEngine executionBpelEngine;

	private ExecutionODEServer odeServer;

	private ExecutorService executorService;

	private BpelDAOConnectionFactory inMemDaoConnectionFactory;

	private EndpointReferenceContext eprContext;

	private static final Field MNGMT_LOCK_FIELD;

	static {

		try {
			MNGMT_LOCK_FIELD = BpelServerImpl.class.getDeclaredField("_mngmtLock");
			MNGMT_LOCK_FIELD.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ReadWriteLock getManagementLock() {
		try {
			return (ReadWriteLock) MNGMT_LOCK_FIELD.get(this);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	};

	public ExecutionBpelServer(ExecutionODEServer odeServer) {
		this.odeServer = odeServer;
		this.executorService = odeServer.getExecutorService();
	}

	public EndpointReferenceContext getEndpointReferenceContext() throws BpelEngineException {
		return eprContext;
	}

	@Override
	public void setEndpointReferenceContext(EndpointReferenceContext eprContext) throws BpelEngineException {
		super.setEndpointReferenceContext(eprContext);
		this.eprContext = eprContext;
	}

	protected BpelEngineImpl createBpelEngineImpl(Contexts contexts) {
		this.executionBpelEngine = new ExecutionBpelEngine(contexts, executorService);
		return executionBpelEngine;
	}

	protected BpelProcess createBpelProcess(ProcessConf conf) {
		return new ExecutionBpelProcess(conf);
	}

	ExecutionBpelEngine getExecutionBpelEngine() {
		return executionBpelEngine;
	}

	public void setInMemDaoConnectionFactory(BpelDAOConnectionFactory daoCF) {
		super.setInMemDaoConnectionFactory(daoCF);
		this.inMemDaoConnectionFactory = daoCF;
	}

	public BpelDAOConnectionFactory getInMemDaoConnectionFactory() {
		return inMemDaoConnectionFactory;
	}

	public ExecutionODEServer getOdeServer() {
		return odeServer;
	}

}
