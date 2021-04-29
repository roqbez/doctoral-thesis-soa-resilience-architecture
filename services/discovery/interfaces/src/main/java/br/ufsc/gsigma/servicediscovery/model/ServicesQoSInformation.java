package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServicesQoSInformation implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElementWrapper
	@XmlElement(name = "serviceQoSInformation")
	private List<ServiceQoSInformation> servicesQoSInformation = new LinkedList<ServiceQoSInformation>();

	public List<ServiceQoSInformation> getServicesQoSInformation() {
		return servicesQoSInformation;
	}

	public void setServicesQoSInformation(List<ServiceQoSInformation> servicesQoSInformation) {
		this.servicesQoSInformation = servicesQoSInformation;
	}

}
