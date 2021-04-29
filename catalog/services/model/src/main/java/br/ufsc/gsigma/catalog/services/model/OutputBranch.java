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
@Table(name = "output_branch")
@Root
public class OutputBranch implements Serializable {

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
	@JoinTable(name = "output_branch_output_contact_points", joinColumns = @JoinColumn(name = "id_output_branch", insertable = true, updatable = true), inverseJoinColumns = @JoinColumn(name = "id_output_contact_point", insertable = true, updatable = true))
	@XmlElementWrapper
	@XmlElement(name = "outputContactPoint")
	@ElementList
	private List<OutputContactPoint> outputContactPoints = new ArrayList<OutputContactPoint>();

	@Attribute(required = false)
	private String condition;

	@Attribute(required = false)
	private Float probabilityPercentage;

	public OutputBranch() {

	}

	public OutputBranch(FlowControlComponent flowControlComponent, String name) {
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

	public List<OutputContactPoint> getOutputContactPoints() {
		return outputContactPoints;
	}

	public void setOutputContactPoints(List<OutputContactPoint> outputContactPoints) {
		this.outputContactPoints = outputContactPoints;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Float getProbabilityPercentage() {
		return probabilityPercentage;
	}

	public void setProbabilityPercentage(Float probabilityPercentage) {
		this.probabilityPercentage = probabilityPercentage;
	}

}
