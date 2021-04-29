package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.sql.Timestamp;

import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.utils.uuid.UUID;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.bridge.builtin.LongBridge;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

@Indexed
public class InfinispanEventDAO implements InfinispanObject<String> {

	private static final long serialVersionUID = 1L;

	private String id;

	private Timestamp tstamp;

	private String type;

	private String detail;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long scopeId;

	@Field(analyze = Analyze.NO)
	private String processId;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long processInstanceId;

	private BpelEvent event;

	public InfinispanEventDAO() {
		this.id = new UUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Timestamp getTstamp() {
		return tstamp;
	}

	public void setTstamp(Timestamp tstamp) {
		this.tstamp = tstamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public BpelEvent getEvent() {
		return event;
	}

	public void setEvent(BpelEvent event) {
		this.event = event;
	}

	public void setProcess(InfinispanProcessDAO process) {
		this.processId = process.getId();
	}

	public void setInstance(InfinispanProcessInstanceDAO instance) {
		this.processInstanceId = instance.getId();
	}

	public void setScopeId(Long scopeId) {
		this.scopeId = scopeId;
	}

}
