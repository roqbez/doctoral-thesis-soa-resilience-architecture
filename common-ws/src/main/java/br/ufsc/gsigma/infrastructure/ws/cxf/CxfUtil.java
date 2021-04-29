package br.ufsc.gsigma.infrastructure.ws.cxf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public abstract class CxfUtil {

	private static final Logger logger = LoggerFactory.getLogger(CxfUtil.class);

	public static boolean isCXFRequest() {
		return PhaseInterceptorChain.getCurrentMessage() != null;
	}

	public static SoapMessage getSoapMessage() {

		Message message = PhaseInterceptorChain.getCurrentMessage();

		if (message instanceof SoapMessage)
			return (SoapMessage) message;

		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Element> getRequestSoapHeadersElements() {

		SoapMessage soapMessage = getSoapMessage();

		if (soapMessage != null) {
			return getRequestSoapHeadersElements(soapMessage);
		}

		return Collections.EMPTY_LIST;
	}

	@SuppressWarnings("unchecked")
	public static List<Element> getRequestSoapHeadersElements(SoapMessage message) {

		List<Header> headers = message.getHeaders();

		if (headers != null && !headers.isEmpty()) {

			List<Element> result = new ArrayList<Element>(headers.size());

			for (Header header : headers) {
				Object obj = header.getObject();
				if (obj instanceof Element)
					result.add((Element) obj);
			}
			return result;
		}

		return Collections.EMPTY_LIST;
	}

	public static Element getRequestSoapHeaderElement(QName name) {

		SoapMessage soapMessage = getSoapMessage();

		if (soapMessage != null) {
			return getRequestSoapHeaderElement(soapMessage, name);
		}

		return null;
	}

	public static Element getRequestSoapHeaderElement(SoapMessage message, QName name) {

		Header header = message.getHeader(name);

		if (header != null) {

			Object obj = header.getObject();
			if (obj instanceof Element)
				return (Element) obj;
		}

		return null;
	}

	public static void setRequestSoapHeader(QName name, Element element) {

		SoapMessage soapMessage = getSoapMessage();

		if (soapMessage != null) {
			soapMessage.getHeaders().add(new Header(name, element));
		}
	}

	public static Bus createBus() {
		return BusFactory.newInstance().createBus();
	}

	public static Server createService(Class<?> serviceInterface, String serviceUrl, Object serviceImplementor) {
		return createService(null, serviceInterface, serviceUrl, serviceImplementor);
	}

	public static Server createService(Class<?> serviceInterface, String serviceUrl, Object serviceImplementor, Collection<AbstractFeature> features) {
		return createService(null, serviceInterface, serviceUrl, serviceImplementor, features);
	}

	public static Server createService(Class<?> serviceInterface, String serviceUrl, Object serviceImplementor, Collection<AbstractFeature> features, DataBinding dataBinding) {
		return createService(null, serviceInterface, serviceUrl, serviceImplementor, features, dataBinding);
	}

	public static Server createService(Bus bus, Class<?> serviceInterface, String serviceUrl, Object serviceImplementor) {
		return createService(bus, serviceInterface, serviceUrl, serviceImplementor, null);
	}

	public static Server createService(Bus bus, Class<?> serviceInterface, String serviceUrl, Object serviceImplementor, Collection<AbstractFeature> features) {
		return createService(bus, serviceInterface, serviceUrl, serviceImplementor, features, null);
	}

	public static Server createService(Bus bus, Class<?> serviceInterface, String serviceUrl, Object serviceImplementor, Collection<AbstractFeature> features, DataBinding dataBinding) {
		return createService(bus, serviceInterface, serviceUrl, serviceImplementor, features, dataBinding, null);
	}

	public static Server createService(Bus bus, Class<?> serviceInterface, String serviceUrl, Object serviceImplementor, Collection<AbstractFeature> features, DataBinding dataBinding, List<Interceptor<? extends Message>> inInterceptors) {
		logger.info("Creating service " + (serviceUrl.startsWith("/") ? serviceUrl.substring(1) : serviceUrl) + " using SEI " + serviceInterface.getName());
		JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
		svrFactory.setServiceClass(serviceInterface);
		svrFactory.setAddress(serviceUrl);
		svrFactory.setServiceBean(serviceImplementor);

		if (inInterceptors != null) {
			svrFactory.getInInterceptors().addAll(inInterceptors);
		}

		svrFactory.getFeatures().addAll(getServerFeatures());

		if (features != null)
			svrFactory.getFeatures().addAll(features);

		if (bus != null)
			svrFactory.setBus(bus);

		if (dataBinding != null)
			svrFactory.setDataBinding(dataBinding);

		return svrFactory.create();
	}

	public static Collection<Feature> getClientFeatures() {
		Collection<Feature> features = new LinkedList<Feature>();
		features.add(new ExecutionContextFeature(false, true));
		return features;
	}

	public static Collection<Feature> getServerFeatures() {
		Collection<Feature> features = new LinkedList<Feature>();
		features.add(new CxfLoggingFeature(true, false, false));
		features.add(new ThreadLocalHolderFeature());
		features.add(new ExecutionContextFeature());
		return features;
	}
}
