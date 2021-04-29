package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.CorrelatorMessageDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.uuid.UUID;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.w3c.dom.Element;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanMessageDAO.MessageRole;

@Indexed
public class InfinispanMessageExchangeDAO implements MessageExchangeDAO, CorrelatorMessageDAO, InfinispanObject<String> {

	private static final Logger logger = Logger.getLogger(InfinispanMessageExchangeDAO.class);

	private static final long serialVersionUID = 1L;

	private String id;

	private String callee;

	private String channel;

	private String correlationId;

	private String correlationStatus;

	private Date createTime;

	private char direction;

	private String epr;

	private transient Element eprElement;

	private String fault;

	private String faultExplanation;

	private String operation;

	private int partnerLinkModelId;

	private String pattern;

	private String portType;

	private boolean propagateTransactionFlag;

	private String status;

	private String pipedMessageExchangeId;

	private int subscriberCount;

	private Map<String, String> props = new HashMap<String, String>();

	@Field(analyze = Analyze.NO)
	private String correlationKeys;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long processInstanceId;

	@Field(analyze = Analyze.NO)
	private String correlatorId;

	@Field(analyze = Analyze.NO)
	private String processId;

	private InfinispanPartnerLinkDAO partnerLink;

	private InfinispanMessageDAO requestMessage;

	private InfinispanMessageDAO responseMessage;

