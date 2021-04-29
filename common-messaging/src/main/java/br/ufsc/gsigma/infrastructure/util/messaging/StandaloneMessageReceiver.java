package br.ufsc.gsigma.infrastructure.util.messaging;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.Consume;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StandaloneMessageReceiver implements ApplicationContextAware {

	private List<EventListener> listeners = new LinkedList<EventListener>();

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public static StandaloneMessageReceiver create(String brokerUrl, String clientId) {

		System.setProperty("jms.broker.url", brokerUrl);
		System.setProperty("jms.clientid", clientId);
		System.setProperty("jms.senderid", clientId);

		ApplicationContext applicationContext = createApplicationContext();

		return applicationContext.getBean(StandaloneMessageReceiver.class);
	}

	public void shutdown() {
		((AbstractApplicationContext) applicationContext).close();
	}

	private static ApplicationContext createApplicationContext() {

		@SuppressWarnings("resource")
		AbstractApplicationContext applicationContext = (AbstractApplicationContext) new ClassPathXmlApplicationContext( //
				"/br/ufsc/gsigma/infrastructure/util/messaging/messaging-receiver-standalone.xml");

		return applicationContext;
	}

	public void registerEventListener(EventListener listener) {
		listeners.add(listener);
	}

	@Consume(uri = MessageEndpoints.EVENT_BUS_ENDPOINT)
	public void receiveBusEvent(Event event) throws Exception {
		for (EventListener listener : listeners) {
			listener.onEvent(event);
		}
	}
}
