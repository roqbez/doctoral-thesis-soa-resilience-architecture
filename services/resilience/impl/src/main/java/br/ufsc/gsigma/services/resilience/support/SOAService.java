package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import br.ufsc.gsigma.services.resilience.impl.ResilienceServiceImpl;

public class SOAService implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serviceClassification;

	private String serviceNamespace;

	private String serviceKey;

	private String bindingTemplateKey;

	private String serviceEndpointURL;

	private Date lastAvailabilityCheck;

	private Boolean available;

	public void updateCache() {
		InfinispanCaches.getInstance().getSOAServices().put(serviceKey, this);
	}

	public Collection<SOAApplication> getSOAApplications() {
		return ResilienceServiceImpl.getInstance().getSOAApplications(this);
	}

	public SOAService(String serviceClassification, String serviceNamespace, String serviceKey, String bindingTemplateKey, String serviceEndpointURL) {
		this.serviceClassification = serviceClassification;
		this.serviceNamespace = serviceNamespace;
		this.serviceKey = serviceKey;
		this.bindingTemplateKey = bindingTemplateKey;
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public SOAService( String serviceEndpointURL, String serviceNamespace) {
		this.serviceEndpointURL = serviceEndpointURL;
		this.serviceNamespace = serviceNamespace;
	}
	
	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getServiceEndpointURL() {
		return serviceEndpointURL;
	}

	public void setServiceEndpointURL(String serviceEndpointURL) {
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
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

	public Date getLastAvailabilityCheck() {
		return lastAvailabilityCheck;
	}

	public void setLastAvailabilityCheck(Date lastAvailabilityCheck) {
		this.lastAvailabilityCheck = lastAvailabilityCheck;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
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
		SOAService other = (SOAService) obj;
		if (serviceKey == null) {
			if (other.serviceKey != null)
				return false;
		} else if (!serviceKey.equals(other.serviceKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SOAService [serviceEndpointURL=" + serviceEndpointURL + ", serviceKey=" + serviceKey + ", serviceNamespace=" + serviceNamespace + ", bindingTemplateKey=" + bindingTemplateKey
				+ ", serviceClassification=" + serviceClassification + ", lastAvailabilityCheck=" + lastAvailabilityCheck + ", available=" + available + "]";
	}

}
