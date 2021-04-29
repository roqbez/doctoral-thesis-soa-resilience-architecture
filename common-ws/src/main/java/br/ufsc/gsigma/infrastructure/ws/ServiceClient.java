package br.ufsc.gsigma.infrastructure.ws;

import java.lang.reflect.Proxy;
import java.util.Collection;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public abstract class ServiceClient {

	private static final int DEFAULT_READ_TIMEOUT = 60000 * 10;

	public static <T> T changeEndpointAddress(T serviceProxy, String endpointUrl) {

		BindingProvider bindingProvider = (BindingProvider) serviceProxy;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);

		return serviceProxy;
	}

	public static <T> T getClient(Class<T> clazz, boolean threadLocalRequestContext) {
		T client = getClient(clazz, "http://0.0.0.0", null);
		((Client) client).setThreadLocalRequestContext(true);
		return client;
	}

	public static <T> T getClient(Class<T> clazz) {
		return getClient(clazz, "http://0.0.0.0", null);
	}

	public static <T> T getClient(Class<T> clazz, String url) {
		return getClient(clazz, url, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getClient(Class<T> clazz, String url, Collection<Feature> features) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(clazz);
		factory.setAddress(url);

		if (features != null) {
			factory.getFeatures().addAll(features);
		}

		T proxy = (T) factory.create();

		// Timeout
		ClientProxy clientProxy = (ClientProxy) Proxy.getInvocationHandler(proxy);
		HTTPConduit conduit = (HTTPConduit) clientProxy.getClient().getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setReceiveTimeout(DEFAULT_READ_TIMEOUT);
		conduit.setClient(httpClientPolicy);

		return proxy;
	}
}
