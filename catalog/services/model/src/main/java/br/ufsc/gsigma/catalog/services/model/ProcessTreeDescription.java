package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.BatchSize;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "process_tree_description")
public class ProcessTreeDescription implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	private String description;

	@ManyToOne(fetch = FetchType.EAGER)
	@BatchSize(size = 100)
	@JoinColumn(name = "id_category", nullable = false)
	private ProcessCategory category;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_parent")
	private ProcessTreeDescription parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
	private List<ProcessTreeDescription> childs = new ArrayList<ProcessTreeDescription>();

	public ProcessTreeDescription() {
	}

	public ProcessTreeDescription(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public ProcessTreeDescription(String name) {
		this.name = name;
	}

	public ProcessTreeDescription(ProcessTreeDescription parent, String name) {
		this.setParent(parent);
		parent.childs.add(this);
		this.name = name;
	}

	public ProcessTreeDescription(ProcessTreeDescription parent, String name, String description) {
		this.setParent(parent);
		parent.childs.add(this);
		this.name = name;
		this.description = description;
	}

	public List<ProcessTreeDescription> getChilds() {
		return childs;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void addChild(ProcessTreeDescription p) {
		p.setParent(this);
		childs.add(p);
	}

	public ProcessTreeDescription addChild(String name) {
		ProcessTreeDescription p = new ProcessTreeDescription(name);
		p.setParent(this);
		childs.add(p);
		return this;
	}

	public ProcessTreeDescription addChildWithSubChilds(String name) {
		ProcessTreeDescription p = new ProcessTreeDescription(name);
		p.setParent(this);
		childs.add(p);
		return p;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setChilds(List<ProcessTreeDescription> childs) {
		this.childs = childs;
	}

	public ProcessTreeDescription getParent() {
		return parent;
	}

	public void setParent(ProcessTreeDescription parent) {
		this.parent = parent;
		this.category = parent.getCategory();
	}

	public ProcessCategory getCategory() {
		return category;
	}

	public void setCategory(ProcessCategory category) {
		this.category = category;
	}

}