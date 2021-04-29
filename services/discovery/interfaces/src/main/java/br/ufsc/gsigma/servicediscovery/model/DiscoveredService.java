package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;

public class DiscoveredService implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private String serviceKey;

	private String bindingTemplateKey;

	private String uddiRepositoryEndpointURL;

	private String serviceName;

	private String serviceEndpointURL;

	private String serviceClassification;

	private String serviceEndpointHostPort;

	private String serviceProtocolConverter = "br.ufsc.gsigma.binding.converters.SoapServiceProtocolConverter";

	private ServiceProvider serviceProvider;

	private List<ServiceQoSInformationItem> qoSInformation = new ArrayList<ServiceQoSInformationItem>();

	private Double utility;

	public Double getQoSValue(String qoSKey) {

		for (ServiceQoSInformationItem sq : qoSInformation) {
			if (sq.getQoSKey().equals(qoSKey))
				return sq.getQoSValue();
		}
		return null;
	}

	public DiscoveredService() {
	}

	public DiscoveredService(String serviceKey, String bindingTemplateKey, String uddiRepositoryEndpointURL, String serviceName, String serviceEndpointURL, String serviceClassification) {
		this.serviceKey = serviceKey;
		this.bindingTemplateKey = bindingTemplateKey;
		this.uddiRepositoryEndpointURL = uddiRepositoryEndpointURL;
		this.serviceName = serviceName;
		this.serviceEndpointURL = serviceEndpointURL;
		this.serviceClassification = serviceClassification;
	}

	@XmlElementWrapper(name = "serviceQoS")
	public List<ServiceQoSInformationItem> getQoSInformation() {
		return qoSInformation;
	}

	public void setQoSInformation(List<ServiceQoSInformationItem> qoSInformation) {
		this.qoSInformation = qoSInformation;
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

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getServiceProtocolConverter() {
		return serviceProtocolConverter;
	}

	public void setServiceProtocolConverter(String serviceProtocolConverter) {
		this.serviceProtocolConverter = serviceProtocolConverter;
	}

	public String getUddiRepositoryEndpointURL() {
		return uddiRepositoryEndpointURL;
	}

	public void setUddiRepositoryEndpointURL(String uddiRepositoryEndpointURL) {
		this.uddiRepositoryEndpointURL = uddiRepositoryEndpointURL;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceEndpointURL() {
		return serviceEndpointURL;
	}

	public void setServiceEndpointURL(String serviceEndpointURL) {
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public String getBindingTemplateKey() {
		return bindingTemplateKey;
	}

	public void setBindingTemplateKey(String bindingTemplateKey) {
		this.bindingTemplateKey = bindingTemplateKey;
	}

	public Double getUtility() {
		return utility;
	}

	public void setUtility(Double utility) {
		this.utility = utility;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		DiscoveredService service = new DiscoveredService();

		service.setServiceKey(getServiceKey());
		service.setBindingTemplateKey(getBindingTemplateKey());
		service.setUddiRepositoryEndpointURL(getUddiRepositoryEndpointURL());
		service.setServiceName(getServiceName());
		service.setServiceEndpointURL(getServiceEndpointURL());
		service.setServiceClassification(getServiceClassification());
		service.setServiceProvider(getServiceProvider());
		service.setQoSInformation(getQoSInformation());

		return service;
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
		DiscoveredService other = (DiscoveredService) obj;
		if (serviceKey == null) {
			if (other.serviceKey != null)
				return false;
		} else if (!serviceKey.equals(other.serviceKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		// return "DiscoveredService ["+serviceKey+"]";
		return "DiscoveredService [" + (utility != null ? "utility=" + utility + ", " : "") + "serviceEndpointURL=" + serviceEndpointURL + ", serviceKey=" + serviceKey + ", qoSInformation=" + qoSInformation + "]";
	}

}
