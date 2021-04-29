package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

@Root
public class BMQoSThresholds {

	private String name;

	private boolean process;

	@ElementMap(attribute = true, key = "qoSKey", value = "value")
	private Map<String, Double> qoSMinValues = new HashMap<String, Double>();

	@ElementMap(attribute = true, key = "qoSKey", value = "value")
	private Map<String, Double> qoSMaxValues = new HashMap<String, Double>();

	public BMQoSThresholds() {
	}

	public BMQoSThresholds(Map<String, Double> qoSMinValues, Map<String, Double> qoSMaxValues) {
		this.qoSMinValues = qoSMinValues;
		this.qoSMaxValues = qoSMaxValues;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isProcess() {
		return process;
	}

	public void setProcess(boolean process) {
		this.process = process;
	}

	public Map<String, Double> getQoSMinValues() {
		return qoSMinValues;
	}

	public void setQoSMinValues(Map<String, Double> qoSMinValues) {
		this.qoSMinValues = qoSMinValues;
	}

	public Map<String, Double> getQoSMaxValues() {
		return qoSMaxValues;
	}

	public void setQoSMaxValues(Map<String, Double> qoSMaxValues) {
		this.qoSMaxValues = qoSMaxValues;
	}

}
