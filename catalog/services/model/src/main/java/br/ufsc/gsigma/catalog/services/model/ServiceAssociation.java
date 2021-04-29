package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "serviceProviderName", "serviceEndpoint", "serviceUtility", "serviceKey", "id", "serviceName", "bindingTemplateKey" })
@Root
@Entity
@Table(name = "service_association")
public class ServiceAssociation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private String serviceName;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private String serviceEndpoint;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private String serviceKey;

	@XmlAttribute
	@Attribute
	@Column(nullable = false)
	private String bindingTemplateKey;

	@XmlAttribute
	@Transient
	@Attribute
	private Double serviceUtility;

	@XmlAttribute
	@Transient
	@Attribute
	private String serviceProviderName;

	@XmlAttribute
	@Transient
	@Attribute
	private String serviceProtocolConverter;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_task", nullable = false)
	private Task task;

	public ServiceAssociation() {

	}

	public ServiceAssociation(Integer id, String serviceName, String serviceEndpoint, Double serviceUtility, Task task) {
		this.id = id;
		this.serviceName = serviceName;
		this.serviceEndpoint = serviceEndpoint;
		this.serviceUtility = serviceUtility;
		this.task = task;
	}

	public ServiceAssociation(String serviceName, String serviceEndpoint, Double serviceUtility) {
		this.serviceName = serviceName;
		this.serviceEndpoint = serviceEndpoint;
		this.serviceUtility = serviceUtility;
	}

	private String getServiceProviderNameInternal(String serviceKey) {
		int idx1 = serviceKey.indexOf(':');
		int idx2 = serviceKey.indexOf(':', idx1 + 1);
		return serviceKey.substring(idx1 + 1, idx2);
	}

	public String getServiceProviderName() {
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName) {
		this.serviceProviderName = serviceProviderName;
	}

	public String getServiceProtocolConverter() {
		return serviceProtocolConverter;
	}

	public void setServiceProtocolConverter(String serviceProtocolConverter) {
		this.serviceProtocolConverter = serviceProtocolConverter;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
		this.serviceProviderName = getServiceProviderNameInternal(serviceKey);
	}

	public String getBindingTemplateKey() {
		return bindingTemplateKey;
	}

	public void setBindingTemplateKey(String bindingTemplateKey) {
		this.bindingTemplateKey = bindingTemplateKey;
	}

	public Double getServiceUtility() {
		return serviceUtility;
	}

	public void setServiceUtility(Double serviceUtility) {
		this.serviceUtility = serviceUtility;
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
		ServiceAssociation other = (ServiceAssociation) obj;
		if (serviceKey == null) {
			if (other.serviceKey != null)
				return false;
		} else if (!serviceKey.equals(other.serviceKey))
			return false;
		return true;
	}

}
