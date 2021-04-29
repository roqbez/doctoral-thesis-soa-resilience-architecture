package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store;

import java.util.Collection;
import java.util.Date;

import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.DeploymentUnitDAO;
import org.infinispan.Cache;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

public class InfinispanConfStoreConnection implements ConfStoreConnection {

	private Cache<String, InfinispanDeploymentUnitDAO> duCache;

	private Cache<String, InfinispanVersionTrackerDAO> versionTrackerCache;

	public InfinispanConfStoreConnection(InfinispanDatabase database) {
		this.duCache = database.getCache(InfinispanDeploymentUnitDAO.class);
		this.versionTrackerCache = database.getCache(InfinispanVersionTrackerDAO.class);
	}

	@Override
	public DeploymentUnitDAO createDeploymentUnit(String name) {

		InfinispanDeploymentUnitDAO du = new InfinispanDeploymentUnitDAO(name);
		du.setDeployDate(new Date());

		du.setExecutionContext(ExecutionContext.get());

		this.duCache.put(du.getName(), du);

		return du;
	}

	@Override
	public DeploymentUnitDAO getDeploymentUnit(String name) {
		return this.duCache.get(name);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<DeploymentUnitDAO> getDeploymentUnits() {
		return (Collection) this.duCache.values();
	}

	@Override
	public long getNextVersion() {

		InfinispanVersionTrackerDAO version = versionTrackerCache.get(InfinispanVersionTrackerDAO.KEY);

		if (version == null) {
			return 1;
		} else {
			return version.getVersion() + 1;
		}
	}

	@Override
	public void setVersion(long version) {
		versionTrackerCache.put(InfinispanVersionTrackerDAO.KEY, new InfinispanVersionTrackerDAO(version));
	}

	@Override
	public void close() {
	}

}
