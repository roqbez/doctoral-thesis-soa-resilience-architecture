package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceQoSInformation implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private String serviceKey;

	@XmlElementWrapper
	@XmlElement(name = "qoSItem")
	private List<ServiceQoSInformationItem> qoSItens = new LinkedList<ServiceQoSInformationItem>();

	public ServiceQoSInformation() {
	}

	public ServiceQoSInformation(String serviceKey, List<ServiceQoSInformationItem> qoSItens) {
		this.serviceKey = serviceKey;
		this.qoSItens = qoSItens;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public List<ServiceQoSInformationItem> getQoSItens() {
		return qoSItens;
	}

	public void setQoSItens(List<ServiceQoSInformationItem> qoSItens) {
		this.qoSItens = qoSItens;
	}

	@Override
	public String toString() {
		return serviceKey + " " + qoSItens;
	}

}
