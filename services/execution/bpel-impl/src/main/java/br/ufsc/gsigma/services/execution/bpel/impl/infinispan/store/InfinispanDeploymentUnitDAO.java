package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import br.ufsc.gsigma.infrastructure.util.ZipUtils;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

public class InfinispanDeploymentUnitDAO implements DeploymentUnitDAO, InfinispanObject<String> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(InfinispanDeploymentUnitDAO.class);

	private String name;

	private String deployer;

	private Date deployDate;

	private String deploymentUnitDir;

	private byte[] deploymentUnitBinary;

	private Collection<InfinispanProcessConfDAO> processes = new HashSet<InfinispanProcessConfDAO>();

	private ExecutionContext executionContext;

	private String nodeId;

	public InfinispanDeploymentUnitDAO(String name) {
		this.name = name;
		this.nodeId = InfinispanDatabase.getInstance().getMyAddress().toString();
	}

	@Override
	public String getId() {
		return this.name;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public void delete() {
		InfinispanDatabase.removeCacheEntry(this);
	}

	@Override
	public ProcessConfDAO createProcess(QName pid, QName type, long version) {
		InfinispanProcessConfDAO p = new InfinispanProcessConfDAO();
		p.setPID(pid);
		p.setType(type);
		p.setDeploymentUnit(this);
		p.setState(ProcessState.ACTIVE);
		p.setVersion(version);

		processes.add(p);

		InfinispanDatabase.updateCacheEntry(this);

		return p;
	}

	@Override
	public ProcessConfDAO getProcess(QName pid) {
		return CollectionsX.find_if(processes, new MemberOfFunction<ProcessConfDAO>() {
			@Override
			public boolean isMember(ProcessConfDAO o) {
				return o.getPID().equals(pid);
			}

		});
	}

	@Override
	public Collection<InfinispanProcessConfDAO> getProcesses() {
		return processes;
	}

	public void setProcesses(Collection<InfinispanProcessConfDAO> processes) {
		this.processes = processes;
	}

	@Override
	public String getDeployer() {
		return deployer;
	}

	public void setDeployer(String deployer) {
		this.deployer = deployer;
	}

	@Override
	public Date getDeployDate() {
		return deployDate;
	}

	public void setDeployDate(Date deployDate) {
		this.deployDate = deployDate;
	}

	@Override
	public String getDeploymentUnitDir() {
		return deploymentUnitDir;
	}

	public void setDeploymentUnitDir(String deploymentUnitDir) {
		this.deploymentUnitDir = deploymentUnitDir;

		try {
			this.deploymentUnitBinary = ZipUtils.zip(new File(deploymentUnitDir));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public byte[] getDeploymentUnitBinary() {
		return deploymentUnitBinary;
	}

	public void setDeploymentUnitBinary(byte[] deploymentUnitBinary) {
		this.deploymentUnitBinary = deploymentUnitBinary;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
