package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import br.ufsc.gsigma.servicediscovery.model.ServiceInfo;
import br.ufsc.gsigma.servicediscovery.model.ServicesComposition;

@Root
public class BMServicesComposition {

	public enum CompositionType {
		HEURISTIC, OPTIMAL, BOTH
	}

	@ElementList(name = "Service")
	private List<BMServiceAssociation> services = new LinkedList<BMServiceAssociation>();

	@ElementList(name = "Service2", required = false)
	private List<BMServiceAssociation> services2 = new LinkedList<BMServiceAssociation>();

	@Attribute
	private Integer ranking;

	@Attribute
	private Double utility;

	@Attribute(required = false)
	private Double utility2;

	@ElementList(name = "QoSValue")
	private List<BMQoSValue> qoSValues = new LinkedList<BMQoSValue>();

	@ElementList(name = "QoSValue2", required = false)
	private List<BMQoSValue> qoSValues2 = new LinkedList<BMQoSValue>();

	@Attribute
	private CompositionType type = CompositionType.HEURISTIC;

	public BMServicesComposition() {
	}

	public BMServicesComposition(ServicesComposition composition, CompositionType type) {

		this.type = type;

		this.ranking = composition.getRanking();
		this.utility = composition.getCompositionUtility();

		for (ServiceInfo s : composition.getServices()) {
			this.services.add(new BMServiceAssociation(s.getServiceKey(), s.getBindingTemplateKey(), s.getAlias(), s.getServiceProtocolConverter()));
		}

		for (Entry<String, Double> e : composition.getQoSValues().entrySet()) {
			String[] parts = e.getKey().split("\\.");
			String qoSItem = parts[0];
			String qoSAttribute = parts[1];
			Double qoSValue = e.getValue();
			this.qoSValues.add(new BMQoSValue(qoSItem, qoSAttribute, qoSValue));
		}
	}

	public String getServicesAlias() {
		if (type == CompositionType.BOTH)
			return getServicesAlias(services) + " / " + getServicesAlias(services2);
		else
			return getServicesAlias(services);
	}

	public String getUtilityLabel() {
		if (type == CompositionType.BOTH)
			return utility + " / " + utility2;
		else
			return String.valueOf(getUtility());
	}

	public String getRatioLabel() {

		if (type == CompositionType.BOTH)
			return new DecimalFormat("#.###").format((utility / utility2) * 100) + "%";
		else
			return "-";

	}

	public String getQoSValueLabel(String qoSItem, String qoSAtt) {

		if (type == CompositionType.BOTH)
			return getQoSValue(qoSValues, qoSItem, qoSAtt) + " / " + getQoSValue(qoSValues2, qoSItem, qoSAtt);
		else
			return getQoSValue(qoSValues, qoSItem, qoSAtt);
	}

	private String getQoSValue(List<BMQoSValue> qoSValues, String qoSItem, String qoSAtt) {
		for (BMQoSValue q : qoSValues) {
			if (q.getQoSItem().equals(qoSItem) && q.getQoSAttribute().equals(qoSAtt))
				return q.getQoSValue() != null ? new DecimalFormat("#.###").format(q.getQoSValue()) : "N/A";
		}
		return "N/A";
	}

	private String getServicesAlias(List<BMServiceAssociation> services) {

		StringBuilder sb = new StringBuilder();

		int i = 0;

		for (BMServiceAssociation s : services) {
			sb.append(s.getAlias());
			if (i++ < services.size() - 1)
				sb.append(", ");
		}

		return sb.toString();
	}

	public List<BMServiceAssociation> getServices() {
		return services;
	}

	public void setServices(List<BMServiceAssociation> services) {
		this.services = services;
	}

	public Double getUtility() {
		return utility;
	}

	public void setUtility(Double utility) {
		this.utility = utility;
	}

	public Double getUtility2() {
		return utility2;
	}

	public void setUtility2(Double utility2) {
		this.utility2 = utility2;
	}

	public List<BMQoSValue> getQoSValues() {
		return qoSValues;
	}

	public void setQoSValues(List<BMQoSValue> qoSValues) {
		this.qoSValues = qoSValues;
	}

	public Integer getRanking() {
		return ranking;
	}

	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	public CompositionType getType() {
		return type;
	}

	public void setType(CompositionType type) {
		this.type = type;
	}

	public List<BMServiceAssociation> getServices2() {
		return services2;
	}

	public void setServices2(List<BMServiceAssociation> services2) {
		this.services2 = services2;
	}

	public List<BMQoSValue> getQoSValues2() {
		return qoSValues2;
	}

	public void setQoSValues2(List<BMQoSValue> qoSValues2) {
		this.qoSValues2 = qoSValues2;
	}

}
