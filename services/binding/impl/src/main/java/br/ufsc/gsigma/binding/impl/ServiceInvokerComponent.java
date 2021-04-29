package br.ufsc.gsigma.binding.impl;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;

public class ServiceInvokerComponent extends HttpComponent {

	protected HttpEndpoint createHttpEndpoint(String uri, HttpComponent component, HttpClientParams clientParams, HttpConnectionManager connectionManager, HttpClientConfigurer configurer)
			throws URISyntaxException {
		return new ServiceInvokerEndpoint(uri, component, clientParams, connectionManager, configurer);
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		return super.createEndpoint(uri, "0.0.0.0", parameters);
	}

}
