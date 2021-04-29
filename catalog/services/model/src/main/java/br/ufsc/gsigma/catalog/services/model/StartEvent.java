package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "start_event")
@XmlRootElement(name = "startEvent")
public class StartEvent extends Event implements Serializable {

	private static final long serialVersionUID = 1L;

	public StartEvent() {
		super();
	}

	public StartEvent(Process process, String name) {
		super(process, name);
	}

	@Override
	public ConnectableComponent buildDefaultContactPoints() {
		outputContactPoints.add(new OutputContactPoint(this, "Output"));
		return this;
	}

}
