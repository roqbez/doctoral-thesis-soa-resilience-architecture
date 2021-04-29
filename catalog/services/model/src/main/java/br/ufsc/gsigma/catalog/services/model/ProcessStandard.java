package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "process_standard")
@Root
public class ProcessStandard implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Attribute
	private String id;

	@Attribute
	@Column(nullable=false)
	private String name;

	public ProcessStandard() {

	}

	public ProcessStandard(String id, String name) {
		this.id = id;
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
		ProcessStandard other = (ProcessStandard) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
