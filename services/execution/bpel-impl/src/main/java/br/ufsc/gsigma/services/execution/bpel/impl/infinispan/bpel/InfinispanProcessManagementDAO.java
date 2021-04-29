package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessManagementDAO;

public class InfinispanProcessManagementDAO implements ProcessManagementDAO {

	Date _createTime = new Date();

	public Date getCreateTime() {
		return _createTime;
	}

	public Object[] findFailedCountAndLastFailedDateForProcessId(InfinispanBpelDAOConnection conn, String status, String processId) {
		Date lastFailureDt = null;
		int failureInstances = 0;

		InstanceFilter instanceFilter = new InstanceFilter("status=" + status + " pid=" + processId);
		for (ProcessInstanceDAO instance : conn.instanceQuery(instanceFilter)) {
			int count = instance.getActivityFailureCount();
			if (count > 0) {
				++failureInstances;
				Date failureDt = instance.getActivityFailureDateTime();
				if (lastFailureDt == null || lastFailureDt.before(failureDt))
					lastFailureDt = failureDt;
			}
		}

		return new Object[] { failureInstances, lastFailureDt };
	}

	public void prefetchActivityFailureCounts(Collection<ProcessInstanceDAO> instances) {
		// do nothing
	}

	public int countInstancesByPidAndString(BpelDAOConnection conn, QName pid, String status) {
		InstanceFilter instanceFilter = new InstanceFilter("status=" + status + " pid=" + pid);

		// TODO: this is grossly inefficient
		return conn.instanceQuery(instanceFilter).size();
	}

	public Map<InstanceSummaryKey, Long> countInstancesSummary(Set<String> pids) {
		return new HashMap<InstanceSummaryKey, Long>();
	}

	public Map<String, FailedSummaryValue> findFailedCountAndLastFailedDateForProcessIds(Set<String> pids) {
		return new HashMap<String, FailedSummaryValue>();
	}
}
