package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;
import com.ibm.btools.bom.model.resources.Role;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;

public class ProcessServiceAssociationList extends AbstractBeanTableList<TaskServiceAssociationInfo> {

	protected static final String TASK_NAME = "Task Name";
	protected static final String TASK_TAXONOMY = "Task Taxonomy";
	protected static final String QOS_CONSTRAINTS = "QoS Constraints";
	protected static final String MAX_RESULTS = "Max Results";
	protected static final String MATCHING_SERVICES = "Matching Services";
	protected static final String TOTAL_SERVICES = "Total Services";

	protected static final String PARTICIPANT = "Participant";

	private StructuredActivityNode processNode;

	private GeneralModelAccessor ivGeneralModelAccessor;

	public ProcessServiceAssociationList(TableViewer tableViewer) {

		super(tableViewer);

		addColumn(TASK_NAME);
		addColumn(TASK_TAXONOMY);
		addColumn(QOS_CONSTRAINTS);
		addColumn(MAX_RESULTS);
		addColumn(MATCHING_SERVICES);
		addColumn(TOTAL_SERVICES);
		addColumn(PARTICIPANT);

		setColumnValueProvider(new ColumnValueProvider<TaskServiceAssociationInfo>() {

			@Override
			public String getColumnValue(TaskServiceAssociationInfo taskServiceAssociationInfo, String columnName) {

				if (columnName.equals(TASK_NAME))
					return taskServiceAssociationInfo.getTaskName();

				else if (columnName.equals(TASK_TAXONOMY))
					return taskServiceAssociationInfo.getTaskTaxonomy();

				else if (columnName.equals(QOS_CONSTRAINTS))
					return String.valueOf(taskServiceAssociationInfo.getQoSConstraints());

				else if (columnName.equals(MAX_RESULTS))
					return String.valueOf(taskServiceAssociationInfo.getMaxNumberOfServicesForDiscovery());

				else if (columnName.equals(MATCHING_SERVICES))
					return String.valueOf(taskServiceAssociationInfo.getMatchingServices());

				else if (columnName.equals(TOTAL_SERVICES))
					return taskServiceAssociationInfo.getTotalServices() != null ? String.valueOf(taskServiceAssociationInfo.getTotalServices()) : "";

				else if (columnName.equals(PARTICIPANT)) {
					return taskServiceAssociationInfo.getParticipantName() != null ? taskServiceAssociationInfo.getParticipantName() : "";
				}

				return null;

			}
		});

		setBeanPropertyModifier(new BeanPropertyModifier<TaskServiceAssociationInfo>() {

			@Override
			public boolean modifyBeanPropertyValue(TaskServiceAssociationInfo t, String columnName, Object value) {

				if (MAX_RESULTS.equals(columnName)) {
					t.setMaxNumberOfServicesForDiscovery(new Integer((String) value));
					return true;
				}
				return false;
			}
		});
	}

	@Override
	protected List<TaskServiceAssociationInfo> loadItems() {

		List<TaskServiceAssociationInfo> result = new ArrayList<TaskServiceAssociationInfo>();

		if (processNode != null) {

			for (Object o : processNode.getNodeContents()) {
				if (o instanceof StructuredActivityNode) {

					StructuredActivityNode taskNode = (StructuredActivityNode) o;

					TaskServiceAssociationInfo info = new TaskServiceAssociationInfo();
					info.setTaskName(taskNode.getName());

					BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(taskNode);

					info.setTaskTaxonomy(taskInformationExtension.getTaxonomyClassification());

					info.setQoSConstraints((taskInformationExtension.getQoSCriterions() != null) ? taskInformationExtension.getQoSCriterions().size() : 0);
					info.setMatchingServices((taskInformationExtension.getServiceAssociations() != null) ? taskInformationExtension.getServiceAssociations().size() : 0);
					info.setMaxNumberOfServicesForDiscovery(taskInformationExtension.getMaxNumberOfServicesForDiscovery());
					info.setTotalServices(taskInformationExtension.getTotalNumberOfServices());

					Role participantRole = ModelExtensionUtils.getParticipantRole(taskNode);

					info.setParticipantName(participantRole != null ? participantRole.getName() : null);

					result.add(info);
				}
			}
		}

		return result;
	}

	@Override
	protected void saveItems(List<TaskServiceAssociationInfo> itens) {

		if (processNode != null) {

			Map<String, BMTaskInformationExtension> m = new HashMap<String, BMTaskInformationExtension>();

			for (Object o : processNode.getNodeContents()) {
				if (o instanceof StructuredActivityNode) {
					StructuredActivityNode taskNode = (StructuredActivityNode) o;
					BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(taskNode);
					m.put(taskNode.getName(), taskInformationExtension);
				}
			}

			for (TaskServiceAssociationInfo i : itens) {
				BMTaskInformationExtension info = m.get(i.getTaskName());
				info.setMaxNumberOfServicesForDiscovery(i.getMaxNumberOfServicesForDiscovery());
				info.write();
			}

			// Only to signal modifications of process (ask user to save the process)
			ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor).write(ivGeneralModelAccessor);
		}
	}

	public void setIvGeneralModelAccessor(GeneralModelAccessor ivGeneralModelAccessor) {
		this.ivGeneralModelAccessor = ivGeneralModelAccessor;

		if (ivGeneralModelAccessor.getModel() instanceof StructuredActivityNode)
			this.processNode = (StructuredActivityNode) ivGeneralModelAccessor.getModel();
	}
}
