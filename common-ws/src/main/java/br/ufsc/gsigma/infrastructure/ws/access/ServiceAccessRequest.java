package br.ufsc.gsigma.infrastructure.ws.access;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceAccessRequest", namespace = "http://gsigma.ufsc.br")
public class ServiceAccessRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String clientId;

	private String applicationId;

	public ServiceAccessRequest() {
	}

	public ServiceAccessRequest(String clientId, String applicationId) {
		this.clientId = clientId;
		this.applicationId = applicationId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

}
