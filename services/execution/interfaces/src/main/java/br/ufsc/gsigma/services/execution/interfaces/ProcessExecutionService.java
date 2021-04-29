package br.ufsc.gsigma.services.execution.interfaces;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.model.NodeInfo;
import br.ufsc.gsigma.services.execution.dto.ProcessDeploymentInfo;
import br.ufsc.gsigma.services.execution.dto.ProcessExecutionInfo;
import br.ufsc.gsigma.services.execution.dto.ProcessInfo;

@WebService
public interface ProcessExecutionService {

	@WebResult(name = "processDeploymentInfo")
	public ProcessDeploymentInfo deployProcess(@WebParam(name = "processName") String processName, @WebParam(name = "executionContext") ExecutionContext executionContext, @WebParam(name = "processBinary") byte[] processBinary);

	@WebResult(name = "processExecutionInfo")
	public ProcessExecutionInfo executeProcess(@WebParam(name = "processName") String processName, @WebParam(name = "executionContext") ExecutionContext executionContext);

	@WebResult(name = "processInfo")
	public List<ProcessInfo> getAllProcesses();

	@WebResult(name = "result")
	public boolean undeployProcess(@WebParam(name = "deploymentPackage") String deploymentPackage);

	@WebResult(name = "result")
	public boolean undeployAllProcesses();

	@WebResult(name = "ip")
	public String getPublicIp();

	public NodeInfo getNodeInfo();

}
