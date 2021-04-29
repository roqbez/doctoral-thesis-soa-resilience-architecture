package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class BMServiceAssociation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String serviceKey;

	@Attribute
	private String bindingTemplateKey;

	@Attribute(required = false)
	private String serviceName;

	@Attribute(required = false)
	private String alias;

	@Attribute(required = false)
	private String serviceProviderName;

	@Attribute(required = false)
	private Double serviceProviderReputation;

	@Attribute(required = false)
	private String serviceEndpoint;

	@Attribute(required = false)
	private String serviceProtocolConverter;

	@Attribute(required = false)
	private Double serviceUtility;

	@ElementList(name = "QoS", required = false)
	private List<BMQoSValue> qoSValues;

	public BMServiceAssociation() {
	}

	public BMServiceAssociation(String serviceKey, String bindingTemplateKey, String alias, String serviceProtocolConverter) {
		this.serviceKey = serviceKey;
		this.bindingTemplateKey = bindingTemplateKey;
		this.alias = alias;
		this.serviceProtocolConverter = serviceProtocolConverter;
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

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getServiceProviderName() {
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName) {
		this.serviceProviderName = serviceProviderName;
	}

	public Double getServiceProviderReputation() {
		return serviceProviderReputation;
	}

	public void setServiceProviderReputation(Double serviceProviderReputation) {
		this.serviceProviderReputation = serviceProviderReputation;
	}

	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	public String getServiceProtocolConverter() {
		return serviceProtocolConverter;
	}

	public void setServiceProtocolConverter(String serviceProtocolConverter) {
		this.serviceProtocolConverter = serviceProtocolConverter;
	}

	public Double getServiceUtility() {
		return serviceUtility;
	}

	public void setServiceUtility(Double serviceUtility) {
		this.serviceUtility = serviceUtility;
	}

	public List<BMQoSValue> getQoSValues() {
		return qoSValues;
	}

	public void setQoSValues(List<BMQoSValue> qoSValues) {
		this.qoSValues = qoSValues;
	}

}
