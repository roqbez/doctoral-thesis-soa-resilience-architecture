package br.ufsc.gsigma.services.deployment.interfaces;

import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import br.ufsc.gsigma.catalog.services.model.DeploymentServer;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider;
import br.ufsc.gsigma.services.deployment.model.Deployment;
import br.ufsc.gsigma.services.deployment.model.DeploymentRequest;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedService;
import br.ufsc.gsigma.services.deployment.model.PlatformManagedServicesDeploymentRequest;
import br.ufsc.gsigma.services.deployment.model.ServiceContainer;
import br.ufsc.gsigma.services.deployment.model.ServicesDeployment;
import br.ufsc.gsigma.services.deployment.model.ServicesDeploymentRequest;

@WebService
public interface DeploymentService {

	public Deployment deployApplication(@WebParam(name = "request") DeploymentRequest request);

	public ServicesDeployment deployServices(@WebParam(name = "request") ServicesDeploymentRequest request);

	public List<ServiceContainer> getServiceContainersByDeploymentServer(@WebParam(name = "deploymentServer") String deploymentServer);

	public List<ServiceContainer> getServiceContainersByServicePath(@WebParam(name = "deploymentServer") String deploymentServer, @WebParam(name = "servicePath") String servicePath);

	public List<ServiceContainer> getServiceContainersByServicePaths(@WebParam(name = "deploymentServer") String deploymentServer, @WebParam(name = "servicePath") List<String> servicePaths);

	public List<ServiceContainer> removeServiceContainersByServicePath(@WebParam(name = "deploymentServer") String deploymentServer, @WebParam(name = "servicePath") String servicePath);

	public List<ServiceContainer> removeServiceContainers(@WebParam(name = "container") List<ServiceContainer> containers);

	public List<ServiceContainer> removeAllServiceContainers(@WebParam(name = "deploymentServer") List<String> deploymentServers);

	public List<ServiceContainer> removeServiceContainersByServersServicePaths(@WebParam(name = "deploymentServer") List<String> deploymentServers, @WebParam(name = "servicePath") List<String> servicePaths);

	public ServiceContainer getServiceContainerByName(@WebParam(name = "deploymentServer") String deploymentServer, @WebParam(name = "containerName") String containerName);

	public ServiceContainer removeServiceContainerByName(@WebParam(name = "deploymentServer") String deploymentServer, @WebParam(name = "containerName") String containerName);

	public List<ServiceContainer> removeRandomServiceContainers(@WebParam(name = "deploymentServer") String deploymentServer, @WebParam(name = "numberOfContainers") int numberOfContainers);

	@WebResult(name = "infrastructureProvider")
	public List<InfrastructureProvider> getInfrastructureProviders();

	@WebResult(name = "infrastructureProvider")
	public InfrastructureProvider getInfrastructureProvider(@WebParam(name = "id") String id);

	public boolean deploySupportServices(@WebParam(name = "dockerSwarmURL") String dockerSwarmURL, @WebParam(name = "bindingServiceReplicas") int bindingServiceReplicas, @WebParam(name = "executionServiceReplicas") int executionServiceReplicas,
			@WebParam(name = "resilienceServiceReplicas") int resilienceServiceReplicas) throws Exception;

	public List<ServiceContainer> getServiceContainersByLabelValues(@WebParam(name = "deploymentServer") List<String> deploymentServers, @WebParam(name = "labelName") String labelName, @WebParam(name = "labelValue") List<String> labelValues);

	// Swarm

	@WebResult(name = "platformManagedService")
	public List<PlatformManagedService> getPlatformManagedServices(@WebParam(name = "managerServer") String managerServer, @WebParam(name = "serviceName") List<String> serviceNames);

	@WebResult(name = "deploymentServer")
	public List<DeploymentServer> getPlatformManagedDeploymentServers(@WebParam(name = "managerServer") String managerServer);

	public ServicesDeployment deployPlatformManagedServices(@WebParam(name = "request") PlatformManagedServicesDeploymentRequest request);

	@WebResult(name = "platformManagedService")
	public List<PlatformManagedService> removePlatformManagedServices(@WebParam(name = "managerServer") String managerServer, @WebParam(name = "serviceName") List<String> serviceNames);

	@WebResult(name = "platformManagedService")
	public PlatformManagedService getPlatformManagedServiceById(@WebParam(name = "managerServer") String managerServer, @WebParam(name = "serviceId") String serviceId);

	@WebResult(name = "platformManagedService")
	public List<PlatformManagedService> getPlatformManagedServiceByIds(@WebParam(name = "managerServer") String managerServer, @WebParam(name = "serviceId") List<String> serviceIds);

	@WebResult(name = "image")
	public Map<String, String> pullImage(@WebParam(name = "deploymentServer") List<String> deploymentServer, @WebParam(name = "image") String image);

}
