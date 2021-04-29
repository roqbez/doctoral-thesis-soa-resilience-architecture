package br.ufsc.gsigma.catalog.plugin.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.catalog.services.locator.CatalogServiceLocator;

public abstract class CatalogServiceUtil {

	private static CatalogService catalogService;

	public static CatalogService getCatalogService() {

		if (catalogService == null) {

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			try {
				new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						monitor.beginTask("Loading Catalog Service", IProgressMonitor.UNKNOWN);

						try {
							catalogService = CatalogServiceLocator.get();
						} catch (Exception e) {
							throw new RuntimeException(e);
						} finally {
							monitor.done();
						}
					}

				});
			} catch (Exception e) {
				MessageDialog.openError(shell, "Catalog Service", "The Catalog Service is unavailable");
			}
		}

		return catalogService;
	}

}
