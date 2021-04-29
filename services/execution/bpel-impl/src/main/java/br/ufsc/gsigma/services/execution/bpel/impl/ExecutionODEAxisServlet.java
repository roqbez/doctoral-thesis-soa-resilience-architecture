package br.ufsc.gsigma.services.execution.bpel.impl;

import br.ufsc.gsigma.services.execution.bpel.ode.AxisODEServer;

public class ExecutionODEAxisServlet extends br.ufsc.gsigma.services.execution.bpel.ode.ODEAxisServlet {

	private static final long serialVersionUID = 1L;

	protected AxisODEServer createODEServer() {
		return new ExecutionODEServer();
	}

}
