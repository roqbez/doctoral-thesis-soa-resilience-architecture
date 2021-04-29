package br.ufsc.gsigma.catalog.plugin.modules.exprt;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.btools.blm.ui.importExport.AbstractBLMImportExportDetailsBasePage;
import com.ibm.btools.blm.ui.navigation.manager.util.BLMEditorInput;
import com.ibm.btools.blm.ui.navigation.manager.util.BLMManagerUtil;
import com.ibm.btools.blm.ui.navigation.model.AbstractBusinessGroupsChildNode;
import com.ibm.btools.blm.ui.navigation.model.AbstractChildLeafNode;
import com.ibm.btools.blm.ui.navigation.model.AbstractLibraryChildNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationBusinessGroupNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationBusinessGroupsNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationLibraryNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessCatalogNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProjectNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationRoot;
import com.ibm.btools.bom.model.artifacts.Element;
import com.ibm.btools.ui.framework.BToolsEditor;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.util.CatalogServiceUtil;
import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.catalog.services.model.ProcessCategory;
import br.ufsc.gsigma.catalog.services.model.ProcessTreeDescription;
import br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo;

public class ExportWizardContributorPage extends AbstractBLMImportExportDetailsBasePage {

	private Composite projectSelectionComposite;
	private CCombo projectSelectionCombo;

	private Composite processSelectionComposite;
	private CCombo processSelectionCombo;

	private Composite categorySelectionComposite;
	private CCombo categorySelectionCombo;

	private org.eclipse.swt.widgets.List processList;

	private Composite chooseProcessNameComposite;
	private Text processNameText;

	private org.eclipse.swt.widgets.Tree processesTree;
	private Composite processesTreeComposite;

	private List<ProcessTreeDescription> listProcessesTreeDescription;

	private List<NavigationProcessNode> projectProcessesNodes = new ArrayList<NavigationProcessNode>();

	List<Element> elements = new ArrayList<Element>();

	private CatalogService catalog;

	private Map<Integer, Integer> mapCategoryComboIndexId = new HashMap<Integer, Integer>();

	private HashMap<String, Object> dynamicOptions = new HashMap<String, Object>();

	public ExportWizardContributorPage() {
		super("Business Process Catalog", "Export a Process to a Business Catalog", new WidgetFactory());
		getDynamicOptions().put("EXPORT_MANAGER", new ProcessCatalogExportManager());
	}

	private ProcessCatalogExportManager getExportManager() {
		return (ProcessCatalogExportManager) getDynamicOptions().get("EXPORT_MANAGER");
	}

