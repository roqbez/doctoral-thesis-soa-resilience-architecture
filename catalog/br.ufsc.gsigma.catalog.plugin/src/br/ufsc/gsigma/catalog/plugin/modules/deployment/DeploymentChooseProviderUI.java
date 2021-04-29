package br.ufsc.gsigma.catalog.plugin.modules.deployment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import br.ufsc.gsigma.catalog.plugin.util.ui.UIUtil;
import br.ufsc.gsigma.catalog.services.model.DeploymentServer;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;
import br.ufsc.gsigma.services.deployment.locator.DeploymentServiceLocator;

public class DeploymentChooseProviderUI extends Dialog {

	protected Object result;

	protected Shell shlChooseProviderService;

	private List<InfrastructureProvider> providers = new LinkedList<InfrastructureProvider>();

	private Table table;
	private TableColumn tblclmnId;
	private TableColumn tblclmnName;
	private TableColumn tblclmnType;
	private TableColumn tblclmnAvailServers;

	private DeploymentUI deploymentUI;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public DeploymentChooseProviderUI(Shell parent, DeploymentUI deploymentUI) {
		super(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		this.deploymentUI = deploymentUI;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {

		Display display = getParent().getDisplay();

		createContents();

		// Center
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shlChooseProviderService.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shlChooseProviderService.setLocation(x, y);

		shlChooseProviderService.open();
		shlChooseProviderService.layout();

		populateTable();

		while (!shlChooseProviderService.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	public void dispose() {
		shlChooseProviderService.dispose();
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlChooseProviderService = new Shell(getParent(), SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		shlChooseProviderService.setText("Choose the Provider Service");
		shlChooseProviderService.setSize(1000, 300);
		shlChooseProviderService.setMinimumSize(shlChooseProviderService.getSize().x, shlChooseProviderService.getSize().y);
		shlChooseProviderService.setLayout(new GridLayout(1, false));
		shlChooseProviderService.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shlChooseProviderService.setBackgroundMode(SWT.INHERIT_FORCE);

		shlChooseProviderService.addControlListener(new ControlListener() {
			private boolean autosizing = false;

			@Override
			public void controlResized(ControlEvent arg0) {

				if (autosizing)
					return;
				autosizing = true;
				try {
					UIUtil.autoSizeColumns(table);
				} finally {
					autosizing = false;
				}
			}

			@Override
			public void controlMoved(ControlEvent arg0) {
			}
		});

		shlChooseProviderService.setImage(getParent().getImage());

		table = new Table(shlChooseProviderService, SWT.BORDER | SWT.NO_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = table.getSelectionIndex();
				selectProvider(i);
			}
		});

		tblclmnId = new TableColumn(table, SWT.NONE);
		tblclmnId.setWidth(400);
		tblclmnId.setText("ID");

		tblclmnName = new TableColumn(table, SWT.NONE);
		tblclmnName.setWidth(300);
		tblclmnName.setText("Name");

		tblclmnType = new TableColumn(table, SWT.NONE);
		tblclmnType.setWidth(250);
		tblclmnType.setText("Type");

		tblclmnAvailServers = new TableColumn(table, SWT.NONE);
		tblclmnAvailServers.setWidth(350);
		tblclmnAvailServers.setText("Available Servers");

		loadProviders();

	}

	private void loadProviders() {
		try {
			new ProgressMonitorDialog(shlChooseProviderService).run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					monitor.beginTask("Loading Infrastructure Providers", IProgressMonitor.UNKNOWN);

					providers.clear();

					DeploymentService deploymentService = DeploymentServiceLocator.get();

					providers.addAll(deploymentService.getInfrastructureProviders());

					try {
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						monitor.done();
					}
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(shlChooseProviderService, "Infrastructure Providers", "Unable to discover infrastructure providers");
		}
	}

	private void selectProvider(int i) {
		InfrastructureProvider p = providers.get(i);
		deploymentUI.selectProvider(p);
		shlChooseProviderService.dispose();
	}

	private void populateTable() {

		for (InfrastructureProvider p : providers) {

			TableItem item = new TableItem(table, SWT.CENTER);

			item.setText(0, p.getId());
			item.setText(1, p.getName());
			item.setText(2, String.valueOf(p.getServerType()));

			List<String> deploymentServers = new ArrayList<String>();

			if (!CollectionUtils.isEmpty(p.getDeploymentServers())) {
				for (DeploymentServer d : p.getDeploymentServers()) {
					deploymentServers.add(d.getAddress());
				}
			}

			item.setText(3, StringUtils.join(deploymentServers, ", "));

		}

		UIUtil.autoSizeColumns(table);
	}

}
