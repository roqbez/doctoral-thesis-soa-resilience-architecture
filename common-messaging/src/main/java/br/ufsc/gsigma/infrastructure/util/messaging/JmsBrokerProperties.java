package br.ufsc.gsigma.infrastructure.util.messaging;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;

public class JmsBrokerProperties implements InitializingBean {

	private static final Logger logger = Logger.getLogger(JmsBrokerProperties.class);

	@Value("${jms.broker.name}")
	private String brokerName;

	@Override
	public void afterPropertiesSet() throws Exception {

		if (DockerContainerUtil.isRunningInContainer() && System.getProperty("jms.broker.name") == null) {

			String clientId = DockerContainerUtil.getContainerClientId();

			this.brokerName = "jmsbroker".equals(clientId) ? "server" : clientId;

			logger.info("Running in a Docker container, using '" + brokerName + "' as brokerName");
		} else {
			this.brokerName = this.brokerName != null ? this.brokerName : "server";
		}

		System.setProperty("jms.broker.name", this.brokerName);

		logger.info("Using brokerName '" + this.brokerName + "'");

	}

	public String getBrokerName() {
		return brokerName;
	}

}
