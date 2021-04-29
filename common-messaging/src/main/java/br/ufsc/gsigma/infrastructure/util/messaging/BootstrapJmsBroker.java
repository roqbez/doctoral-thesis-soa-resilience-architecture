package br.ufsc.gsigma.infrastructure.util.messaging;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import br.ufsc.gsigma.infrastructure.util.AppContext;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class BootstrapJmsBroker {

	private static final Logger logger = Logger.getLogger(BootstrapJmsBroker.class);

	public static void main(String[] args) throws IOException {

		LogConfigurer.configure();

		String dataDir = "db/jms-broker/jms";

		System.setProperty("jms.broker.url", "tcp://0.0.0.0:" + ServicesAddresses.JMS_BROKER_PORT);
		System.setProperty("jms.data.dir", dataDir);
		System.setProperty("jms.data.kahadb.dir", dataDir + "/kahadb");

		boolean clean = Boolean.valueOf(System.getProperty("clean", "false"));

		File dataDirFile = new File(dataDir);

		if (clean && dataDirFile.exists()) {
			logger.info("Cleaning up storage at " + dataDirFile.getAbsolutePath());
			FileUtils.deleteDirectory(dataDirFile);
		}

		AbstractApplicationContext applicationContext = (AbstractApplicationContext) AppContext.getApplicationContext("/br/ufsc/gsigma/infrastructure/util/messaging/messaging-broker-standalone.xml");

		applicationContext.registerShutdownHook();

		final Object lock = new Object();

		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}

	}

}
