package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.TableViewer;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMQoSValue;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServiceAssociation;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;

public class TaskServiceAssociationList extends AbstractBeanTableList<BMServiceAssociation> {

	protected static final String SERVICE_NAME = "Service Name";
	protected static final String SERVICE_ENDPOINT = "Service Endpoint";
	protected static final String SERVICE_PROTOCOL = "Protocol";
	protected static final String SERVICE_PROVIDER_NAME = "Service Provider";
	protected static final String SERVICE_UTILITY = "Service Utility";
	protected static final String SERVICE_PROVIDER_REPUTATION = "Service Provider Reputation";

	private GeneralModelAccessor ivGeneralModelAccessor;

	protected Map<String, QoSAttribute> mapColumnNameToQoSAttribute;

	public TaskServiceAssociationList(TableViewer tableViewer, Map<String, QoSAttribute> mapColNameToQoSAttribute) {
		super(tableViewer);

		addColumn(SERVICE_NAME);
		addColumn(SERVICE_ENDPOINT);
		addColumn(SERVICE_PROTOCOL);
		addColumn(SERVICE_PROVIDER_NAME);
		addColumn(SERVICE_UTILITY);

		this.mapColumnNameToQoSAttribute = mapColNameToQoSAttribute;

		setColumnValueProvider(new ColumnValueProvider<BMServiceAssociation>() {

			@Override
			public String getColumnValue(BMServiceAssociation serviceAssociation, String columnName) {

				if (columnName.equals(SERVICE_NAME))
					return serviceAssociation.getServiceName();

				else if (columnName.equals(SERVICE_ENDPOINT))
					return serviceAssociation.getServiceEndpoint();

				else if (columnName.equals(SERVICE_PROTOCOL)) {

					if (StringUtils.containsIgnoreCase(serviceAssociation.getServiceProtocolConverter(), "soap"))
						return "SOAP";
					else if (StringUtils.containsIgnoreCase(serviceAssociation.getServiceProtocolConverter(), "json"))
						return "JSON";
					else
						return serviceAssociation.getServiceProtocolConverter();

				} else if (columnName.equals(SERVICE_PROVIDER_NAME))
					return serviceAssociation.getServiceProviderName();

				else if (columnName.equals(SERVICE_PROVIDER_REPUTATION))
					return (serviceAssociation.getServiceProviderReputation() != null) ? serviceAssociation.getServiceProviderReputation().toString() : "";

				else if (columnName.equals(SERVICE_UTILITY))
					return serviceAssociation.getServiceUtility() != null ? serviceAssociation.getServiceUtility().toString() : "N/A";

				else if (mapColumnNameToQoSAttribute.get(columnName) != null) {

					QoSAttribute qoSAttribute = mapColumnNameToQoSAttribute.get(columnName);

					List<BMQoSValue> serviceQoSInformation = serviceAssociation.getQoSValues();

					if (serviceQoSInformation != null) {
						for (BMQoSValue item : serviceQoSInformation) {
							if (item.getQoSItem().equals(qoSAttribute.getQoSItem()) && item.getQoSAttribute().equals(qoSAttribute.getName()))
								return String.valueOf(item.getQoSValue());
						}
					}

					return "N/A";
				}
				return null;
			}
		});

	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<BMServiceAssociation> loadItems() {

		if (ivGeneralModelAccessor == null)
			return Collections.EMPTY_LIST;

		BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);

		return taskInformationExtension != null ? taskInformationExtension.getServiceAssociations() : new ArrayList<BMServiceAssociation>();
	}

	@Override
	protected void saveItems(List<BMServiceAssociation> items) {
		if (ivGeneralModelAccessor != null) {
			BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);
			if (taskInformationExtension != null) {
				taskInformationExtension.setServiceAssociations(items);
				taskInformationExtension.write(ivGeneralModelAccessor);
			}
		}
	}

	public void setIvGeneralModelAccessor(GeneralModelAccessor ivGeneralModelAccessor) {
		this.ivGeneralModelAccessor = ivGeneralModelAccessor;
	}

}
