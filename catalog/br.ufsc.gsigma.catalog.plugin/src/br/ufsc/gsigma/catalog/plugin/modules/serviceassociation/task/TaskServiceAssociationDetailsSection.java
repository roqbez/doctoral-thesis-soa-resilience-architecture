package br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.task;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;
import com.ibm.btools.ui.framework.WidgetFactory;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServiceAssociation;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.QoSUtil;
import br.ufsc.gsigma.catalog.plugin.util.ServiceAssociationUtil;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractCatalogTabContentSection;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryService;
import br.ufsc.gsigma.servicediscovery.locator.DiscoveryServiceLocator;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequest;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryResult;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;

public class TaskServiceAssociationDetailsSection extends AbstractCatalogTabContentSection {

	private TaskServiceAssociationList servicesAssociationList;

	private TableViewer servicesTableViewer = null;

	protected Map<String, QoSAttribute> mapColumnNameToQoSAttribute = new HashMap<String, QoSAttribute>();

	private Text taxonomyName;

	public TaskServiceAssociationDetailsSection(WidgetFactory widgetFactory) {
		super(widgetFactory);
	}

	protected Composite createClient(final Composite paramComposite) {

		super.createClient(paramComposite);

		gridData = new GridData(GridData.FILL_BOTH);
		layout = new GridLayout(1, false);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		createTaxonomyRow();
		// createDiscoverRow();

		createServicesTable(mainComposite);

		return mainComposite;
	}

