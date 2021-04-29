package br.ufsc.gsigma.services.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.ws.ServiceClient;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessContract;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessRequest;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessToken;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceEndpointInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ServiceEndpointInfo.class);

	private String serviceKey;

	private String bindingTemplateKey;

	private String serviceEndpointURL;

	private String serviceEndpointHostPort;

	private String serviceClassification;

	private String serviceNamespace;

	private Double serviceUtility;

	private Map<String, Double> qoSValues = new HashMap<String, Double>();

	private boolean adhoc = false;

	private String serviceProviderName;

	private String serviceProtocolConverter;

	private String serviceAccessToken;

	private static final Map<String, String> serviceAccessTokenCache = new HashMap<String, String>();

	public ServiceEndpointInfo() {
	}

	public ServiceEndpointInfo(String serviceProviderName, String serviceClassification, String serviceNamespace, String serviceKey, String bindingTemplateKey, String serviceEndpointURL, Double serviceUtility, String serviceProtocolConverter) {
		this.serviceProviderName = serviceProviderName;
		this.serviceClassification = serviceClassification;
		this.serviceNamespace = serviceNamespace;
		this.serviceKey = serviceKey;
		this.bindingTemplateKey = bindingTemplateKey;
		this.serviceEndpointURL = serviceEndpointURL;
		this.serviceUtility = serviceUtility;
		this.serviceProtocolConverter = serviceProtocolConverter;
	}

	public ServiceEndpointInfo(String serviceProviderName, String serviceClassification, String serviceNamespace, String serviceKey, String serviceEndpointURL, boolean adhoc) {
		this.serviceProviderName = serviceProviderName;
		this.serviceClassification = serviceClassification;
		this.serviceNamespace = serviceNamespace;
		this.serviceKey = serviceKey;
		this.serviceEndpointURL = serviceEndpointURL;
		this.adhoc = adhoc;
	}

	public static String getServiceAccessToken(ServiceEndpointInfo serviceEndpointInfo, ServiceAccessContract serviceClient, ServiceAccessRequest serviceAccessRequest) {

		String token = serviceAccessTokenCache.get(serviceEndpointInfo.getServiceKey());

		if (token == null) {
			String serviceEndpointURL = serviceEndpointInfo.getServiceEndpointURL();

			try {
				ServiceClient.changeEndpointAddress(serviceClient, serviceEndpointURL);
				ServiceAccessToken resp = serviceClient.requestAccess(serviceAccessRequest);
				serviceAccessTokenCache.put(serviceEndpointInfo.getServiceKey(), resp.getToken());
				logger.info("Obtained access token for " + serviceEndpointURL + " --> " + resp.getToken());
			} catch (Exception e) {
				logger.warn("Can't obtain access token for " + serviceEndpointURL + " --> " + e.getMessage());
				throw new RuntimeException(e);
			}
		}

		return token;
	}

	public String getServiceProtocolConverter() {
		return serviceProtocolConverter;
	}

	public void setServiceProtocolConverter(String serviceProtocolConverter) {
		this.serviceProtocolConverter = serviceProtocolConverter;
	}

	public String getServiceProviderName() {
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName) {
		this.serviceProviderName = serviceProviderName;
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getBindingTemplateKey() {
		return bindingTemplateKey;
	}

	public void setBindingTemplateKey(String bindingTemplateKey) {
		this.bindingTemplateKey = bindingTemplateKey;
	}

	public String getServiceEndpointURL() {
		return serviceEndpointURL;
	}

	public void setServiceEndpointURL(String serviceEndpointURL) {
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public String getServiceEndpointHostPort() {

		if (serviceEndpointHostPort == null && serviceEndpointURL != null) {
			try {
				URL url = new URL(serviceEndpointURL);
				this.serviceEndpointHostPort = url.getHost() + ":" + url.getPort();
			} catch (MalformedURLException e) {
			}
		}

		return serviceEndpointHostPort;
	}

	public void setServiceEndpointHostPort(String serviceEndpointHostPort) {
		this.serviceEndpointHostPort = serviceEndpointHostPort;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public Double getServiceUtility() {
		return serviceUtility;
	}

	public void setServiceUtility(Double serviceUtility) {
		this.serviceUtility = serviceUtility;
	}

	public Map<String, Double> getQoSValues() {
		return qoSValues;
	}

	public void setQoSValues(Map<String, Double> qoSValues) {
		this.qoSValues = qoSValues;
	}

	public boolean isAdhoc() {
		return adhoc;
	}

	public void setAdhoc(boolean adhoc) {
		this.adhoc = adhoc;
	}

	public String getServiceAccessToken() {
		return serviceAccessToken;
	}

	public void setServiceAccessToken(String serviceAccessToken) {
		this.serviceAccessToken = serviceAccessToken;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serviceKey == null) ? 0 : serviceKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceEndpointInfo other = (ServiceEndpointInfo) obj;
		if (serviceKey == null) {
			if (other.serviceKey != null)
				return false;
		} else if (!serviceKey.equals(other.serviceKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[serviceUtility=" + serviceUtility + ", serviceKey=" + serviceKey + ", bindingTemplateKey=" + bindingTemplateKey + ", endpoint=" + serviceEndpointURL + "]";
	}

}
