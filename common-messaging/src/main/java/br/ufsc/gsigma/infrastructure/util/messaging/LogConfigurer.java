package br.ufsc.gsigma.infrastructure.util.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public abstract class LogConfigurer {

	public static void configure() throws IOException {
		configure("log4j.properties");
	}

	public static void configure(String logFile) throws IOException {

		Properties log4jProps = new Properties();
		InputStream in = BootstrapJmsBroker.class.getResourceAsStream("/br/ufsc/gsigma/infrastructure/util/messaging/" + logFile);
		if (in != null) {
			try {
				log4jProps.load(in);
			} catch (IOException e) {
				in.close();
				throw e;
			}
			PropertyConfigurator.configure(log4jProps);
		}
	}

}