	public InfinispanMessageExchangeDAO(char direction) {
		this.id = new UUID().toString();
		this.direction = direction;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getMessageExchangeId() {
		return id;
	}

	@Override
	public char getDirection() {
		return direction;
	}

	public MessageDAO createMessage(QName type) {
		InfinispanMessageDAO msg = new InfinispanMessageDAO(type, this);
		return msg;
	}

	void setCorrelator(InfinispanCorrelatorDAO correlator) {
		this.correlatorId = correlator.getId();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public ProcessDAO getProcess() {
		return processId != null ? InfinispanDatabase.getCacheEntry(InfinispanProcessDAO.class, processId) : null;
	}

	@Override
	public void setProcess(ProcessDAO process) {
		this.processId = ((InfinispanProcessDAO) process).getId();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public ProcessInstanceDAO getInstance() {
		return processInstanceId != null ? InfinispanDatabase.getCacheEntry(InfinispanProcessInstanceDAO.class, processInstanceId) : null;
	}

	@Override
	public void setInstance(ProcessInstanceDAO dao) {
		this.processInstanceId = ((InfinispanProcessInstanceDAO) dao).getId();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public MessageDAO getRequest() {
		return requestMessage;
	}

	@Override
	public void setRequest(MessageDAO msg) {
		this.requestMessage = (InfinispanMessageDAO) msg;
		this.requestMessage.setRole(MessageRole.REQUEST);
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public MessageDAO getResponse() {
		return responseMessage;
	}

	@Override
	public void setResponse(MessageDAO msg) {
		this.responseMessage = (InfinispanMessageDAO) msg;
		this.requestMessage.setRole(MessageRole.RESPONSE);
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public PartnerLinkDAO getPartnerLink() {
		return partnerLink;
	}

	@Override
	public void setPartnerLink(PartnerLinkDAO plinkDAO) {
		this.partnerLink = (InfinispanPartnerLinkDAO) plinkDAO;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public String getProperty(String key) {
		return props.get(key);
	}

	@Override
	public void setProperty(String key, String value) {
		props.put(key, value);
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public Set<String> getPropertyNames() {
		return new HashSet<String>(props.keySet());
	}

	@Override
	public Date getCreateTime() {
		return createTime;
	}

	@Override
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public boolean getPropagateTransactionFlag() {
		return propagateTransactionFlag;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	@Override
	public void setChannel(String channel) {
		this.channel = channel;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public int getPartnerLinkModelId() {
		return partnerLinkModelId;
	}

	@Override
	public void setPartnerLinkModelId(int partnerLinkModelId) {
		this.partnerLinkModelId = partnerLinkModelId;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public String getCorrelationStatus() {
		return correlationStatus;
	}

	@Override
	public void setCorrelationStatus(String correlationStatus) {
		this.correlationStatus = correlationStatus;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public String getOperation() {
		return operation;
	}

	@Override
	public void setOperation(String operation) {
		this.operation = operation;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public String getCorrelationId() {
		return correlationId;
	}

	@Override
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public void setPattern(String pattern) {
		this.pattern = pattern;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public QName getCallee() {
		return callee == null ? null : QName.valueOf(callee);
	}

	@Override
	public QName getPortType() {
		return portType == null ? null : QName.valueOf(portType);
	}

	@Override
	public void setPortType(QName porttype) {
		this.portType = porttype.toString();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public void setCallee(QName callee) {
		this.callee = callee.toString();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public String getPipedMessageExchangeId() {
		return this.pipedMessageExchangeId;
	}

	@Override
	public void setPipedMessageExchangeId(String pipedMessageExchangeId) {
		this.pipedMessageExchangeId = pipedMessageExchangeId;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public CorrelationKey getCorrelationKey() {
		if (this.correlationKeys == null)
			return null;
		return getCorrelationKeySet().iterator().next();
	}

	CorrelationKeySet getCorrelationKeySet() {
		return new CorrelationKeySet(correlationKeys);
	}

	void setCorrelationKeySet(CorrelationKeySet correlationKeySet) {
		this.correlationKeys = correlationKeySet.toCanonicalString();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public void setCorrelationKey(CorrelationKey ckey) {
		this.correlationKeys = ckey.toCanonicalString();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public QName getFault() {
		return fault == null ? null : QName.valueOf(fault);
	}

	@Override
	public String getFaultExplanation() {
		return faultExplanation;
	}

	@Override
	public void setFault(QName faultType) {
		this.fault = faultType == null ? null : faultType.toString();
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public void setFaultExplanation(String explanation) {
		if (explanation != null && explanation.length() > 255)
			explanation = explanation.substring(0, 254);
		this.faultExplanation = explanation;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public Element getEPR() {
		if (eprElement == null && epr != null && !"".equals(epr)) {
			try {
				eprElement = DOMUtils.stringToDOM(epr);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return eprElement;
	}

	@Override
	public void setEPR(Element epr) {
		this.eprElement = epr;
		this.epr = DOMUtils.domToString(epr);
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public int getSubscriberCount() {
		return this.subscriberCount;
	}

	@Override
	public void setSubscriberCount(int subscriberCount) {
		this.subscriberCount = subscriberCount;
		InfinispanDatabase.updateCacheEntry(this);
	}

	@Override
	public void release(boolean doClean) {
		if (doClean) {
			deleteMessages();
		}
	}

	@Override
	public void releasePremieMessages() {
	}

	public void deleteMessages() {

		if (logger.isDebugEnabled()) {
			logger.debug("Removing mex " + id);
		}

		InfinispanDatabase.removeCacheEntry(this);
	}

	@Override
	public String toString() {
		return "[correlationKeys=" + correlationKeys + ", id=" + id + ", callee=" + callee + ", channel=" + channel + ", correlationId=" + correlationId + ", correlationStatus=" + correlationStatus + ", createTime=" + createTime + ", direction=" + direction + ", epr=" + epr + ", fault=" + fault
				+ ", faultExplanation=" + faultExplanation + ", operation=" + operation + ", partnerLinkModelId=" + partnerLinkModelId + ", pattern=" + pattern + ", portType=" + portType + ", propagateTransactionFlag=" + propagateTransactionFlag + ", status=" + status + ", pipedMessageExchangeId="
				+ pipedMessageExchangeId + ", subscriberCount=" + subscriberCount + ", props=" + props + ", processInstanceId=" + processInstanceId + ", correlatorId=" + correlatorId + ", processId=" + processId + ", partnerLink=" + partnerLink //
				//+ "\n\trequestMessage=" + requestMessage //
				//+ "\n\tresponseMessage=" + responseMessage //
				+ "\n]";
	}

}
