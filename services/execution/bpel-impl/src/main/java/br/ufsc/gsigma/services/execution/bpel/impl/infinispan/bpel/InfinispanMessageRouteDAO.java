package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.utils.uuid.UUID;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.bridge.builtin.LongBridge;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

@Indexed
public class InfinispanMessageRouteDAO implements MessageRouteDAO, InfinispanObject<String> {

	private static final long serialVersionUID = 1L;

	private String id;

	@Field(analyze = Analyze.NO)
	private String correlatorId;

	@Field(analyze = Analyze.NO)
	private String correlatorKey;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long processInstanceId;

	@Field(analyze = Analyze.NO)
	private String processId;

	@Field(analyze = Analyze.NO)
	private String processType;

	private String groupId;

	private int index;

	@Field(analyze = Analyze.NO)
	private String correlationKey;

	private String routePolicy;

	public InfinispanMessageRouteDAO(CorrelationKeySet keySet, String groupId, int index, InfinispanProcessInstanceDAO processInst, InfinispanCorrelatorDAO correlator, String routePolicy) {
		this.id = new UUID().toString();
		this.correlationKey = keySet.toCanonicalString();
		this.groupId = groupId;
		this.index = index;
		this.processInstanceId = processInst.getId();
		this.processId = ((InfinispanProcessDAO) processInst.getProcess()).getId();
		this.processType = processInst.getProcess().getType().toString();
		this.correlatorId = correlator.getId();
		this.correlatorKey = correlator.getCorrelatorKey();
		this.routePolicy = routePolicy;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getCorrelatorId() {
		return correlatorId;
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public String getRoute() {
		return routePolicy;
	}

	@Override
	public CorrelationKey getCorrelationKey() {
		return new CorrelationKey(correlationKey);
	}

	@Override
	public void setCorrelationKey(CorrelationKey key) {
		correlationKey = key.toCanonicalString();
	}

	@Override
	public CorrelationKeySet getCorrelationKeySet() {
		return new CorrelationKeySet(correlationKey);
	}

	@Override
	public void setCorrelationKeySet(CorrelationKeySet keySet) {
		correlationKey = keySet.toCanonicalString();
	}

	@Override
	public ProcessInstanceDAO getTargetInstance() {
		return InfinispanDatabase.getCacheEntry(InfinispanProcessInstanceDAO.class, processInstanceId);
	}

	@Override
	public String toString() {
		return "[correlationKey=" + correlationKey + ", correlatorId=" + correlatorId + ", correlatorKey=" + correlatorKey + ", processInstanceId=" + processInstanceId + ", processId=" + processId + ", processType=" + processType + ", groupId="
				+ groupId + ", index=" + index + ", routePolicy=" + routePolicy + ", id=" + id + "]";
	}

}