	@SuppressWarnings("unused")
	private void createDiscoverRow() {
		Composite buttonComposite = getWidgetFactory().createComposite(mainComposite);
		buttonComposite.setLayout(new GridLayout(2, false));

		Button discoverButton = ivFactory.createButton(buttonComposite, "Discover Services", 0);

		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		discoverButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				try {

					final BMTaskInformationExtension taskInformation = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);

					if (StringUtils.isBlank(taskInformation.getTaxonomyClassification())) {
						MessageDialog.openError(shell, "Error", "This task doesn't have an associated Taxonomy Classification");

					} else {

						try {

							final StructuredActivityNode taskNode = (StructuredActivityNode) ivModelAccessor.getModel();

							final StructuredActivityNode processNode = (StructuredActivityNode) taskNode.eContainer();

							final BMProcessInformationExtension processInfo = ModelExtensionUtils.getProcessInformationExtension(processNode);

							final List<BMServiceAssociation> matchingServiceAssociations = new ArrayList<BMServiceAssociation>();

							try {

								new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {

									@Override
									public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

										monitor.beginTask("Discovering services for " + taskInformation.getTaxonomyClassification(), IProgressMonitor.UNKNOWN);

										final DiscoveryService discoveryService = DiscoveryServiceLocator.get();

										try {

											QoSInformation qoSInformation = QoSUtil.getQoSInformation(taskInformation.getQoSCriterions());
											qoSInformation.setGlobalQoSDelta(processInfo.getGlobalQoSDelta());
											qoSInformation.setQoSWeights(processInfo.getQoSWeights());

											int maxResults = taskInformation.getMaxNumberOfServicesForDiscovery();

											DiscoveryRequest discoveryRequest = new DiscoveryRequest(taskInformation.getTaxonomyClassification(), qoSInformation, maxResults);

											DiscoveryResult discoveryResult = discoveryService.discover(discoveryRequest);

											for (DiscoveredService s : discoveryResult.getMatchingServices()) {
												matchingServiceAssociations.add(ServiceAssociationUtil.getBMServiceAssociation(s));
											}

										} catch (Throwable e) {
											throw new RuntimeException(e);
										}

										monitor.done();
									}

								});

							} catch (Throwable e) {
								MessageDialog.openError(shell, "Discovery Service", "The Discovery Service is unavailable");
								return;
							}

							servicesAssociationList.set(matchingServiceAssociations);

							if (matchingServiceAssociations.size() == 0)
								MessageDialog.openWarning(shell, "Discovery Result",
										"The Discovery Service didn't find any service that matches your QoS Constraints. Please relax your QoS requirements to use these services or take the risk and let the system discover them at the online phase");
							// else if (matchingServiceAssociations.size() == 0 && nonMatchingServiceAssociations.size() == 0)
							// MessageDialog.openWarning(shell, "Discovery Result", "The System didn't find any candidate service");
							// else if (matchingServiceAssociations.size() > 0)
							else
								MessageDialog.openInformation(shell, "Discovery Result", "The Discovery Service found " + matchingServiceAssociations.size() + " service(s) that match perfectly your QoS Constraints");

						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();

				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {

			}
		});

		Button clearServicesButton = ivFactory.createButton(buttonComposite, "Clear Services", 0);

		clearServicesButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				servicesAssociationList.clear();
				// nonMatchingServicesAssociationList.clear();
			}
		});
	}

	private void createTaxonomyRow() {

		Composite taxonomyComposite = ivFactory.createComposite(mainComposite);

		GridLayout twoRows = new GridLayout(2, false);
		twoRows.marginWidth = 0;

		taxonomyComposite.setLayout(twoRows);
		taxonomyComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite nameComposite = ivFactory.createComposite(taxonomyComposite);
		nameComposite.setLayout(twoRows);
		nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ivFactory.createLabel(nameComposite, "Taxonomy Classification:");

		Composite inputComposite = ivFactory.createComposite(nameComposite);
		GridLayout oneRow = new GridLayout(1, false);
		oneRow.marginRight = 3;
		inputComposite.setLayout(oneRow);
		inputComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		taxonomyName = ivFactory.createText(inputComposite, "", 4);
		taxonomyName.setEditable(false);
		taxonomyName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ivFactory.paintBordersFor(inputComposite);

		Button chooseTaxonomyButton = ivFactory.createButton(taxonomyComposite, "Choose the Taxonomy", 0);

		final TaskServiceAssociationDetailsSection instance = this;

		chooseTaxonomyButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				try {
					TaskChooseTaxonomyDialog dialog = new TaskChooseTaxonomyDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), instance);

					dialog.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {

			}
		});
	}

	private void loadQoSColumns(final TableViewer tableViewer, AbstractBeanTableList<?> list) {
		try {

			for (final QoSAttribute qoSAttribute : QoSUtil.getEnabledQoSAttributes().values()) {

				String columnName = qoSAttribute.getQoSItem() + "/" + qoSAttribute.getName();
				list.addColumn(columnName);

				TableColumn col = new TableColumn(tableViewer.getTable(), SWT.NONE);
				col.setText(columnName);
				col.setWidth(180);
				col.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setServicesListSorter(tableViewer, TaskServiceAssociationListSorter.QOS_VALUE, qoSAttribute);
					}
				});

				mapColumnNameToQoSAttribute.put(columnName, qoSAttribute);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Table createServicesTable(Composite parent) {
		int style = SWT.SINGLE | SWT.FULL_SELECTION;

		Table table = new Table(parent, style);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 150;

		table.setLayoutData(data);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn col1 = new TableColumn(table, SWT.NONE);
		col1.setText(TaskServiceAssociationList.SERVICE_NAME);
		col1.setWidth(150);
		col1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesListSorter(servicesTableViewer, TaskServiceAssociationListSorter.SERVICE_NAME);
			}
		});

		TableColumn col2 = new TableColumn(table, SWT.NONE);
		col2.setText(TaskServiceAssociationList.SERVICE_ENDPOINT);
		col2.setWidth(150);
		col2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesListSorter(servicesTableViewer, TaskServiceAssociationListSorter.SERVICE_NAME);
			}
		});

		TableColumn col3 = new TableColumn(table, SWT.NONE);
		col3.setText(TaskServiceAssociationList.SERVICE_PROTOCOL);
		col3.setWidth(150);
		col3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesListSorter(servicesTableViewer, TaskServiceAssociationListSorter.SERVICE_PROTOCOL);
			}
		});

		TableColumn col4 = new TableColumn(table, SWT.NONE);
		col4.setText(TaskServiceAssociationList.SERVICE_PROVIDER_NAME);
		col4.setWidth(150);
		col4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesListSorter(servicesTableViewer, TaskServiceAssociationListSorter.SERVICE_PROVIDER_NAME);
			}
		});

		TableColumn col5 = new TableColumn(table, SWT.NONE);
		col5.setText(TaskServiceAssociationList.SERVICE_UTILITY);
		col5.setWidth(150);
		col5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setServicesListSorter(servicesTableViewer, TaskServiceAssociationListSorter.SERVICE_UTILITY);
			}
		});

		// TableColumn col5 = new TableColumn(ivTable, SWT.NONE);
		// col5.setText(SERVICE_PROVIDER_REPUTATION);
		// col5.setWidth(180);
		// col5.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// setServicesListSorter(ivTableViewer, ServicesListSorter.SERVICE_PROVIDER_REPUTATION);
		// }
		// });

		this.servicesTableViewer = new TableViewer(table);
		this.servicesTableViewer.setUseHashlookup(true);

		this.servicesAssociationList = new TaskServiceAssociationList(this.servicesTableViewer, mapColumnNameToQoSAttribute);

		loadQoSColumns(servicesTableViewer, servicesAssociationList);

		return table;

	}

	private void setServicesListSorter(TableViewer table, int criteria) {
		setServicesListSorter(table, criteria, null);
	}

	private void setServicesListSorter(TableViewer table, int criteria, QoSAttribute qoSAttribute) {
		TaskServiceAssociationListSorter sorter = (TaskServiceAssociationListSorter) table.getSorter();
		if (sorter == null)
			sorter = new TaskServiceAssociationListSorter();
		else
			sorter = (TaskServiceAssociationListSorter) sorter.clone();
		sorter.setCriteria(criteria);
		sorter.setQoSAttribute(qoSAttribute);
		sorter.toggleReverse();
		table.setSorter(sorter);
	}

	@Override
	public void refresh() {

		super.refresh();

		servicesAssociationList.refresh();

		if (ivGeneralModelAccessor != null) {
			BMTaskInformationExtension extension = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);
			taxonomyName.setText((String) ObjectUtils.defaultIfNull(extension != null ? extension.getTaxonomyClassification() : null, ""));
		}
	}

	protected void setTaxonomyName(String name) {
		taxonomyName.setText(name);

		BMTaskInformationExtension extension = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);

		extension.setTaxonomyClassification(name);

		extension.write(ivGeneralModelAccessor);
	}

	@Override
	public void setModelAccessor(ModelAccessor modelAccessor) {
		ivModelAccessor = modelAccessor;
		ivGeneralModelAccessor = new GeneralModelAccessor(modelAccessor);
		servicesAssociationList.setIvGeneralModelAccessor(ivGeneralModelAccessor);
	}

}
