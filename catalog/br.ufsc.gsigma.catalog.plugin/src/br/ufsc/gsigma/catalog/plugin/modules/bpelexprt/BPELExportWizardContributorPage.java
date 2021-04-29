package br.ufsc.gsigma.catalog.plugin.modules.bpelexprt;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.btools.blm.ui.importExport.AbstractBLMImportExportDetailsBasePage;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.util.CatalogServiceUtil;
import br.ufsc.gsigma.catalog.plugin.util.ProcessNodeHelper;
import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;

public class BPELExportWizardContributorPage extends AbstractBLMImportExportDetailsBasePage {

	private static final Logger logger = LoggerFactory.getLogger(BPELExportWizardContributorPage.class);

	private Composite projectSelectionComposite;
	private CCombo projectSelectionCombo;

	private Composite processExecutionSelectionComposite;
	private CCombo processExecutionSelectionCombo;

	private Composite processSelectionComposite;
	private CCombo processSelectionCombo;

	private Composite chooseProcessNameComposite;
	private Text processNameText;

	private List<NavigationProcessNode> projectProcessesNodes = new ArrayList<NavigationProcessNode>();

	private CatalogService catalog;

	private HashMap<String, Object> dynamicOptions = new HashMap<String, Object>();

	// private Text folderNameText;
	// private String defaultFolderName = "c:\\";

	public BPELExportWizardContributorPage() {
		super("Business Process Catalog", "Export a Process to Business Process Execution Language (BPEL)", new WidgetFactory());

		// Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		getDynamicOptions().put("EXPORT_MANAGER", new BPELExportManager());
	}

	private BPELExportManager getExportManager() {
		return (BPELExportManager) getDynamicOptions().get("EXPORT_MANAGER");
	}

	@Override
	protected void createClientArea(Composite parentComposite) {

		try {

			((Wizard) getWizard()).setNeedsProgressMonitor(true);

			GridData gridDataFillHorizontalWithSpace = new GridData(GridData.FILL_HORIZONTAL);
			gridDataFillHorizontalWithSpace.horizontalIndent = 2;

			GridData gridDataFillBothWithSpace = new GridData(GridData.FILL_BOTH);
			gridDataFillBothWithSpace.horizontalIndent = 2;

			String currentProjectSelection = ProcessNodeHelper.getCurrentProjectSelection();
			String[] existingProjects = ProcessNodeHelper.getExistingProjects();

			createProjectSelection(parentComposite, currentProjectSelection, existingProjects, "Choose the Project");
			createProcessSelection(parentComposite, "Choose the Process");

			createProcessExecutionSelection(parentComposite, "Choose the Process Execution Environment");

			createChooseProcessNameComposite(parentComposite, "Choose the Process Name that will be used in the execution environment");

			// createChooseDestinationFolder(parentComposite, "Choose the folder where you want to store the exported BPEL process.");

			if (currentProjectSelection != null)
				setProjectProcesses(currentProjectSelection);

			getWidgetFactory().paintBordersFor(parentComposite);

			// folderNameText.setText(defaultFolderName);
			// getExportManager().setFolderName(defaultFolderName);

			try {
				catalog = CatalogServiceUtil.getCatalogService();
				setErrorMessage(null);
			} catch (Exception e) {
				chooseProcessNameComposite.setVisible(false);
				setErrorMessage("Could not connect to the Catalog Service");
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}

		setControl(parentComposite);
	}

	private void createProjectSelection(Composite paramComposite, String initialProject, String[] existingProjects, String label) {

		String[] existingAndNewProjects = new String[existingProjects.length];
		int i = 0;
		while (i < existingProjects.length) {
			existingAndNewProjects[i] = existingProjects[i];
			++i;
		}
		Arrays.sort(existingAndNewProjects, Collator.getInstance());

		projectSelectionComposite = getWidgetFactory().createComposite(paramComposite);
		projectSelectionComposite.setLayout(new GridLayout(1, false));
		projectSelectionComposite.setLayoutData(new GridData(768));
		Label localLabel = getWidgetFactory().createLabel(projectSelectionComposite, label);
		localLabel.setLayoutData(new GridData(768));

		projectSelectionCombo = getWidgetFactory().createCCombo(projectSelectionComposite, 8);
		projectSelectionCombo.setLayoutData(new GridData(768));
		projectSelectionCombo.setItems(existingProjects);
		if ((initialProject != null) && (initialProject.length() > 0)) {
			projectSelectionCombo.setText(initialProject);
		}

		projectSelectionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {

				if (projectSelectionCombo.getText().length() > 0) {
					setProjectProcesses(projectSelectionCombo.getText());
					setErrorMessage(null);
				}
				getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent event) {

			}
		});

