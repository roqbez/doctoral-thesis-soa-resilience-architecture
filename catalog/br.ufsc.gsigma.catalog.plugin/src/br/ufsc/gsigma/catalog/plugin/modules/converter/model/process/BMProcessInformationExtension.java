package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;

@Root
public class BMProcessInformationExtension {

	@ElementList(name = "QoS")
	private List<QoSCriterion> qoSCriterions = new LinkedList<QoSCriterion>();

	@ElementMap(attribute = true, key = "qoSKey", value = "value", required = false)
	private Map<String, Double> globalQoSDelta;

	@ElementMap(attribute = true, key = "qoSKey", value = "value", required = false)
	private Map<String, Double> qoSWeights = new LinkedHashMap<String, Double>();

	@ElementList(name = "ServicesComposition")
	private List<BMServicesComposition> compositions = new LinkedList<BMServicesComposition>();

	@Attribute(required = false)
	private Integer totalNumberOfCompositions;

	@Attribute(required = false)
	private Integer maxNumberOfCompositions = 100;

	@Attribute
	private boolean useOptimalServiceSelection;

	@Attribute
	private boolean useHeuristicServiceSelection;

	@Attribute(required = false)
	private Integer initialNumberOfQoSLevels = 40;

	@Attribute(required = false)
	private Integer maxNumberOfQoSLevels = 160;

	@Element(required = false)
	private BMQoSThresholds qoSThresholds;

	@Element(required = false)
	private BMInfrastructureProvider infrastructureProvider;

	@Element(required = false)
	private BMResilienceConfiguration resilienceConfiguration;

	public void write(GeneralModelAccessor ivGeneralModelAccessor) {
		ivGeneralModelAccessor.setDescription(ModelExtensionUtils.getXMLFromProcessInformationExtension(this));
	}

	public List<QoSCriterion> getQoSCriterions() {
		return qoSCriterions;
	}

	public void setQoSCriterions(List<QoSCriterion> qoSCriterions) {
		this.qoSCriterions = qoSCriterions;
	}

	public Map<String, Double> getGlobalQoSDelta() {
		return globalQoSDelta;
	}

	public void setGlobalQoSDelta(Map<String, Double> globalQoSDelta) {
		this.globalQoSDelta = globalQoSDelta;
	}

	public Map<String, Double> getQoSWeights() {
		return qoSWeights;
	}

	public void setQoSWeights(Map<String, Double> qoSWeights) {
		this.qoSWeights = qoSWeights;
	}

	public List<BMServicesComposition> getCompositions() {
		return compositions;
	}

	public void setCompositions(List<BMServicesComposition> compositions) {
		this.compositions = compositions;
	}

	public Integer getTotalNumberOfCompositions() {
		return totalNumberOfCompositions;
	}

	public void setTotalNumberOfCompositions(Integer totalNumberOfCompositions) {
		this.totalNumberOfCompositions = totalNumberOfCompositions;
	}

	public Integer getMaxNumberOfCompositions() {
		return maxNumberOfCompositions;
	}

	public void setMaxNumberOfCompositions(Integer maxNumberOfCompositions) {
		this.maxNumberOfCompositions = maxNumberOfCompositions;
	}

	public boolean isUseOptimalServiceSelection() {
		return useOptimalServiceSelection;
	}

	public void setUseOptimalServiceSelection(boolean useOptimalServiceSelection) {
		this.useOptimalServiceSelection = useOptimalServiceSelection;
	}

	public boolean isUseHeuristicServiceSelection() {
		return useHeuristicServiceSelection;
	}

	public void setUseHeuristicServiceSelection(boolean useHeuristicServiceSelection) {
		this.useHeuristicServiceSelection = useHeuristicServiceSelection;
	}

	public int getMaxNumberOfQoSLevels() {
		return maxNumberOfQoSLevels;
	}

	public void setMaxNumberOfQoSLevels(int maxNumberOfQoSLevels) {
		this.maxNumberOfQoSLevels = maxNumberOfQoSLevels;
	}

	public int getInitialNumberOfQoSLevels() {
		return initialNumberOfQoSLevels;
	}

	public void setInitialNumberOfQoSLevels(int initialNumberOfQoSLevels) {
		this.initialNumberOfQoSLevels = initialNumberOfQoSLevels;
	}

	public BMQoSThresholds getQoSThresholds() {
		return qoSThresholds;
	}

	public void setQoSThresholds(BMQoSThresholds qoSThresholds) {
		this.qoSThresholds = qoSThresholds;
	}

	public BMInfrastructureProvider getInfrastructureProvider() {
		return infrastructureProvider;
	}

	public void setInfrastructureProvider(BMInfrastructureProvider infrastructureProvider) {
		this.infrastructureProvider = infrastructureProvider;
	}

	public BMResilienceConfiguration getResilienceConfiguration() {
		return resilienceConfiguration;
	}

	public void setResilienceConfiguration(BMResilienceConfiguration resilienceConfiguration) {
		this.resilienceConfiguration = resilienceConfiguration;
	}

	public String getXml() {
		return ModelExtensionUtils.getXMLFromProcessInformationExtension(this);
	}

	public String getXmlEscaped() {
		return StringEscapeUtils.escapeHtml(getXml());
	}

}
