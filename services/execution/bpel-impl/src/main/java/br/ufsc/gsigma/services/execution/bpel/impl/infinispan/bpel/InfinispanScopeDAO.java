package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.bridge.builtin.LongBridge;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

@Indexed
public class InfinispanScopeDAO implements ScopeDAO, InfinispanObject<Long> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(InfinispanScopeDAO.class);

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long scopeInstanceId;

	private int modelId;

	@Field(analyze = Analyze.NO)
	private String name;

	private String scopeState;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long processInstanceId;

	@Field(analyze = Analyze.NO)
	private String processId;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long parentScopeId;

	private Map<String, InfinispanXmlDataDAO> variables = new HashMap<String, InfinispanXmlDataDAO>();

	@IndexedEmbedded(prefix = "correlation")
	private Map<String, InfinispanCorrelationSetDAO> correlations = new HashMap<String, InfinispanCorrelationSetDAO>();

	private Map<Integer, InfinispanPartnerLinkDAO> eprs = new HashMap<Integer, InfinispanPartnerLinkDAO>();

	public InfinispanScopeDAO() {
	}

	public InfinispanScopeDAO(InfinispanScopeDAO parentScope, String name, int scopeModelId, InfinispanProcessInstanceDAO pi) {
		this.scopeInstanceId = InfinispanIdGen.newScopeId();
		this.parentScopeId = parentScope != null ? parentScope.getId() : null;
		this.name = name;
		this.modelId = scopeModelId;
		this.processInstanceId = pi.getId();
		this.processId = ((InfinispanProcessDAO) pi.getProcess()).getId();
	}

	@Override
	public Long getId() {
		return scopeInstanceId;
	}

	@Override
	public List<BpelEvent> listEvents() {

		List<InfinispanEventDAO> events = InfinispanDatabase.query(InfinispanEventDAO.class, new Term("scopeId", String.valueOf(getId())));

		List<BpelEvent> result = new ArrayList<BpelEvent>(events.size());

		for (InfinispanEventDAO eventDao : events) {
			result.add(eventDao.getEvent());
		}

		return result;
	}

	@Override
	public PartnerLinkDAO createPartnerLink(int plinkModelId, String pLinkName, String myRole, String partnerRole) {

		InfinispanPartnerLinkDAO pl = new InfinispanPartnerLinkDAO(plinkModelId, pLinkName, myRole, partnerRole);
		pl.setScope(this);

		if (logger.isDebugEnabled()) {
			logger.debug("createPartnerLink --> " + pl);
		}

		eprs.put(plinkModelId, pl);

		InfinispanDatabase.updateCacheEntry(this);

		return pl;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection<ScopeDAO> getChildScopes() {
		Collection scopes = (Collection) InfinispanDatabase.query(InfinispanScopeDAO.class, new Term("parentScopeId", String.valueOf(getId())));
		return scopes;
	}

	@Override
	public int getModelId() {
		return modelId;
	}

	@Override
	public String getName() {
		return name;
	}

	public XmlDataDAO getVariable(String varName) {

		InfinispanXmlDataDAO ret = null;

		ret = variables.get(varName);

		if (ret == null) {
			ret = new InfinispanXmlDataDAO(this, varName);
			variables.put(varName, ret);
			InfinispanDatabase.addCacheEntry(this);
		}

		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<XmlDataDAO> getVariables() {
		return (Collection) variables.values();
	}

	void updateVariable(InfinispanXmlDataDAO value) {
		variables.put(value.getName(), value);
	}

	@Override
	public PartnerLinkDAO getPartnerLink(int plinkModelId) {

		InfinispanPartnerLinkDAO partnerLink = eprs.get(plinkModelId);

		if (partnerLink == null) {
			logger.warn("getPartnerLink --> partnerLink is NULL for scopeId=" + getId() + ", plinkModelId=" + plinkModelId);
		}

		return partnerLink;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<PartnerLinkDAO> getPartnerLinks() {
		return (Collection) eprs.values();
	}

	@Override
	public ScopeDAO getParentScope() {
		return InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, parentScopeId);
	}

	@Override
	public ProcessInstanceDAO getProcessInstance() {
		InfinispanProcessInstanceDAO instance = InfinispanDatabase.getCacheEntry(InfinispanProcessInstanceDAO.class, processInstanceId);
		return instance;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<CorrelationSetDAO> getCorrelationSets() {
		return (Collection) correlations.values();
	}

	public void deleteCorrelationSets() {
		correlations.clear();
		InfinispanDatabase.updateCacheEntry(this);
	}

	public CorrelationSetDAO getCorrelationSet(String corrSetName) {

		InfinispanCorrelationSetDAO ret = correlations.get(corrSetName);

		if (ret == null) {
			ret = new InfinispanCorrelationSetDAO(this, corrSetName);
			correlations.put(corrSetName, ret);
		}

		return ret;
	}

	@Override
	public Long getScopeInstanceId() {
		return scopeInstanceId;
	}

	@Override
	public ScopeStateEnum getState() {
		return ScopeStateEnum.valueOf(scopeState);
	}

	@Override
	public void setState(ScopeStateEnum state) {
		this.scopeState = state.toString();
		InfinispanDatabase.updateCacheEntry(this);
	}
}
