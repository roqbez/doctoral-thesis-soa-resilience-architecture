package br.ufsc.gsigma.catalog.plugin.modules.qos.process;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.qos.task.TaskQoSList;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;

public class ProcessQoSList extends TaskQoSList {

	public ProcessQoSList(TableViewer tableViewer) {
		super(tableViewer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<QoSCriterion> loadItems() {

		if (ivGeneralModelAccessor == null)
			return Collections.EMPTY_LIST;

		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

		return processInformationExtension.getQoSCriterions();
	}

	@Override
	public void saveItems(List<QoSCriterion> itens) {
		if (ivGeneralModelAccessor != null) {
			BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);
			processInformationExtension.setQoSCriterions(itens);
			processInformationExtension.write(ivGeneralModelAccessor);
		}
	}

}
