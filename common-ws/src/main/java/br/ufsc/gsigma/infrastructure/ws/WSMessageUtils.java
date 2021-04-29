package br.ufsc.gsigma.infrastructure.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.message.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfLoggingFeature;
import br.ufsc.gsigma.infrastructure.ws.cxf.ExecutionContextFeature;

public class WSMessageUtils {

	// private static final Logger logger = Logger.getLogger(WSMessageUtils.class);

	private static final WSMessageUtils INSTANCE = new WSMessageUtils();

	public static WSMessageUtils getInstance() {
		return INSTANCE;
	}

	public boolean sendMessageOneAway(Object objectToSend, String endPointURL, String soapAction) throws Exception {
		return sendMessageOneAway(objectToSend, endPointURL, soapAction, null, null);
	}

	public boolean sendMessageOneAway(Object objectToSend, String endPointURL, String soapAction, Map<String, String> requestHeaders, List<Interceptor<? extends Message>> outInterceptors) throws Exception {
		Dispatch<DOMSource> dispatch = getDispatch(endPointURL, soapAction, requestHeaders, outInterceptors);
		dispatch.invokeOneWay(getDOMSource(objectToSend));
		return true;
	}

	public boolean sendMessage(Object objectToSend, String endPointURL, String soapAction, Map<String, String> requestHeaders, List<Interceptor<? extends Message>> outInterceptors) throws Exception {
		Dispatch<DOMSource> dispatch = getDispatch(endPointURL, soapAction, requestHeaders, outInterceptors);
		dispatch.invoke(getDOMSource(objectToSend));
		return true;
	}

	public DOMSource getDOMSource(Object objectToSend) throws Exception {

		JAXBContext jbc = JAXBContext.newInstance(objectToSend.getClass());

		Marshaller m = jbc.createMarshaller();

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document requestDoc = db.newDocument();

		m.marshal(objectToSend, requestDoc);

		return new DOMSource(requestDoc);
	}

	@SuppressWarnings("rawtypes")
	protected Dispatch<DOMSource> getDispatch(String endPointURL, String soapAction, Map<String, String> requestHeaders, List<Interceptor<? extends Message>> outInterceptors) {

		final QName serviceName = new QName("ns", "serviceName");
		final QName portName = new QName("ns", "portName");

		Service service = Service.create(serviceName);
		service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endPointURL);

		Dispatch<DOMSource> dispatch = service.createDispatch(portName, DOMSource.class, Service.Mode.PAYLOAD);

		Map<String, Object> requestContext = dispatch.getRequestContext();

		requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);

		Client client = ((DispatchImpl) dispatch).getClient();

		if (requestHeaders != null && !requestHeaders.isEmpty()) {

			Map<String, List<String>> headers = new HashMap<String, List<String>>();

			for (Entry<String, String> e : requestHeaders.entrySet()) {
				headers.put(e.getKey(), Collections.singletonList(e.getValue()));
			}

			client.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);
		}

		configureClientFeatures(client, outInterceptors);

		return dispatch;
	}

	protected void configureClientFeatures(Client client, List<Interceptor<? extends Message>> outInterceptors) {
		CxfLoggingFeature.configureIn(client);
		CxfLoggingFeature.configureOut(client);
		ExecutionContextFeature.configure(client);

		if (outInterceptors != null) {
			client.getOutInterceptors().addAll(outInterceptors);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getObjectFromSoapHeader(SOAPMessage soapMessage, QName headerQName, Class<T> clazz) {

		try {
			SOAPHeader headers = null;
			try {
				headers = soapMessage.getSOAPHeader();
			} catch (SOAPException e) {
			}

			if (headers != null) {
				Iterator<SOAPElement> it = headers.getChildElements(headerQName);
				while (it.hasNext()) {
					SOAPElement header = it.next();

					if (clazz == String.class) {
						return (T) header.getTextContent();
					} else {
						return JAXBSerializerUtil.read(clazz, header);
					}
				}
			}
			return null;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SOAPElement removeSoapHeader(SOAPMessage soapMessage, QName headerQName) {

		try {
			SOAPHeader headers = null;
			try {
				headers = soapMessage.getSOAPHeader();
			} catch (SOAPException e) {
			}

			SOAPElement header = null;

			if (headers != null) {

				Iterator<SOAPElement> it = headers.getChildElements(headerQName);
				while (it.hasNext()) {
					header = it.next();
					break;
				}

				if (header != null) {
					headers.removeChild(header);
				}
			}
			return header;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static Node getSoapBodyElement(SOAPMessage soapMessage) throws SOAPException {
		SOAPPart sp = soapMessage.getSOAPPart();
		SOAPEnvelope se = sp.getEnvelope();
		SOAPBody sb = se.getBody();

		Iterator<Node> childElements = sb.getChildElements();

		while (childElements.hasNext()) {

			Node n = childElements.next();

			if (n instanceof SOAPBodyElement)
				return n;

		}
		return null;
	}

}
