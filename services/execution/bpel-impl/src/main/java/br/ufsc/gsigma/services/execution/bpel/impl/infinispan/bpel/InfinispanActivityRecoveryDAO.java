package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.io.Serializable;
import java.util.Date;

import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

public class InfinispanActivityRecoveryDAO implements ActivityRecoveryDAO, Serializable {

	private static final long serialVersionUID = 1L;

	private long activityId;

	private String channel;

	private String reason;

	private Date dateTime;

	private String details;

	private String actions;

	private int retries;

	public InfinispanActivityRecoveryDAO() {
	}

	public InfinispanActivityRecoveryDAO(String channel, long activityId, String reason, Date dateTime, Element data, String[] actions, int retries) {
		this.channel = channel;
		this.activityId = activityId;
		this.reason = reason;
		this.dateTime = dateTime;

		if (data != null)
			details = DOMUtils.domToString(data);

		String alist = actions[0];

		for (int i = 1; i < actions.length; ++i) {
			alist += " " + actions[i];
		}

		this.actions = alist;

		this.retries = retries;
	}

	public String getActions() {
		return actions;
	}

	public String[] getActionsList() {
		return getActions().split(" ");
	}

	public long getActivityId() {
		return activityId;
	}

	public String getChannel() {
		return channel;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public Element getDetails() {
		Element ret = null;
		if (details != null) {
			try {
				ret = DOMUtils.stringToDOM(details);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return ret;
	}

	public String getReason() {
		return reason;
	}

	public int getRetries() {
		return retries;
	}

}
