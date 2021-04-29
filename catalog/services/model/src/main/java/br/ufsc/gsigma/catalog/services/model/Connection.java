package br.ufsc.gsigma.catalog.services.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "connection")
@Root
@XmlRootElement(name = "connection")
public class Connection implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Attribute(required = false)
	private Integer id;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "id_process", nullable = false)
	private Process process;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_input_contact_point")
	@Element
	private InputContactPoint input;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_output_contact_point")
	@Element
	private OutputContactPoint output;

	public Connection() {

	}

	public Connection(Process process, OutputContactPoint output, InputContactPoint input) {
		this.process = process;
		this.output = output;
		this.input = input;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public InputContactPoint getInput() {
		return input;
	}

	public void setInput(InputContactPoint input) {
		this.input = input;
	}

	public OutputContactPoint getOutput() {
		return output;
	}

	public void setOutput(OutputContactPoint output) {
		this.output = output;
	}

	@Override
	public String toString() {
		return output + " --> " + input;
	}

}
