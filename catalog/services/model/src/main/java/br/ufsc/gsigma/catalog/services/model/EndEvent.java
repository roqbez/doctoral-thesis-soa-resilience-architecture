package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "end_event")
@XmlRootElement(name = "endEvent")
public class EndEvent extends Event implements Serializable {

	private static final long serialVersionUID = 1L;

	public EndEvent() {
	}

	public EndEvent(Process process, String name) {
		super(process, name);
	}

	@Override
	public ConnectableComponent buildDefaultContactPoints() {
		inputContactPoints.add(new InputContactPoint(this, "Input"));
		return this;
	}

}
