package br.ufsc.gsigma.services.execution.bpel.ode;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.transport.http.AxisServlet;
import org.apache.ode.axis2.service.DeploymentBrowser;

public class ODEAxisServlet extends AxisServlet {

	private static final long serialVersionUID = 1L;

	private AxisODEServer _odeServer;
	private DeploymentBrowser _browser;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		_odeServer = createODEServer();
		_odeServer.init(config, axisConfiguration);
		_browser = new DeploymentBrowser(_odeServer.getProcessStore(), axisConfiguration, _odeServer.getAppRoot());
	}

	public void init() throws ServletException {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!_browser.doFilter(request, response))
			super.doGet(request, response);
	}

	public void destroy() {
		super.destroy();
		_odeServer.shutDown();
	}

	protected AxisODEServer createODEServer() {
		return new AxisODEServer();
	}

	public AxisODEServer getODEServer() {
		return _odeServer;
	}

}
