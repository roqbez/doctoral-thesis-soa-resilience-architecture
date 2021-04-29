package br.ufsc.gsigma.catalog.plugin.modules.qos.task;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.btools.ui.UiPlugin;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.servicediscovery.locator.DiscoveryServiceLocator;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSItem;

public class AddTaskQoSConstraintDialog extends TitleAreaDialog {

	private WidgetFactory ivFactory = new WidgetFactory();

	private WidgetFactory ivWidgetFactory = new WidgetFactory();

	private org.eclipse.swt.widgets.Tree qoSTree;
	private Composite qoSTreeComposite;

	private Text qoSDescriptionText;
	private Composite qoSDescriptionComposite;

	private TaskQoSDetailsSection qoSDetailsSection;

	private QoSAttribute selectedQoSAttribute = null;

	public AddTaskQoSConstraintDialog(Shell shell, TaskQoSDetailsSection qoSDetailsSection) {
		super(shell);
		if (UiPlugin.isRIGHTTOLEFT())
			setShellStyle(getShellStyle() | 0x4000000);

		this.qoSDetailsSection = qoSDetailsSection;
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

		getShell().setText("Add QoS Constraint");

		setMessage("Choose one of the available QoS Constraints you want to add to your task");

		createQoSTree(parentComposite, "Choose the QoS Attribute you want to use");

		qoSDescriptionComposite = getWidgetFactory().createComposite(parentComposite);
		qoSDescriptionComposite.setVisible(false);
		qoSDescriptionComposite.setLayout(new GridLayout(1, false));
		qoSDescriptionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		getWidgetFactory().paintBordersFor(qoSDescriptionComposite);

		qoSDescriptionText = getWidgetFactory().createText(qoSDescriptionComposite, SWT.MULTI);
		qoSDescriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));
		qoSDescriptionText.setEditable(false);

		// Getting QoS Items
		try {

			List<QoSItem> qoSItems = DiscoveryServiceLocator.get().getEnabledQoSItems();
			buildTreeItemFromQoSItems(qoSTree, qoSItems);
			qoSTreeComposite.setVisible(true);
			qoSDescriptionComposite.setVisible(true);
		} catch (Throwable e) {
			setErrorMessage("Could not connect to Discovery Service");
			qoSTreeComposite.setVisible(false);
			qoSDescriptionComposite.setVisible(false);
			e.printStackTrace();
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
				qoSDetailsSection.getQoSList().add(selectedQoSAttribute);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

		return localComposite;
	}

	private void createQoSTree(Composite paramComposite, String label) {
		qoSTreeComposite = getWidgetFactory().createComposite(paramComposite);
		qoSTreeComposite.setVisible(false);
		qoSTreeComposite.setLayout(new GridLayout(1, false));
		qoSTreeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		getWidgetFactory().createLabel(qoSTreeComposite, label);

		qoSTree = new org.eclipse.swt.widgets.Tree(qoSTreeComposite, SWT.BORDER);
		qoSTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		qoSTree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				try {

					if (qoSTree.getSelection().length > 0) {

						Object o = qoSTree.getSelection()[0].getData();

						if (o instanceof QoSItem) {
							setErrorMessage(null);
							qoSDescriptionText.setText(((QoSItem) o).getDescription());
							getOKButton().setEnabled(false);
							selectedQoSAttribute = null;
						} else if (o instanceof QoSAttribute) {
							qoSDescriptionText.setText(((QoSItem) qoSTree.getSelection()[0].getParentItem().getData()).getDescription());

							if (!qoSDetailsSection.getQoSList().isAttributeAlreadySelected((QoSAttribute) o)) {

								getOKButton().setEnabled(true);
								selectedQoSAttribute = (QoSAttribute) o;
								setErrorMessage(null);
							} else {
								setErrorMessage("You can't add this attribute, because it was already added");
								getOKButton().setEnabled(false);
								selectedQoSAttribute = null;
							}

						}
					} else {
						qoSDescriptionText.setText(null);
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}
		});

	}

	private List<TreeItem> buildTreeItemFromQoSItems(Tree treeParent, List<QoSItem> list) {
		List<TreeItem> result = new ArrayList<TreeItem>();

		for (QoSItem qoSItem : list) {
			TreeItem item = new TreeItem((Tree) treeParent, 0);

			item.setData(qoSItem);
			item.setText(qoSItem.getName());

			for (QoSAttribute att : qoSItem.getQoSAttributes()) {

				TreeItem attItem = new TreeItem(item, 0);
				attItem.setData(att);
				attItem.setText(att.getName());
				qoSTreeComposite.setVisible(true);
			}
		}

		return result;
	}

	public Button getOKButton() {
		Button oktButton = getButton(0);
		return oktButton;
	}

	public WidgetFactory getWidgetFactory() {
		return ivWidgetFactory;
	}

}