package br.ufsc.gsigma.catalog.plugin.modules.imprt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import br.ufsc.gsigma.catalog.plugin.modules.imprt.ProcessCatalogImportManager.IMPORT_MODE;
import br.ufsc.gsigma.catalog.services.model.ProcessCategory;
import br.ufsc.gsigma.catalog.services.model.ProcessTreeDescription;
import br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo;

public class ImportUserStoredProcessUI {

	protected Composite categoryComboComposite;
	private CCombo categoryCombo;
	private org.eclipse.swt.widgets.Tree processesTree;
	protected Composite processesTreeComposite;

	private List<ProcessTreeDescription> listProcessesTreeDescription;

	private Text processFilterTextField;

	private Map<Integer, Integer> mapCategoryComboIndexId = new HashMap<Integer, Integer>();

	protected Composite processListComposite;
	protected org.eclipse.swt.widgets.List processList;

	protected Map<Integer, Integer> mapProcessListIndexId = new HashMap<Integer, Integer>();

	private ImportWizardContributorPage importWizardContributorPage;

	protected ImportUserStoredProcessUI(ImportWizardContributorPage importWizardContributorPage) {
		this.importWizardContributorPage = importWizardContributorPage;

	}

	protected void load() {

		categoryCombo.removeAll();
		mapCategoryComboIndexId.clear();

		importWizardContributorPage.getImportManager().setImportMode(IMPORT_MODE.FROM_USER_STORED_PROCESS);

		importWizardContributorPage.getImportManager().setCatalogService(importWizardContributorPage.catalog);

		List<ProcessCategory> categories = importWizardContributorPage.catalog.getProcessCategories();

		int i = 0;
		for (ProcessCategory c : categories) {
			categoryCombo.add(c.getName(), i);
			mapCategoryComboIndexId.put(i++, c.getId());
		}

		categoryCombo.setText(categories.size() + " available categories");
		categoryComboComposite.setVisible(true);

		processesTreeComposite.setVisible(false);
		processListComposite.setVisible(false);
		importWizardContributorPage.projectSelectionComposite.setVisible(false);

	}

	protected void createUI(Composite tabUserStoredProcessComposite) {

		// //Escolha da Categoria
		categoryComboComposite = importWizardContributorPage.getWidgetFactory().createComposite(tabUserStoredProcessComposite);
		categoryComboComposite.setVisible(false);

		categoryComboComposite.setLayout(new GridLayout(1, false));
		categoryComboComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		importWizardContributorPage.getWidgetFactory().createLabel(categoryComboComposite, "Choose one of the process categories:");

		Composite composite = importWizardContributorPage.getWidgetFactory().createComposite(categoryComboComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		categoryCombo = importWizardContributorPage.getWidgetFactory().createCombo(composite);
		categoryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		categoryCombo.setEditable(false);
		categoryCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				if (categoryCombo.getText() != null) {

					listProcessesTreeDescription = importWizardContributorPage.catalog
							.getListProcessTreeDescription(mapCategoryComboIndexId.get(categoryCombo.getSelectionIndex()));

					processesTree.removeAll();

					buildTreeItemFromProcessDescription(processesTree, listProcessesTreeDescription, false);

					processesTreeComposite.setVisible(true);
					processListComposite.setVisible(false);
					importWizardContributorPage.projectSelectionComposite.setVisible(false);

					importWizardContributorPage.setErrorMessage(null);

				}
				importWizardContributorPage.getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});
		importWizardContributorPage.getWidgetFactory().paintBordersFor(composite);

		// Árvore de Processos
		processesTreeComposite = importWizardContributorPage.getWidgetFactory().createComposite(tabUserStoredProcessComposite);
		processesTreeComposite.setVisible(false);
		processesTreeComposite.setLayout(new GridLayout(1, false));
		processesTreeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		importWizardContributorPage.getWidgetFactory().createLabel(processesTreeComposite,
				"Choose one of the available processes categories:");

