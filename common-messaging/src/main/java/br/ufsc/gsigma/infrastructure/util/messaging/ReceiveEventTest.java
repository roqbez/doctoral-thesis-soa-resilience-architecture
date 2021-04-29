package br.ufsc.gsigma.infrastructure.util.messaging;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Consume;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import br.ufsc.gsigma.infrastructure.util.AppContext;

public class ReceiveEventTest {

	private static final Logger logger = Logger.getLogger(ReceiveEventTest.class);

	public static void main(String[] args) throws IOException {

		LogConfigurer.configure("log4j-test.properties");

		if (System.getProperty("jms.broker.name") == null) {
			System.setProperty("jms.broker.name", "testclientreceiver");
		}

		if (System.getProperty("jms.clientid") == null) {
			System.setProperty("jms.clientid", "testclientreceiver");
		}

		if (System.getProperty("jms.senderid") == null) {
			System.setProperty("jms.senderid", "testclientreceiver");
		}

		if (System.getProperty("jms.data.dir") == null) {
			System.setProperty("jms.data.dir", new File(System.getProperty("java.io.tmpdir"), "jms-data-" + System.currentTimeMillis()).getAbsolutePath());
		}

		if (System.getProperty("jms.data.kahadb.dir") == null) {
			System.setProperty("jms.data.kahadb.dir", new File(System.getProperty("jms.data.dir"), "kahadb").getAbsolutePath());
		}

		AbstractApplicationContext applicationContext = (AbstractApplicationContext) AppContext.getApplicationContext("/br/ufsc/gsigma/infrastructure/util/messaging/receive-event-test.xml");

		applicationContext.registerShutdownHook();

		final Object lock = new Object();

		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	@Consume(uri = MessageEndpoints.EVENT_BUS_ENDPOINT)
	public void receiveBusEvent(Event event) throws Exception {
		logger.info("Received event --> " + event.getClass());
	}
}
