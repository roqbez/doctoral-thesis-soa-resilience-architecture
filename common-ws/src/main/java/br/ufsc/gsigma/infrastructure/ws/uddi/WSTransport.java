package br.ufsc.gsigma.infrastructure.ws.uddi;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.juddi.v3.client.JUDDIApiService;
import org.apache.juddi.v3.client.UDDIService;
import org.apache.juddi.v3.client.transport.Transport;
import org.apache.juddi.v3.client.transport.TransportException;
import org.apache.juddi.v3_service.JUDDIApiPortType;
import org.uddi.v3_service.UDDICustodyTransferPortType;
import org.uddi.v3_service.UDDIInquiryPortType;
import org.uddi.v3_service.UDDIPublicationPortType;
import org.uddi.v3_service.UDDISecurityPortType;
import org.uddi.v3_service.UDDISubscriptionListenerPortType;
import org.uddi.v3_service.UDDISubscriptionPortType;

public class WSTransport extends Transport {

	private String baseUDDIServicesUrl;

	private UDDIInquiryPortType inquiryService = null;
	private UDDISecurityPortType securityService = null;
	private UDDIPublicationPortType publishService = null;
	private UDDISubscriptionPortType subscriptionService = null;
	private UDDISubscriptionListenerPortType subscriptionListenerService = null;
	private UDDICustodyTransferPortType custodyTransferService = null;
	private JUDDIApiPortType publisherService = null;

	public WSTransport(String baseUDDIServicesUrl) {
		this.baseUDDIServicesUrl = baseUDDIServicesUrl;
	}

	public UDDIInquiryPortType getUDDIInquiryService(String endpointURL) throws TransportException {
		try {
			if (inquiryService == null) {
				if (endpointURL == null) {
					endpointURL = baseUDDIServicesUrl + "/inquiry";
				}
				UDDIService service = new UDDIService();
				inquiryService = service.getUDDIInquiryPort();
			}

			if (endpointURL != null) {
				Map<String, Object> requestContext = ((BindingProvider) inquiryService).getRequestContext();
				requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			}
		} catch (Exception e) {
			throw new TransportException(e.getMessage(), e);
		}
		return inquiryService;
	}

	public UDDISecurityPortType getUDDISecurityService(String endpointURL) throws TransportException {
		try {
			if (securityService == null) {
				if (endpointURL == null) {
					endpointURL = baseUDDIServicesUrl + "/security";
				}
				UDDIService service = new UDDIService();
				securityService = service.getUDDISecurityPort();
			}
			
			if (endpointURL != null) {
				Map<String, Object> requestContext = ((BindingProvider) securityService).getRequestContext();
				requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			}
		} catch (Exception e) {
			throw new TransportException(e.getMessage(), e);
		}
		return securityService;
	}

	public UDDIPublicationPortType getUDDIPublishService(String endpointURL) throws TransportException {
		try {
			if (publishService == null) {

				if (endpointURL == null) {
					endpointURL = baseUDDIServicesUrl + "/publish";
				}
				UDDIService service = new UDDIService();
				publishService = service.getUDDIPublicationPort();
			}

			if (endpointURL != null) {
				Map<String, Object> requestContext = ((BindingProvider) publishService).getRequestContext();
				requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			}
		} catch (Exception e) {
			throw new TransportException(e.getMessage(), e);
		}
		return publishService;
	}

	public UDDISubscriptionPortType getUDDISubscriptionService(String endpointURL) throws TransportException {
		try {
			if (subscriptionService == null) {
				if (endpointURL == null) {
					endpointURL = baseUDDIServicesUrl + "/subscription";
				}
				UDDIService service = new UDDIService();
				subscriptionService = service.getUDDISubscriptionPort();
			}

			if (endpointURL != null) {
				Map<String, Object> requestContext = ((BindingProvider) subscriptionService).getRequestContext();
				requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			}
		} catch (Exception e) {
			throw new TransportException(e.getMessage(), e);
		}
		return subscriptionService;
	}

	public UDDISubscriptionListenerPortType getUDDISubscriptionListenerService(String endpointURL) throws TransportException {
		try {
			if (subscriptionListenerService == null) {
				if (endpointURL == null) {
					endpointURL = baseUDDIServicesUrl + "/subscription-listener";
				}
				UDDIService service = new UDDIService();
				subscriptionListenerService = service.getUDDISubscriptionListenerPort();
			}

			if (endpointURL != null) {
				Map<String, Object> requestContext = ((BindingProvider) subscriptionListenerService).getRequestContext();
				requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			}
		} catch (Exception e) {
			throw new TransportException(e.getMessage(), e);
		}
		return subscriptionListenerService;
	}

	public UDDICustodyTransferPortType getUDDICustodyTransferService(String endpointURL) throws TransportException {
		try {
			if (custodyTransferService == null) {
				if (endpointURL == null) {
					endpointURL = baseUDDIServicesUrl + "/custody-transfer";
				}
				UDDIService service = new UDDIService();
				custodyTransferService = service.getUDDICustodyPort();
			}

			if (endpointURL != null) {
				Map<String, Object> requestContext = ((BindingProvider) custodyTransferService).getRequestContext();
				requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			}
		} catch (Exception e) {
			throw new TransportException(e.getMessage(), e);
		}
		return custodyTransferService;
	}

	public JUDDIApiPortType getJUDDIApiService(String endpointURL) throws TransportException {
		try {
			if (publisherService == null) {
				if (endpointURL == null) {
					endpointURL = baseUDDIServicesUrl + "/juddi-api";
				}
				JUDDIApiService service = new JUDDIApiService();
				publisherService = (JUDDIApiPortType) service.getPort(JUDDIApiPortType.class);
			}

			if (endpointURL != null) {
				Map<String, Object> requestContext = ((BindingProvider) publisherService).getRequestContext();
				requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			}
		} catch (Exception e) {
			throw new TransportException(e.getMessage(), e);
		}
		return publisherService;
	}

}
