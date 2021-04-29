package br.ufsc.gsigma.catalog.plugin.modules.imprt;

import java.io.File;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.ibm.btools.blm.ui.importExport.AbstractBLMImportDetailsPage;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.util.CatalogServiceUtil;
import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;

public class ImportWizardContributorPage extends AbstractBLMImportDetailsPage {

	private static final String PROCESSES_FROM_STANDARDS_SPECIFICATIONS = "Processes from Standard Specifications";
	private static final String USER_STORED_PROCESSES = "User stored processes";

	private TabFolder importSourceTypeTabFolder;

	private HashMap<String, Object> dynamicOptions = new HashMap<String, Object>();

	protected CatalogService catalog;

	protected Composite projectSelectionComposite;
	protected CCombo projectSelectionCombo;

	private ImportProcessSpecificationUI importProcessSpecificationUI = new ImportProcessSpecificationUI(this);
	private ImportUserStoredProcessUI importUserStoredProcessUI = new ImportUserStoredProcessUI(this);

	public ImportWizardContributorPage() {
		super("Business Process Catalog", "Import a Process from a Business Catalog", new WidgetFactory());
		// setDescription("UIMessages.ExcelImportOptionsDesc");

		getDynamicOptions().put("IMPORT_MANAGER", new ProcessCatalogImportManager());
	}

	protected ProcessCatalogImportManager getImportManager() {
		return (ProcessCatalogImportManager) getDynamicOptions().get("IMPORT_MANAGER");
	}

	@Override
	public HashMap<String, Object> getDynamicOptions() {
		return dynamicOptions;
	}

