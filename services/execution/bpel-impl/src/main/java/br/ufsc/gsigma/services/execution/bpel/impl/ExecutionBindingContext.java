package br.ufsc.gsigma.services.execution.bpel.impl;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.ode.axis2.ODEService;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.ProcessConf;

import br.ufsc.gsigma.services.execution.bpel.ode.AxisODEServer;
import br.ufsc.gsigma.services.execution.bpel.ode.BindingContextImpl;

public class ExecutionBindingContext extends BindingContextImpl {

	public ExecutionBindingContext(AxisODEServer server) {
		super(server);
	}

	public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType, Endpoint initialPartnerEndpoint) {
		ProcessConf pconf = getServer().getProcessStore().getProcessConfiguration(processId);
		return new BindingServiceExternalService(portType, initialPartnerEndpoint, pconf, this);
	}

	@Override
	protected ODEService createODEService(ProcessConf pconf, QName serviceName, String portName, AxisService axisService) throws AxisFault {
		return new ExecutionODEService(axisService, pconf, serviceName, portName, _server.getBpelServer(), _server.getTransactionManager());
	}

	AxisODEServer getServer() {
		return _server;
	}

}
