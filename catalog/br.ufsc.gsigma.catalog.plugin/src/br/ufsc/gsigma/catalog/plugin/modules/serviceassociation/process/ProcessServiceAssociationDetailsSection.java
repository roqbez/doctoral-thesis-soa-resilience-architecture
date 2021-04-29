package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.modules.converter.ProcessConverterHelper;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServiceAssociation;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServicesComposition;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServicesComposition.CompositionType;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ObjectHolder;
import br.ufsc.gsigma.catalog.plugin.util.ProcessNodeHelper;
import br.ufsc.gsigma.catalog.plugin.util.QoSUtil;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTabContentSection;
import br.ufsc.gsigma.catalog.plugin.util.ui.UIUtil;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryService;
import br.ufsc.gsigma.servicediscovery.locator.DiscoveryServiceLocator;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequest;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequestItem;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryResult;
import br.ufsc.gsigma.servicediscovery.model.ProcessQoSInfo;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.ServiceInfo;
import br.ufsc.gsigma.servicediscovery.model.ServicesComposition;

public class ProcessServiceAssociationDetailsSection extends AbstractCatalogTabContentSection {

	protected ProcessServiceAssociationList processServiceAssociationList;

	protected ProcessCompositionList processCompositionList;

	protected Map<String, QoSAttribute> mapColumnNameToQoSAttribute = new HashMap<String, QoSAttribute>();

	private StyledText compositionsLabel;

	private CCombo selectionTypeCombo;

	private Composite hybridSelectionOptionsComposite;

	private Text maxNumberOfCompositions;

	private Text initialQoSLevels;

	private Text maxQoSLevels;

	public ProcessServiceAssociationDetailsSection(WidgetFactory widgetFactory) {
		super(widgetFactory);
	}

	protected Composite createClient(final Composite paramComposite) {

		super.createClient(paramComposite);

		gridData = new GridData(GridData.FILL_BOTH);

		layout = new GridLayout(1, false);

		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		Composite headerComposite = UIUtil.createComposite(mainComposite, 2);
		createDiscoverServicesButton(headerComposite);
		createSelectionTypeCombo(headerComposite);

		Composite selectionTypeOptionsComposite = UIUtil.createComposite(mainComposite, 2, SWT.NONE);
		createSelectionGeneralOptions(selectionTypeOptionsComposite);
		createHybridSelectionOptions(selectionTypeOptionsComposite);

		TabFolder tabFolder = UIUtil.createTabFolder(mainComposite);

		// Task Services Tab
		Composite tabTaskServicesComposite = UIUtil.createTabComposite(tabFolder, "Task Services");
		createTaskServicesTable(tabTaskServicesComposite);

		// Available Compositions Tab
		Composite tabAvailableCompositionsComposite = UIUtil.createTabComposite(tabFolder, "Available Compositions");

		Composite labelComposite = getWidgetFactory().createComposite(tabAvailableCompositionsComposite);
		labelComposite.setLayout(new GridLayout(1, false));
		labelComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		compositionsLabel = new StyledText(labelComposite, SWT.NONE);
		compositionsLabel.setLayout(new GridLayout(1, false));
		compositionsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tabAvailableCompositionsTableComposite = getWidgetFactory().createComposite(tabAvailableCompositionsComposite);
		GridLayout tabAvailableCompositionsTableCompositeLayout = new GridLayout(1, false);
		tabAvailableCompositionsTableCompositeLayout.marginWidth = 0;
		tabAvailableCompositionsTableComposite.setLayout(tabAvailableCompositionsTableCompositeLayout);
		tabAvailableCompositionsTableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProcessCompositionTable(tabAvailableCompositionsTableComposite);

		return mainComposite;
	}

