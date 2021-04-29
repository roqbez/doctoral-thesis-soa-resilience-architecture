package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.lucene.index.Term;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.dao.jpa.ActivityRecoveryDAOImpl;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.hibernate.search.bridge.builtin.ShortBridge;
import org.w3c.dom.Element;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

@Indexed
public class InfinispanProcessInstanceDAO implements ProcessInstanceDAO, InfinispanObject<Long> {

	private static final long serialVersionUID = 1L;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long instanceId;

	private Date lastRecovery;

	private Date lastActiveTime;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = ShortBridge.class))
	private short state;

	private short previousState;

	private byte[] executionState;

	private long sequence;

	private Date createTime = new Date();

	@Field(analyze = Analyze.NO)
	private String instantiatingCorrelatorId;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long rootScopeId;

	@Field(analyze = Analyze.NO)
	private String processId;

	private InfinispanFaultDAO fault;

	private List<InfinispanActivityRecoveryDAO> recoveries = new ArrayList<InfinispanActivityRecoveryDAO>();

	private ExecutionContext executionContext;

	//
	private transient int activityFailureCount = -1;

	public InfinispanProcessInstanceDAO(InfinispanCorrelatorDAO correlator, InfinispanProcessDAO process) {
		this.instanceId = InfinispanIdGen.newProcessInstanceId();
		this.instantiatingCorrelatorId = correlator.getId();
		this.processId = process.getId();
	}

	@Override
	public BpelDAOConnection getConnection() {
		return InfinispanBpelDAOConnection.INSTANCE;
	}

	@Override
	public Long getId() {
		return instanceId;
	}

	@Override
	public ProcessDAO getProcess() {
		return InfinispanDatabase.getCacheEntry(InfinispanProcessDAO.class, processId);
	}

	@Override
	public XmlDataDAO[] getVariables(String variableName, int scopeModelId) {

		List<XmlDataDAO> results = new ArrayList<XmlDataDAO>();

		for (ScopeDAO sElement : getScopes()) {
			if (sElement.getModelId() == scopeModelId) {
				XmlDataDAO var = sElement.getVariable(variableName);
				if (var != null)
					results.add(var);
			}
		}
		return results.toArray(new XmlDataDAO[results.size()]);
	}

	void removeRoutes(String routeGroupId) {
		getProcess().removeRoutes(routeGroupId, this);
	}

	public Date getCreateTime() {
		return createTime;
	}

	@Override
	public Long getInstanceId() {
		return instanceId;
	}

	@Override
	public Date getActivityFailureDateTime() {
		return lastRecovery;
	}

	@Override
	public Date getLastActiveTime() {
		return lastActiveTime;
	}

	@Override
	public void setLastActiveTime(Date lastActive) {
		this.lastActiveTime = lastActive;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public short getPreviousState() {
		return previousState;
	}

	@Override
	public short getState() {
		return state;
	}

	@Override
	public void setState(short state) {
		this.previousState = state;
		this.state = state;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public EventsFirstLastCountTuple getEventsFirstLastCount() {
		return null;
	}

	@Override
	public byte[] getExecutionState() {
		return executionState;
	}

	@Override
	public void setExecutionState(byte[] execState) {
		this.executionState = execState;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public long genMonotonic() {
		return sequence++;
	}

	@Override
	public void finishCompletion() {
		assert (ProcessState.isFinished(this.getState()));
	}

	@Override
	public Collection<String> getMessageExchangeIds() {

		List<InfinispanMessageExchangeDAO> messageExchanges = InfinispanDatabase.query(InfinispanMessageExchangeDAO.class, //
				new Term("processInstanceId", String.valueOf(getId())));

		Collection<String> c = new HashSet<String>();

		for (MessageExchangeDAO m : messageExchanges) {
			c.add(m.getMessageExchangeId());
		}

		return c;
	}

	@Override
	public FaultDAO getFault() {
		return fault;
	}

	@Override
	public void setFault(FaultDAO fault) {
		this.fault = (InfinispanFaultDAO) fault;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public void setFault(QName faultName, String explanation, int faultLineNo, int activityId, Element faultMessage) {
		this.fault = new InfinispanFaultDAO(faultName, explanation, faultLineNo, activityId, faultMessage);
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public CorrelatorDAO getInstantiatingCorrelator() {
		return InfinispanDatabase.getCacheEntry(InfinispanCorrelatorDAO.class, instantiatingCorrelatorId);
	}

	@Override
	public int getActivityFailureCount() {
		if (activityFailureCount == -1) {
			activityFailureCount = recoveries.size();
		}
		return activityFailureCount;
	}

	@Override
	public void createActivityRecovery(String channel, long activityId, String reason, Date dateTime, Element data, String[] actions, int retries) {
		InfinispanActivityRecoveryDAO ar = new InfinispanActivityRecoveryDAO(channel, activityId, reason, dateTime, data, actions, retries);
		recoveries.add(ar);
		lastRecovery = dateTime;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<ActivityRecoveryDAO> getActivityRecoveries() {
		return (Collection) recoveries;
	}

	@Override
	public void deleteActivityRecovery(String channel) {
		ActivityRecoveryDAOImpl toRemove = null;
		for (ActivityRecoveryDAO _recovery : recoveries) {
			ActivityRecoveryDAOImpl arElement = (ActivityRecoveryDAOImpl) _recovery;
			if (arElement.getChannel().equals(channel)) {
				toRemove = arElement;
				break;
			}
		}
		if (toRemove != null) {
			recoveries.remove(toRemove);
			InfinispanDatabase.updateCacheEntry(this);
		}
	}

	@Override
	public Set<CorrelationSetDAO> getCorrelationSets() {
		return new HashSet<CorrelationSetDAO>();
	}

	@Override
	public CorrelationSetDAO getCorrelationSet(String name) {
		throw new UnsupportedOperationException();
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public void updateExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public void insertBpelEvent(ProcessInstanceEvent event) {
		InfinispanBpelDAOConnection.s_insertBpelEvent(event, getProcess(), this);
	}

	@Override
	public ScopeDAO getRootScope() {
		return InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, rootScopeId);
	}

	@Override
	public ScopeDAO getScope(Long scopeInstanceId) {
		InfinispanScopeDAO scope = InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, scopeInstanceId);
		return scope;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection<ScopeDAO> getScopes() {
		Collection scopes = (Collection) InfinispanDatabase.query(InfinispanScopeDAO.class, //
				new Term("processInstanceId", String.valueOf(getId())));
		return scopes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<ScopeDAO> getScopes(String scopeName) {
		Collection scopes = (Collection) InfinispanDatabase.query(InfinispanScopeDAO.class, //
				new Term("processInstanceId", String.valueOf(getId())), //
				new Term("name", scopeName));
		return scopes;
	}

	@Override
	public ScopeDAO createScope(ScopeDAO parentScope, String name, int scopeModelId) {

		InfinispanScopeDAO scope = new InfinispanScopeDAO((InfinispanScopeDAO) parentScope, name, scopeModelId, this);
		scope.setState(ScopeStateEnum.ACTIVE);

		this.rootScopeId = (parentScope == null) ? scope.getId() : rootScopeId;

		InfinispanDatabase.addCacheEntry(scope);

		return scope;
	}

	@Override
	public void delete(Set<CLEANUP_CATEGORY> cleanupCategories) {
		delete(cleanupCategories, true);
	}

	@Override
	public void delete(Set<CLEANUP_CATEGORY> cleanupCategories, boolean deleteMyRoleMex) {

		setExecutionState(null);

		if (cleanupCategories.contains(CLEANUP_CATEGORY.EVENTS)) {
			deleteEvents();
		}

		if (cleanupCategories.contains(CLEANUP_CATEGORY.CORRELATIONS)) {
			deleteCorrelations();
		}

		if (cleanupCategories.contains(CLEANUP_CATEGORY.MESSAGES)) {
			deleteMessageRoutes();
		}

		if (cleanupCategories.contains(CLEANUP_CATEGORY.VARIABLES)) {
			deleteVariables();
		}

		if (cleanupCategories.contains(CLEANUP_CATEGORY.INSTANCE)) {
			deleteInstance();
		}
	}

	private void deleteEvents() {
		InfinispanDatabase.delete(InfinispanEventDAO.class, new Term("processInstanceId", String.valueOf(getId())));
	}

	private void deleteCorrelations() {

		List<InfinispanScopeDAO> scopes = InfinispanDatabase.query(InfinispanScopeDAO.class, new Term("processInstanceId", String.valueOf(getId())));

		for (InfinispanScopeDAO scope : scopes) {
			scope.deleteCorrelationSets();
		}
	}

	private void deleteMessageRoutes() {
		InfinispanDatabase.delete(InfinispanMessageRouteDAO.class, new Term("processInstanceId", String.valueOf(getId())));
	}

	private void deleteVariables() {
		InfinispanDatabase.delete(InfinispanScopeDAO.class, new Term("processInstanceId", String.valueOf(getId())));
	}

	private void deleteInstance() {
		InfinispanDatabase.removeCacheEntry(this);
	}

}
