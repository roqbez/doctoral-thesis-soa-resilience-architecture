package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "serviceProviderName", "serviceClassification", "serviceUtility", "serviceProviderUtility", "alias", "serviceKey", "bindingTemplateKey" })
public class ServiceInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String serviceKey;

	@XmlAttribute
	private String serviceClassification;

	@XmlAttribute
	private String bindingTemplateKey;

	@XmlAttribute
	private String alias;

	@XmlAttribute
	private String serviceProviderName;

	@XmlAttribute
	private Double serviceUtility;

	@XmlAttribute
	private Double serviceProviderUtility;

	@XmlAttribute
	private String serviceProtocolConverter;

	public ServiceInfo() {
	}

	public ServiceInfo(String serviceKey, String serviceClassification, String bindingTemplateKey, Double serviceUtility, String serviceProviderName, Double serviceProviderUtility, String serviceProtocolConverter) {
		this.serviceKey = serviceKey;
		this.serviceClassification = serviceClassification;
		this.bindingTemplateKey = bindingTemplateKey;
		this.serviceUtility = serviceUtility;
		this.serviceProviderName = serviceProviderName;
		this.serviceProviderUtility = serviceProviderUtility;
		this.serviceProtocolConverter = serviceProtocolConverter;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
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

	public String getServiceProviderName() {
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName) {
		this.serviceProviderName = serviceProviderName;
	}

	public Double getServiceProviderUtility() {
		return serviceProviderUtility;
	}

	public void setServiceProviderUtility(Double serviceProviderUtility) {
		this.serviceProviderUtility = serviceProviderUtility;
	}

	public String getServiceProtocolConverter() {
		return serviceProtocolConverter;
	}

	public void setServiceProtocolConverter(String serviceProtocolConverter) {
		this.serviceProtocolConverter = serviceProtocolConverter;
	}

	public String getBindingTemplateKey() {
		return bindingTemplateKey;
	}

	public void setBindingTemplateKey(String bindingTemplateKey) {
		this.bindingTemplateKey = bindingTemplateKey;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
