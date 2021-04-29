package br.ufsc.gsigma.infrastructure.util.messaging;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;

public class JmsMessagingConnectionFactory extends CachingConnectionFactory {

	@Override
	public void afterPropertiesSet() {

		if (DockerContainerUtil.isRunningInContainer() && System.getProperty("jms.clientid") == null) {

			String clientId = DockerContainerUtil.getContainerClientId();

			logger.info("Running in a Docker container, using '" + clientId + "' as clientId");

			System.setProperty("jms.clientid", clientId);

			if (getTargetConnectionFactory() instanceof ActiveMQConnectionFactory) {
				((ActiveMQConnectionFactory) getTargetConnectionFactory()).setClientID(clientId);
			}
		}

		super.afterPropertiesSet();
	}

}
