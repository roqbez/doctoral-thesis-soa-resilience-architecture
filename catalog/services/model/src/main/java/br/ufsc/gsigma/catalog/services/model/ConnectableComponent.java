package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Root
public abstract class ConnectableComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GenericGenerator(name = "pk_max", strategy = "increment")
	@GeneratedValue(generator = "pk_max")
	@Attribute(required = false)
	protected Integer id;

	@Attribute
	@Column(nullable = false)
	protected String name;

	@OneToMany(mappedBy = "connectableComponent", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "inputContactPoint")
	@ElementList
	protected List<InputContactPoint> inputContactPoints = new ArrayList<InputContactPoint>();

	@OneToMany(mappedBy = "connectableComponent", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@XmlElementWrapper
	@XmlElement(name = "outputContactPoint")
	@ElementList
	protected List<OutputContactPoint> outputContactPoints = new ArrayList<OutputContactPoint>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<InputContactPoint> getInputContactPoints() {
		return inputContactPoints;
	}

	public void setInputContactPoints(List<InputContactPoint> inputContactPoints) {
		this.inputContactPoints = inputContactPoints;
	}

	public List<OutputContactPoint> getOutputContactPoints() {
		return outputContactPoints;
	}

	public void setOutputContactPoints(List<OutputContactPoint> outputContactPoints) {
		this.outputContactPoints = outputContactPoints;
	}

	public abstract ConnectableComponent buildDefaultContactPoints();

	public ConnectableComponent setContactPoints(String[] inputs, String[] outputs) {
		inputContactPoints.clear();
		outputContactPoints.clear();

		for (String s : inputs)
			inputContactPoints.add(new InputContactPoint(this, s));

		for (String s : outputs)
			outputContactPoints.add(new OutputContactPoint(this, s));

		return this;
	}

	public OutputContactPoint getOutputContactPointByName(String name) {
		for (OutputContactPoint contactPoint : outputContactPoints)
			if (contactPoint.getName().equals(name))
				return contactPoint;
		return null;
	}

	public InputContactPoint getInputContactPointByName(String name) {
		for (InputContactPoint contactPoint : inputContactPoints)
			if (contactPoint.getName().equals(name))
				return contactPoint;
		return null;
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
		ConnectableComponent other = (ConnectableComponent) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}
