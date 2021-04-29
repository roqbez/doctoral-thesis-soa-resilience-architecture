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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "input_branch")
@Root
public class InputBranch implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Attribute(required = false)
	private Integer id;

	@Attribute
	@Column(nullable = false)
	private String name;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_flow_control_component", nullable = false)
	@Element(required = false)
	private FlowControlComponent flowControlComponent;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "input_branch_input_contact_points", joinColumns = @JoinColumn(name = "id_input_branch", insertable = true, updatable = true), inverseJoinColumns = @JoinColumn(name = "id_input_contact_point", insertable = true, updatable = true))
	@XmlElementWrapper
	@XmlElement(name = "inputContactPoint")
	@ElementList
	private List<InputContactPoint> inputContactPoints = new ArrayList<InputContactPoint>();

	public InputBranch() {

	}

	public InputBranch(FlowControlComponent flowControlComponent, String name) {
		this.flowControlComponent = flowControlComponent;
		this.name = name;
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

	public FlowControlComponent getFlowControlComponent() {
		return flowControlComponent;
	}

	public void setFlowControlComponent(FlowControlComponent flowControlComponent) {
		this.flowControlComponent = flowControlComponent;
	}

	public List<InputContactPoint> getInputContactPoints() {
		return inputContactPoints;
	}

	public void setInputContactPoints(List<InputContactPoint> inputContactPoints) {
		this.inputContactPoints = inputContactPoints;
	}

}
