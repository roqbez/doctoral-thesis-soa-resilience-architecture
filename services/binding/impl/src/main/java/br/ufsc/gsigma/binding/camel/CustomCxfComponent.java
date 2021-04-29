package br.ufsc.gsigma.binding.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfEndpoint;

public class CustomCxfComponent extends CxfComponent {

	public CustomCxfComponent() {
		super();
	}

	public CustomCxfComponent(CamelContext context) {
		super(context);
	}

	protected CxfEndpoint createCxfEndpoint(String remaining) {
		return new CustomCxfEndpoint(remaining, this);
	}

}
