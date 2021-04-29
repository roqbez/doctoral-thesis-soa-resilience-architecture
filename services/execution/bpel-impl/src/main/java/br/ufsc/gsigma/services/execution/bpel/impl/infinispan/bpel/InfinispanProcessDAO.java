package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.lucene.index.Term;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

public class InfinispanProcessDAO implements ProcessDAO, InfinispanObject<String> {

	private static final long serialVersionUID = 1L;

	private String processId;

	private String processType;

	private String guid;

	private long version;

	public InfinispanProcessDAO(QName pid, QName type, String guid, long version) {
		this.processId = pid.toString();
		this.processType = type.toString();
		this.guid = guid;
		this.version = version;
	}

	@Override
	public String getId() {
		return processId;
	}

	@Override
	public QName getProcessId() {
		return QName.valueOf(processId);
	}

	@Override
	public QName getType() {
		return QName.valueOf(processType);
	}

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public Collection<ProcessInstanceDAO> findInstance(CorrelationKey cckey) {

		List<InfinispanScopeDAO> scopes = InfinispanDatabase.query(InfinispanScopeDAO.class, //
				new Term("correlation.correlationKey", cckey.toCanonicalString()));

		Collection<ProcessInstanceDAO> result = new ArrayList<ProcessInstanceDAO>();

		for (InfinispanScopeDAO s : scopes) {
			result.add(s.getProcessInstance());
		}

		return result;
	}

	@Override
	public ProcessInstanceDAO createInstance(CorrelatorDAO instantiatingCorrelator) {
		InfinispanProcessInstanceDAO inst = new InfinispanProcessInstanceDAO((InfinispanCorrelatorDAO) instantiatingCorrelator, this);
		InfinispanDatabase.addCacheEntry(inst);
		return inst;
	}

	@Override
	public ProcessInstanceDAO getInstance(Long iid) {
		InfinispanProcessInstanceDAO instance = InfinispanDatabase.getCacheEntry(InfinispanProcessInstanceDAO.class, iid);
		return instance;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<ProcessInstanceDAO> getInstances() {
		Collection instances = (Collection) InfinispanDatabase.query(InfinispanProcessInstanceDAO.class, //
				new Term("processId", getId()));
		return instances;
	}

	@Override
	public void instanceCompleted(ProcessInstanceDAO instance) {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<ProcessInstanceDAO> getActiveInstances() {
		Collection instances = (Collection) InfinispanDatabase.query(InfinispanProcessInstanceDAO.class, //
				new Term("processId", getId()), //
				new Term("state", String.valueOf(ProcessState.STATE_ACTIVE)));
		return instances;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private Collection<ProcessInstanceDAO> getAllInstances() {
		Collection instances = (Collection) InfinispanDatabase.query(InfinispanProcessInstanceDAO.class, //
				new Term("processId", getId()));
		return instances;
	}

	@Override
	public int getNumInstances() {
		int num = InfinispanDatabase.count(InfinispanProcessInstanceDAO.class, //
				new Term("processId", getId()));
		return num;
	}

	@Override
	public CorrelatorDAO addCorrelator(String correlatorKey) {
		InfinispanCorrelatorDAO correlator = new InfinispanCorrelatorDAO(correlatorKey, this);
		InfinispanDatabase.addCacheEntry(correlator);
		return correlator;
	}

	@Override
	public CorrelatorDAO getCorrelator(String correlatorId) {
		InfinispanCorrelatorDAO correlator = InfinispanDatabase.querySingleResult(InfinispanCorrelatorDAO.class, //
				new Term("correlatorId", correlatorId), //
				new Term("processId", getId()));
		return correlator;
	}

	@Override
	public void removeRoutes(String routeId, ProcessInstanceDAO target) {
		for (CorrelatorDAO c : getCorrelators()) {
			((InfinispanCorrelatorDAO) c).removeLocalRoutes(routeId, target);
		}
	}

	private List<InfinispanCorrelatorDAO> getCorrelators() {
		List<InfinispanCorrelatorDAO> correlators = InfinispanDatabase.query(InfinispanCorrelatorDAO.class, new Term("processId", getId()));
		return correlators;
	}

	@Override
	public void deleteProcessAndRoutes() {

		InfinispanDatabase.delete(InfinispanMessageRouteDAO.class, new Term("processId", getId()));

		InfinispanDatabase.delete(InfinispanCorrelatorDAO.class, new Term("processId", getId()));

		deleteInstances(Integer.MAX_VALUE);

		InfinispanDatabase.removeCacheEntry(this);
	}

	private int deleteInstances(int transactionSize) {
		deleteEvents();
		deleteCorrelations();
		deleteMessages();
		deleteVariables();
		deleteProcessInstances();

		return 0;
	}

	private void deleteEvents() {
		InfinispanDatabase.delete(InfinispanEventDAO.class, new Term("processId", getId()));
	}

	private void deleteCorrelations() {

		List<InfinispanScopeDAO> scopes = InfinispanDatabase.query(InfinispanScopeDAO.class, new Term("processId", getId()));

		for (InfinispanScopeDAO scope : scopes) {
			scope.deleteCorrelationSets();
		}
	}

	private void deleteMessages() {
		InfinispanDatabase.delete(InfinispanMessageExchangeDAO.class, new Term("processId", getId()));
	}

	private void deleteVariables() {
		InfinispanDatabase.delete(InfinispanScopeDAO.class, new Term("processId", getId()));
	}

	private void deleteProcessInstances() {
		InfinispanDatabase.delete(InfinispanProcessInstanceDAO.class, new Term("processId", getId()));
	}

}
