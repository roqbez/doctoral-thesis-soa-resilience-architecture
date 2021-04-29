package br.ufsc.gsigma.binding.interfaces;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.binding.model.BindingConfigurationRequest;
import br.ufsc.gsigma.infrastructure.ws.model.NodeInfo;

@WebService
public interface BindingService {

	public BindingConfiguration registerBindingConfiguration(@WebParam(name = "bindingConfigurationRequest") BindingConfigurationRequest bindingConfigurationRequest);

	public String getServiceMediationEndpoint();

	@WebResult(name = "bindingConfiguration")
	public List<BindingConfiguration> getBindingConfigurations();

	public void clearBindingConfigurations();

	@WebResult(name = "bindingConfiguration")
	public BindingConfiguration unregisterBindingConfiguration(@WebParam(name = "bindingToken") String bindingToken);

	@WebResult(name = "ip")
	public String getPublicIp();

	public NodeInfo getNodeInfo();

}
