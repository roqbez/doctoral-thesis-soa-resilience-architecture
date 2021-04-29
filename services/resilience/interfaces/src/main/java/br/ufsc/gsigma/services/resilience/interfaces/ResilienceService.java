package br.ufsc.gsigma.services.resilience.interfaces;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import br.ufsc.gsigma.infrastructure.ws.model.NodeInfo;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.model.ServicesCheckResult;

@WebService
public interface ResilienceService {

	public ServicesCheckResult checkServicesAvailable(@WebParam(name = "service") List<ServiceEndpointInfo> services);

	public ServicesCheckResult checkServicesEndpointsAvailable(@WebParam(name = "serviceEndpointURL") List<String> serviceEndpointURLs, @WebParam(name = "serviceNamespace") String serviceNamespace);

	@WebResult(name = "ip")
	public String getPublicIp();

	public NodeInfo getNodeInfo();

}