		getWidgetFactory().paintBordersFor(projectSelectionComposite);

	}

	private void createProcessSelection(Composite paramComposite, String label) {

		processSelectionComposite = getWidgetFactory().createComposite(paramComposite);
		processSelectionComposite.setLayout(new GridLayout(1, false));
		processSelectionComposite.setLayoutData(new GridData(768));

		Label localLabel = getWidgetFactory().createLabel(processSelectionComposite, label);
		localLabel.setLayoutData(new GridData(768));

		processSelectionCombo = getWidgetFactory().createCCombo(processSelectionComposite, 8);
		processSelectionCombo.setLayoutData(new GridData(768));

		processSelectionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {

				if (processSelectionCombo.getText().length() > 0) {
					processNameText.setText(processSelectionCombo.getText());
					getExportManager().setProcessName(processNameText.getText());

					getExportManager().setProcessNode(projectProcessesNodes.get(processSelectionCombo.getSelectionIndex()));
					setErrorMessage(null);
				}

				getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		getWidgetFactory().paintBordersFor(processSelectionComposite);

	}

	private void createProcessExecutionSelection(Composite paramComposite, String label) {

		processExecutionSelectionComposite = getWidgetFactory().createComposite(paramComposite);
		processExecutionSelectionComposite.setLayout(new GridLayout(1, false));
		processExecutionSelectionComposite.setLayoutData(new GridData(768));

		Label localLabel = getWidgetFactory().createLabel(processExecutionSelectionComposite, label);
		localLabel.setLayoutData(new GridData(768));

		processExecutionSelectionCombo = getWidgetFactory().createCCombo(processExecutionSelectionComposite, 8);
		processExecutionSelectionCombo.setLayoutData(new GridData(768));

		processExecutionSelectionCombo.clearSelection();
		processExecutionSelectionCombo.add("BPEL Process Execution Engine - ODE");

		processExecutionSelectionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {

				if (processExecutionSelectionCombo.getText() != null && !processExecutionSelectionCombo.getText().isEmpty()) {
					chooseProcessNameComposite.setVisible(true);
					getExportManager().setProcessName(processNameText.getText());
				} else
					chooseProcessNameComposite.setVisible(false);

				getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		getWidgetFactory().paintBordersFor(processExecutionSelectionComposite);

	}

	private void createChooseProcessNameComposite(Composite paramComposite, String label) {

		chooseProcessNameComposite = getWidgetFactory().createComposite(paramComposite);
		chooseProcessNameComposite.setVisible(false);
		chooseProcessNameComposite.setLayout(new GridLayout(1, false));
		chooseProcessNameComposite.setLayoutData(new GridData(768));

		Label localLabel = getWidgetFactory().createLabel(chooseProcessNameComposite, label);
		localLabel.setLayoutData(new GridData(768));

		processNameText = getWidgetFactory().createText(chooseProcessNameComposite, 0);
		processNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		processNameText.setEditable(true);
		processNameText.setText(projectSelectionCombo.getText());

		processNameText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent evt) {
				getExportManager().setProcessName(processNameText.getText());
				getWizard().getContainer().updateButtons();
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});

		getWidgetFactory().paintBordersFor(chooseProcessNameComposite);

	}

	// private void createChooseDestinationFolder(Composite paramComposite, String label) {
	//
	// chooseProcessNameComposite = getWidgetFactory().createComposite(paramComposite);
	// chooseProcessNameComposite.setVisible(false);
	//
	// chooseProcessNameComposite.setLayout(new GridLayout(1, false));
	// chooseProcessNameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	// getWidgetFactory().createLabel(chooseProcessNameComposite, label);
	//
	// Composite composite = getWidgetFactory().createComposite(chooseProcessNameComposite);
	// composite.setLayout(new GridLayout(2, false));
	// composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	//
	// folderNameText = getWidgetFactory().createText(composite, 0);
	// folderNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	// folderNameText.setEditable(true);
	//
	// // folderNameText.setEditable(false);
	//
	// folderNameText.addKeyListener(new KeyListener() {
	// @Override
	// public void keyReleased(KeyEvent evt) {
	// getExportManager().setFolderName(folderNameText.getText());
	// }
	//
	// @Override
	// public void keyPressed(KeyEvent arg0) {
	// }
	// });
	//
	// final Button chooseFolderButton = getWidgetFactory().createButton(composite, "Choose", 0);
	// chooseFolderButton.addSelectionListener(new SelectionListener() {
	// public void widgetSelected(SelectionEvent ev) {
	// DirectoryDialog dialog = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
	// dialog.setFilterPath(defaultFolderName);
	// getExportManager().setFolderName(dialog.open());
	// folderNameText.setText(getExportManager().getFolderName());
	// getWizard().getContainer().updateButtons();
	// }
	//
	// @Override
	// public void widgetDefaultSelected(SelectionEvent arg0) {
	// }
	// });
	//
	// getWidgetFactory().paintBordersFor(composite);
	// }

	private void setProjectProcesses(String projectName) {

		projectProcessesNodes.clear();
		processSelectionCombo.removeAll();

		int i = 0;

		for (NavigationProcessNode processNode : ProcessNodeHelper.getProcesses(projectName)) {
			projectProcessesNodes.add(processNode);
			processSelectionCombo.add(processNode.getLabel());

			if (i++ == 0) {
				getExportManager().setProcessNode(processNode);
				processSelectionCombo.setText(processNode.getLabel());

				if (processNameText != null) {
					processNameText.setText(processNode.getLabel());
					getExportManager().setProcessName(processNode.getLabel());
				}
			}
		}

	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public boolean canFinish() {

		boolean b1 = projectSelectionCombo.getText().length() > 0;
		boolean b2 = processSelectionCombo.getText().length() > 0;
		boolean b3 = catalog != null;
		boolean b4 = getExportManager().getProcessNode() != null;
		boolean b5 = getExportManager().getProcessName() != null && !getExportManager().getProcessName().trim().isEmpty();
		boolean b6 = getErrorMessage() == null;
		boolean b7 = projectSelectionComposite.isVisible();
		boolean b8 = processSelectionComposite.isVisible();
		boolean b9 = chooseProcessNameComposite.isVisible();

		return b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9;
	}

	@Override
	protected void directorySelectionChangedDirectly() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processDirectorySelectionChange(String paramString) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setSavedDirectoriesIntoControls() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectedType(int paramInt) {
		// TODO Auto-generated method stub

	}

	@Override
	public HashMap<String, Object> getDynamicOptions() {
		return dynamicOptions;
	}
}