package br.ufsc.gsigma.services.execution.bpel.impl;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ode.axis2.ODEConfigProperties;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.GUID;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanBpelDAOConnectionFactory;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.scheduler.InfinispanDatabaseDelegate;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store.InfinispanConfStoreConnectionFactory;
import br.ufsc.gsigma.services.execution.bpel.ode.AxisODEServer;
import br.ufsc.gsigma.services.execution.bpel.ode.DeploymentPoller;
import br.ufsc.gsigma.services.execution.bpel.ode.ODEManagementService;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.DatabaseDelegate;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.JdbcDelegate;

public class ExecutionODEServer extends AxisODEServer {

	private BpelDAOConnectionFactory bpelDAOConnectionFactory;

	private static ExecutionODEServer INSTANCE;

	public ExecutionODEServer() {
		INSTANCE = this;
	}

	public static ExecutionODEServer getInstance() {
		return INSTANCE;
	}

	public AxisConfiguration getAxisConfiguration() {
		return _axisConfig;
	}

	@Override
	protected BpelServerImpl createBpelServer() {
		return new ExecutionBpelServer(this);
	}

	@Override
	public void init(String contextPath, AxisConfiguration axisConf) throws ServletException {
		super.init(contextPath, axisConf);
	}

	public DeploymentPoller getDeploymentPoller() {
		return _poller;
	}

	@Override
	protected File obtainDeployDir() {

		File f = getDataDir();

		if (f != null) {
			_odeConfig.getProperties().put(OdeConfigProperties.PROP_DEPLOY_DIR, f.getAbsolutePath());
			return f;
		} else {
			return super.obtainDeployDir();
		}

	}

	private File getDataDir() {

		String dataDir = System.getProperty("data.dir", null);

		if (dataDir != null) {
			File f = new File(dataDir, "processes");
			if (!f.exists()) {
				f.mkdirs();
			}
			return f;
		} else {
			return null;
		}
	}

	@Override
	protected void createDeploymentWebService(AxisConfiguration axisConfig, ProcessStoreImpl store, DeploymentPoller poller, File appRoot, File workRoot) throws ServletException {
		File f = getDataDir();
		super.createDeploymentWebService(axisConfig, store, poller, appRoot, f != null ? f.getParentFile() : workRoot);
	}

	@Override
	protected ProcessStoreImpl createProcessStore(EndpointReferenceContext eprContext, DataSource ds) {

		ExecutionProcessStore store;

		if (_db instanceof InfinispanDatabase) {
			store = new ExecutionProcessStore(eprContext, ds, InfinispanBpelDAOConnectionFactory.class.getName(), _odeConfig, false);
			store.setConfStoreConnectionFactory(new InfinispanConfStoreConnectionFactory((InfinispanDatabase) _db));
		} else {
			store = new ExecutionProcessStore(eprContext, ds, _odeConfig.getDAOConnectionFactory(), _odeConfig, false);
		}

		store.registerListener(new ExecutionProcessStoreListener(this));
		return store;
	}

	@Override
	protected Database createDatabase(ODEConfigProperties odeConfig) {

		if (InfinispanBpelDAOConnectionFactory.class.getName().equals(odeConfig.getProperty("dao.factory"))) {
			return new InfinispanDatabase(odeConfig, (ExecutionBpelServer) _bpelServer, this);
		} else {
			return super.createDatabase(odeConfig);
		}
	}

	@Override
	protected ODEManagementService createManagementService() {
		return new ExecutionODEManagementService();
	}

	@Override
	protected void initDAO() throws ServletException {
		super.initDAO();
		this.bpelDAOConnectionFactory = _daoCF;
		_daoCF = new SwitchableBpelDAOConnectionFactory(_daoCF);
	}

	public BpelDAOConnectionFactory getBpelDAOConnectionFactory() {
		return this.bpelDAOConnectionFactory;
	}

	@Override
	protected Scheduler createScheduler() {
		ExecutionScheduler scheduler = new ExecutionScheduler(new GUID().toString(), createDatabaseDelegate(), _odeConfig.getProperties());
		scheduler.setExecutorService(_executorService);
		scheduler.setTransactionManager(_txMgr);
		return scheduler;
	}

	protected DatabaseDelegate createDatabaseDelegate() {
		if (_db instanceof InfinispanDatabase) {
			return new InfinispanDatabaseDelegate((InfinispanDatabase) _db);
		} else {
			return new JdbcDelegate(_db.getDataSource());
		}
	}

	@Override
	protected BindingContext createBindingContext() {
		return new ExecutionBindingContext(this);
	}

	@Override
	protected ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
		return new ExecutionExecutorService(super.newCachedThreadPool(threadFactory));
	}

	@Override
	protected ExecutorService newFixedThreadPool(ThreadFactory threadFactory) {
		return new ExecutionExecutorService(super.newFixedThreadPool(threadFactory));
	}

}
