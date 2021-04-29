package br.ufsc.gsigma.infrastructure.util.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;

public class EventSender implements ApplicationContextAware, InitializingBean {

	private static EventSender INSTANCE;

	private static final Logger logger = Logger.getLogger(EventSender.class);

	@EndpointInject(uri = MessageEndpoints.EVENT_BUS_ENDPOINT + "&preserveMessageQos=true")
	private ProducerTemplate producer;

	private String senderId;

	@Override
	public void afterPropertiesSet() throws Exception {

		if (DockerContainerUtil.isRunningInContainer() && System.getProperty("jms.senderid") == null) {

			String clientId = DockerContainerUtil.getContainerClientId();

			logger.info("Running in a Docker container, using '" + clientId + "' as senderId");

			System.setProperty("jms.senderid", clientId);

			this.senderId = clientId;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		INSTANCE = applicationContext.getBean(EventSender.class);
	}

	public static EventSender getInstance() {
		return INSTANCE;
	}

	public void sendEvent(Event event) {
		sendEvent(event, event.getTTL());
	}

	public void sendEvent(Event event, long ttl) {

		if (senderId == null)
			throw new IllegalArgumentException("Property 'jms.senderid' not set");

		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(JmsMessagingComponent.HEADER_SEND_ID, senderId);

		if (ttl > 0) {
			headers.put("JMSExpiration", System.currentTimeMillis() + ttl);
		} else {
			headers.put("JMSExpiration", Long.MAX_VALUE);
		}

		producer.sendBodyAndHeaders(event, headers);
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

}
