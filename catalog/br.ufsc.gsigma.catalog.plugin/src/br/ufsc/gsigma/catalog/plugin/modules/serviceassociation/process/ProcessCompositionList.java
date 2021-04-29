package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServicesComposition;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServicesComposition.CompositionType;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;

public class ProcessCompositionList extends AbstractBeanTableList<BMServicesComposition> {

	protected static final String RANKING = "Ranking";
	protected static final String SERVICES = "Services Combination";
	protected static final String TYPE = "Type";
	protected static final String PROCESS_UTILITY = "Process Utility";
	protected static final String RATIO = "Ratio";

	protected Map<String, QoSAttribute> mapColumnNameToQoSAttribute;

	protected GeneralModelAccessor ivGeneralModelAccessor;

	public ProcessCompositionList(TableViewer tableViewer, Map<String, QoSAttribute> mapColNameToQoSAttribute) {

		super(tableViewer);

		addColumn(RANKING);
		addColumn(SERVICES);
		addColumn(TYPE);
		addColumn(PROCESS_UTILITY);
		addColumn(RATIO);

		this.mapColumnNameToQoSAttribute = mapColNameToQoSAttribute;

		setColumnValueProvider(new ColumnValueProvider<BMServicesComposition>() {

			@Override
			public String getColumnValue(BMServicesComposition processComposition, String columnName) {

				if (columnName.equals(RANKING)) {
					if (processComposition.getRanking() > 0)
						return String.valueOf(processComposition.getRanking());
					else
						return "";
				}

				else if (columnName.equals(SERVICES))
					return processComposition.getServicesAlias();

				else if (columnName.equals(TYPE))
					//@formatter:off
					return  processComposition.getType() == CompositionType.HEURISTIC ? "Heuristic" : //
							processComposition.getType() == CompositionType.OPTIMAL ? "Optimal" : //
							processComposition.getType() == CompositionType.BOTH ? "Mixed" : "";
					//@formatter:on

				else if (columnName.equals(PROCESS_UTILITY))
					return processComposition.getUtilityLabel();

				else if (columnName.equals(RATIO))
					return processComposition.getRatioLabel();

				else if (mapColumnNameToQoSAttribute.get(columnName) != null) {
					QoSAttribute qoSAttribute = mapColumnNameToQoSAttribute.get(columnName);
					if (qoSAttribute != null)
						return processComposition.getQoSValueLabel(qoSAttribute.getQoSItem(), qoSAttribute.getName());

					return "N/A";
				}
				return null;

			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<BMServicesComposition> loadItems() {

		if (ivGeneralModelAccessor == null)
			return Collections.EMPTY_LIST;

		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());

		return processInformationExtension.getCompositions();
	}

	public Integer getTotalNumberOfCompositions() {

		if (ivGeneralModelAccessor == null)
			return null;

		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());

		return processInformationExtension.getTotalNumberOfCompositions();

	}

	public Integer getNumberOfShowingCompositions() {

		if (ivGeneralModelAccessor == null)
			return null;

		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());

		return processInformationExtension.getCompositions().size();
	}

	public void setIvGeneralModelAccessor(GeneralModelAccessor ivGeneralModelAccessor) {
		this.ivGeneralModelAccessor = ivGeneralModelAccessor;
	}

}
