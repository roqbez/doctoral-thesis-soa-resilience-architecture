package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "input_contact_point")
@Root
public class InputContactPoint implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Attribute
	private Integer id;

	@Attribute
	@Column(nullable = false)
	private String name;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_connectable_component")
	@Element(required = false)
	private ConnectableComponent connectableComponent;

	@Transient
	private String connectableComponentName;

	@ManyToOne
	@JoinColumn(name = "id_document")
	@Element(required = false)
	private Document associatedDocument;

	public InputContactPoint() {

	}

	public InputContactPoint(ConnectableComponent connectableComponent, String name) {
		setConnectableComponent(connectableComponent);
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ConnectableComponent getConnectableComponent() {
		return connectableComponent;
	}

	public void setConnectableComponent(ConnectableComponent connectableComponent) {
		this.connectableComponent = connectableComponent;
		this.connectableComponentName = connectableComponent.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConnectableComponentName() {
		return connectableComponentName;
	}

	public void setConnectableComponentName(String connectableComponentName) {
		this.connectableComponentName = connectableComponentName;
	}

	public Document getAssociatedDocument() {
		return associatedDocument;
	}

	public void setAssociatedDocument(Document associatedDocument) {
		this.associatedDocument = associatedDocument;
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
		InputContactPoint other = (InputContactPoint) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + "(" + connectableComponentName + ")";
	}

}
