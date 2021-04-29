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
@Table(name = "resource")
@Root
public class Resource implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Attribute
	private String id;

	@Attribute
	@Column(nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "id_process_standard")
	@Element
	private ProcessStandard processStandard;

	public Resource() {

	}

	public Resource(String name) {
		this.id = name.toUpperCase().replaceAll(" ", "_");
		this.name = name;
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

	public ProcessStandard getProcessStandard() {
		return processStandard;
	}

	public void setProcessStandard(ProcessStandard processStandard) {
		this.processStandard = processStandard;
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
		Resource other = (Resource) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
