package br.ufsc.gsigma.infrastructure.util.messaging;

import java.io.File;
import java.io.IOException;

import org.springframework.context.support.AbstractApplicationContext;

import br.ufsc.gsigma.infrastructure.util.AppContext;

public abstract class SendEventTest {

	public static void main(String[] args) throws IOException {

		LogConfigurer.configure("log4j-test.properties");

		if (System.getProperty("jms.clientid", null) == null) {
			System.setProperty("jms.clientid", "testclient");
		}

		if (System.getProperty("jms.senderid", null) == null) {
			System.setProperty("jms.senderid", "testclient");
		}

		if (System.getProperty("jms.data.dir", null) == null) {
			System.setProperty("jms.data.dir", new File(System.getProperty("java.io.tmpdir"), "jms-data-" + System.currentTimeMillis()).getAbsolutePath());
		}

		if (System.getProperty("jms.data.kahadb.dir", null) == null) {
			System.setProperty("jms.data.kahadb.dir", new File(System.getProperty("jms.data.dir"), "kahadb").getAbsolutePath());
		}

//		System.setProperty("jms.broker.url", "tcp://jmsbroker.d-201603244.ufsc.br:61616");
		
		AbstractApplicationContext applicationContext = (AbstractApplicationContext) AppContext.getApplicationContext("/br/ufsc/gsigma/infrastructure/util/messaging/send-event-test.xml");

		EventSender eventSender = applicationContext.getBean(EventSender.class);

		String message = args.length > 0 ? args[0] : "test message";

		while (true) {

			System.out.println("Sending message '" + message + "'");
			eventSender.sendEvent(new StringEvent(message));

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
			}
		}

	}

}
