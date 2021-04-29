package br.ufsc.gsigma.services.execution.bpel.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.apache.log4j.Logger;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.Namespaces;

import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;
import br.ufsc.gsigma.infrastructure.ws.HostAddressUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.model.NodeInfo;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.ode.DeploymentPoller;
import br.ufsc.gsigma.services.execution.bpel.ode.ODEAxisServlet;
import br.ufsc.gsigma.services.execution.dto.ProcessDeploymentInfo;
import br.ufsc.gsigma.services.execution.dto.ProcessExecutionInfo;
import br.ufsc.gsigma.services.execution.dto.ProcessInfo;
import br.ufsc.gsigma.services.execution.interfaces.ProcessExecutionService;

public class ExecutionServiceImpl implements ProcessExecutionService {

	private static final Logger logger = Logger.getLogger(ExecutionServiceImpl.class);

	private ExecutionODEServer odeServer;

	private ConfigurationContext configContext;

	private OMFactory factory = OMAbstractFactory.getOMFactory();

	@SuppressWarnings("unused")
	private String serverRootPath;

	public ExecutionServiceImpl(String host, int port, String odeContext) {
		this.serverRootPath = "http://" + host + ":" + port + "/" + odeContext;
	}

	public void setOdeAxisServlet(ODEAxisServlet odeAxisService) {
		this.odeServer = ((ExecutionODEServer) odeAxisService.getODEServer());

		ConfigurationContext configContext = new ConfigurationContext(odeServer.getAxisConfiguration());
		configContext.setServicePath("services");
		configContext.setContextRoot("/");

		LocalTransportReceiver.CONFIG_CONTEXT = configContext;
	}

	@Override
	public String getPublicIp() {
		return HostAddressUtil.getPublicIp();
	}

	@Override
	public NodeInfo getNodeInfo() {
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		InfinispanDatabase infinispanDatabase = InfinispanDatabase.getInstance();
		return new NodeInfo(infinispanDatabase.getMyAddress().toString(), DockerContainerUtil.getContainerId(), getPublicIp(), hostName, InfinispanDatabase.getInstance().isLeader());
	}

	@Override
	public ProcessDeploymentInfo deployProcess(String processName, ExecutionContext executionContext, byte[] processData) {

		Lock lock = ((ExecutionBpelServer) odeServer.getBpelServer()).getManagementLock().writeLock();

		DeploymentPoller deploymentPoller = odeServer.getDeploymentPoller();

		try {

			lock.lock();

			deploymentPoller.hold();

			if (executionContext != null) {
				ExecutionContext.set(executionContext);
			}

			processName = processName.replaceAll(" ", "");

			OMNamespace pmapi = factory.createOMNamespace(Namespaces.ODE_PMAPI_NS, "pmapi");

			OMElement msg = factory.createOMElement("deploy", pmapi);

			OMElement namePart = factory.createOMElement(new QName(null, "name"));
			namePart.setText(processName);
			msg.addChild(namePart);

			OMElement zipPart = factory.createOMElement(new QName(null, "package"));
			OMElement zipElmt = factory.createOMElement(new QName(Namespaces.ODE_DEPLOYAPI_NS, "zip"));
			zipPart.addChild(zipElmt);

			String base64Enc = Base64.encode(processData);
			OMText zipContent = factory.createOMText(base64Enc, "application/zip", true);
			msg.addChild(zipPart);
			zipElmt.addChild(zipContent);

			OMElement responseElement = getDeploymentServiceClient().sendReceive(msg);

			ProcessDeploymentInfo result = new ProcessDeploymentInfo();

			String id = responseElement.getFirstElement().getFirstChildWithName(new QName(Namespaces.ODE_DEPLOYAPI_NS, "id")).getText();
			String name = responseElement.getFirstElement().getFirstChildWithName(new QName(Namespaces.ODE_DEPLOYAPI_NS, "name")).getText();

			if (name != null && name.indexOf('-') > 0)
				name = name.substring(0, name.indexOf('-'));

			result.setProcessId(id);
			result.setProcessName(name);

			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			ProcessDeploymentInfo result = new ProcessDeploymentInfo();
			result.setError(true);
			result.setErrorMessage(e.getMessage());
			return result;
		} finally {
			deploymentPoller.release();
			lock.unlock();
		}

	}

