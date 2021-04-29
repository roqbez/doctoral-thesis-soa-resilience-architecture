package br.ufsc.gsigma.catalog.plugin.modules.qos.task;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import br.ufsc.gsigma.catalog.plugin.util.QoSUtil;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTabContentSection;
import br.ufsc.gsigma.catalog.plugin.util.ui.UIUtil;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.ui.framework.WidgetFactory;

public class TaskQoSDetailsSection extends AbstractCatalogTabContentSection {

	protected TaskQoSList qoSList;

	private Button removeButton;

	public TaskQoSDetailsSection(WidgetFactory widgetFactory) {
		super(widgetFactory);
	}

	// To extend...
	protected void createQoSConstraintsButtonsExtensions(Composite buttonComposite) {
	}

	protected Composite createClient(final Composite paramComposite) {

		// Loading QoS Before
		QoSUtil.getEnabledQoSAttributes();

		parentCreateClient(paramComposite);

		layout = new GridLayout(1, false);
		mainComposite.setLayout(layout);

		createQoSConstraintsContainer(mainComposite);

		return mainComposite;
	}

	protected void parentCreateClient(final Composite paramComposite) {
		super.createClient(paramComposite);
	}

	protected void createQoSConstraintsContainer(final Composite paramComposite) {

		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		Composite buttonComposite = UIUtil.createComposite(paramComposite, 2);

		Button addButton = ivFactory.createButton(buttonComposite, "Add QoS Constraint", 0);

		final TaskQoSDetailsSection instance = this;

		addButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				try {

					final AddTaskQoSConstraintDialog dialog = new AddTaskQoSConstraintDialog(shell, instance);

					dialog.open();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {

			}
		});

		removeButton = ivFactory.createButton(buttonComposite, "Remove QoS Constraint", 0);
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				QoSCriterion qoSCriterion = (QoSCriterion) ((IStructuredSelection) ivTableViewer.getSelection()).getFirstElement();
				if (qoSCriterion != null && !qoSCriterion.isManaged())
					qoSList.remove(qoSCriterion);

			}
		});

		createQoSConstraintsButtonsExtensions(buttonComposite);
		createQoSConstraintsTable(paramComposite);
	}

	protected void createQoSConstraintsTable(Composite parent) {
		createTable(parent, true);
	}

	protected void createTable(Composite parent, boolean showDerivedFromGlobalColumn) {

		ivTable = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);

		ivTable.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event paramEvent) {
				IStructuredSelection selection = (IStructuredSelection) ivTableViewer.getSelection();
				QoSCriterion qoSCriterion = (QoSCriterion) selection.getFirstElement();
				removeButton.setEnabled(!qoSCriterion.isManaged());
			}
		});

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 70;
		ivTable.setLayoutData(data);

		ivTable.setLinesVisible(true);
		ivTable.setHeaderVisible(true);

		TableColumn col1 = new TableColumn(ivTable, SWT.NONE);
		col1.setText(TaskQoSList.QOS_ITEM);
		col1.setWidth(150);
		col1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setQoSConstraintSorter(ivTableViewer, QoSConstraintsListSorter.QOS_ITEM);
			}
		});

		TableColumn col2 = new TableColumn(ivTable, SWT.NONE);
		col2.setText(TaskQoSList.QOS_ATTRIBUTE);
		col2.setWidth(150);
		col2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setQoSConstraintSorter(ivTableViewer, QoSConstraintsListSorter.QOS_ATTRIBUTE);
			}
		});

		TableColumn col3 = new TableColumn(ivTable, SWT.NONE);
		col3.setText(TaskQoSList.QOS_COMPARISION);
		col3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setQoSConstraintSorter(ivTableViewer, QoSConstraintsListSorter.QOS_COMPARISON);
			}
		});

		TableColumn col4 = new TableColumn(ivTable, SWT.NONE);
		col4.setText(TaskQoSList.QOS_VALUE);
		col4.setWidth(150);
		col4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setQoSConstraintSorter(ivTableViewer, QoSConstraintsListSorter.QOS_VALUE);
			}
		});

		TableColumn col5 = new TableColumn(ivTable, SWT.NONE);
		col5.setText(TaskQoSList.QOS_UNIT);
		col5.setWidth(150);
		col5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setQoSConstraintSorter(ivTableViewer, QoSConstraintsListSorter.QOS_UNIT);
			}
		});

		if (showDerivedFromGlobalColumn) {
			TableColumn col6 = new TableColumn(ivTable, SWT.NONE);
			col6.setText(TaskQoSList.QOS_DERIVED_FROM_GLOBAL);
			col6.setWidth(150);
			col6.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setQoSConstraintSorter(ivTableViewer, QoSConstraintsListSorter.QOS_DERIVED_FROM_GLOBAL);
				}
			});
		}

		createQoSConstraintsTableViewer(showDerivedFromGlobalColumn);
	}

	private void setQoSConstraintSorter(TableViewer table, int criteria) {
		QoSConstraintsListSorter sorter = (QoSConstraintsListSorter) table.getSorter();
		if (sorter == null)
			sorter = new QoSConstraintsListSorter();
		else
			sorter = (QoSConstraintsListSorter) sorter.clone();
		sorter.setCriteria(criteria);
		sorter.toggleReverse();
		table.setSorter(sorter);
	}

	protected static String[] getComparisionTypeChoices() {
		return new String[] { "=", ">=", "<=", ">", "<" };
	}

	private void createQoSConstraintsTableViewer(boolean showDerivedFromGlobalColumn) {

		ivTableViewer = new TableViewer(ivTable);
		ivTableViewer.setUseHashlookup(true);

		CellEditor[] editors = new CellEditor[6];

		// // Comparision Type Choose
		// editors[2] = new ComboBoxCellEditor(ivTable, getComparisionTypeChoices(), SWT.READ_ONLY);

		// Only Numeric
		editors[3] = new TextCellEditor(ivTable, SWT.NONE);
		((Text) editors[3].getControl()).addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = "0123456789.".indexOf(e.text) >= 0;
			}
		});

		ivTableViewer.setCellEditors(editors);

		qoSList = createTaskQoSList(ivTableViewer);

		qoSList.addColumn(TaskQoSList.QOS_ITEM);
		qoSList.addColumn(TaskQoSList.QOS_ATTRIBUTE);
		qoSList.addColumn(TaskQoSList.QOS_COMPARISION);
		qoSList.addColumn(TaskQoSList.QOS_VALUE);
		qoSList.addColumn(TaskQoSList.QOS_UNIT);

		if (showDerivedFromGlobalColumn)
			qoSList.addColumn(TaskQoSList.QOS_DERIVED_FROM_GLOBAL);
	}

	protected TaskQoSList createTaskQoSList(TableViewer tableViewer) {
		return new TaskQoSList(tableViewer);
	}

	@Override
	public void refresh() {
		super.refresh();
		qoSList.refresh();
	}

	@Override
	public void setModelAccessor(ModelAccessor modelAccessor) {
		ivModelAccessor = modelAccessor;
		ivGeneralModelAccessor = new GeneralModelAccessor(modelAccessor);
		qoSList.setIvGeneralModelAccessor(ivGeneralModelAccessor);
	}

	public GeneralModelAccessor getSelectedGeneralModelAccessor() {
		return ivGeneralModelAccessor;
	}

	public TaskQoSList getQoSList() {
		return qoSList;
	}

}
