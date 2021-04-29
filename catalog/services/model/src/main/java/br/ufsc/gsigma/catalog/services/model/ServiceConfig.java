package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import br.ufsc.gsigma.common.hash.HashUtil.NotHash;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String namespace;

	@XmlAttribute
	private String classification;

	@XmlAttribute
	private String partnerLinkName;

	@NotHash
	@XmlAttribute
	private Integer maxNumberOfServices;

	@NotHash
	@XmlAttribute
	private Integer numberOfReplicas;

	@NotHash
	private ExecutionContext executionContext;

	@NotHash
	@XmlElementWrapper
	@XmlElement(name = "qoSCriterion")
	private List<QoSCriterion> qoSCriterions = new LinkedList<QoSCriterion>();

	@NotHash
	@XmlElementWrapper
	@XmlElement(name = "qoSCriterion")
	private List<QoSCriterion> managedQoSCriterions = new LinkedList<QoSCriterion>();

	@NotHash
	@XmlElementWrapper
	@XmlElement(name = "serviceAssociation")
	private List<ServiceAssociation> serviceAssociations = new LinkedList<ServiceAssociation>();

	public ServiceConfig() {
	}

	public ServiceConfig(String classification) {
		this.classification = classification;
	}

	public ServiceConfig(String namespace, String classification, String partnerLinkName) {
		this.namespace = namespace;
		this.classification = classification;
		this.partnerLinkName = partnerLinkName;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getPartnerLinkName() {
		return partnerLinkName;
	}

	public void setPartnerLinkName(String partnerLinkName) {
		this.partnerLinkName = partnerLinkName;
	}

	public Integer getMaxNumberOfServices() {
		return maxNumberOfServices;
	}

	public void setMaxNumberOfServices(Integer maxNumberOfServices) {
		this.maxNumberOfServices = maxNumberOfServices;
	}

	public Integer getNumberOfReplicas() {
		return numberOfReplicas;
	}

	public void setNumberOfReplicas(Integer numberOfReplicas) {
		this.numberOfReplicas = numberOfReplicas;
	}

	public List<QoSCriterion> getQoSCriterions() {
		return qoSCriterions;
	}

	public void setQoSCriterions(List<QoSCriterion> qoSCriterions) {
		this.qoSCriterions = qoSCriterions;
	}

	public List<QoSCriterion> getManagedQoSCriterions() {
		return managedQoSCriterions;
	}

	public void setManagedQoSCriterions(List<QoSCriterion> managedQoSCriterions) {
		this.managedQoSCriterions = managedQoSCriterions;
	}

	public List<ServiceAssociation> getServiceAssociations() {
		return serviceAssociations;
	}

	public void setServiceAssociations(List<ServiceAssociation> serviceAssociations) {
		this.serviceAssociations = serviceAssociations;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classification == null) ? 0 : classification.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
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
		ServiceConfig other = (ServiceConfig) obj;
		if (classification == null) {
			if (other.classification != null)
				return false;
		} else if (!classification.equals(other.classification))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[namespace=" + namespace + ", classification=" + classification + ", partnerLinkName=" + partnerLinkName + ", maxNumberOfServices=" + maxNumberOfServices + "]";
	}

}
