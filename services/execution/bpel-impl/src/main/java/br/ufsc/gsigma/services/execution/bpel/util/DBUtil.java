package br.ufsc.gsigma.services.execution.bpel.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public abstract class DBUtil {

	public static void clearDatabase() throws Exception {

		Properties odeProps = new Properties();

		try (InputStream in = new FileInputStream("src/main/webapp/WEB-INF/conf/ode-axis2.properties")) {
			odeProps.load(in);
		}

		String url = odeProps.getProperty("ode-axis2.db.int.jdbcurl");
		String user = odeProps.getProperty("ode-axis2.db.int.username");
		String password = odeProps.getProperty("ode-axis2.db.int.password");

		if (url == null && user == null || password == null) {
			return;
		}

		Connection conn = null;
		Statement stmt = null;

		try {
			conn = DriverManager.getConnection(url, user, password);

			try {
				stmt = conn.createStatement();

				stmt.executeUpdate("DELETE FROM ODE_ACTIVITY_RECOVERY");
				stmt.executeUpdate("DELETE FROM ODE_CORRELATION_SET");
				stmt.executeUpdate("DELETE FROM ODE_CORRELATOR");
				stmt.executeUpdate("DELETE FROM ODE_CORSET_PROP");
				stmt.executeUpdate("DELETE FROM ODE_EVENT");
				stmt.executeUpdate("DELETE FROM ODE_FAULT");
				stmt.executeUpdate("DELETE FROM ODE_JOB");
				stmt.executeUpdate("DELETE FROM ODE_MESSAGE");
				stmt.executeUpdate("DELETE FROM ODE_MESSAGE_EXCHANGE");
				stmt.executeUpdate("DELETE FROM ODE_MESSAGE_ROUTE");
				stmt.executeUpdate("DELETE FROM ODE_MEX_PROP");
				stmt.executeUpdate("DELETE FROM ODE_PARTNER_LINK");
				stmt.executeUpdate("DELETE FROM ODE_PROCESS");
				stmt.executeUpdate("DELETE FROM ODE_PROCESS_INSTANCE");
				stmt.executeUpdate("DELETE FROM ODE_SCOPE");
				stmt.executeUpdate("DELETE FROM ODE_XML_DATA");
				stmt.executeUpdate("DELETE FROM ODE_XML_DATA_PROP");
				stmt.executeUpdate("DELETE FROM OPENJPA_SEQUENCE_TABLE");
				stmt.executeUpdate("DELETE FROM STORE_DU");
				stmt.executeUpdate("DELETE FROM STORE_PROCESS");
				stmt.executeUpdate("DELETE FROM STORE_PROCESS_PROP");
				stmt.executeUpdate("DELETE FROM STORE_PROC_TO_PROP");
				stmt.executeUpdate("DELETE FROM STORE_VERSIONS");

			} finally {
				stmt.close();
			}
		} catch (Exception e) {
			
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

}
