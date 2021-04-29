package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class BMQoSValue implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String qoSItem;

	@Attribute
	private String qoSAttribute;

	@Attribute
	private Double qoSValue;

	public BMQoSValue() {
	}

	public BMQoSValue(String qoSItem, String qoSAttribute, Double qoSValue) {
		this.qoSItem = qoSItem;
		this.qoSAttribute = qoSAttribute;
		this.qoSValue = qoSValue;
	}

	public String getQoSItem() {
		return qoSItem;
	}

	public void setQoSItem(String qoSItem) {
		this.qoSItem = qoSItem;
	}

	public String getQoSAttribute() {
		return qoSAttribute;
	}

	public void setQoSAttribute(String qoSAttribute) {
		this.qoSAttribute = qoSAttribute;
	}

	public Double getQoSValue() {
		return qoSValue;
	}

	public void setQoSValue(Double qoSValue) {
		this.qoSValue = qoSValue;
	}

}
