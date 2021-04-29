package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

public class InfinispanFaultDAO implements FaultDAO, Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String explanation;

	private String data;

	private int lineNo;

	private int activityId;

	public InfinispanFaultDAO(QName faultName, String explanation, int faultLineNo, int activityId, Element faultMessage) {
		this.name = faultName.toString();
		this.explanation = explanation;
		this.lineNo = faultLineNo;
		this.activityId = activityId;
		this.data = (faultMessage == null) ? null : DOMUtils.domToString(faultMessage);
	}

	@Override
	public int getActivityId() {
		return activityId;
	}

	@Override
	public Element getData() {
		Element ret = null;

		try {
			ret = (data == null) ? null : DOMUtils.stringToDOM(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	@Override
	public String getExplanation() {
		return explanation;
	}

	@Override
	public int getLineNo() {
		return lineNo;
	}

	@Override
	public QName getName() {
		return name == null ? null : QName.valueOf(name);
	}

}
