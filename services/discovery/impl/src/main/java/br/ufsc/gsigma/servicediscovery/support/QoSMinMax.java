package br.ufsc.gsigma.servicediscovery.support;

public interface QoSMinMax {

	public Double getQoSMinValue(String serviceClassification, String qoSKey);

	public Double getQoSMaxValue(String serviceClassification, String qoSKey);

	public Double getGlobalQoSDeltaValue(String qoSKey);

}
