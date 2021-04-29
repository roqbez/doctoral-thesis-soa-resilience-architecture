package br.ufsc.gsigma.infrastructure.util.messaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.jms.JmsConsumer;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.processor.CamelInternalProcessor;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class JmsMessagingComponent extends ActiveMQComponent {

	private static final Logger logger = Logger.getLogger(JmsMessagingComponent.class);

	static final String HEADER_SEND_ID = "SenderId";

	private static final Pattern PATTERN_CAMEL_EVENT_BUS_THREAD = Pattern.compile("Camel Thread (#\\d+) - JmsConsumer\\[EventBus\\]");

	@Value("${jms.senderid}")
	private String senderId;

	@Override
	protected JmsConfiguration createConfiguration() {
		JmsConfiguration cfg = super.createConfiguration();

		if (DockerContainerUtil.isRunningInContainer() && System.getProperty("jms.senderid") == null) {

			String clientId = DockerContainerUtil.getContainerClientId();

			logger.info("Running in a Docker container, using '" + clientId + "' as senderId");

			cfg.setSelector(HEADER_SEND_ID + " <> '" + clientId + "'");

		} else if (senderId != null) {
			cfg.setSelector(HEADER_SEND_ID + " <> '" + senderId + "'");
		}

		return cfg;
	}

	@Override
	protected JmsEndpoint createTopicEndpoint(String uri, JmsComponent component, String subject, JmsConfiguration configuration) {
		return new JmsEndpoint(uri, component, subject, true, configuration) {
			@Override
			public JmsConsumer createConsumer(Processor processor) throws Exception {
				if (processor instanceof CamelInternalProcessor) {
					AsyncProcessor delegate = ((CamelInternalProcessor) processor).getProcessor();
					if (delegate instanceof BeanProcessor) {

						final BeanProcessor beanProcessor = (BeanProcessor) delegate;

						processor = new CamelInternalProcessor(new AsyncProcessor() {
							@Override
							public void process(Exchange exchange) throws Exception {
								AsyncProcessorHelper.process(this, exchange);
							}

							@Override
							public boolean process(Exchange exchange, AsyncCallback callback) {

								Object payload = exchange.getIn().getBody();

								ExecutionContext executionContext = null;

								String threadName = Thread.currentThread().getName();

								try {

									if (payload instanceof Event) {

										Matcher matcher = PATTERN_CAMEL_EVENT_BUS_THREAD.matcher(threadName);

										if (matcher.matches()) {
											String tnum = matcher.group(1);
											Thread.currentThread().setName("EventBus[" + payload.getClass().getSimpleName() + "]-" + tnum);
										}

										executionContext = ((Event) payload).getExecutionContext();
										if (executionContext != null)
											executionContext.set();
									}
									return beanProcessor.process(exchange, callback);

								} finally {
									if (executionContext != null)
										executionContext.remove();

									Thread.currentThread().setName(threadName);
								}
							}
						});
					}
				}
				return super.createConsumer(processor);
			}
		};
	}

}
