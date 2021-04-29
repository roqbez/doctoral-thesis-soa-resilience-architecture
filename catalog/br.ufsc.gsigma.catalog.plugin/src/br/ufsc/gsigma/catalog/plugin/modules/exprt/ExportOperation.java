package br.ufsc.gsigma.catalog.plugin.modules.exprt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.btools.blm.ie.exprt.AbstractExportOperation;
import com.ibm.btools.blm.ie.exprt.ExportSession;

public class ExportOperation extends AbstractExportOperation {

	@Override
	public void export() {

		ExportSession exportSession = getExportSession();

		IProgressMonitor monitor = null;

		if (exportSession != null) {
			monitor = exportSession.getProgressMonitor();
			monitor.setTaskName("Business Process Catalog Export");
		}

		ProcessCatalogExportManager exportManager = (ProcessCatalogExportManager) exportSession.getExportOptions().getAdditionalOption(
				"EXPORT_MANAGER");

		IProgressMonitor progressMonitor = (monitor != null) ? new SubProgressMonitor(monitor, 990) : null;

		if (progressMonitor != null) {
			progressMonitor.beginTask("Exporting to Business Process Catalog...", IProgressMonitor.UNKNOWN);
		}

		try {

			exportManager.exportCatalog(progressMonitor);

		} catch (Throwable e) {

			e.printStackTrace();
		}

	}

}