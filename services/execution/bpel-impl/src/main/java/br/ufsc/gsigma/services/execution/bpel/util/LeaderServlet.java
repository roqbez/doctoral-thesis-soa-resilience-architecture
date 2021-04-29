package br.ufsc.gsigma.services.execution.bpel.util;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServlet;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

public class LeaderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void service(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) throws javax.servlet.ServletException, java.io.IOException {

		boolean leader = InfinispanDatabase.getInstance().isLeader();

		if (leader) {
			resp.getOutputStream().write("true".getBytes(StandardCharsets.UTF_8));
			resp.setStatus(200);
		} else {
			resp.getOutputStream().write("false".getBytes(StandardCharsets.UTF_8));
			resp.setStatus(501);
		}

	};

}
