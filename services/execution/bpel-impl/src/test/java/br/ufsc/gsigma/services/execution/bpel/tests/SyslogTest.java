package br.ufsc.gsigma.services.execution.bpel.tests;

import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SyslogTest {

	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("log4j.appender.SYSLOG", "br.ufsc.services.log.util.Log4jSyslogAppender");
		properties.put("log4j.appender.SYSLOG.syslogHost", "logging.d-201603244.ufsc.br");
		properties.put("log4j.appender.SYSLOG.connectionMode", "UDP");
		properties.put("log4j.appender.SYSLOG.appName", "execution-service");
		properties.put("log4j.appender.SYSLOG.Facility", "LOCAL1");

		properties.put("log4j.logger.TESTE", "INFO, SYSLOG");

		new PropertyConfigurator().doConfigure(properties, LogManager.getLoggerRepository());

		//LogMessage msg = new LogMessage("PROCESS_INSTANCE", "Process instance started", new String[] { "processName", "PaymentProcess", "processInstanceId", "43" });

//		Logger.getLogger("TESTE").info(msg);

		Logger.getLogger("TESTE").info("Minha msg de teste2");

	}

}
