package br.ufsc.gsigma.catalog.plugin.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.servicediscovery.locator.DiscoveryServiceLocator;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.QoSItem;
import br.ufsc.gsigma.servicediscovery.model.QoSValueComparisionType;

public abstract class QoSUtil {

	private static List<QoSItem> enabledQoSItens;

	private static Map<String, QoSAttribute> enabledQoSAttributes;

	public static QoSInformation getQoSInformation(List<QoSCriterion> qoSCriterions) {
		return getQoSInformation(qoSCriterions, false);
	}

	public static QoSInformation getQoSInformation(List<QoSCriterion> qoSCriterions, boolean onlyNotManaged) {

		QoSInformation qoSInformation = new QoSInformation();

		if (qoSCriterions != null) {
			for (QoSCriterion qoSCriterion : qoSCriterions) {
				if (!onlyNotManaged || !qoSCriterion.isManaged()) {
					QoSValueComparisionType comparisionType = QoSInformation.getQoSValueComparisionType(qoSCriterion.getComparisionType());
					QoSConstraint qoSConstraint = new QoSConstraint(qoSCriterion.getQoSItem(), qoSCriterion.getQoSAttribute(), comparisionType, qoSCriterion.getQoSValue());
					qoSInformation.getQoSConstraints().add(qoSConstraint);
				}
			}
		}

		return qoSInformation;
	}

	public static List<QoSItem> getEnabledQoSItens() {

		if (enabledQoSItens == null) {
			synchronized (QoSUtil.class) {
				if (enabledQoSItens == null) {

					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					try {
						new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

								monitor.beginTask("Loading QoS Itens", IProgressMonitor.UNKNOWN);

								try {
									enabledQoSItens = DiscoveryServiceLocator.get().getEnabledQoSItems();
								} catch (Exception e) {
									throw new RuntimeException(e);
								} finally {
									monitor.done();
								}
							}

						});
					} catch (Exception e) {
						MessageDialog.openError(shell, "Service Discovery", "The Discovery Service is unavailable");
					}

				}
			}
		}
		return enabledQoSItens;
	}

	public static Map<String, QoSAttribute> getEnabledQoSAttributes() {

		if (enabledQoSAttributes == null) {
			synchronized (QoSUtil.class) {
				if (enabledQoSAttributes == null) {

					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					try {
						new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

								monitor.beginTask("Loading QoS Attributes", IProgressMonitor.UNKNOWN);

								try {
									enabledQoSAttributes = new TreeMap<String, QoSAttribute>(DiscoveryServiceLocator.get().getEnabledQoSAttributes());
								} catch (Exception e) {
									enabledQoSAttributes = null;
									throw new RuntimeException(e);
								} finally {
									monitor.done();
								}
							}

						});
					} catch (Exception e) {
						e.printStackTrace();
						MessageDialog.openError(shell, "Service Discovery", "The Discovery Service is unavailable");
					}
				}
			}
		}
		return enabledQoSAttributes;
	}

	public static QoSAttribute getEnabledQoSAttribute(String qoSKey) {
		Map<String, QoSAttribute> enabledQoSAttributes = getEnabledQoSAttributes();

		if (enabledQoSAttributes != null) {
			for (QoSAttribute q : enabledQoSAttributes.values())
				if (q.getKey().equals(qoSKey))
					return q;
		}
		return null;
	}

}
