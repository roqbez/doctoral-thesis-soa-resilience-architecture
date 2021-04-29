package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

public class InfinispanCorrelationSetDAO implements CorrelationSetDAO, Serializable {

	private static final long serialVersionUID = 1L;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long correlationSetId;

	@Field(analyze = Analyze.NO)
	private String name;

	@Field(analyze = Analyze.NO)
	private String correlationKey;

	private Map<String, String> props = new HashMap<String, String>();

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long scopeId;

	private String processId;

	private Long processInstanceId;

	public InfinispanCorrelationSetDAO(InfinispanScopeDAO scope, String name) {
		this.correlationSetId = InfinispanIdGen.newCorrelationSetId();
		this.name = name;
		this.scopeId = scope.getId();
		this.processInstanceId = ((InfinispanProcessInstanceDAO) scope.getProcessInstance()).getId();
		this.processId = ((InfinispanProcessDAO) scope.getProcessInstance().getProcess()).getId();
	}

	@Override
	public Long getCorrelationSetId() {
		return correlationSetId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Map<QName, String> getProperties() {
		HashMap<QName, String> map = new HashMap<QName, String>();
		for (Entry<String, String> prop : props.entrySet()) {
			map.put(QName.valueOf(prop.getKey()), prop.getValue());
		}
		return map;
	}

	@Override
	public ScopeDAO getScope() {
		return InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, scopeId);
	}

	@Override
	public CorrelationKey getValue() {

		if (this.correlationKey == null)
			return null;

		return new CorrelationKey(this.correlationKey);
	}

	@Override
	public void setValue(QName[] names, CorrelationKey values) {

		this.correlationKey = values.toCanonicalString();

		if (names != null) {
			for (int m = 0; m < names.length; m++) {
				props.put(names[m].toString(), values.getValues()[m]);
			}
		}

		InfinispanDatabase.updateCacheEntry((InfinispanScopeDAO) getScope());
	}

	@Override
	public ProcessDAO getProcess() {
		return InfinispanDatabase.getCacheEntry(InfinispanProcessDAO.class, processId);
	}

	@Override
	public ProcessInstanceDAO getInstance() {
		return InfinispanDatabase.getCacheEntry(InfinispanProcessInstanceDAO.class, processInstanceId);
	}

}
