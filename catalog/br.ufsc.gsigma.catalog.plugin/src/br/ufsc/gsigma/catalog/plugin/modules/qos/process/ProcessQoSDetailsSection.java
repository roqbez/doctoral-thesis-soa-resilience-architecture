package br.ufsc.gsigma.catalog.plugin.modules.qos.process;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.modules.converter.ProcessConverterHelper;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMQoSThresholds;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.qos.task.TaskQoSDetailsSection;
import br.ufsc.gsigma.catalog.plugin.modules.qos.task.TaskQoSList;
import br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process.ProcessServiceAssociationTab;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ObjectHolder;
import br.ufsc.gsigma.catalog.plugin.util.ProcessNodeHelper;
import br.ufsc.gsigma.catalog.plugin.util.QoSUtil;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.catalog.plugin.util.ui.UIUtil;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.servicediscovery.locator.DiscoveryServiceLocator;
import br.ufsc.gsigma.servicediscovery.model.ProcessQoSInfo;
import br.ufsc.gsigma.servicediscovery.model.ProcessQoSThresholds;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.QoSThreshold;

public class ProcessQoSDetailsSection extends TaskQoSDetailsSection {

	protected static final String QOS_WEIGHT = "QoS Weight";

	private ProcessServiceAssociationTab processServiceAssociationTab;

	protected QoSWeightList qoSWeightsList;

	private TableViewer qoSWeightsTableViewer;

	protected ProcessQoSThresholdList qoSThresholdList;

	private TableViewer qoSThresholdTableViewer;

	protected Map<String, QoSAttribute> mapColumnNameToQoSAttribute = new HashMap<String, QoSAttribute>();

	public ProcessQoSDetailsSection(WidgetFactory widgetFactory, ProcessServiceAssociationTab processServiceAssociationTab) {
		super(widgetFactory);
		this.processServiceAssociationTab = processServiceAssociationTab;
	}

	@Override
	protected TaskQoSList createTaskQoSList(TableViewer tableViewer) {
		return new ProcessQoSList(tableViewer);
	}

