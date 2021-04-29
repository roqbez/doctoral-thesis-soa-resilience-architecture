package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class QoSItem implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String name;

	private String description;

	private List<QoSAttribute> qoSAttributes = new ArrayList<QoSAttribute>();

	public QoSItem() {

	}

	public QoSItem(Integer id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	@Override
	public Object clone() {
		QoSItem c = new QoSItem();
		c.setId(id);
		c.setName(name);
		c.setDescription(description);
		if (qoSAttributes != null)
			c.setQoSAttributes(new LinkedList<QoSAttribute>(qoSAttributes));
		return c;
	}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<QoSAttribute> getQoSAttributes() {
		return qoSAttributes;
	}

	public void setQoSAttributes(List<QoSAttribute> qoSAttributes) {
		this.qoSAttributes = qoSAttributes;
	}

	@Override
	public String toString() {
		return id + " - " + name + " - " + description;
	}

}
