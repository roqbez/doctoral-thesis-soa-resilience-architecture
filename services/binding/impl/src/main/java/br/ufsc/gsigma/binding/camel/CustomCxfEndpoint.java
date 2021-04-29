package br.ufsc.gsigma.binding.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfConsumer;
import org.apache.camel.component.cxf.CxfEndpoint;

public class CustomCxfEndpoint extends CxfEndpoint {

	public CustomCxfEndpoint() {
		super();
	}

	public CustomCxfEndpoint(String remaining, CxfComponent cxfComponent) {
		super(remaining, cxfComponent);
	}

	public Consumer createConsumer(Processor processor) throws Exception {

		CxfConsumer consumer = (CxfConsumer) super.createConsumer(processor);

		// System.out.println("InInterceptors:");
		// for (Interceptor i : consumer.getServer().getEndpoint().getInInterceptors()) {
		// if (i instanceof PhaseInterceptor)
		// System.out.println("[IN] " + ((PhaseInterceptor) i).getPhase() + " - " + i);
		// }

		return consumer;
	}
}
