package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;

public class InfinispanBpelDAOConnectionFactory implements BpelDAOConnectionFactoryJDBC {

	public static InfinispanBpelDAOConnectionFactory INSTANCE = new InfinispanBpelDAOConnectionFactory();

	private InfinispanBpelDAOConnectionFactory() {
	}

	@Override
	public void init(Properties properties) {
	}

	@Override
	public BpelDAOConnection getConnection() {
		return InfinispanBpelDAOConnection.INSTANCE;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public DataSource getDataSource() {
		return null;
	}

	@Override
	public void setDataSource(DataSource ds) {
	}

	@Override
	public void setUnmanagedDataSource(DataSource ds) {
	}

	@Override
	public void setTransactionManager(Object tm) {
	}

}
