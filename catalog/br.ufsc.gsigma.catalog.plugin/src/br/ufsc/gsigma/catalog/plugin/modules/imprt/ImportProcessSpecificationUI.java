package br.ufsc.gsigma.catalog.plugin.modules.imprt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import br.ufsc.gsigma.catalog.plugin.modules.imprt.ProcessCatalogImportManager.IMPORT_MODE;
import br.ufsc.gsigma.catalog.services.model.ProcessStandard;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaxonomy;

public class ImportProcessSpecificationUI {

	protected Composite processSpecificationComboComposite;
	private CCombo processSpecificationCombo;
	private org.eclipse.swt.widgets.Tree processesTree;
	protected Composite processesTreeComposite;

	private ProcessTaxonomy processTaxonomy;

	private Map<Integer, String> mapProcessSpecificationComboIndexId = new HashMap<Integer, String>();

	private ImportWizardContributorPage importWizardContributorPage;

	protected ImportProcessSpecificationUI(ImportWizardContributorPage importWizardContributorPage) {
		this.importWizardContributorPage = importWizardContributorPage;

	}

	protected void load() {

		try {

			processSpecificationCombo.removeAll();
			mapProcessSpecificationComboIndexId.clear();

			importWizardContributorPage.getImportManager().setImportMode(IMPORT_MODE.FROM_PROCESS_SPECIFICATION);

			importWizardContributorPage.getImportManager().setCatalogService(importWizardContributorPage.catalog);

			List<ProcessStandard> processStandards = importWizardContributorPage.catalog.getListProcessStandard();

			int i = 0;
			for (ProcessStandard c : processStandards) {
				processSpecificationCombo.add(c.getName(), i);
				mapProcessSpecificationComboIndexId.put(i++, c.getId());
			}

			processSpecificationCombo.setText(processStandards.size() + " available process specifications");
			processSpecificationComboComposite.setVisible(true);

			processesTreeComposite.setVisible(false);
			importWizardContributorPage.projectSelectionComposite.setVisible(false);

			importWizardContributorPage.getImportManager().setImportingFromProcessSpecificationSelectedProcess(null);

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	protected void createUI(Composite tabProcessFromSpecificationComposite) {

		// //Escolha da Categoria
		processSpecificationComboComposite = importWizardContributorPage.getWidgetFactory().createComposite(
				tabProcessFromSpecificationComposite);
		processSpecificationComboComposite.setVisible(false);

		processSpecificationComboComposite.setLayout(new GridLayout(1, false));
		processSpecificationComboComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		importWizardContributorPage.getWidgetFactory().createLabel(processSpecificationComboComposite,
				"Choose one of the process specifications:");

		Composite composite = importWizardContributorPage.getWidgetFactory().createComposite(processSpecificationComboComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		processSpecificationCombo = importWizardContributorPage.getWidgetFactory().createCombo(composite);
		processSpecificationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		processSpecificationCombo.setEditable(false);
		processSpecificationCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				if (processSpecificationCombo.getText() != null) {

					try {

						processTaxonomy = importWizardContributorPage.catalog.getProcessStandardSpecification(
								mapProcessSpecificationComboIndexId.get(processSpecificationCombo.getSelectionIndex()))
								.getProcessTaxonomy();

						processesTree.removeAll();

						buildTreeItemFromProcessStandardCategory(processesTree, processTaxonomy.getChilds(), false);

						processesTreeComposite.setVisible(true);
						importWizardContributorPage.projectSelectionComposite.setVisible(false);
						importWizardContributorPage.setErrorMessage(null);

						importWizardContributorPage.getImportManager().setImportingFromProcessSpecificationSelectedProcess(null);

					} catch (Throwable ex) {
						ex.printStackTrace();
					}

				}
				importWizardContributorPage.getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});
		importWizardContributorPage.getWidgetFactory().paintBordersFor(composite);

		// Árvore de Processos
		processesTreeComposite = importWizardContributorPage.getWidgetFactory().createComposite(tabProcessFromSpecificationComposite);
		processesTreeComposite.setVisible(false);
		processesTreeComposite.setLayout(new GridLayout(1, false));
		processesTreeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		importWizardContributorPage.getWidgetFactory().createLabel(processesTreeComposite, "Choose one of the available processes:");

		composite = importWizardContributorPage.getWidgetFactory().createComposite(processesTreeComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		processesTree = new org.eclipse.swt.widgets.Tree(composite, SWT.BORDER);
		processesTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		processesTree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				boolean isLeafSelected = processesTree.getSelection().length == 1 && processesTree.getSelection()[0].getItemCount() == 0;

				if (isLeafSelected) {
					ProcessTaxonomy processTaxonomy = (ProcessTaxonomy) processesTree.getSelection()[0].getData();

					// if (processStandardCategory.getProcess() != null) {
					// importWizardContributorPage.getImportManager().setImportingFromProcessSpecificationSelectedProcess(
					// processStandardCategory.getProcess());

					if (processTaxonomy.getTaxonomyClassification() != null) {

						importWizardContributorPage.getImportManager().setImportingFromProcessSpecificationSelectedProcess(
								importWizardContributorPage.catalog.getProcessFromProcessStandardSpecification(processTaxonomy
										.getTaxonomyClassification()));

						importWizardContributorPage.projectSelectionComposite.setVisible(isLeafSelected);

					} else
						importWizardContributorPage.getImportManager().setImportingFromProcessSpecificationSelectedProcess(null);

				} else {
					importWizardContributorPage.getImportManager().setImportingFromProcessSpecificationSelectedProcess(null);
				}

				importWizardContributorPage.getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		importWizardContributorPage.getWidgetFactory().paintBordersFor(composite);

	}

	private List<TreeItem> buildTreeItemFromProcessStandardCategory(Object treeParent, List<ProcessTaxonomy> list, boolean expand) {
		List<TreeItem> result = new ArrayList<TreeItem>();
		for (ProcessTaxonomy p : list) {
			TreeItem item;

			if (treeParent instanceof Tree)
				item = new TreeItem((Tree) treeParent, 0);
			else
				item = new TreeItem((TreeItem) treeParent, 0);

			item.setData(p);
			item.setText(p.getName());

			if (p.getChilds().size() > 0)
				buildTreeItemFromProcessStandardCategory(item, p.getChilds(), expand);

			result.add(item);

			item.setExpanded(expand);

		}

		return result;
	}

}