	@Override
	public boolean undeployProcess(String deploymentPackage) {
		try {

			return odeServer.getProcessStore().undeploy(deploymentPackage).size() > 0;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean undeployAllProcesses() {

		boolean result = true;

		try {

			OMNamespace pmapi = factory.createOMNamespace(Namespaces.ODE_PMAPI_NS, "pmapi");

			OMElement msg = factory.createOMElement("listDeployedPackages", pmapi);

			OMElement responseElement = getDeploymentServiceClient().sendReceive(msg);

			Iterator<OMElement> it = responseElement.getFirstElement().getChildrenWithLocalName("name");

			while (it.hasNext()) {

				String packageName = it.next().getText();
				try {
					result = undeployProcess(packageName) && result;
				} catch (Throwable e) {
				}
			}

			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProcessInfo> getAllProcesses() {

		try {

			List<ProcessInfo> result = new LinkedList<ProcessInfo>();

			OMNamespace pmapi = factory.createOMNamespace(Namespaces.ODE_PMAPI_NS, "pmapi");

			OMElement msg = factory.createOMElement("listAllProcesses", pmapi);

			OMElement responseElement = getProcessManagementServiceClient().sendReceive(msg);

			QName processInfoName = new QName(Namespaces.ODE_PMAPI_TYPES_NS, "process-info");

			Iterator<OMElement> it = responseElement.getFirstElement().getChildrenWithName(processInfoName);

			while (it.hasNext()) {

				ProcessInfo info = new ProcessInfo();

				OMElement element = it.next();

				Iterator<OMElement> it2 = element.getChildElements();

				while (it2.hasNext()) {

					OMElement el = it2.next();

					if (el.getLocalName().equals("pid"))
						info.setProcessId(QName.valueOf(el.getText()).getLocalPart());
					if (el.getLocalName().equals("version"))
						info.setVersion(el.getText());
					else if (el.getLocalName().equals("definition-info"))
						info.setProcessName(el.getFirstElement().getTextAsQName().getLocalPart());
					else if (el.getLocalName().equals("deployment-info"))
						info.setDeploymentPackage((((OMElement) el.getChildrenWithLocalName("package").next()).getText()));

				}
				result.add(info);
			}

			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ProcessExecutionInfo executeProcess(String processName, ExecutionContext executionContext) {

		try {

			if (executionContext != null) {
				ExecutionContext.set(executionContext);
			}

			ProcessStoreImpl processStore = odeServer.getProcessStore();

			ProcessExecutionInfo result = new ProcessExecutionInfo();

			ProcessConf pConf = null;

			for (QName processId : odeServer.getProcessStore().getProcesses()) {
				ProcessConf processConf = processStore.getProcessConfiguration(processId);
				if (processConf.getType().getLocalPart().equals(processName)) {
					pConf = processConf;
					break;
				}
			}

			if (pConf != null) {

				AxisService service = odeServer.getAxisConfiguration().getService(processName);

				if (service != null) {

					Iterator<AxisOperation> operations = service.getOperations();

					// Trying to get the "execute" operation
					while (operations.hasNext()) {

						AxisOperation op = operations.next();

						if (op.getName().getLocalPart().equals("execute")) {

							ServiceClient client = getProcessExecutionServiceClient(service.getName(), op.getSoapAction());

							OMElement msg = factory.createOMElement(new QName(op.getName().getNamespaceURI(), service.getName() + "Request"));
							OMElement responseElement = client.sendReceive(msg);

							String instanceId = ((OMElement) responseElement.getChildrenWithLocalName("processId").next()).getText();

							result.setInstanceId(instanceId);
							result.setProcessName(service.getName());
							result.setProcessId(pConf.getProcessId().toString());
							result.setVersion(String.valueOf(pConf.getVersion()));

						}
					}
				}
			}
			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private ServiceClient getDeploymentServiceClient() throws AxisFault {
		ServiceClient serviceClient = new ServiceClient(configContext, null);
		Options options = new Options();
		options.setTo(new EndpointReference("local://services/DeploymentService"));
		serviceClient.setOptions(options);
		return serviceClient;
	}

	private ServiceClient getProcessManagementServiceClient() throws AxisFault {
		ServiceClient serviceClient = new ServiceClient(configContext, null);
		Options options = new Options();
		options.setTo(new EndpointReference("local://services/ProcessManagement"));
		serviceClient.setOptions(options);
		return serviceClient;
	}

	private ServiceClient getProcessExecutionServiceClient(String serviceName, String soapAction) throws AxisFault {
		ServiceClient serviceClient = new ServiceClient(configContext, null);
		Options options = new Options();
		options.setTo(new EndpointReference("local://processes/" + serviceName));
		options.setAction(soapAction);
		serviceClient.setOptions(options);
		return serviceClient;
	}

}
