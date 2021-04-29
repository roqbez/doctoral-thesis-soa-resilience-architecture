package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.bom.model.artifacts.Comment;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;

import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ServiceAssociationUtil;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;

@Root
public class BMTaskInformationExtension {

	@ElementList(name = "QoS", required = false)
	private List<QoSCriterion> qoSCriterions = new LinkedList<QoSCriterion>();

	@ElementList(name = "ManagedQoS", required = false)
	private List<QoSCriterion> managedQoSCriterions = new LinkedList<QoSCriterion>();

	@ElementList(name = "ServiceAssociation", required = false)
	private List<BMServiceAssociation> serviceAssociations = new LinkedList<BMServiceAssociation>();

	@Attribute(required = false)
	private String taxonomyClassification;

	@Attribute(required = false)
	private Integer totalNumberOfServices;

	@Attribute
	private int maxNumberOfServicesForDiscovery = 5;

	@Element(required = false)
	private BMQoSThresholds qoSThresholds;

	private transient StructuredActivityNode taskNode;

	@Element(required = false)
	private BMTaskResilienceConfiguration resilienceConfiguration;

	public void removeManagedQoSConstraints() {

		ListIterator<QoSCriterion> it = qoSCriterions.listIterator();

		while (it.hasNext()) {
			QoSCriterion c = it.next();
			if (c.isManaged())
				it.remove();
		}

		managedQoSCriterions.clear();
	}

	public void removeQoSConstraint(QoSConstraint q) {

		ListIterator<QoSCriterion> it = qoSCriterions.listIterator();

		while (it.hasNext()) {
			QoSCriterion c = it.next();
			if (c.getQoSKey().equals(q.getQoSKey()))
				it.remove();
		}
	}

	public void addManagedQoSConstraints(List<QoSConstraint> constraints) {

		removeManagedQoSConstraints();

		if (constraints != null) {
			for (QoSConstraint q : constraints) {

				removeQoSConstraint(q);

				QoSCriterion c = new QoSCriterion();
				c.setQoSItem(q.getQoSItem());
				c.setQoSAttribute(q.getQoSAttribute());
				c.setComparisionType(q.getComparisionType().toString().toLowerCase());
				c.setQoSValue(q.getQoSValue());
				c.setManaged(true);

				qoSCriterions.add(c);
				managedQoSCriterions.add(c);
			}
		}
	}

	public void defineServices(List<DiscoveredService> services) {

		if (serviceAssociations != null) {
			clearServiceAssociations();
		} else {
			serviceAssociations = new LinkedList<BMServiceAssociation>();
		}

		for (DiscoveredService s : services) {
			addService(s);
		}
	}

	public void clearServiceAssociations() {
		if (serviceAssociations != null) {
			serviceAssociations.clear();
		}
	}

	public void addService(DiscoveredService service) {

		if (serviceAssociations == null) {
			serviceAssociations = new LinkedList<BMServiceAssociation>();
		}

		serviceAssociations.add(ServiceAssociationUtil.getBMServiceAssociation(service));
	}

	public StructuredActivityNode getTaskNode() {
		return taskNode;
	}

	public void setTaskNode(StructuredActivityNode taskNode) {
		this.taskNode = taskNode;
	}

	public void write() {
		Comment commment = (Comment) taskNode.getOwnedComment().get(0);
		commment.setBody(ModelExtensionUtils.getXMLFromTaskInformationExtension(this));
	}

	public void write(GeneralModelAccessor ivGeneralModelAccessor) {
		ivGeneralModelAccessor.setDescription(ModelExtensionUtils.getXMLFromTaskInformationExtension(this));
	}

	public List<QoSCriterion> getQoSCriterions() {
		return qoSCriterions;
	}

	public void setQoSCriterions(List<QoSCriterion> qoSCriterions) {
		this.qoSCriterions = qoSCriterions;
	}

	public List<QoSCriterion> getManagedQoSCriterions() {
		return managedQoSCriterions;
	}

	public void setManagedQoSCriterions(List<QoSCriterion> managedQoSCriterions) {
		this.managedQoSCriterions = managedQoSCriterions;
	}

	public String getTaxonomyClassification() {
		return taxonomyClassification;
	}

	public void setTaxonomyClassification(String taxonomyClassification) {
		this.taxonomyClassification = taxonomyClassification;
	}

	public List<BMServiceAssociation> getServiceAssociations() {
		return serviceAssociations;
	}

	public void setServiceAssociations(List<BMServiceAssociation> serviceAssociations) {
		this.serviceAssociations = serviceAssociations;
	}

	public int getMaxNumberOfServicesForDiscovery() {
		return maxNumberOfServicesForDiscovery;
	}

	public void setMaxNumberOfServicesForDiscovery(int maxNumberOfServicesForDiscovery) {
		this.maxNumberOfServicesForDiscovery = maxNumberOfServicesForDiscovery;
	}

	public Integer getTotalNumberOfServices() {
		return totalNumberOfServices;
	}

	public void setTotalNumberOfServices(Integer totalNumberOfServices) {
		this.totalNumberOfServices = totalNumberOfServices;
	}

	public BMQoSThresholds getQoSThresholds() {
		return qoSThresholds;
	}

	public void setQoSThresholds(BMQoSThresholds qoSThresholds) {
		this.qoSThresholds = qoSThresholds;
	}

	public BMTaskResilienceConfiguration getResilienceConfiguration() {
		return resilienceConfiguration;
	}

	public void setResilienceConfiguration(BMTaskResilienceConfiguration resilienceConfiguration) {
		this.resilienceConfiguration = resilienceConfiguration;
	}

	public String getXml() {
		return ModelExtensionUtils.getXMLFromTaskInformationExtension(this);
	}

	public String getXmlEscaped() {
		return StringEscapeUtils.escapeHtml(getXml());
	}

}
