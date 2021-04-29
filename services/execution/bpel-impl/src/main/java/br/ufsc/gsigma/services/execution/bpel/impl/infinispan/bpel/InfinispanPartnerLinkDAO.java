package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

public class InfinispanPartnerLinkDAO implements PartnerLinkDAO, Serializable {

	private static final long serialVersionUID = 1L;

	private String myEPR;

	private transient Element myEPRElement;

	private String myRoleName;

	private String myRoleServiceName;

	private String mySessionId;

	private String partnerEPR;

	private transient Element partnerEPRElement;

	private int partnerLinkModelId;

	private String partnerLinkName;

	private String partnerRoleName;

	private String partnerSessionId;

	private Long scopeId;

	private String processId;

	private Long processInstanceId;

	public InfinispanPartnerLinkDAO(int modelId, String name, String myRole, String partnerRole) {
		this.partnerLinkModelId = modelId;
		this.partnerLinkName = name;
		this.myRoleName = myRole;
		this.partnerRoleName = partnerRole;
	}

	@Override
	public Element getMyEPR() {
		if (myEPRElement == null && myEPR != null && !"".equals(myEPR)) {
			try {
				myEPRElement = DOMUtils.stringToDOM(myEPR);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return myEPRElement;
	}

	@Override
	public String getMyRoleName() {
		return myRoleName;
	}

	@Override
	public QName getMyRoleServiceName() {
		return myRoleServiceName == null ? null : QName.valueOf(myRoleServiceName);
	}

	@Override
	public String getMySessionId() {
		return mySessionId;
	}

	@Override
	public Element getPartnerEPR() {
		if (partnerEPRElement == null && partnerEPR != null && !"".equals(partnerEPR)) {
			try {
				partnerEPRElement = DOMUtils.stringToDOM(partnerEPR);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return partnerEPRElement;
	}

	@Override
	public int getPartnerLinkModelId() {
		return partnerLinkModelId;
	}

	@Override
	public String getPartnerLinkName() {
		return partnerLinkName;
	}

	@Override
	public String getPartnerRoleName() {
		return partnerRoleName;
	}

	@Override
	public String getPartnerSessionId() {
		return partnerSessionId;
	}

	@Override
	public void setMyEPR(Element val) {
		this.myEPRElement = val;
		this.myEPR = DOMUtils.domToString(val);
	}

	@Override
	public void setMyRoleServiceName(QName svcName) {
		this.myRoleServiceName = svcName.toString();

	}

	@Override
	public void setMySessionId(String sessionId) {
		this.mySessionId = sessionId;

	}

	@Override
	public void setPartnerEPR(Element val) {
		this.partnerEPRElement = val;
		this.partnerEPR = DOMUtils.domToString(val);

	}

	@Override
	public void setPartnerSessionId(String session) {
		this.partnerSessionId = session;
	}

	void setScope(InfinispanScopeDAO scope) {
		this.scopeId = scope.getId();
		this.processInstanceId = ((InfinispanProcessInstanceDAO) scope.getProcessInstance()).getId();
		this.processId = ((InfinispanProcessDAO) scope.getProcessInstance().getProcess()).getId();
	}

	@Override
	public String toString() {
		return "[processInstanceId=" + processInstanceId + ", processId=" + processId + ", partnerLinkModelId=" + partnerLinkModelId + ", partnerLinkName=" + partnerLinkName + ", partnerRoleName=" + partnerRoleName + ", partnerSessionId="
				+ partnerSessionId + ", scopeId=" + scopeId + ", myEPR=" + myEPR + ", myRoleName=" + myRoleName + ", myRoleServiceName=" + myRoleServiceName + ", mySessionId=" + mySessionId + ", partnerEPR=" + partnerEPR + "]";
	}

}
