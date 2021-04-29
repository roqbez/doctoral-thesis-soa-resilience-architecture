package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "identification", "name" })
public class ServiceProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String identification;

	@XmlAttribute
	private String name;

	@XmlAttribute
	private Double utility;

	public ServiceProvider() {

	}

	public ServiceProvider(String identification, String name) {
		this.identification = identification;
		this.name = name;
	}

	public ServiceProvider(String identification) {
		this.identification = identification;
	}

	public ServiceProvider(String identification, String name, Double utility) {
		this.identification = identification;
		this.name = name;
		this.utility = utility;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getUtility() {
		return utility;
	}

	public void setUtility(Double utility) {
		this.utility = utility;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identification == null) ? 0 : identification.hashCode());
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
		ServiceProvider other = (ServiceProvider) obj;
		if (identification == null) {
			if (other.identification != null)
				return false;
		} else if (!identification.equals(other.identification))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServiceProvider [identification=" + identification + ", name=" + name + "]";
	}

}
