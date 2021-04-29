package br.ufsc.gsigma.catalog.plugin.modules.bpelexprt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;

import br.ufsc.gsigma.catalog.plugin.util.DeploymentUtil;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;
import br.ufsc.gsigma.services.deployment.locator.DeploymentServiceLocator;
import br.ufsc.gsigma.services.deployment.model.Deployment;
import br.ufsc.gsigma.services.deployment.model.DeploymentRequest;
import br.ufsc.gsigma.services.execution.dto.ProcessExecutionInfo;
import br.ufsc.gsigma.services.execution.interfaces.ProcessExecutionService;
import br.ufsc.gsigma.services.execution.locator.ProcessExecutionServiceLocator;

public class BPELExportManager {

	private static final Logger logger = LoggerFactory.getLogger(BPELExportManager.class);

	private NavigationProcessNode processNode;

	private String folderName;

	private String processName;

	public void exportBPEL(final IProgressMonitor progressMonitor) throws Exception {

		final String projectName = processNode.getProjectNode().getLabel();

		DeploymentService deploymentService = DeploymentServiceLocator.get();

		DeploymentUtil.getDeploymentRequest(processNode);

		DeploymentRequest deploymentRequest = DeploymentUtil.getDeploymentRequest(processNode);

		final Deployment deployment = deploymentService.deployApplication(deploymentRequest);

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {

				try {

					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					if (deployment != null && !deployment.isError()) {

						final ProcessExecutionService processExecutionService = ProcessExecutionServiceLocator.get();

						boolean isExecuteProcess = MessageDialog.openQuestion(shell, "BPEL Exporter", "The process '" + projectName + "' was successfully deployed. Do you want to execute it ?");
						if (isExecuteProcess) {
							ProcessExecutionInfo processExecutionInfo = processExecutionService.executeProcess(deployment.getBusinessProcessName(), null);
							if (processExecutionInfo != null) {
								MessageDialog.openInformation(shell, "BPEL Exporter", "The BPEL Process (" + processExecutionInfo.getProcessName() + ") is executing. The instance id is " + processExecutionInfo.getInstanceId());
							}
						}

					} else {
						MessageDialog.openError(shell, "BPEL Exporter", "The BPEL Exporter was unable to deploy the BPEL process");
					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}

		});

	}

	public NavigationProcessNode getProcessNode() {
		return processNode;
	}

	public void setProcessNode(NavigationProcessNode processNode) {
		this.processNode = processNode;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

}