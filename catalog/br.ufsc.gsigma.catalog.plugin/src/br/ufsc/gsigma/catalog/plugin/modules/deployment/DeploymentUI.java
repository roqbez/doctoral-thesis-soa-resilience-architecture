package br.ufsc.gsigma.catalog.plugin.modules.deployment;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.bom.model.processes.activities.Activity;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMDeploymentServer;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMInfrastructureProvider;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.util.DeploymentUtil;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ObjectHolder;
import br.ufsc.gsigma.catalog.plugin.util.ProcessNodeHelper;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;
import br.ufsc.gsigma.services.deployment.locator.DeploymentServiceLocator;
import br.ufsc.gsigma.services.deployment.model.Deployment;
import br.ufsc.gsigma.services.deployment.model.DeploymentRequest;

public class DeploymentUI extends Composite {

	private Text txtInfrastructureProvider;

	private ModelAccessor ivModelAccessor;

	private GeneralModelAccessor ivGeneralModelAccessor;
	private Label providerId;
	private Label providerType;

	private Composite providerInfoComposite;
	private Label providerServices;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public DeploymentUI(Composite parent, int style) {
		super(parent, style);

		setBackground(parent.getBackground());

		setLayout(new GridLayout(1, false));

		final DeploymentUI deploymentUI = this;

		Composite wrapperComposite = new Composite(this, SWT.NONE);
		wrapperComposite.setBackground(parent.getBackground());
		wrapperComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		GridLayout gl_wrapperComposite = new GridLayout(1, false);
		gl_wrapperComposite.marginWidth = 0;
		gl_wrapperComposite.marginHeight = 0;
		gl_wrapperComposite.verticalSpacing = 0;
		gl_wrapperComposite.horizontalSpacing = 0;
		wrapperComposite.setLayout(gl_wrapperComposite);

		Composite composite = new Composite(wrapperComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		composite.setBackground(parent.getBackground());

		composite.setLayout(new GridLayout(3, false));

		Label lblDeploymentServer = new Label(composite, SWT.NONE);
		lblDeploymentServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDeploymentServer.setBackground(parent.getBackground());
		lblDeploymentServer.setText("Infrastructure Provider:");

		txtInfrastructureProvider = new Text(composite, SWT.BORDER);
		txtInfrastructureProvider.setEditable(false);
		GridData gd_txtInfrastructureProvider = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtInfrastructureProvider.widthHint = 300;
		txtInfrastructureProvider.setLayoutData(gd_txtInfrastructureProvider);

		Button btnChoose = new Button(composite, SWT.NONE);
		btnChoose.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		btnChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new DeploymentChooseProviderUI(getShell(), deploymentUI).open();
			}
		});
		btnChoose.setText("Choose");

		providerInfoComposite = new Composite(composite, SWT.NONE);
		providerInfoComposite.setBackground(parent.getBackground());

		GridLayout gl_providerInfoComposite = new GridLayout(2, false);
		gl_providerInfoComposite.marginHeight = 0;
		gl_providerInfoComposite.marginWidth = 0;
		providerInfoComposite.setLayout(gl_providerInfoComposite);
		providerInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 3, 1));

		Label lblProviderId = new Label(providerInfoComposite, SWT.NONE);
		lblProviderId.setBackground(parent.getBackground());
		lblProviderId.setText("ID:");

		providerId = new Label(providerInfoComposite, SWT.NONE);
		providerId.setBackground(parent.getBackground());
		providerId.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		providerId.setSize(76, 21);
		providerId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblProviderType = new Label(providerInfoComposite, SWT.NONE);
		lblProviderType.setBackground(parent.getBackground());
		lblProviderType.setSize(28, 15);
		lblProviderType.setText("Type:");

		providerType = new Label(providerInfoComposite, SWT.NONE);
		providerType.setBackground(parent.getBackground());
		providerType.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		providerType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		providerType.setSize(18, 21);

		Label lblProviderServices = new Label(providerInfoComposite, SWT.NONE);
		lblProviderServices.setBackground(parent.getBackground());
		lblProviderServices.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProviderServices.setText("Servers:");

		providerServices = new Label(providerInfoComposite, SWT.NONE);
		providerServices.setBackground(parent.getBackground());
		providerServices.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		providerServices.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnDeployProcess = new Button(wrapperComposite, SWT.NONE);
		btnDeployProcess.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deploySOAApplication();
			}
		});
		btnDeployProcess.setText("Deploy SOA Application");

	}

	private void deploySOAApplication() {

		try {

			final String processName = ivModelAccessor.getModel() instanceof StructuredActivityNode ? ((StructuredActivityNode) ivModelAccessor.getModel()).getName() : ((Activity) ivModelAccessor.getModel()).getName();

			final String currentProject = ProcessNodeHelper.getCurrentProjectSelection();

			final NavigationProcessNode navigationProcessNode = ProcessNodeHelper.getProcess(currentProject, processName);

			final DeploymentService deploymentService = DeploymentServiceLocator.get();

			final ObjectHolder<Deployment> deploymentHolder = new ObjectHolder<Deployment>();

			new ProgressMonitorDialog(getShell()).run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Deploying SOA Application", IProgressMonitor.UNKNOWN);
						try {
							final DeploymentRequest deploymentRequest = DeploymentUtil.getDeploymentRequest(navigationProcessNode);
							deploymentHolder.set(deploymentService.deployApplication(deploymentRequest));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} finally {
						monitor.done();
					}
				}
			});

			final Deployment deployment = deploymentHolder.get();

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			if (deployment != null && !deployment.isError()) {
				MessageDialog.openInformation(shell, "Application Deployment", "The process '" + deployment.getBusinessProcessName() + "' was successfully deployed.");
			} else {
				MessageDialog.openError(shell, "Application Deployment", "Unable to deploy the business process");
			}

		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), "Infrastructure Providers", "Unable to deploy the business process");
		}
	}

	public void selectProvider(InfrastructureProvider p) {
		writeInfrastructureProvider(p);
		updateValues();

	}

	private BMInfrastructureProvider writeInfrastructureProvider(InfrastructureProvider p) {
		BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);
		processInformation.setInfrastructureProvider(new BMInfrastructureProvider(p));
		processInformation.write(ivGeneralModelAccessor);
		return processInformation.getInfrastructureProvider();
	}

	private void updateValues() {

		BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

		BMInfrastructureProvider infrastructureProvider = processInformation.getInfrastructureProvider();

		if (infrastructureProvider != null) {
			txtInfrastructureProvider.setText(infrastructureProvider.getName());
			providerInfoComposite.setVisible(true);
			providerId.setText(infrastructureProvider.getId());
			providerType.setText(infrastructureProvider.getServerType());

			Set<String> servers = new TreeSet<String>();

			if (!CollectionUtils.isEmpty(infrastructureProvider.getDeploymentServers())) {
				for (BMDeploymentServer server : infrastructureProvider.getDeploymentServers()) {
					servers.add(server.getAddress());
				}
			}

			if (!CollectionUtils.isEmpty(infrastructureProvider.getOrchestratorServers())) {
				for (BMDeploymentServer server : infrastructureProvider.getOrchestratorServers()) {
					servers.add(server.getAddress());
				}
			}

			providerServices.setText(StringUtils.join(servers, ", "));

		} else {
			providerInfoComposite.setVisible(false);
			providerId.setText("");
			providerType.setText("");
			providerServices.setText("");
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setModelAccessor(ModelAccessor modelAccessor) {
		if (modelAccessor != null) {
			ivModelAccessor = modelAccessor;
			ivGeneralModelAccessor = new GeneralModelAccessor(modelAccessor);
			updateValues();
		}
	}
}
