package br.ufsc.gsigma.infrastructure.util.messaging;

public interface MessageEndpoints {

//	public static final String EVENT_BUS_ENDPOINT = "activemq:topic:EventBus?durableSubscriptionName=eventBus&concurrentConsumers=1";
	
	public static final String EVENT_BUS_ENDPOINT = "activemq:topic:EventBus?concurrentConsumers=1";
}
