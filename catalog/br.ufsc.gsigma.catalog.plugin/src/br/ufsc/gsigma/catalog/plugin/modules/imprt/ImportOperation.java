package br.ufsc.gsigma.catalog.plugin.modules.imprt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.btools.blm.ie.imprt.AbstractImportOperation;
import com.ibm.btools.blm.ie.imprt.ImportSession;
import com.ibm.btools.blm.ie.imprt.rule.util.NavigatorUtil;
import com.ibm.btools.blm.ui.navigation.model.NavigationProjectNode;

public class ImportOperation extends AbstractImportOperation {

	public ImportOperation() {
	}

	@Override
	public void readObjects() {

		ImportSession importSession = getImportSession();

		String projectName = getProjectName();

		NavigationProjectNode targetProject = NavigatorUtil.getProjectNode(projectName);

		IProgressMonitor monitor = null;

		if (importSession != null) {
			monitor = importSession.getProgressMonitor();
			monitor.setTaskName("Business Process Catalog Import");
		}

		ProcessCatalogImportManager importManager = (ProcessCatalogImportManager) importSession.getImportOptions().getAdditionalOption(
				"IMPORT_MANAGER");

		IProgressMonitor progressMonitor = (monitor != null) ? new SubProgressMonitor(monitor, 990) : null;

		if (progressMonitor != null) {
			progressMonitor.beginTask("Importing from Business Process Catalog...", IProgressMonitor.UNKNOWN);
		}

		try {

			importManager.importCatalog(progressMonitor, targetProject, projectName);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}