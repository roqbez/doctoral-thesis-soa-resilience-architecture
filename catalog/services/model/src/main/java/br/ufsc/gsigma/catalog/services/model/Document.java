package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "document")
@Root
public class Document implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Attribute
	private String id;

	@Attribute
	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Attribute
	private boolean primitive = false;

	@Attribute(required = false)
	private String xmlNamespace;

	@Attribute(required = false)
	private String xmlName;

	@ManyToOne
	@JoinColumn(name = "id_process_standard")
	@Element(required = false)
	private ProcessStandard processStandard;

	public Document() {

	}

	public Document(String name) {
		this.id = name.toUpperCase().replaceAll(" ", "_");
		this.name = name;
	}

	public Document(String name, boolean primitive) {
		this(name);
		this.primitive = primitive;
	}

	public Document(String name, String xmlNamespace, String xmlName, ProcessStandard processStandard) {
		this(name);
		this.xmlNamespace = xmlNamespace;
		this.xmlName = xmlName;
		this.processStandard = processStandard;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXmlNamespace() {
		return xmlNamespace;
	}

	public void setXmlNamespace(String xmlNamespace) {
		this.xmlNamespace = xmlNamespace;
	}

	public String getXmlName() {
		return xmlName;
	}

	public void setXmlName(String xmlName) {
		this.xmlName = xmlName;
	}

	public ProcessStandard getProcessStandard() {
		return processStandard;
	}

	public void setProcessStandard(ProcessStandard processStandard) {
		this.processStandard = processStandard;
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public void setPrimitive(boolean primitive) {
		this.primitive = primitive;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Document other = (Document) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