	protected void createHybridSelectionOptions(Composite selectionTypeOptionsComposite) {
		hybridSelectionOptionsComposite = UIUtil.createComposite(selectionTypeOptionsComposite, 2);
		getWidgetFactory().paintBordersFor(hybridSelectionOptionsComposite);

		Composite initialQoSLevelsComposite = UIUtil.createComposite(hybridSelectionOptionsComposite, 2);
		getWidgetFactory().paintBordersFor(initialQoSLevelsComposite);

		getWidgetFactory().createLabel(initialQoSLevelsComposite, "Initial Number of QoS Levels");
		initialQoSLevels = createNumberInput(initialQoSLevelsComposite, 3, 20);
		initialQoSLevels.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Text input = (Text) e.getSource();
				if (NumberUtils.isDigits(input.getText())) {
					BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);
					Integer v = Integer.valueOf(input.getText());

					String maxLevels = maxQoSLevels.getText();
					if (StringUtils.isBlank(maxLevels) || new Integer(maxLevels) < v) {
						maxQoSLevels.setText(input.getText());
						processInformation.setMaxNumberOfQoSLevels(v);
					}

					processInformation.setInitialNumberOfQoSLevels(v);
					processInformation.write(ivGeneralModelAccessor);
				}
			}
		});

		Composite maxQoSLevelsComposite = UIUtil.createComposite(hybridSelectionOptionsComposite, 2);
		getWidgetFactory().paintBordersFor(maxQoSLevelsComposite);

		getWidgetFactory().createLabel(maxQoSLevelsComposite, "Max Number of QoS Levels");
		maxQoSLevels = createNumberInput(maxQoSLevelsComposite, 3, 20);
		maxQoSLevels.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Text input = (Text) e.getSource();
				if (NumberUtils.isDigits(input.getText())) {
					BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);
					Integer v = Integer.valueOf(input.getText());

					String initialLevels = initialQoSLevels.getText();
					if (StringUtils.isBlank(initialLevels) || new Integer(initialLevels) > v) {
						initialQoSLevels.setText(input.getText());
						processInformation.setInitialNumberOfQoSLevels(v);
					}

					processInformation.setMaxNumberOfQoSLevels(v);
					processInformation.write(ivGeneralModelAccessor);
				}
			}
		});
	}

	protected void createSelectionGeneralOptions(Composite selectionTypeOptionsComposite) {

		Composite selectionGeneralOptionsComposite = UIUtil.createComposite(selectionTypeOptionsComposite, 2);
		getWidgetFactory().paintBordersFor(selectionGeneralOptionsComposite);

		getWidgetFactory().createLabel(selectionGeneralOptionsComposite, "Max number of compositions");
		maxNumberOfCompositions = createNumberInput(selectionGeneralOptionsComposite, 3, 20);
		maxNumberOfCompositions.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Text input = (Text) e.getSource();
				if (NumberUtils.isDigits(input.getText())) {
					BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);
					processInformation.setMaxNumberOfCompositions(Integer.valueOf(input.getText()));
					processInformation.write(ivGeneralModelAccessor);
				}
			}
		});
	}

	protected void createSelectionTypeCombo(Composite parent) {

		String[] selectionTypeOptions = { "Heuristic Selection", "Optimal Selection (may be slower)", "Both types (use for compare)" };

		selectionTypeCombo = ivFactory.createCombo(parent, SWT.READ_ONLY);

		selectionTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				CCombo combo = (CCombo) event.getSource();
				BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

				if (combo.getSelectionIndex() == 0) {
					processInformation.setUseOptimalServiceSelection(false);
					processInformation.setUseHeuristicServiceSelection(true);
				} else if (combo.getSelectionIndex() == 1) {
					processInformation.setUseOptimalServiceSelection(true);
					processInformation.setUseHeuristicServiceSelection(false);
				} else if (combo.getSelectionIndex() == 2) {
					processInformation.setUseOptimalServiceSelection(true);
					processInformation.setUseHeuristicServiceSelection(true);
				}

				processInformation.write(ivGeneralModelAccessor);
				updateSelectionTypeOptions();

			}
		});

		selectionTypeCombo.setItems(selectionTypeOptions);
		selectionTypeCombo.select(0);
	}

	protected Text createNumberInput(Composite parent, int maxDigits, int width) {

		Text input = getWidgetFactory().createText(parent, SWT.NONE);
		input.setTextLimit(maxDigits);
		input.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = "0123456789".indexOf(e.text) >= 0 || NumberUtils.isDigits(e.text);
			}
		});

		input.setLayoutData(new GridData());
		((GridData) input.getLayoutData()).widthHint = width;

		return input;
	}

	protected void createDiscoverServicesButton(final Composite paramComposite) {

		Button discoverButton = ivFactory.createButton(paramComposite, "Discover Services", 0);

		ivFactory.paintBordersFor(paramComposite);

		discoverButton.addSelectionListener(getDiscoveryButtonSelectionListener());
	}

	protected SelectionListener getDiscoveryButtonSelectionListener() {

		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				try {

					if (ivModelAccessor.getModel() instanceof StructuredActivityNode) {

						final BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

						final Map<String, BMTaskInformationExtension> mapServiceClassificationTaskInfo = new HashMap<String, BMTaskInformationExtension>();

						final ObjectHolder<Boolean> successHolder = new ObjectHolder<Boolean>(false);
						final ObjectHolder<Long> durationHolder = new ObjectHolder<Long>();

						final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

						final DiscoveryRequest discoveryRequest = getDiscoveryRequest(processInformation, mapServiceClassificationTaskInfo);

						discoveryRequest.getMaxCandidatesPerParticipant().put("Customer", 1);

						new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

								try {

									durationHolder.set(System.currentTimeMillis());

									monitor.beginTask("Discovering services for tasks", IProgressMonitor.UNKNOWN);

									final DiscoveryService discoveryService = DiscoveryServiceLocator.get();

									boolean bothTypesOfSelection = processInformation.isUseHeuristicServiceSelection() && processInformation.isUseOptimalServiceSelection();

									if (bothTypesOfSelection) {

										ExecutorService pool = Executors.newFixedThreadPool(2);

										// Hybrid
										Future<List<BMServicesComposition>> fCompositionsHybrid = pool.submit(new Callable<List<BMServicesComposition>>() {
											@Override
											public List<BMServicesComposition> call() throws Exception {
												List<BMServicesComposition> compositionsHybrid = new LinkedList<BMServicesComposition>();
												DiscoveryRequest requestForHybrid = (DiscoveryRequest) discoveryRequest.clone();
												requestForHybrid.setUseOptimalSelection(false);
												DiscoveryResult resultForHybrid = discoveryService.discover(requestForHybrid);
												handleDiscoveryResult(requestForHybrid, resultForHybrid, processInformation, mapServiceClassificationTaskInfo, compositionsHybrid);
												return compositionsHybrid;
											}
										});

										// Optimal
										Future<List<BMServicesComposition>> fCompositionsOptimal = pool.submit(new Callable<List<BMServicesComposition>>() {
											@Override
											public List<BMServicesComposition> call() throws Exception {
												List<BMServicesComposition> compositionsOptimal = new LinkedList<BMServicesComposition>();
												DiscoveryRequest requestForOptimal = (DiscoveryRequest) discoveryRequest.clone();
												requestForOptimal.setUseOptimalSelection(true);
												DiscoveryResult resultForOptimal = discoveryService.discover(requestForOptimal);
												handleDiscoveryResult(requestForOptimal, resultForOptimal, processInformation, mapServiceClassificationTaskInfo, compositionsOptimal);
												return compositionsOptimal;
											}
										});

										pool.shutdown();

										List<BMServicesComposition> compositionsHybrid = fCompositionsHybrid.get();
										List<BMServicesComposition> compositionsOptimal = fCompositionsOptimal.get();

										int n = Math.max(compositionsHybrid.size(), compositionsOptimal.size());

										List<BMServicesComposition> compositions = new ArrayList<BMServicesComposition>(n);

										int lastIndexBoth = Math.min(compositionsHybrid.size(), compositionsOptimal.size());

										int i = 0;

										Iterator<BMServicesComposition> it1 = compositionsHybrid.iterator();
										Iterator<BMServicesComposition> it2 = compositionsOptimal.iterator();

										while (i++ < lastIndexBoth) {

											BMServicesComposition c1 = it1.next();
											BMServicesComposition c2 = it2.next();

											BMServicesComposition c = new BMServicesComposition();
											c.setType(CompositionType.BOTH);
											c.setRanking(i);

											c.setServices(c1.getServices());
											c.setQoSValues(c1.getQoSValues());
											c.setUtility(c1.getUtility());

											c.setServices2(c2.getServices());
											c.setQoSValues2(c2.getQoSValues());
											c.setUtility2(c2.getUtility());

											compositions.add(c);
										}

										List<BMServicesComposition> compositions2 = new ArrayList<BMServicesComposition>(n - lastIndexBoth);

										while (it1.hasNext()) {
											BMServicesComposition c1 = it1.next();
											if (lastIndexBoth > 0)
												c1.setRanking(-1);
											compositions2.add(c1);
										}
										while (it2.hasNext()) {
											BMServicesComposition c2 = it2.next();
											if (lastIndexBoth > 0)
												c2.setRanking(-1);
											compositions2.add(c2);
										}

										Collections.sort(compositions2, new Comparator<BMServicesComposition>() {
											@Override
											public int compare(BMServicesComposition o1, BMServicesComposition o2) {
												return o2.getUtility().compareTo(o1.getUtility());
											}
										});

										compositions.addAll(compositions2);

										processInformation.setCompositions(compositions);

										processInformation.setTotalNumberOfCompositions(compositions.size());

									} else {

										List<BMServicesComposition> compositions = processInformation.getCompositions();
										compositions.clear();

										discoveryRequest.setUseOptimalSelection(processInformation.isUseOptimalServiceSelection());
										DiscoveryResult result = discoveryService.discover(discoveryRequest);
										handleDiscoveryResult(discoveryRequest, result, processInformation, mapServiceClassificationTaskInfo, compositions);

										processInformation.setTotalNumberOfCompositions(result.getTotalNumberOfCompositions());

									}

									successHolder.set(true);

								} catch (Throwable e) {
									e.printStackTrace();
								} finally {
									durationHolder.set(System.currentTimeMillis() - durationHolder.get());
									monitor.done();
								}
							}

						});

						processInformation.write(ivGeneralModelAccessor);

						for (BMTaskInformationExtension taskInformation : mapServiceClassificationTaskInfo.values()) {
							taskInformation.write();
						}

						refresh();

						if (successHolder.get()) {
							MessageDialog.openInformation(shell, "Service Discovery", "The Service Discovery was completed in " + durationHolder.get() + " ms");
						} else {
							MessageDialog.openError(shell, "Service Discovery", "The Discovery Service is unavailable");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			protected DiscoveryRequest getDiscoveryRequest(final BMProcessInformationExtension processInformation, final Map<String, BMTaskInformationExtension> mapServiceClassificationTaskInfo) throws IOException, ParserConfigurationException, SAXException {
				final DiscoveryRequest discoveryRequest = new DiscoveryRequest();

				final StructuredActivityNode processNode = (StructuredActivityNode) ivModelAccessor.getModel();

				final String processName = processNode.getName();

				final String currentProject = ProcessNodeHelper.getCurrentProjectSelection();

				final List<StructuredActivityNode> tasks = ProcessNodeHelper.getProcessTasks(processNode);

				final NavigationProcessNode blmProcess = ProcessNodeHelper.getProcess(currentProject, processName);

				Process process = ProcessConverterHelper.getProcess(blmProcess, true, false, null);
				discoveryRequest.setProcess(process);

				// Global constraints
				QoSInformation qoSInformation = QoSUtil.getQoSInformation(processInformation.getQoSCriterions());
				qoSInformation.setQoSWeights(processInformation.getQoSWeights());
				discoveryRequest.setQoSInformation(qoSInformation);

				discoveryRequest.setInitialNumberOfQoSLevels(processInformation.getInitialNumberOfQoSLevels());
				discoveryRequest.setMaxNumberOfQoSLevels(processInformation.getMaxNumberOfQoSLevels());

				discoveryRequest.setTotalNumberOfCompositions(processInformation.getMaxNumberOfCompositions());

				// discoveryRequest.setCheckServicesAvailability(true);

				// Local constraints
				for (StructuredActivityNode taskNode : tasks) {

					final BMTaskInformationExtension taskInformation = ModelExtensionUtils.getTaskInformationExtension(taskNode);

					String serviceClassification = taskInformation.getTaxonomyClassification();

					if (!StringUtils.isBlank(serviceClassification)) {

						mapServiceClassificationTaskInfo.put(serviceClassification, taskInformation);

						QoSInformation taskQoSInformation = QoSUtil.getQoSInformation(taskInformation.getQoSCriterions(), true);

						DiscoveryRequestItem item = new DiscoveryRequestItem(serviceClassification, taskQoSInformation, taskInformation.getMaxNumberOfServicesForDiscovery());

						discoveryRequest.getItens().add(item);
					}
				}
				return discoveryRequest;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {

			}
		};
	}

	private void createTaskServicesTable(Composite parent) {
		int style = SWT.SINGLE | SWT.FULL_SELECTION;

		Table taskServicesTable = new Table(parent, style);

		final TableViewer taskServicesTableViewer = new TableViewer(taskServicesTable);
		taskServicesTableViewer.setUseHashlookup(true);

		GridData data = new GridData(GridData.FILL_BOTH);
		taskServicesTable.setLayoutData(data);

		taskServicesTable.setLinesVisible(true);
		taskServicesTable.setHeaderVisible(true);

		TableColumn col1 = new TableColumn(taskServicesTable, SWT.NONE);
		col1.setText(ProcessServiceAssociationList.TASK_NAME);
		col1.setWidth(250);
		col1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProcessServiceAssociationListSorter(taskServicesTableViewer, ProcessServiceAssociationListSorter.TASK_NAME);
			}
		});

		TableColumn col2 = new TableColumn(taskServicesTable, SWT.NONE);
		col2.setText(ProcessServiceAssociationList.TASK_TAXONOMY);
		col2.setWidth(250);
		col2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProcessServiceAssociationListSorter(taskServicesTableViewer, ProcessServiceAssociationListSorter.TASK_TAXONOMY);
			}
		});

		TableColumn col3 = new TableColumn(taskServicesTable, SWT.NONE);
		col3.setText(ProcessServiceAssociationList.QOS_CONSTRAINTS);
		col3.setWidth(130);
		col3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProcessServiceAssociationListSorter(taskServicesTableViewer, ProcessServiceAssociationListSorter.QOS_CONSTRAINTS);
			}
		});

		TableColumn col4 = new TableColumn(taskServicesTable, SWT.NONE);
		col4.setText(ProcessServiceAssociationList.MAX_RESULTS);
		col4.setWidth(130);
		col4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProcessServiceAssociationListSorter(taskServicesTableViewer, ProcessServiceAssociationListSorter.MAX_RESULTS);
			}
		});

		TableColumn col5 = new TableColumn(taskServicesTable, SWT.NONE);
		col5.setText(ProcessServiceAssociationList.MATCHING_SERVICES);
		col5.setWidth(130);
		col5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProcessServiceAssociationListSorter(taskServicesTableViewer, ProcessServiceAssociationListSorter.MATCHING_SERVICES);
			}
		});

		TableColumn col6 = new TableColumn(taskServicesTable, SWT.NONE);
		col6.setText(ProcessServiceAssociationList.TOTAL_SERVICES);
		col6.setWidth(130);
		col6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProcessServiceAssociationListSorter(taskServicesTableViewer, ProcessServiceAssociationListSorter.TOTAL_SERVICES);
			}
		});

		TableColumn col7 = new TableColumn(taskServicesTable, SWT.NONE);
		col7.setText(ProcessServiceAssociationList.PARTICIPANT);
		col7.setWidth(250);
		col7.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProcessServiceAssociationListSorter(taskServicesTableViewer, ProcessServiceAssociationListSorter.PARTICIPANT);
			}
		});

		CellEditor[] editors = new CellEditor[4];

		// Only Numeric
		editors[3] = new TextCellEditor(taskServicesTable, SWT.NONE);
		((Text) editors[3].getControl()).addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = "0123456789".indexOf(e.text) >= 0;
			}
		});

		taskServicesTableViewer.setCellEditors(editors);

		processServiceAssociationList = new ProcessServiceAssociationList(taskServicesTableViewer);
	}

	private void setProcessServiceAssociationListSorter(TableViewer table, int criteria) {

		ProcessServiceAssociationListSorter sorter = (ProcessServiceAssociationListSorter) table.getSorter();
		if (sorter == null)
			sorter = new ProcessServiceAssociationListSorter();
		else
			sorter = (ProcessServiceAssociationListSorter) sorter.clone();

		sorter.setCriteria(criteria);
		sorter.toggleReverse();
		table.setSorter(sorter);
	}

	private void createProcessCompositionTable(Composite parent) {
		int style = SWT.SINGLE | SWT.FULL_SELECTION;

		final Table processCompositionTable = new Table(parent, style);

		final TableViewer processCompositionTableViewer = new TableViewer(processCompositionTable);
		processCompositionTableViewer.setUseHashlookup(true);

		GridData data = new GridData(GridData.FILL_BOTH);
		processCompositionTable.setLayoutData(data);

		processCompositionTable.setLinesVisible(true);
		processCompositionTable.setHeaderVisible(true);

		TableColumn col1 = new TableColumn(processCompositionTable, SWT.NONE);
		col1.setText(ProcessCompositionList.RANKING);
		col1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesCompositionListSorter(processCompositionTableViewer, ProcessCompositionListSorter.RANKING);
			}
		});

		TableColumn col2 = new TableColumn(processCompositionTable, SWT.NONE);
		col2.setText(ProcessCompositionList.SERVICES);
		col2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesCompositionListSorter(processCompositionTableViewer, ProcessCompositionListSorter.SERVICES);
			}
		});

		TableColumn col3 = new TableColumn(processCompositionTable, SWT.NONE);
		col3.setText(ProcessCompositionList.TYPE);
		col3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesCompositionListSorter(processCompositionTableViewer, ProcessCompositionListSorter.SERVICES);
			}
		});

		TableColumn col4 = new TableColumn(processCompositionTable, SWT.NONE);
		col4.setText(ProcessCompositionList.PROCESS_UTILITY);
		col4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesCompositionListSorter(processCompositionTableViewer, ProcessCompositionListSorter.PROCESS_UTILITY);
			}
		});

		TableColumn col5 = new TableColumn(processCompositionTable, SWT.NONE);
		col5.setText(ProcessCompositionList.RATIO);

		processCompositionList = new ProcessCompositionList(processCompositionTableViewer, mapColumnNameToQoSAttribute);
		loadQoSColumns(processCompositionTableViewer, processCompositionList);
	}

	private void loadQoSColumns(final TableViewer tableViewer, AbstractBeanTableList<?> list) {
		try {

			for (final QoSAttribute qoSAttribute : QoSUtil.getEnabledQoSAttributes().values()) {

				String columnName = qoSAttribute.getQoSItem() + "/" + qoSAttribute.getName();
				list.addColumn(columnName);

				TableColumn col = new TableColumn(tableViewer.getTable(), SWT.NONE);
				col.setText(columnName);
				col.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setServicesCompositionListSorter(tableViewer, ProcessCompositionListSorter.QOS_VALUE, qoSAttribute);
					}
				});

				mapColumnNameToQoSAttribute.put(columnName, qoSAttribute);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setServicesCompositionListSorter(TableViewer table, int criteria) {
		setServicesCompositionListSorter(table, criteria, null);
	}

	private void setServicesCompositionListSorter(TableViewer table, int criteria, QoSAttribute qoSAttribute) {
		ProcessCompositionListSorter sorter = (ProcessCompositionListSorter) table.getSorter();
		if (sorter == null)
			sorter = new ProcessCompositionListSorter();
		else
			sorter = (ProcessCompositionListSorter) sorter.clone();
		sorter.setCriteria(criteria);
		sorter.setQoSAttribute(qoSAttribute);
		sorter.toggleReverse();
		table.setSorter(sorter);
	}

	@Override
	public void refresh() {

		processServiceAssociationList.refresh();
		processCompositionList.refresh();

		updateSelectionTypeOptions();
		updateCompostionsLabel();

		super.refresh();
	}

	protected void updateCompostionsLabel() {
		Integer n = processCompositionList.getTotalNumberOfCompositions();

		if (n == null) {
			compositionsLabel.setText("There aren't available compositions yet. Please search for services");
			compositionsLabel.setStyleRanges(new StyleRange[] {});

		} else if (n == 1)
			compositionsLabel.setText("There is just one available composition");
		else {

			Integer p = processCompositionList.getNumberOfShowingCompositions();

			String s1 = "There is a total of ";
			String s2 = " Showing the first ";
			String s3 = n > p ? s2 + p + " ones" : "";

			String text = s1 + n + " available compositions." + s3;

			compositionsLabel.setText(text);

			List<StyleRange> styles = new LinkedList<StyleRange>();

			styles.add(new StyleRange(s1.length(), n.toString().length(), null, null, SWT.BOLD));

			if (text.indexOf(s2) != -1)
				styles.add(new StyleRange(text.indexOf(s2) + s2.length(), p.toString().length(), null, null, SWT.BOLD));

			compositionsLabel.setStyleRanges((StyleRange[]) styles.toArray(new StyleRange[styles.size()]));
		}
	}

	@Override
	public void setModelAccessor(ModelAccessor modelAccessor) {

		if (modelAccessor != null) {
			ivModelAccessor = modelAccessor;
			ivGeneralModelAccessor = new GeneralModelAccessor(modelAccessor);

			processServiceAssociationList.setIvGeneralModelAccessor(ivGeneralModelAccessor);
			processCompositionList.setIvGeneralModelAccessor(ivGeneralModelAccessor);

			BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

			if (processInformation.isUseHeuristicServiceSelection() && processInformation.isUseOptimalServiceSelection()) {
				selectionTypeCombo.select(2);
			} else if (processInformation.isUseHeuristicServiceSelection()) {
				selectionTypeCombo.select(0);
			} else if (processInformation.isUseOptimalServiceSelection()) {
				selectionTypeCombo.select(1);
			}

			updateSelectionTypeOptions();
			updateCompostionsLabel();

			if (processInformation.getMaxNumberOfCompositions() != null)
				maxNumberOfCompositions.setText(String.valueOf(processInformation.getMaxNumberOfCompositions()));
			else
				maxNumberOfCompositions.setText("100");

			initialQoSLevels.setText(String.valueOf(processInformation.getInitialNumberOfQoSLevels()));
			maxQoSLevels.setText(String.valueOf(processInformation.getMaxNumberOfQoSLevels()));

		}
	}

	protected void updateSelectionTypeOptions() {
		if (ivGeneralModelAccessor != null) {
			BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

			((GridData) hybridSelectionOptionsComposite.getLayoutData()).exclude = !processInformation.isUseHeuristicServiceSelection();
			hybridSelectionOptionsComposite.setVisible(!((GridData) hybridSelectionOptionsComposite.getLayoutData()).exclude);

		}
	}

	protected void handleDiscoveryResult(final DiscoveryRequest discoveryRequest, DiscoveryResult result, final BMProcessInformationExtension processInformation, final Map<String, BMTaskInformationExtension> mapServiceClassificationTaskInfo, List<BMServicesComposition> compositions) {

		// Services

		Map<String, List<DiscoveredService>> groupedByProviderServiceUtility = result.groupByServiceClassificationProviderUtility();

		// for (Entry<String, List<DiscoveredService>> e : groupedByProviderServiceUtility.entrySet()) {
		// System.out.println(e.getKey() + " - " + e.getValue().size());
		// }

		// Get the best combination and use it as the first services for each task
		if (!CollectionUtils.isEmpty(result.getCompositions())) {

			Map<String, DiscoveredService> mapServices = new HashMap<String, DiscoveredService>();

			for (DiscoveredService service : result.getMatchingServices()) {
				mapServices.put(service.getServiceKey(), service);
			}

			ServicesComposition bestComposition = result.getCompositions().get(0);

			for (ServiceInfo serviceInfo : bestComposition.getServices()) {
				BMTaskInformationExtension taskInformation = mapServiceClassificationTaskInfo.get(serviceInfo.getServiceClassification());
				taskInformation.clearServiceAssociations();

				DiscoveredService discoveredService = mapServices.get(serviceInfo.getServiceKey());
				taskInformation.addService(discoveredService);
				groupedByProviderServiceUtility.get(serviceInfo.getServiceClassification()).remove(discoveredService);
			}
		}

		for (Entry<String, BMTaskInformationExtension> e : mapServiceClassificationTaskInfo.entrySet()) {

			String serviceClassification = e.getKey();

			BMTaskInformationExtension taskInformation = e.getValue();

			if (!taskInformation.getServiceAssociations().isEmpty()) {

				BMServiceAssociation first = taskInformation.getServiceAssociations().get(0);

				String firstServiceProvider = first.getServiceProviderName();

				List<DiscoveredService> services = groupedByProviderServiceUtility.get(serviceClassification);

				if (services != null) {
					ListIterator<DiscoveredService> it = services.listIterator();

					while (it.hasNext()) {
						DiscoveredService discoveredService = it.next();
						if (discoveredService.getServiceProvider().getName().equals(firstServiceProvider)) {
							taskInformation.addService(discoveredService);
							it.remove();
						}
					}
				}
			}
		}

		for (Entry<String, List<DiscoveredService>> e : groupedByProviderServiceUtility.entrySet()) {

			String serviceClassification = e.getKey();
			List<DiscoveredService> services = e.getValue();

			BMTaskInformationExtension taskInformation = mapServiceClassificationTaskInfo.get(serviceClassification);

			for (DiscoveredService service : services) {
				taskInformation.addService(service);
			}
		}

		// Total number of services
		for (Entry<String, Integer> e : result.getTotalNumberServicesPerServiceClassification().entrySet()) {

			String serviceClassification = e.getKey();
			Integer totalNumberOfServices = e.getValue();

			BMTaskInformationExtension taskInformation = mapServiceClassificationTaskInfo.get(serviceClassification);
			taskInformation.setTotalNumberOfServices(totalNumberOfServices);
		}

		// Local QoS Constraints
		if (!discoveryRequest.isUseOptimalSelection()) {
			Map<String, List<QoSConstraint>> localQoSConstraintsMap = result.getProcessQoSInfo().getLocalQoSConstraints();

			for (BMTaskInformationExtension taskInformation : mapServiceClassificationTaskInfo.values()) {

				List<QoSConstraint> localQoSConstraints = localQoSConstraintsMap != null ? localQoSConstraintsMap.get(taskInformation.getTaxonomyClassification()) : null;

				if (localQoSConstraints != null) {
					taskInformation.addManagedQoSConstraints(localQoSConstraints);
				} else {
					taskInformation.removeManagedQoSConstraints();
				}
			}
		}

		ProcessQoSInfo processQoSInfo = result.getProcessQoSInfo();

		if (processQoSInfo != null) {
			synchronized (processInformation) {
				processInformation.setGlobalQoSDelta(processQoSInfo.getGlobalQoSDelta());
			}
		}

		// Compositions
		boolean useOptimalSelection = discoveryRequest.isUseOptimalSelection();

		CompositionType compositionType = useOptimalSelection ? CompositionType.OPTIMAL : CompositionType.HEURISTIC;

		for (ServicesComposition composition : result.getCompositions()) {
			compositions.add(new BMServicesComposition(composition, compositionType));
		}

	}
}
