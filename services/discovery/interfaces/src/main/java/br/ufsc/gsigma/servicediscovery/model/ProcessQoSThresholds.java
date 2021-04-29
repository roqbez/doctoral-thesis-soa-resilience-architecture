package br.ufsc.gsigma.servicediscovery.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.servicediscovery.support.jaxb.TaskQoSThresholdAttributeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessQoSThresholds implements Serializable {

	private static final long serialVersionUID = 1L;

	private String processName;

	@XmlElementWrapper
	@XmlElement(name = "qoSThreshold")
	private List<QoSThreshold> qoSThresholds = new LinkedList<QoSThreshold>();

	@XmlJavaTypeAdapter(TaskQoSThresholdAttributeAdapter.class)
	private Map<String, List<QoSThreshold>> taskQoSThresholds = new LinkedHashMap<String, List<QoSThreshold>>();

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public List<QoSThreshold> getQoSThresholds() {
		return qoSThresholds;
	}

	public void setQoSThresholds(List<QoSThreshold> qoSThresholds) {
		this.qoSThresholds = qoSThresholds;
	}

	public Map<String, List<QoSThreshold>> getTaskQoSThresholds() {
		return taskQoSThresholds;
	}

	public void setTaskQoSThresholds(Map<String, List<QoSThreshold>> taskQoSThresholds) {
		this.taskQoSThresholds = taskQoSThresholds;
	}

}
