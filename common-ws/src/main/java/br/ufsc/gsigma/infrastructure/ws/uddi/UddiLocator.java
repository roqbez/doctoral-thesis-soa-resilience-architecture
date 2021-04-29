package br.ufsc.gsigma.infrastructure.ws.uddi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.juddi.v3.client.config.UDDIClient;
import org.apache.juddi.v3.client.transport.Transport;
import org.apache.juddi.v3.client.transport.TransportException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.input.DOMBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.AccessPoint;
import org.uddi.api_v3.BindingTemplate;
import org.uddi.api_v3.BusinessService;
import org.uddi.api_v3.GetServiceDetail;
import org.uddi.api_v3.ServiceDetail;
import org.uddi.v3_service.DispositionReportFaultMessage;
import org.uddi.v3_service.UDDIInquiryPortType;

public class UddiLocator {

	private static final String DEFAULT_NODE_NAME = "default";

	private final static Logger logger = LoggerFactory.getLogger(UddiLocator.class);

	private static final DOMBuilder domBuilder = new DOMBuilder();

	private static UDDIInquiryPortType inquiryService = null;

	private static UDDIClient uddiClient = null;

	private static Transport transport = null;

	private static final Map<String, Transport> cacheTransport = new HashMap<String, Transport>();

	public static Transport getUDDITransport(String nodeName) {
		try {
			UDDIClient uddiClient = new UDDIClient();
			return uddiClient.getTransport(nodeName);
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Transport getUDDITransportByServicesBaseURL(String baseURL) {

		Transport transport = cacheTransport.get(baseURL);

		if (transport == null) {
			synchronized (cacheTransport) {
				transport = cacheTransport.get(baseURL);
				if (transport == null) {
					transport = new WSTransport(baseURL);
					cacheTransport.put(baseURL, transport);
				}
			}
		}
		return transport;
	}

	public static UDDIInquiryPortType getUDDIInquiryService() {

		if (inquiryService == null) {
			synchronized (UddiLocator.class) {
				if (inquiryService == null) {
					try {
						Transport transport = getUDDITransport();
						inquiryService = transport.getUDDIInquiryService();
					} catch (ConfigurationException e) {
						throw new RuntimeException(e);
					} catch (TransportException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return inquiryService;
	}

	public static String getUDDIInquiryServiceUrl(UDDIInquiryPortType inquiryService) throws ConfigurationException {
		if (inquiryService instanceof BindingProvider) {
			return getUrlFromBindingProvider((BindingProvider) inquiryService);
		}
		return null;
	}

	public static Transport getUDDITransport() throws ConfigurationException {
		if (transport == null) {
			synchronized (UddiLocator.class) {
				transport = getUDDIClient().getTransport(DEFAULT_NODE_NAME);
			}
		}

		return transport;
	}

	public static String getUrlFromUDDIService(Object service) {
		if (service instanceof BindingProvider)
			return getUrlFromBindingProvider((BindingProvider) service);
		return null;
	}

	private static String getUrlFromBindingProvider(BindingProvider bindingProvider) {
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		return (String) requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
	}

	public static UDDIClient getUDDIClient() throws ConfigurationException {
		if (uddiClient == null) {
			synchronized (UddiLocator.class) {
				if (uddiClient == null) {
					uddiClient = new UDDIClient();
				}
			}
		}
		return uddiClient;
	}

	public static Map<String, String> findServices(Set<String> serviceKeys) {

		Map<String, String> result = new HashMap<String, String>();

		UDDIInquiryPortType inquiry = getUDDIInquiryService();

		if (inquiry != null) {

			GetServiceDetail gsd = new GetServiceDetail();

			gsd.getServiceKey().addAll(serviceKeys);

			try {

				ServiceDetail serviceDetail = inquiry.getServiceDetail(gsd);

				for (BusinessService service : serviceDetail.getBusinessService()) {

					for (BindingTemplate b : service.getBindingTemplates().getBindingTemplate()) {

						try {
							AccessPoint acessPoint = b.getAccessPoint();

							if (acessPoint.getUseType() != null && acessPoint.getUseType().equals("wsdlDeployment")) {
								String wsdlUrl = b.getAccessPoint().getValue();
								// If it is wsdlDeployment, parse the wsdl and
								// try to discover the endpoint

								result.put(service.getServiceKey(), wsdlUrl.split("\\?")[0]);

								// result.put(service.getServiceKey(),
								// getServiceEndpointURL(wsdlUrl));
								break;

							} else if (acessPoint.getUseType() != null && acessPoint.getUseType().equals("endpoint")) {
								String endpoint = b.getAccessPoint().getValue();
								result.put(service.getServiceKey(), endpoint);
								break;
							}

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return result;
	}

	public static String getServiceWSDL(String serviceKey) throws DispositionReportFaultMessage, RemoteException {

		UDDIInquiryPortType inquiry = getUDDIInquiryService();

		if (inquiry != null) {

			GetServiceDetail gsd = new GetServiceDetail();

			gsd.getServiceKey().add(serviceKey);

			ServiceDetail serviceDetail = inquiry.getServiceDetail(gsd);

			for (BusinessService service : serviceDetail.getBusinessService()) {

				for (BindingTemplate b : service.getBindingTemplates().getBindingTemplate()) {

					AccessPoint accessPoint = b.getAccessPoint();

					if (accessPoint.getUseType() != null && accessPoint.getUseType().equals("wsdlDeployment"))
						return readURL(accessPoint.getValue());

				}
			}

		}

		return null;
	}

	private static String readURL(String url) {

		URL u;
		InputStream is = null;

		try {

			u = new URL(url);

			is = u.openStream();

			return convertStreamToString(is);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
			}
		}
	}

	private static String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	@SuppressWarnings("unused")
	private static String getServiceEndpointURL(String wsdlUrl) throws Exception {

		InputStream is = null;

		try {
			URL u = new URL(wsdlUrl);

			is = new DataInputStream(u.openStream());

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document doc = db.parse(is);

			Document jdomDocument = domBuilder.build(doc);

			XPath xPath = XPath//
					.newInstance("//wsdl:definitions/wsdl:service/wsdl:port/soap:address/@location");
			xPath.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
			xPath.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");

			return ((Attribute) xPath.selectSingleNode(jdomDocument)).getValue();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}

	}
}
