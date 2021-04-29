package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class DiscoveryRequestItem implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String serviceProvider;

	@XmlAttribute
	private String participantName;

	@XmlAttribute
	private String serviceClassification;

	@XmlAttribute
	private String serviceEndpointURL;

	@XmlAttribute
	private Integer maxResults;

	private QoSInformation qoSInformation;

	@XmlElement(name = "serviceKey")
	private List<String> serviceKeys;

	@XmlElement(name = "excludeServiceKey")
	private List<String> excludeServiceKeys;

	@XmlElement(name = "excludeServiceEndpointHostPort")
	private List<String> excludeServiceEndpointsHostPort;

	public DiscoveryRequestItem() {
	}

	public DiscoveryRequestItem(List<String> serviceKeys) {
		this.serviceKeys = serviceKeys;
	}

	public DiscoveryRequestItem(String serviceClassification, QoSInformation qoSInformation) {
		this.serviceClassification = serviceClassification;
		this.qoSInformation = qoSInformation;
	}

	public DiscoveryRequestItem(String serviceClassification, QoSInformation qoSInformation, Integer maxResults) {
		this.serviceClassification = serviceClassification;
		this.qoSInformation = qoSInformation;
		this.maxResults = maxResults;
	}

	public DiscoveryRequestItem(String serviceClassification, Integer maxResults) {
		this.serviceClassification = serviceClassification;
		this.maxResults = maxResults;
	}

	public DiscoveryRequestItem(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public DiscoveryRequestItem(String serviceClassification, String serviceEndpointURL) {
		this.serviceClassification = serviceClassification;
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public void excludeServiceKey(String serviceKey) {
		if (this.excludeServiceKeys == null)
			this.excludeServiceKeys = new LinkedList<String>();
		this.excludeServiceKeys.add(serviceKey);
	}

	public void excludeServiceKeys(Collection<String> serviceKeys) {
		if (this.excludeServiceKeys == null)
			this.excludeServiceKeys = new LinkedList<String>();
		this.excludeServiceKeys.addAll(serviceKeys);
	}

	public void excludeServiceEndpointHostPort(String serviceEndpointHostPort) {
		if (this.excludeServiceEndpointsHostPort == null)
			this.excludeServiceEndpointsHostPort = new LinkedList<String>();
		this.excludeServiceEndpointsHostPort.add(serviceEndpointHostPort);
	}

	public void excludeServiceEndpointHostPorts(Collection<String> serviceEndpointsHostPort) {
		if (this.excludeServiceEndpointsHostPort == null)
			this.excludeServiceEndpointsHostPort = new LinkedList<String>();
		this.excludeServiceEndpointsHostPort.addAll(serviceEndpointsHostPort);
	}

	public String getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(String serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public List<String> getExcludeServiceKeys() {
		return excludeServiceKeys;
	}

	public void setExcludeServiceKeys(List<String> excludeServiceKeys) {
		this.excludeServiceKeys = excludeServiceKeys;
	}

	public List<String> getExcludeServiceEndpointsHostPort() {
		return excludeServiceEndpointsHostPort;
	}

	public void setExcludeServiceEndpointsHostPort(List<String> excludeServiceEndpointsHostPort) {
		this.excludeServiceEndpointsHostPort = excludeServiceEndpointsHostPort;
	}

	public List<String> getServiceKeys() {
		return serviceKeys;
	}

	public void setServiceKeys(List<String> serviceKeys) {
		this.serviceKeys = serviceKeys;
	}

	public String getServiceClassification() {
		return serviceClassification;
	}

	public void setServiceClassification(String serviceClassification) {
		this.serviceClassification = serviceClassification;
	}

	public String getServiceEndpointURL() {
		return serviceEndpointURL;
	}

	public void setServiceEndpointURL(String serviceEndpointURL) {
		this.serviceEndpointURL = serviceEndpointURL;
	}

	public QoSInformation getQoSInformation() {
		return qoSInformation;
	}

	public void setQoSInformation(QoSInformation qoSInformation) {
		this.qoSInformation = qoSInformation;
	}

	public void setQoSConstraints(List<QoSConstraint> qoSConstraints) {
		if (qoSInformation == null) {
			qoSInformation = new QoSInformation();
		}
		qoSInformation.setQoSConstraints(qoSConstraints);
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serviceClassification == null) ? 0 : serviceClassification.hashCode());
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
		DiscoveryRequestItem other = (DiscoveryRequestItem) obj;
		if (serviceClassification == null) {
			if (other.serviceClassification != null)
				return false;
		} else if (!serviceClassification.equals(other.serviceClassification))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return serviceClassification + (maxResults != null ? " (" + maxResults + ")" : "") + (participantName != null ? " (" + participantName + ")" : "") + (qoSInformation != null ? " --> " + qoSInformation : "");
	}

}
