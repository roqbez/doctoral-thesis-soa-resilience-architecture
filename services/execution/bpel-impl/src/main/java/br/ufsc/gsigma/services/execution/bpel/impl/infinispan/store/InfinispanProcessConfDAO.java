package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

public class InfinispanProcessConfDAO implements ProcessConfDAO, Serializable {

	private static final long serialVersionUID = 1L;

	private String processId;

	private InfinispanDeploymentUnitDAO deploymentUnit;

	private Map<String, String> properties = new HashMap<String, String>();

	private String type;

	private long version;

	private String state;

	@Override
	public void delete() {
		deploymentUnit.getProcesses().remove(this);
		InfinispanDatabase.updateCacheEntry(deploymentUnit);
	}

	public QName getType() {
		return QName.valueOf(type);
	}

	public void setType(QName type) {
		this.type = type.toString();
	}

	public QName getPID() {
		return QName.valueOf(processId);
	}

	public void setPID(QName pid) {
		processId = pid.toString();
	}

	@Override
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public DeploymentUnitDAO getDeploymentUnit() {
		return deploymentUnit;
	}

	public void setDeploymentUnit(InfinispanDeploymentUnitDAO deploymentUnit) {
		this.deploymentUnit = deploymentUnit;
	}

	@Override
	public ProcessState getState() {
		return ProcessState.valueOf(state);
	}

	@Override
	public void setState(ProcessState state) {
		this.state = state.toString();
	}

	@Override
	public String getProperty(QName name) {
		return properties.get(name.toString());
	}

	@Override
	public void setProperty(QName name, String content) {
		properties.put(name.toString(), content);
	}

	@Override
	public Collection<QName> getPropertyNames() {
		return CollectionsX.transform(new ArrayList<QName>(), properties.keySet(), new UnaryFunction<String, QName>() {
			public QName apply(String x) {
				return QName.valueOf(x);
			}

		});
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processId == null) ? 0 : processId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InfinispanProcessConfDAO other = (InfinispanProcessConfDAO) obj;
		if (processId == null) {
			if (other.processId != null)
				return false;
		} else if (!processId.equals(other.processId))
			return false;
		return true;
	}

}
