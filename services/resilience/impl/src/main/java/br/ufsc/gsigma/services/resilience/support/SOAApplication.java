package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;

import br.ufsc.gsigma.binding.events.BindingConfigurationRequestEvent;
import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.servicediscovery.support.ProcessStructureParser;
import br.ufsc.gsigma.servicediscovery.support.struct.TaskStructure;

public class SOAApplication implements Serializable {

	private static final long serialVersionUID = 1L;

	private String applicationId;

	private br.ufsc.gsigma.catalog.services.model.Process businessProcess;

	private ServicesInformation servicesInformation;

	private BindingConfiguration bindingConfiguration;

	private ExecutionContext executionContext;

	private AtomicInteger reconfigurationTimestamp = new AtomicInteger();

	private Map<String, Collection<String>> nextTasks = new LinkedHashMap<String, Collection<String>>();

	private BindingConfigurationRequestEvent bindingReconfigurationEvent;

	public SOAApplication(String applicationId, ExecutionContext executionContext) {
		this.applicationId = applicationId;
		this.executionContext = executionContext;
	}

	public Process getBusinessProcess() {
		return businessProcess;
	}

	public void setBusinessProcess(Process businessProcess) {

		this.businessProcess = businessProcess;

		ProcessStructureParser parser = new ProcessStructureParser(businessProcess);

		for (TaskStructure t : parser.getTasks()) {
			String classification = t.getTaxonomyClassification();
			Collection<String> successors = new LinkedHashSet<String>();
			calculateNextTasks(parser, t, successors);
			nextTasks.put(classification, successors);
		}
	}

	private void calculateNextTasks(ProcessStructureParser parser, ConnectableComponent c, Collection<String> successors) {
		for (ConnectableComponent c0 : parser.getComponentSuccessors(c)) {
			if (c0 instanceof TaskStructure) {
				if (!successors.add(((TaskStructure) c0).getTaxonomyClassification())) {
					continue;
				}
			}
			calculateNextTasks(parser, c0, successors);
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getNextTasks(String taxonomyClassification) {
		Collection<String> l = nextTasks.get(taxonomyClassification);
		return l != null ? new ArrayList<String>(l) : Collections.EMPTY_LIST;
	}

	public void updateCache() {
		InfinispanCaches.getInstance().getSOAApplications().replace(applicationId, this);
	}

	public void updateCacheAsync() {
		InfinispanCaches.getInstance().getSOAApplications().replaceAsync(applicationId, this);
	}

	public double getEngineOverheadEstimative() {

		double r = 0.5d;

		double sum = 0;
		int n = 0;

		for (SOAApplicationInstance i : InfinispanCaches.getInstance().getSOAApplicationInstances().values()) {

			// TODO: improve
			if (i.getApplicationId().equals(applicationId) && i.isComplete()) {
				sum += i.getEngineOverhead();
				n++;
			}
		}

		if (n < 0) {
			r = sum / (double) n;
		}
		return r;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public BindingConfigurationRequestEvent getBindingReconfigurationEvent() {
		return bindingReconfigurationEvent;
	}

	public void setBindingReconfigurationEvent(BindingConfigurationRequestEvent bindingReconfigurationEvent) {
		this.bindingReconfigurationEvent = bindingReconfigurationEvent;
	}

	public int getReconfigurationTimestamp() {
		return reconfigurationTimestamp.get();
	}

	public int incrementReconfigurationTimestamp() {
		return reconfigurationTimestamp.incrementAndGet();
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public ServicesInformation getServicesInformation() {
		return servicesInformation;
	}

	public void setServicesInformation(ServicesInformation servicesInformation) {
		this.servicesInformation = servicesInformation;
	}

	public BindingConfiguration getBindingConfiguration() {
		return bindingConfiguration;
	}

	public void setBindingConfiguration(BindingConfiguration bindingConfiguration) {
		this.bindingConfiguration = bindingConfiguration;
	}

	public Double getResponseTimeConstraint() {
		if (servicesInformation != null && !CollectionUtils.isEmpty(servicesInformation.getQoSCriterions())) {
			for (QoSCriterion q : servicesInformation.getQoSCriterions()) {
				if ("Performance.ResponseTime".equals(q.getQoSKey())) {
					return q.getQoSValue();
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SOAApplication other = (SOAApplication) obj;
		if (applicationId == null) {
			if (other.applicationId != null)
				return false;
		} else if (!applicationId.equals(other.applicationId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (businessProcess != null) {
			return "SOAApplication [applicationId=" + applicationId + ", bussinessProcess=" + businessProcess.getName() + ", reconfigurationTimestamp=" + reconfigurationTimestamp + "]";
		} else {
			return "SOAApplication [applicationId=" + applicationId + ", reconfigurationTimestamp=" + reconfigurationTimestamp + "]";
		}
	}

	public String getName() {
		return businessProcess != null ? businessProcess.getName() : null;
	}

}
