package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store;

import org.apache.log4j.Logger;
import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.ConfStoreConnectionFactory;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.util.TxLog;

public class InfinispanConfStoreConnectionFactory implements ConfStoreConnectionFactory {

	private static final Logger logger = Logger.getLogger(InfinispanConfStoreConnectionFactory.class);

	private InfinispanDatabase database;

	public InfinispanConfStoreConnectionFactory(InfinispanDatabase database) {
		this.database = database;
	}

	@Override
	public ConfStoreConnection getConnection() {
		return new InfinispanConfStoreConnection(database);
	}

	@Override
	public void beginTransaction() {
		try {
			if (logger.isDebugEnabled())
				logger.debug("begin transaction on " + database.getTransactionManager());
			database.getTransactionManager().begin();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void commitTransaction() {
		try {
			if (logger.isDebugEnabled())
				logger.debug("commit transaction on " + database.getTransactionManager());

			long s = System.currentTimeMillis();
			database.getTransactionManager().commit();
			long d = System.currentTimeMillis() - s;

			TxLog.info("TX Commit in " + d + " ms");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void rollbackTransaction() {
		try {
			if (logger.isDebugEnabled())
				logger.debug("rollback transaction on " + database.getTransactionManager());
			database.getTransactionManager().rollback();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
