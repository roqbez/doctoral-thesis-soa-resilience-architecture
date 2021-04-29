package br.ufsc.gsigma.catalog.plugin.modules.bpelexprt;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.btools.blm.ie.exprt.AbstractExportOperation;
import com.ibm.btools.blm.ie.exprt.ExportSession;

import br.ufsc.gsigma.catalog.plugin.util.ui.UIUtil;

public class BPELExportOperation extends AbstractExportOperation {

	private static final Logger logger = LoggerFactory.getLogger(BPELExportOperation.class);

	@Override
	public void export() {

		ExportSession exportSession = getExportSession();

		IProgressMonitor monitor = null;

		if (exportSession != null) {
			monitor = exportSession.getProgressMonitor();
			monitor.setTaskName("Business Process Catalog Export");
			// monitor.beginTask("Exporting to Business Process Catalog...", IProgressMonitor.UNKNOWN);
		}

		BPELExportManager exportManager = (BPELExportManager) exportSession.getExportOptions().getAdditionalOption("EXPORT_MANAGER");

		// IProgressMonitor progressMonitor = (monitor != null) ? new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN) : null;
		//
		// if (progressMonitor != null) {
		// progressMonitor.beginTask("Exporting to Business Process Catalog...", IProgressMonitor.UNKNOWN);
		// }

		AtomicBoolean isDone = null;
		try {
			isDone = UIUtil.tickProgressMonitor(monitor);
			exportManager.exportBPEL(monitor);
		} catch (final Throwable e) {
			logger.error(e.getMessage(), e);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openError(shell, "BPEL Exporter", "The BPEL Exporter was unable to deploy the BPEL process: " + e.getMessage());
				}
			});
		} finally {
			if (isDone != null)
				isDone.set(true);
			// monitor.done();
		}
	}

}