	@Override
	protected void createClientArea(Composite parentComposite) {

		try {

			((Wizard) getWizard()).setNeedsProgressMonitor(true);

			GridData gridDataFillHorizontalWithSpace = new GridData(GridData.FILL_HORIZONTAL);
			gridDataFillHorizontalWithSpace.horizontalIndent = 2;

			GridData gridDataFillBothWithSpace = new GridData(GridData.FILL_BOTH);
			gridDataFillBothWithSpace.horizontalIndent = 2;

			String currentProjectSelection = getCurrentProjectSelection();
			String[] existingProjects = getExistingProjects();

			createProjectSelection(parentComposite, currentProjectSelection, existingProjects, "Choose the Project");
			createProcessSelection(parentComposite, "Choose the Process");
			createProcessCategorySelection(parentComposite, "Choose one of the process categories");
			createProcessTree(parentComposite);

			createChooseProcessName(parentComposite, "Choose the name of the process to store in the Catalog. You can overwrite a existing process or create a new one.");

			if (currentProjectSelection != null)
				setProjectProcesses(currentProjectSelection);

			try {

				catalog = CatalogServiceUtil.getCatalogService();

				List<ProcessCategory> categories = catalog.getProcessCategories();

				categorySelectionComposite.setVisible(true);
				processesTreeComposite.setVisible(false);
				chooseProcessNameComposite.setVisible(false);

				categorySelectionCombo.removeAll();
				mapCategoryComboIndexId.clear();

				int i = 0;
				for (ProcessCategory c : categories) {
					categorySelectionCombo.add(c.getName(), i);
					mapCategoryComboIndexId.put(i++, c.getId());
				}

				categorySelectionCombo.setText(categories.size() + " available categories");

				setErrorMessage(null);

			} catch (Exception e) {
				categorySelectionComposite.setVisible(false);
				processesTreeComposite.setVisible(false);
				chooseProcessNameComposite.setVisible(false);
				setErrorMessage("Could not connect to the Catalog Service");
			}

			getWidgetFactory().paintBordersFor(parentComposite);

		} catch (Throwable e) {
			e.printStackTrace();
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
					getExportManager().setNewProcessName(processSelectionCombo.getText());
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

	private void createProcessCategorySelection(Composite paramComposite, String label) {

		categorySelectionComposite = getWidgetFactory().createComposite(paramComposite);
		categorySelectionComposite.setVisible(false);

		categorySelectionComposite.setLayout(new GridLayout(1, false));
		categorySelectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getWidgetFactory().createLabel(categorySelectionComposite, label);

		Composite composite = getWidgetFactory().createComposite(categorySelectionComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		categorySelectionCombo = getWidgetFactory().createCombo(composite);
		categorySelectionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		categorySelectionCombo.setEditable(false);
		categorySelectionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent ev) {

				if (categorySelectionCombo.getText() != null) {

					try {

						listProcessesTreeDescription = catalog.getListProcessTreeDescription(mapCategoryComboIndexId.get(categorySelectionCombo.getSelectionIndex()));

						processesTree.removeAll();

						buildTreeItemFromProcessDescription(processesTree, listProcessesTreeDescription, false);

						processesTreeComposite.setVisible(true);

						setErrorMessage(null);
					} catch (Exception e) {
						processesTreeComposite.setVisible(false);
						chooseProcessNameComposite.setVisible(false);
						setErrorMessage("Could not connect to the Catalog Service");
					}

				}
				getWizard().getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});
		getWidgetFactory().paintBordersFor(composite);
	}

	private void createProcessTree(Composite paramComposite) {

		processesTreeComposite = getWidgetFactory().createComposite(paramComposite);
		processesTreeComposite.setVisible(false);
		processesTreeComposite.setLayout(new GridLayout(2, true));
		processesTreeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		getWidgetFactory().createLabel(processesTreeComposite, "Choose the classification which your process belongs to");
		getWidgetFactory().createLabel(processesTreeComposite, "Processes in this category:");

		Composite composite = getWidgetFactory().createComposite(processesTreeComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		processesTree = new org.eclipse.swt.widgets.Tree(composite, SWT.BORDER);
		processesTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		processesTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (processesTree.getSelection().length > 0 && ((ProcessTreeDescription) processesTree.getSelection()[0].getData()).getChilds().isEmpty()) {

					ProcessTreeDescription treeDescription = (ProcessTreeDescription) processesTree.getSelection()[0].getData();
					getExportManager().setProcessTreeDescription(treeDescription);

					processList.removeAll();

					try {
						List<ProcessInfo> list = catalog.getListProcessInfo(treeDescription.getId());

						if (list != null)
							for (ProcessInfo info : list)
								processList.add(info.getName());

						setErrorMessage(null);
						chooseProcessNameComposite.setVisible(true);

					} catch (Exception ex) {
						ex.printStackTrace();

						setErrorMessage(ex.getMessage());
					}

				} else {
					processList.removeAll();
					chooseProcessNameComposite.setVisible(false);
				}

				getWizard().getContainer().updateButtons();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		composite = getWidgetFactory().createComposite(processesTreeComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		processList = new org.eclipse.swt.widgets.List(composite, SWT.SINGLE);
		processList.setLayoutData(new GridData(GridData.FILL_BOTH));
		processList.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (processList.getSelection().length > 0)
					processNameText.setText(processList.getSelection()[0]);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		getWidgetFactory().paintBordersFor(composite);

	}

	private void createChooseProcessName(Composite paramComposite, String label) {

		chooseProcessNameComposite = getWidgetFactory().createComposite(paramComposite);
		chooseProcessNameComposite.setVisible(false);

		chooseProcessNameComposite.setLayout(new GridLayout(1, false));
		chooseProcessNameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getWidgetFactory().createLabel(chooseProcessNameComposite, label);

		Composite composite = getWidgetFactory().createComposite(chooseProcessNameComposite);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		processNameText = getWidgetFactory().createText(composite, 0);
		processNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		processNameText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent arg0) {

				getExportManager().setNewProcessName(processNameText.getText());

				getWizard().getContainer().updateButtons();
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

		});
		getWidgetFactory().paintBordersFor(composite);
	}

	private NavigationRoot getNavigationRoot() {
		return BLMManagerUtil.getNavigationRoot();
	}

	private void setProjectProcesses(String projectName) {

		projectProcessesNodes.clear();
		processSelectionCombo.removeAll();

		for (NavigationProjectNode projectNode : getExistingProjectNodes()) {

			if (projectNode.getLabel().equals(projectName)) {

				int i = 0;

				for (NavigationProcessCatalogNode processCatalog : projectNode.getLibraryNode().getProcessCatalogsNodes().getProcessCatalogNodes()) {

					for (NavigationProcessNode processNode : processCatalog.getProcessesNode().getProcessNodes()) {
						projectProcessesNodes.add(processNode);
						processSelectionCombo.add(processNode.getLabel());

						if (i++ == 0) {
							getExportManager().setProcessNode(processNode);
							processSelectionCombo.setText(processNode.getLabel());
							processNameText.setText(processNode.getLabel());
							getExportManager().setNewProcessName(processNode.getLabel());
						}

					}

				}

			}

		}
	}

	private NavigationProjectNode[] getExistingProjectNodes() {
		List<NavigationProjectNode> result = new ArrayList<NavigationProjectNode>();

		for (NavigationProjectNode n : getNavigationRoot().getProjectNodes())
			if (!BLMManagerUtil.isPredefinedProject(n))
				result.add(n);

		return result.toArray(new NavigationProjectNode[result.size()]);
	}

	private String[] getExistingProjects() {
		NavigationProjectNode[] projecNodes = getExistingProjectNodes();

		String[] result = new String[projecNodes.length];

		int i = 0;
		for (NavigationProjectNode n : projecNodes)
			result[i++] = n.getLabel();

		return result;
	}

	private Object getCurrentlySelectedNode() {

		if (BLMManagerUtil.getNavigationTreeViewer() != null) {

			ISelection selection = BLMManagerUtil.getNavigationTreeViewer().getSelection();

			if (selection.isEmpty()) {

				for (NavigationProjectNode n : getNavigationRoot().getProjectNodes())
					if (!BLMManagerUtil.isPredefinedProject(n))
						return n;

			}
			if (selection instanceof StructuredSelection)
				return ((StructuredSelection) selection).getFirstElement();
		} else {
			try {
				IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (activeEditor instanceof BToolsEditor) {
					IEditorInput editorInput = (BLMEditorInput) ((BToolsEditor) activeEditor).getEditorInput();
					return ((BLMEditorInput) editorInput).getNode();
				}
			} catch (Exception localException) {
			}
		}
		return null;
	}

	private String getCurrentProjectSelection() {

		Object selectedNode = getCurrentlySelectedNode();

		if (selectedNode instanceof NavigationProjectNode)
			return ((NavigationProjectNode) selectedNode).getLabel();

		else if (selectedNode instanceof AbstractChildLeafNode)
			return ((AbstractChildLeafNode) selectedNode).getProjectNode().getLabel();

		else if (selectedNode instanceof AbstractLibraryChildNode)
			return ((AbstractLibraryChildNode) selectedNode).getProjectNode().getLabel();

		else if (selectedNode instanceof NavigationBusinessGroupsNode)
			return ((NavigationBusinessGroupsNode) selectedNode).getProjectNode().getLabel();

		else if (selectedNode instanceof NavigationBusinessGroupsNode)
			return ((NavigationBusinessGroupNode) selectedNode).getProjectNode().getLabel();

		else if (selectedNode instanceof AbstractBusinessGroupsChildNode)
			return ((AbstractBusinessGroupsChildNode) selectedNode).getProjectNode().getLabel();

		else if (selectedNode instanceof NavigationLibraryNode)
			return ((NavigationLibraryNode) selectedNode).getProjectNode().getLabel();

		return null;
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

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public boolean canFinish() {

		try {
			boolean b1 = projectSelectionCombo.getText().length() > 0;
			boolean b2 = processSelectionCombo.getText().length() > 0;
			boolean b3 = categorySelectionCombo.getText().length() > 0;
			boolean b4 = catalog != null;
			boolean b5 = getExportManager().getProcessTreeDescription() != null;
			boolean b6 = getExportManager().getProcessNode() != null;
			boolean b7 = getExportManager().getNewProcessName() != null && getExportManager().getNewProcessName().length() > 0;
			boolean b8 = getErrorMessage() == null;
			boolean b9 = projectSelectionComposite.isVisible();
			boolean b10 = processSelectionComposite.isVisible();
			boolean b11 = categorySelectionComposite.isVisible();
			boolean b12 = chooseProcessNameComposite.isVisible();

			return b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9 && b10 && b11 && b12;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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