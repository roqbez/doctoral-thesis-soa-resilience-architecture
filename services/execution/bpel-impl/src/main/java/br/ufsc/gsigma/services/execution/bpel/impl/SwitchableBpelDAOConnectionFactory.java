package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;

public class SwitchableBpelDAOConnectionFactory implements BpelDAOConnectionFactory {

	private BpelDAOConnectionFactory delegate;

	private static final ThreadLocal<BpelDAOConnectionFactory> threadLocal = new ThreadLocal<BpelDAOConnectionFactory>();

	public SwitchableBpelDAOConnectionFactory(BpelDAOConnectionFactory delegate) {
		this.delegate = delegate;
	}

	public static void setThreadLocalBpelDAOConnectionFactory(BpelDAOConnectionFactory cf) {
		threadLocal.set(cf);
	}

	public static void removeThreadLocalBpelDAOConnectionFactory() {
		threadLocal.remove();
	}

	private BpelDAOConnectionFactory getDelegate() {
		BpelDAOConnectionFactory cf = threadLocal.get();
		return cf != null ? cf : delegate;
	}

	public BpelDAOConnection getConnection() {
		return getDelegate().getConnection();
	}

	public void init(Properties properties) {
		getDelegate().init(properties);
	}

	public void shutdown() {
		getDelegate().shutdown();
	}

	public DataSource getDataSource() {
		return getDelegate().getDataSource();
	}

}
