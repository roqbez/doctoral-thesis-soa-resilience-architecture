package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.btools.ui.UiPlugin;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.services.locator.CatalogServiceLocator;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaskTaxonomy;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaxonomy;

public class TaskChooseTaxonomyDialog extends TitleAreaDialog {

	private WidgetFactory ivFactory = new WidgetFactory();

	private WidgetFactory ivWidgetFactory = new WidgetFactory();

	private org.eclipse.swt.widgets.Tree taxonomyTree;
	private Composite taxonomyTreeComposite;

	private TaskServiceAssociationDetailsSection taskServiceAssociationDetailsSection;

	private String selectedTaxonomy = null;

	public TaskChooseTaxonomyDialog(Shell shell, TaskServiceAssociationDetailsSection taxonomyDetailsSection) {
		super(shell);
		if (UiPlugin.isRIGHTTOLEFT())
			setShellStyle(getShellStyle() | 0x4000000);

		this.taskServiceAssociationDetailsSection = taxonomyDetailsSection;
	}

	@Override
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		return new Point(Math.max(convertHorizontalDLUsToPixels(350), shellSize.x), Math.max(convertVerticalDLUsToPixels(350), shellSize.y));
	}

	@Override
	protected Control createDialogArea(Composite parentComposite) {
		setHelpAvailable(false);

		GridLayout gridLayout = new GridLayout();

		gridLayout.marginWidth = 10;

		Label label = new Label(parentComposite, 258);
		label.setLayoutData(new GridData(768));

		Composite composite = this.ivFactory.createComposite(parentComposite);

		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createClientArea(composite);

		return composite;
	}

	private Control createClientArea(Composite parentComposite) {
		getShell().setText("Choose Taxonomy");

		setMessage("Choose the taxonomy that you want to assign to the task. The taxonomy will be used to match services with the tasks");

		createTaxonomyTree(parentComposite, "Choose the taxonomy that you want to assign to the task");

		try {

			// TODO: mudar
			ProcessTaxonomy processTaxonomy = CatalogServiceLocator.get().getProcessTaxonomy("UBL");

			taxonomyTree.removeAll();

			buildTreeItemFromProcessTaxonomyItems(taxonomyTree, processTaxonomy.getChilds());

			taxonomyTreeComposite.setVisible(true);
			selectedTaxonomy = null;

			setErrorMessage(null);

		} catch (Throwable e) {
			e.printStackTrace();

			setErrorMessage("Could not connect to the Catalog Service");
			taxonomyTreeComposite.setVisible(false);
			getOKButton().setEnabled(false);
			selectedTaxonomy = null;

		}
		return parentComposite;
	}

	@Override
	protected Control createContents(Composite composite) {
		Composite localComposite = (Composite) super.createContents(composite);

		Button okButton = getButton(0);

		okButton.setEnabled(false);

		okButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				taskServiceAssociationDetailsSection.setTaxonomyName(selectedTaxonomy);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		return localComposite;
	}

	private void createTaxonomyTree(Composite paramComposite, String label) {
		taxonomyTreeComposite = getWidgetFactory().createComposite(paramComposite);
		taxonomyTreeComposite.setVisible(false);
		taxonomyTreeComposite.setLayout(new GridLayout(1, false));
		taxonomyTreeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		getWidgetFactory().createLabel(taxonomyTreeComposite, label);

		taxonomyTree = new org.eclipse.swt.widgets.Tree(taxonomyTreeComposite, SWT.BORDER);
		taxonomyTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		taxonomyTree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				try {

					if (taxonomyTree.getSelection().length > 0) {

						Object o = taxonomyTree.getSelection()[0].getData();

						if (o instanceof ProcessTaskTaxonomy) {

							getOKButton().setEnabled(true);
							selectedTaxonomy = ((ProcessTaskTaxonomy) o).getTaxonomyClassification();
							setErrorMessage(null);

						} else {
							getOKButton().setEnabled(false);
						}
					} else {
						getOKButton().setEnabled(false);
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

	}

	private void buildTreeItemFromProcessTaxonomyItems(Object treeParent, List<ProcessTaxonomy> list) {

		for (ProcessTaxonomy processTaxonomy : list) {

			TreeItem item;

			if (treeParent instanceof Tree)
				item = new TreeItem((Tree) treeParent, 0);
			else
				item = new TreeItem((TreeItem) treeParent, 0);

			item.setData(processTaxonomy);
			item.setText(processTaxonomy.getName());

			if (processTaxonomy.getChilds() != null)
				buildTreeItemFromProcessTaxonomyItems(item, processTaxonomy.getChilds());

			if (processTaxonomy.getTasks().size() > 0) {

				Map<String, List<ProcessTaskTaxonomy>> mapRoleToTasks = new HashMap<String, List<ProcessTaskTaxonomy>>();

				for (ProcessTaskTaxonomy taskTaxonomy : processTaxonomy.getTasks()) {

					List<ProcessTaskTaxonomy> tasks = mapRoleToTasks.get(taskTaxonomy.getParticipantName());
					if (tasks == null) {
						tasks = new ArrayList<ProcessTaskTaxonomy>();
						mapRoleToTasks.put(taskTaxonomy.getParticipantName(), tasks);
					}

					tasks.add(taskTaxonomy);
				}

				for (Entry<String, List<ProcessTaskTaxonomy>> e : mapRoleToTasks.entrySet()) {

					TreeItem roleTree = new TreeItem(item, 0);
					roleTree.setData(processTaxonomy);
					roleTree.setText(e.getKey());

					for (ProcessTaskTaxonomy taskTaxonomy : e.getValue()) {
						TreeItem attItem = new TreeItem(roleTree, 0);
						attItem.setData(taskTaxonomy);
						attItem.setText(taskTaxonomy.getName());
					}
				}

			}
		}

	}

	public Button getOKButton() {
		Button oktButton = getButton(0);
		return oktButton;
	}

	public WidgetFactory getWidgetFactory() {
		return ivWidgetFactory;
	}

}