		composite = importWizardContributorPage.getWidgetFactory().createComposite(processesTreeComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		processesTree = new org.eclipse.swt.widgets.Tree(composite, SWT.BORDER);
		processesTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		processesTree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				boolean isLeafSelected = processesTree.getSelection().length == 1 && processesTree.getSelection()[0].getItemCount() == 0;

				importWizardContributorPage.projectSelectionComposite.setVisible(isLeafSelected);

				if (isLeafSelected) {
					ProcessTreeDescription treeDescription = (ProcessTreeDescription) processesTree.getSelection()[0].getData();

					try {

						List<ProcessInfo> list = importWizardContributorPage.catalog.getListProcessInfo(treeDescription.getId());

						processList.removeAll();
						mapProcessListIndexId.clear();

						int i = 0;

						if (list != null) {
							for (ProcessInfo info : list) {
								processList.add(info.getName());
								mapProcessListIndexId.put(i++, info.getId());
							}
						}

						importWizardContributorPage.setErrorMessage(null);
						processListComposite.setVisible(true);
						importWizardContributorPage.projectSelectionComposite.setVisible(false);
					} catch (Throwable ex) {
						ex.printStackTrace();

						importWizardContributorPage.setErrorMessage(ex.getMessage());
						processListComposite.setVisible(false);
						importWizardContributorPage.getImportManager().setImportingFromUserStoredProcessSelectedProcessId(null);
						importWizardContributorPage.projectSelectionComposite.setVisible(false);

						processList.removeAll();
						mapProcessListIndexId.clear();
					}

				}

				importWizardContributorPage.getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		processListComposite = importWizardContributorPage.getWidgetFactory().createComposite(tabUserStoredProcessComposite);
		processListComposite.setVisible(false);
		processListComposite.setLayout(new GridLayout(1, false));
		processListComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		importWizardContributorPage.getWidgetFactory().createLabel(processListComposite, "Choose one of the available processes below:");

		composite = importWizardContributorPage.getWidgetFactory().createComposite(processListComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		processList = new org.eclipse.swt.widgets.List(composite, SWT.SINGLE);
		processList.setLayoutData(new GridData(GridData.FILL_BOTH));
		processList.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				if (processList.getSelectionCount() > 0) {

					importWizardContributorPage.getImportManager().setImportingFromUserStoredProcessSelectedProcessId(
							mapProcessListIndexId.get(processList.getSelectionIndex()));

					importWizardContributorPage.projectSelectionComposite.setVisible(true);

					importWizardContributorPage.setErrorMessage(null);

				}
				importWizardContributorPage.getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});
		importWizardContributorPage.getWidgetFactory().paintBordersFor(composite);

	}

	@SuppressWarnings("unused")
	private void buildSearch(Composite composite) {
		Composite searchComposite = importWizardContributorPage.getWidgetFactory().createComposite(composite);
		searchComposite.setLayout(new GridLayout(2, false));
		searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		importWizardContributorPage.getWidgetFactory().createLabel(searchComposite, "Search for:");
		processFilterTextField = importWizardContributorPage.getWidgetFactory().createText(searchComposite, SWT.BORDER);
		processFilterTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		processFilterTextField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				processesTree.removeAll();

				buildTreeItemFromProcessDescription(processesTree, filterProcessDescriptionByText(listProcessesTreeDescription,
						processFilterTextField.getText()), (processFilterTextField.getText().trim().length() > 0));

			}
		});
	}

	private List<TreeItem> buildTreeItemFromProcessDescription(Object treeParent, List<ProcessTreeDescription> list, boolean expand) {
		List<TreeItem> result = new ArrayList<TreeItem>();
		for (ProcessTreeDescription p : list) {
			TreeItem item;

			if (treeParent instanceof Tree)
				item = new TreeItem((Tree) treeParent, 0);
			else
				item = new TreeItem((TreeItem) treeParent, 0);

			item.setData(p);
			item.setText(p.getName());

			if (p.getChilds().size() > 0)
				buildTreeItemFromProcessDescription(item, p.getChilds(), expand);

			result.add(item);

			item.setExpanded(expand);

		}

		return result;
	}

	private List<ProcessTreeDescription> filterProcessDescriptionByText(List<ProcessTreeDescription> allItems, String text) {

		List<ProcessTreeDescription> filteredItems = new ArrayList<ProcessTreeDescription>();

		for (ProcessTreeDescription item : allItems) {

			if (text == null || text.trim().length() == 0 || item.getName().toLowerCase().contains(text.toLowerCase()))
				filteredItems.add(item);
			else if (item.getChilds().size() > 0) {

				if (filterProcessDescriptionByText(item.getChilds(), text).size() > 0)
					filteredItems.add(item);
			}

		}

		return filteredItems;

	}

}