	protected void createClientArea(Composite paramComposite) {

		try {

			paramComposite.setLayoutData(new GridLayout(1, false));

			((Wizard) getWizard()).setNeedsProgressMonitor(true);

			importSourceTypeTabFolder = new TabFolder(paramComposite, SWT.BORDER);
			importSourceTypeTabFolder.setVisible(false);
			importSourceTypeTabFolder.setLayout(new GridLayout(1, false));
			importSourceTypeTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

			importSourceTypeTabFolder.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {

					if (catalog != null && importSourceTypeTabFolder.getSelectionIndex() == 0) {

						importSourceTypeTabFolder.setEnabled(false);
						importProcessSpecificationUI.load();
						importSourceTypeTabFolder.setEnabled(true);

						getWizard().getContainer().updateButtons();

					} else if (catalog != null && importSourceTypeTabFolder.getSelectionIndex() == 1) {

						importSourceTypeTabFolder.setEnabled(false);
						importUserStoredProcessUI.load();
						importSourceTypeTabFolder.setEnabled(true);

						getWizard().getContainer().updateButtons();
					}
				}

				public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
				}
			});

			Composite tabProcessFromSpecificationComposite = getWidgetFactory().createComposite(importSourceTypeTabFolder);
			importProcessSpecificationUI.createUI(tabProcessFromSpecificationComposite);
			TabItem tabProcessFromSpecification = new TabItem(importSourceTypeTabFolder, SWT.NONE);
			tabProcessFromSpecification.setText(PROCESSES_FROM_STANDARDS_SPECIFICATIONS);
			tabProcessFromSpecificationComposite.setLayout(new GridLayout(1, false));
			tabProcessFromSpecificationComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			tabProcessFromSpecification.setControl(tabProcessFromSpecificationComposite);

			Composite tabUserStoredProcessComposite = getWidgetFactory().createComposite(importSourceTypeTabFolder);
			importUserStoredProcessUI.createUI(tabUserStoredProcessComposite);
			TabItem tabUserStoredProcess = new TabItem(importSourceTypeTabFolder, SWT.NONE);
			tabUserStoredProcess.setText(USER_STORED_PROCESSES);
			tabUserStoredProcessComposite.setLayout(new GridLayout(1, false));
			tabUserStoredProcessComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			tabUserStoredProcess.setControl(tabUserStoredProcessComposite);

			getWidgetFactory().paintBordersFor(paramComposite);

			createProjectSelection(paramComposite, getBLMProjectCreationHelper().getInitialProject(), getBLMProjectCreationHelper().getExistingBLMProjects(), "Target Project");

			projectSelectionComposite.setVisible(false);

			try {

				catalog = CatalogServiceUtil.getCatalogService();

				importSourceTypeTabFolder.setEnabled(false);
				importProcessSpecificationUI.load();
				importSourceTypeTabFolder.setEnabled(true);

				projectSelectionComposite.setVisible(false);

				importSourceTypeTabFolder.setVisible(true);

				setErrorMessage(null);

			} catch (Throwable e) {
				e.printStackTrace();

				importSourceTypeTabFolder.setVisible(false);
				projectSelectionComposite.setVisible(false);
				setErrorMessage("Could not connect to the Catalog Service");
			}

			setControl(paramComposite);

		} catch (Throwable e) {
			e.printStackTrace();
		}

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
		GridLayout localGridLayout = new GridLayout();
		localGridLayout.numColumns = 1;
		projectSelectionComposite.setLayout(localGridLayout);
		GridData localGridData1 = new GridData(768);
		localGridData1.verticalAlignment = 8;
		projectSelectionComposite.setLayoutData(localGridData1);
		Label localLabel = getWidgetFactory().createLabel(projectSelectionComposite, label);
		GridData localGridData2 = new GridData(768);
		localLabel.setLayoutData(localGridData2);

		Composite localComposite1 = getWidgetFactory().createComposite(projectSelectionComposite);
		localGridLayout = new GridLayout();
		localGridLayout.numColumns = 2;
		localComposite1.setLayout(localGridLayout);
		localComposite1.setLayoutData(new GridData(1808));

		Composite localComposite2 = getWidgetFactory().createComposite(localComposite1);
		localGridLayout = new GridLayout();
		localGridLayout.marginHeight = 2;
		localGridLayout.marginWidth = 2;
		localComposite2.setLayout(localGridLayout);
		localComposite2.setLayoutData(new GridData(768));
		projectSelectionCombo = getWidgetFactory().createCCombo(localComposite2, 8);
		projectSelectionCombo.setLayoutData(new GridData(768));
		projectSelectionCombo.setItems(existingProjects);
		if ((initialProject != null) && (initialProject.length() > 0)) {
			projectSelectionCombo.setText(initialProject);
		}

		projectSelectionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		getWidgetFactory().paintBordersFor(localComposite2);

		Button createNewProjectBtn = getWidgetFactory().createButton(localComposite1, "New Project", 8);
		createNewProjectBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				String str = getBLMProjectCreationHelper().getProjectCreator().createBLMProject(getBLMProjectCreationHelper().getNavigationRootNode(), false);
				projectSelectionCombo.setText(str);
				getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

	}

	@Override
	public File[] getSelectedFiles() {
		return null;
	}

	@Override
	public String getSelectedProject() {
		return projectSelectionCombo.getText();
	}

	@Override
	public boolean canFinish() {

		if (importSourceTypeTabFolder.getSelectionIndex() == 0) {

			if (!importProcessSpecificationUI.processSpecificationComboComposite.getVisible())
				return false;

			if (!importProcessSpecificationUI.processesTreeComposite.getVisible())
				return false;

			if (getImportManager().getImportingFromProcessSpecificationSelectedProcess() == null)
				return false;

			if (!projectSelectionComposite.getVisible())
				return false;

			return (projectSelectionCombo.getText().length() >= 1);

		} else if (importSourceTypeTabFolder.getSelectionIndex() == 1) {

			if (!importUserStoredProcessUI.categoryComboComposite.getVisible())
				return false;

			if (!importUserStoredProcessUI.processesTreeComposite.getVisible())
				return false;

			if (!importUserStoredProcessUI.processListComposite.getVisible())
				return false;

			if (importUserStoredProcessUI.mapProcessListIndexId.get(importUserStoredProcessUI.processList.getSelectionIndex()) == null)
				return false;

			if (!projectSelectionComposite.getVisible())
				return false;

			return (projectSelectionCombo.getText().length() >= 1);
		}

		return false;

	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
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

}