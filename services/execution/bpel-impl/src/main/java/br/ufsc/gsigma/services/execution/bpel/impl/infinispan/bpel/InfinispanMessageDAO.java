package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.utils.DOMUtils;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.w3c.dom.Element;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

public class InfinispanMessageDAO implements MessageDAO, Serializable {

	private static final long serialVersionUID = 1L;

	public enum MessageRole {
		REQUEST, RESPONSE
	}

	private String type;

	private String data;

	private String header;

	private String messageExchangeId;

	private MessageRole role;

	public InfinispanMessageDAO(QName type, InfinispanMessageExchangeDAO me) {
		this.type = (type != null) ? type.toString() : null;
		this.messageExchangeId = me.getId();
	}

	@Field(analyze = Analyze.NO)
	public String getProcessId() {
		InfinispanProcessDAO process = (InfinispanProcessDAO) getMessageExchange().getProcess();
		return process != null ? process.getId() : null;
	}

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	public Long getProcessInstanceId() {
		InfinispanProcessInstanceDAO instance = (InfinispanProcessInstanceDAO) getMessageExchange().getInstance();
		return instance != null ? instance.getId() : null;
	}

	public Element getData() {
		try {
			return this.data == null ? null : DOMUtils.stringToDOM(this.data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setData(Element value) {
		if (value == null) {
			return;
		}
		this.data = DOMUtils.domToString(value);

		updateMessageExchangeDAO();
	}

	public Element getHeader() {
		try {
			return this.header == null ? null : DOMUtils.stringToDOM(this.header);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setHeader(Element value) {
		if (value == null)
			return;
		this.header = DOMUtils.domToString(value);

		updateMessageExchangeDAO();

	}

	public QName getType() {
		return this.type == null ? null : QName.valueOf(this.type);
	}

	public void setType(QName type) {
		this.type = (type != null) ? type.toString() : null;

		updateMessageExchangeDAO();
	}

	@Override
	public MessageExchangeDAO getMessageExchange() {
		return InfinispanDatabase.getCacheEntry(InfinispanMessageExchangeDAO.class, messageExchangeId);
	}

	public MessageRole getRole() {
		return role;
	}

	public void setRole(MessageRole role) {
		this.role = role;
	}

	private void updateMessageExchangeDAO() {
		InfinispanMessageExchangeDAO messageExchange = (InfinispanMessageExchangeDAO) getMessageExchange();

		if (role == MessageRole.REQUEST) {
			messageExchange.setRequest(this);
			InfinispanDatabase.updateCacheEntry(messageExchange);
		} else if (role == MessageRole.RESPONSE) {
			messageExchange.setResponse(this);
			InfinispanDatabase.updateCacheEntry(messageExchange);
		}
	}

	@Override
	public String toString() {
		return "\n\t\tmessageExchangeId=" + messageExchangeId //
				+ "\n\t\ttype=" + type //
				+ (header != null ? "\n\t\theader=" + header.replaceAll("\n", "") : "")//
				+ (data != null ? "\n\t\tdata=" + data.replaceAll("\n", "") : "");
	}

}