	protected Composite createClient(final Composite paramComposite) {

		parentCreateClient(paramComposite);

		layout = new GridLayout(1, false);
		mainComposite.setLayout(layout);

		TabFolder tabFolder = UIUtil.createTabFolder(mainComposite);

		// Task Services Tab
		Composite tabQoSConstraintsComposite = UIUtil.createTabComposite(tabFolder, "QoS Constraints");
		createQoSConstraintsContainer(tabQoSConstraintsComposite);
		createQoSWeightsContainer(tabQoSConstraintsComposite);

		getWidgetFactory().paintBordersFor(tabQoSConstraintsComposite);

		Composite tabQoSThresholdsComposite = UIUtil.createTabComposite(tabFolder, "QoS Thresholds");

		Composite buttonComposite = UIUtil.createComposite(tabQoSThresholdsComposite, 2);

		final Button reloadButton = ivFactory.createButton(buttonComposite, "Reload QoS Thresholds", 0);

		ivFactory.createLabel(tabQoSThresholdsComposite, "These are the minimum and maximum QoS values that can be reached with available services in the repository");

		reloadButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				try {

					final StructuredActivityNode processNode = (StructuredActivityNode) ivModelAccessor.getModel();

					final String processName = processNode.getName();

					final String currentProject = ProcessNodeHelper.getCurrentProjectSelection();

					final NavigationProcessNode blmProcess = ProcessNodeHelper.getProcess(currentProject, processName);

					final List<StructuredActivityNode> tasks = ProcessNodeHelper.getProcessTasks(processNode);

					final ObjectHolder<ProcessQoSThresholds> holderProcessQoSThresholds = new ObjectHolder<ProcessQoSThresholds>();

					final ObjectHolder<Long> durationHolder = new ObjectHolder<Long>();

					final Shell shell = reloadButton.getShell();

					new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

							monitor.beginTask("Calculating QoS Thresholds...", IProgressMonitor.UNKNOWN);

							try {

								durationHolder.set(System.currentTimeMillis());
								final Process process = ProcessConverterHelper.getProcess(blmProcess, true, false, null);

								holderProcessQoSThresholds.set(DiscoveryServiceLocator.get().getProcessQoSThresholds(process));

							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								durationHolder.set(System.currentTimeMillis() - durationHolder.get());
								monitor.done();
							}
						}

					});

					final ProcessQoSThresholds processQoSThresholds = holderProcessQoSThresholds.get();

					final BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

					Map<String, Double> qoSMinValues = new HashMap<String, Double>();
					Map<String, Double> qoSMaxValues = new HashMap<String, Double>();

					for (QoSThreshold q : processQoSThresholds.getQoSThresholds()) {
						qoSMinValues.put(q.getQoSKey(), q.getQoSMinValue());
						qoSMaxValues.put(q.getQoSKey(), q.getQoSMaxValue());
					}

					processInformationExtension.setQoSThresholds(new BMQoSThresholds(qoSMinValues, qoSMaxValues));
					processInformationExtension.write(ivGeneralModelAccessor);

					for (StructuredActivityNode taskNode : tasks) {

						BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(taskNode);

						List<QoSThreshold> taskQoSThresholds = processQoSThresholds.getTaskQoSThresholds().get(taskNode.getName());

						if (taskQoSThresholds != null) {

							Map<String, Double> taskQoSMinValues = new HashMap<String, Double>();
							Map<String, Double> taskQoSMaxValues = new HashMap<String, Double>();

							for (QoSThreshold q : taskQoSThresholds) {
								taskQoSMinValues.put(q.getQoSKey(), q.getQoSMinValue());
								taskQoSMaxValues.put(q.getQoSKey(), q.getQoSMaxValue());
							}
							taskInformationExtension.setQoSThresholds(new BMQoSThresholds(taskQoSMinValues, taskQoSMaxValues));
							taskInformationExtension.write();
						}
					}

					refresh();

				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

		});

		Table qoSWeightsTable = UIUtil.createTable(tabQoSThresholdsComposite);

		TableColumn col1 = new TableColumn(qoSWeightsTable, SWT.NONE);
		col1.setText(ProcessQoSThresholdList.NAME);

		qoSThresholdTableViewer = new TableViewer(qoSWeightsTable);
		qoSThresholdTableViewer.setUseHashlookup(true);
		qoSThresholdList = new ProcessQoSThresholdList(qoSThresholdTableViewer, mapColumnNameToQoSAttribute);
		loadQoSColumns(qoSThresholdTableViewer, qoSThresholdList);

		getWidgetFactory().paintBordersFor(tabQoSThresholdsComposite);

		return mainComposite;
	}

	private void loadQoSColumns(final TableViewer tableViewer, AbstractBeanTableList<?> list) {
		try {

			for (final QoSAttribute qoSAttribute : QoSUtil.getEnabledQoSAttributes().values()) {

				String columnName = qoSAttribute.getQoSItem() + "/" + qoSAttribute.getName();
				list.addColumn(columnName);

				TableColumn col = new TableColumn(tableViewer.getTable(), SWT.NONE);
				col.setText(columnName);

				mapColumnNameToQoSAttribute.put(columnName, qoSAttribute);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void createQoSWeightsContainer(Composite paramComposite) {
		// QoS Weights
		ivFactory.createLabel(paramComposite, "QoS Weights");

		Table qoSWeightsTable = new Table(paramComposite, SWT.SINGLE | SWT.FULL_SELECTION);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 80;
		qoSWeightsTable.setLayoutData(data);

		qoSWeightsTable.setLinesVisible(true);
		qoSWeightsTable.setHeaderVisible(true);

		TableColumn col1 = new TableColumn(qoSWeightsTable, SWT.NONE);
		col1.setText(ProcessQoSList.QOS_ITEM);
		col1.setWidth(150);

		TableColumn col2 = new TableColumn(qoSWeightsTable, SWT.NONE);
		col2.setText(ProcessQoSList.QOS_ATTRIBUTE);
		col2.setWidth(150);

		TableColumn col3 = new TableColumn(qoSWeightsTable, SWT.NONE);
		col3.setText("Weight");
		col3.setWidth(150);

		qoSWeightsTableViewer = new TableViewer(qoSWeightsTable);
		qoSWeightsTableViewer.setUseHashlookup(true);

		CellEditor[] editors = new CellEditor[3];

		// Only Numeric
		editors[2] = new TextCellEditor(qoSWeightsTable, SWT.NONE);
		((Text) editors[2].getControl()).addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = "0123456789.".indexOf(e.text) >= 0;
			}
		});

		qoSWeightsTableViewer.setCellEditors(editors);

		this.qoSWeightsList = new QoSWeightList(qoSWeightsTableViewer);

	}

	@Override
	protected void createQoSConstraintsButtonsExtensions(Composite buttonComposite) {

		GridLayout buttonCompositeLayout = new GridLayout(3, false);
		buttonCompositeLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonCompositeLayout);

		final Shell shell = buttonComposite.getShell();

		Button setQoSConstraints = ivFactory.createButton(buttonComposite, "Calculate local QoS constraints for tasks", 0);
		setQoSConstraints.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				try {

					final StructuredActivityNode processNode = (StructuredActivityNode) ivModelAccessor.getModel();

					final String processName = processNode.getName();

					final String currentProject = ProcessNodeHelper.getCurrentProjectSelection();

					final NavigationProcessNode blmProcess = ProcessNodeHelper.getProcess(currentProject, processName);

					final List<StructuredActivityNode> tasks = ProcessNodeHelper.getProcessTasks(processNode);

					final ObjectHolder<ProcessQoSInfo> holderProcessQoSInfo = new ObjectHolder<ProcessQoSInfo>();

					final ObjectHolder<Long> durationHolder = new ObjectHolder<Long>();

					new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

							monitor.beginTask("Deriving local QoS constraints for " + tasks.size() + " tasks", IProgressMonitor.UNKNOWN);

							try {

								durationHolder.set(System.currentTimeMillis());
								final Process process = ProcessConverterHelper.getProcess(blmProcess, true, false, null);

								final QoSInformation qoSInformation = QoSUtil.getQoSInformation(qoSList.getItens());
								qoSInformation.setQoSWeights(qoSWeightsList.getQoSWeights());

								ProcessQoSInfo processQoSInfo = DiscoveryServiceLocator.get().getLocalQoSConstraints(process, qoSInformation);

								holderProcessQoSInfo.set(processQoSInfo);

							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								durationHolder.set(System.currentTimeMillis() - durationHolder.get());
								monitor.done();
							}
						}

					});

					final ProcessQoSInfo processQoSInfo = holderProcessQoSInfo.get();

					final Map<String, List<QoSConstraint>> localConstraints = processQoSInfo.getLocalQoSConstraints();

					final BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);
					processInformationExtension.setGlobalQoSDelta(processQoSInfo.getGlobalQoSDelta());
					processInformationExtension.write(ivGeneralModelAccessor);

					boolean hasLocalConstraints = localConstraints != null && !localConstraints.isEmpty();

					for (StructuredActivityNode taskNode : tasks) {

						BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(taskNode);

						String serviceClassification = taskInformationExtension.getTaxonomyClassification();

						if (serviceClassification != null) {
							if (hasLocalConstraints) {
								List<QoSConstraint> constraints = localConstraints.get(serviceClassification);
								taskInformationExtension.addManagedQoSConstraints(constraints);
							} else {
								taskInformationExtension.removeManagedQoSConstraints();
							}
							taskInformationExtension.write();
						}
					}

					if (hasLocalConstraints) {
						MessageDialog.openInformation(shell, "QoS Constraints", "The QoS Constraints were calculated and added to all process tasks in " + durationHolder.get() + " ms");
					} else {
						MessageDialog.openError(shell, "QoS Constraints", "It was not possible to derive local QoS constraints from these global constraints");
					}

					processServiceAssociationTab.getSection().refresh();

				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

		});
	}

	@Override
	protected void createQoSConstraintsTable(Composite parent) {
		super.createTable(parent, false);
	}

	@Override
	public void refresh() {
		super.refresh();
		qoSWeightsList.refresh();
		qoSThresholdList.refresh();
	}

	@Override
	public void setModelAccessor(ModelAccessor modelAccessor) {
		super.setModelAccessor(modelAccessor);
		if (qoSWeightsList != null)
			qoSWeightsList.setIvGeneralModelAccessor(ivGeneralModelAccessor);
		if (qoSThresholdList != null)
			qoSThresholdList.setIvGeneralModelAccessor(ivGeneralModelAccessor);
	}

}
