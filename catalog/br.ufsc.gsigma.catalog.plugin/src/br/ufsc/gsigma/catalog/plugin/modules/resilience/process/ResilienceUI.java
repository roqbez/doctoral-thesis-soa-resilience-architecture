package br.ufsc.gsigma.catalog.plugin.modules.resilience.process;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMResilienceConfiguration;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ui.UIUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

public class ResilienceUI extends Composite {

	@SuppressWarnings("unused")
	private ModelAccessor ivModelAccessor;

	private GeneralModelAccessor ivGeneralModelAccessor;
	private Text txtExecutionServiceReplicas;
	private Text txtBindingServiceReplicas;
	private Text txtResilienceServiceReplicas;
	private Text txtServicesCheckInterval;
	private Text txtServicesCheckTimeout;
	private Text txtWaitNewConfigurationTimeout;

	private Button chkExecutionServiceReplication;

	private Button chkBindingServiceReplication;

	private Button chkResilienceServiceReplication;

	private static final VerifyListener onlyNumericVerifyListener = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
			e.doit = NumberUtils.isDigits(e.text);
		}
	};
	private Table tableResilienceParams;

	private ResilienceParamsList resilienceParamsList;

	private Table tableResilienceFlags;

	private ResilienceFlagsList resilienceFlagsList;

	private TableViewer tableViewerResilienceParams;

	private TableViewer tableViewerResilienceFlags;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ResilienceUI(Composite parent, int style) {
		super(parent, style);

		setBackground(parent.getBackground());

		setLayout(new GridLayout(1, false));

		Composite wrapperComposite = new Composite(this, SWT.NONE);
		wrapperComposite.setBackground(parent.getBackground());
		GridData gd_wrapperComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_wrapperComposite.widthHint = 555;
		wrapperComposite.setLayoutData(gd_wrapperComposite);

		GridLayout gl_wrapperComposite = new GridLayout(1, false);
		gl_wrapperComposite.marginWidth = 0;
		gl_wrapperComposite.marginHeight = 0;
		gl_wrapperComposite.verticalSpacing = 0;
		gl_wrapperComposite.horizontalSpacing = 0;
		wrapperComposite.setLayout(gl_wrapperComposite);

		Composite composite = new Composite(wrapperComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		composite.setBackground(parent.getBackground());

		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);

		chkExecutionServiceReplication = new Button(composite, SWT.CHECK);
		chkExecutionServiceReplication.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setEnableExecutionServiceReplication(btn.getSelection());
				writeResilienceConfiguration(resilienceConfiguration);
				updateValues();
			}
		});
		chkExecutionServiceReplication.setBackground(parent.getBackground());
		chkExecutionServiceReplication.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		chkExecutionServiceReplication.setText("Create process execution engine replicas");
		new Label(composite, SWT.NONE);

		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setBackground(parent.getBackground());
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.marginWidth = 0;
		gl_composite_1.verticalSpacing = 0;
		gl_composite_1.marginHeight = 0;
		composite_1.setLayout(gl_composite_1);

		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setBackground(parent.getBackground());
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("N\u00BA of Replicas:");

		txtExecutionServiceReplicas = new Text(composite_1, SWT.BORDER);
		txtExecutionServiceReplicas.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				Text txt = (Text) evt.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setExecutionServiceReplicas(!StringUtils.isEmpty(txt.getText()) ? Integer.valueOf(txt.getText()) : 0);
				writeResilienceConfiguration(resilienceConfiguration);
			}
		});

		GridData gd_txtExecutionServiceReplicas = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtExecutionServiceReplicas.widthHint = 10;
		txtExecutionServiceReplicas.setLayoutData(gd_txtExecutionServiceReplicas);
		txtExecutionServiceReplicas.addVerifyListener(onlyNumericVerifyListener);

		chkBindingServiceReplication = new Button(composite, SWT.CHECK);
		chkBindingServiceReplication.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setEnableBindingServiceReplication(btn.getSelection());
				writeResilienceConfiguration(resilienceConfiguration);
				updateValues();
			}
		});
		chkBindingServiceReplication.setBackground(parent.getBackground());
		chkBindingServiceReplication.setText("Create services mediation (ESB) replicas");
		new Label(composite, SWT.NONE);

		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setBackground(parent.getBackground());
		composite_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_2 = new GridLayout(2, false);
		gl_composite_2.marginWidth = 0;
		gl_composite_2.verticalSpacing = 0;
		gl_composite_2.marginHeight = 0;
		composite_2.setLayout(gl_composite_2);

		Label label = new Label(composite_2, SWT.NONE);
		label.setBackground(parent.getBackground());
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("N\u00BA of Replicas:");

		txtBindingServiceReplicas = new Text(composite_2, SWT.BORDER);
		GridData gd_txtBindingServiceReplicas = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtBindingServiceReplicas.widthHint = 10;
		txtBindingServiceReplicas.setLayoutData(gd_txtBindingServiceReplicas);
		txtBindingServiceReplicas.addVerifyListener(onlyNumericVerifyListener);
		txtBindingServiceReplicas.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				Text txt = (Text) evt.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setBindingServiceReplicas(!StringUtils.isEmpty(txt.getText()) ? Integer.valueOf(txt.getText()) : 0);
				writeResilienceConfiguration(resilienceConfiguration);
			}
		});

		chkResilienceServiceReplication = new Button(composite, SWT.CHECK);
		chkResilienceServiceReplication.setBackground(parent.getBackground());
		chkResilienceServiceReplication.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setEnableResilienceServiceReplication(btn.getSelection());
				writeResilienceConfiguration(resilienceConfiguration);
				updateValues();
			}
		});
		chkResilienceServiceReplication.setText("Create resilience module replicas");
		new Label(composite, SWT.NONE);

		Composite composite_3 = new Composite(composite, SWT.NONE);
		composite_3.setBackground(parent.getBackground());
		composite_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_3 = new GridLayout(2, false);
		gl_composite_3.marginWidth = 0;
		gl_composite_3.verticalSpacing = 0;
		gl_composite_3.marginHeight = 0;
		composite_3.setLayout(gl_composite_3);

		Label label_1 = new Label(composite_3, SWT.NONE);
		label_1.setBackground(parent.getBackground());
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_1.setText("N\u00BA of Replicas:");

		txtResilienceServiceReplicas = new Text(composite_3, SWT.BORDER);
		GridData gd_txtResilienceServiceReplicas = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtResilienceServiceReplicas.widthHint = 10;
		txtResilienceServiceReplicas.setLayoutData(gd_txtResilienceServiceReplicas);
		txtResilienceServiceReplicas.addVerifyListener(onlyNumericVerifyListener);
		txtResilienceServiceReplicas.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				Text txt = (Text) evt.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setResilienceServiceReplicas(!StringUtils.isEmpty(txt.getText()) ? Integer.valueOf(txt.getText()) : 0);
				writeResilienceConfiguration(resilienceConfiguration);
			}
		});

		Label lblCheckBoundServices = new Label(composite, SWT.NONE);
		lblCheckBoundServices.setBackground(parent.getBackground());
		lblCheckBoundServices.setText("Services check interval (ms):");
		new Label(composite, SWT.NONE);

		Composite composite_4 = new Composite(composite, SWT.NONE);
		composite_4.setBackground(parent.getBackground());
		composite_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_composite_4 = new GridLayout(1, false);
		gl_composite_4.marginWidth = 0;
		gl_composite_4.verticalSpacing = 0;
		gl_composite_4.marginHeight = 0;
		composite_4.setLayout(gl_composite_4);

		txtServicesCheckInterval = new Text(composite_4, SWT.BORDER);
		GridData gd_txtServicesCheckInterval = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_txtServicesCheckInterval.widthHint = 30;
		txtServicesCheckInterval.setLayoutData(gd_txtServicesCheckInterval);
		txtServicesCheckInterval.addVerifyListener(onlyNumericVerifyListener);
		txtServicesCheckInterval.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				Text txt = (Text) evt.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setServicesCheckInterval(!StringUtils.isEmpty(txt.getText()) ? Integer.valueOf(txt.getText()) : 0);
				resilienceConfiguration.getParams().put("SERVICE_MONITOR_CHECK_INTERVAL", String.valueOf(resilienceConfiguration.getServicesCheckInterval()));
				writeResilienceConfiguration(resilienceConfiguration);
				tableViewerResilienceParams.refresh();
			}
		});

		Label lblServiceCheckConnect = new Label(composite, SWT.NONE);
		lblServiceCheckConnect.setBackground(parent.getBackground());
		lblServiceCheckConnect.setText("Services check timeout (ms):");
		new Label(composite, SWT.NONE);

		Composite composite_5 = new Composite(composite, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_composite_5 = new GridLayout(1, false);
		gl_composite_5.verticalSpacing = 0;
		gl_composite_5.marginWidth = 0;
		gl_composite_5.marginHeight = 0;
		composite_5.setLayout(gl_composite_5);

		txtServicesCheckTimeout = new Text(composite_5, SWT.BORDER);
		GridData gd_txtServicesCheckTimeout = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_txtServicesCheckTimeout.widthHint = 30;
		txtServicesCheckTimeout.setLayoutData(gd_txtServicesCheckTimeout);
		txtServicesCheckTimeout.addVerifyListener(onlyNumericVerifyListener);
		txtServicesCheckTimeout.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				Text txt = (Text) evt.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setServicesCheckTimeout(!StringUtils.isEmpty(txt.getText()) ? Integer.valueOf(txt.getText()) : 0);
				resilienceConfiguration.getParams().put("DEFAULT_HTTP_READ_TIMEOUT", String.valueOf(resilienceConfiguration.getServicesCheckTimeout()));
				writeResilienceConfiguration(resilienceConfiguration);
				tableViewerResilienceParams.refresh();
			}
		});

		Label lblWaitForNew = new Label(composite, SWT.NONE);
		lblWaitForNew.setBackground(parent.getBackground());
		lblWaitForNew.setText("Wait for new configuration timeout (ms):");
		new Label(composite, SWT.NONE);

		Composite composite_6 = new Composite(composite, SWT.NONE);
		composite_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_composite_6 = new GridLayout(1, false);
		gl_composite_6.verticalSpacing = 0;
		gl_composite_6.marginWidth = 0;
		gl_composite_6.marginHeight = 0;
		composite_6.setLayout(gl_composite_6);

		txtWaitNewConfigurationTimeout = new Text(composite_6, SWT.BORDER);
		GridData gd_txtWaitNewConfigurationTimeout = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_txtWaitNewConfigurationTimeout.widthHint = 30;
		txtWaitNewConfigurationTimeout.setLayoutData(gd_txtWaitNewConfigurationTimeout);

		ExpandBar expandBar = new ExpandBar(wrapperComposite, SWT.NONE);
		expandBar.setSpacing(0);
		expandBar.setBackground(parent.getBackground());
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

		ExpandItem advancedConfigurationBar = new ExpandItem(expandBar, SWT.NONE);
		advancedConfigurationBar.setExpanded(true);
		advancedConfigurationBar.setText("Advanced Configuration");

		Composite composite_7 = new Composite(expandBar, SWT.NONE);
		composite_7.setBackground(parent.getBackground());
		advancedConfigurationBar.setControl(composite_7);
		advancedConfigurationBar.setHeight(200);
		GridLayout gl_composite_7 = new GridLayout(1, false);
		gl_composite_7.verticalSpacing = 0;
		gl_composite_7.horizontalSpacing = 0;
		gl_composite_7.marginHeight = 0;
		gl_composite_7.marginWidth = 0;
		composite_7.setLayout(gl_composite_7);

		TabFolder tabFolder = new TabFolder(composite_7, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TabItem tbtmParams = new TabItem(tabFolder, SWT.NONE);
		tbtmParams.setText("Parameters");

		tableResilienceParams = new Table(tabFolder, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tbtmParams.setControl(tableResilienceParams);
		tableResilienceParams.setHeaderVisible(true);
		tableResilienceParams.setLinesVisible(true);

		TableColumn clmnParamName = new TableColumn(tableResilienceParams, SWT.NONE);
		clmnParamName.setResizable(false);
		clmnParamName.setWidth(345);
		clmnParamName.setText("Name");

		TableColumn clmParamValue = new TableColumn(tableResilienceParams, SWT.NONE);
		clmParamValue.setResizable(false);
		clmParamValue.setWidth(128);
		clmParamValue.setText("Value");

		TabItem tbtmFlags = new TabItem(tabFolder, SWT.NONE);
		tbtmFlags.setText("Flags");

		tableResilienceFlags = new Table(tabFolder, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableResilienceFlags.setLinesVisible(true);
		tableResilienceFlags.setHeaderVisible(true);
		tbtmFlags.setControl(tableResilienceFlags);

		TableColumn clmnFlagName = new TableColumn(tableResilienceFlags, SWT.NONE);
		clmnFlagName.setWidth(352);
		clmnFlagName.setText("Name");
		clmnFlagName.setResizable(false);

		TableColumn clmnFlagValue = new TableColumn(tableResilienceFlags, SWT.NONE);
		clmnFlagValue.setWidth(135);
		clmnFlagValue.setText("Value");
		clmnFlagValue.setResizable(false);
		txtWaitNewConfigurationTimeout.addVerifyListener(onlyNumericVerifyListener);
		txtWaitNewConfigurationTimeout.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				Text txt = (Text) evt.getSource();
				BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setWaitNewConfigurationTimeout(!StringUtils.isEmpty(txt.getText()) ? Integer.valueOf(txt.getText()) : 0);
				resilienceConfiguration.getParams().put("WAIT_FOR_NEW_BINDING_CONFIGURATION_TIMEOUT", String.valueOf(resilienceConfiguration.getWaitNewConfigurationTimeout()));
				writeResilienceConfiguration(resilienceConfiguration);
				tableViewerResilienceParams.refresh();
			}
		});
	}

	private void loadResilienceParamsTable() {

		BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();

		Map<String, String> params = resilienceConfiguration.getParams();

		for (Field f : ResilienceParams.class.getDeclaredFields()) {

			if (!params.containsKey(f.getName())) {
				try {
					params.put(f.getName(), String.valueOf(f.get(null)));
				} catch (Exception e) {
				}
			}
		}

		BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

		writeResilienceConfiguration(processInformation, resilienceConfiguration);

		// HashMap<Integer, Integer> fixedColumns = new HashMap<Integer, Integer>();
		// fixedColumns.put(1, 120);
		// UIUtil.autoSizeColumns(tableResilienceParams, fixedColumns, new HashMap<Integer, Integer>());
	}

	private void loadResilienceFlagsTable() {

		BMResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();

		Map<String, Boolean> flags = resilienceConfiguration.getFlags();

		for (Field f : ExecutionFlags.class.getDeclaredFields()) {

			if (!flags.containsKey(f.getName())) {
				try {
					flags.put(f.getName(), false);
				} catch (Exception e) {
				}
			}
		}

		BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

		writeResilienceConfiguration(processInformation, resilienceConfiguration);

		HashMap<Integer, Integer> fixedColumns = new HashMap<Integer, Integer>();
		fixedColumns.put(1, 150);
		UIUtil.autoSizeColumns(tableResilienceFlags, fixedColumns, new HashMap<Integer, Integer>());
	}

	private void updateValues() {

		BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

		BMResilienceConfiguration resilienceConfiguration = processInformation.getResilienceConfiguration();

		if (resilienceConfiguration == null) {
			resilienceConfiguration = new BMResilienceConfiguration();
			writeResilienceConfiguration(processInformation, resilienceConfiguration);
		}

		chkBindingServiceReplication.setSelection(resilienceConfiguration.isEnableBindingServiceReplication());
		chkExecutionServiceReplication.setSelection(resilienceConfiguration.isEnableExecutionServiceReplication());
		chkResilienceServiceReplication.setSelection(resilienceConfiguration.isEnableResilienceServiceReplication());

		txtExecutionServiceReplicas.setText(String.valueOf(resilienceConfiguration.getExecutionServiceReplicas()));
		txtBindingServiceReplicas.setText(String.valueOf(resilienceConfiguration.getBindingServiceReplicas()));
		txtResilienceServiceReplicas.setText(String.valueOf(resilienceConfiguration.getResilienceServiceReplicas()));

		txtExecutionServiceReplicas.setEnabled(resilienceConfiguration.isEnableExecutionServiceReplication());
		txtBindingServiceReplicas.setEnabled(resilienceConfiguration.isEnableBindingServiceReplication());
		txtResilienceServiceReplicas.setEnabled(resilienceConfiguration.isEnableResilienceServiceReplication());

		txtServicesCheckInterval.setText(String.valueOf(resilienceConfiguration.getServicesCheckInterval()));
		txtServicesCheckTimeout.setText(String.valueOf(resilienceConfiguration.getServicesCheckTimeout()));
		txtWaitNewConfigurationTimeout.setText(String.valueOf(resilienceConfiguration.getWaitNewConfigurationTimeout()));

		loadResilienceParamsTable();
		loadResilienceFlagsTable();
	}

	private BMResilienceConfiguration getResilienceConfiguration() {

		BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

		BMResilienceConfiguration resilienceConfiguration = processInformation.getResilienceConfiguration();

		if (resilienceConfiguration == null) {
			resilienceConfiguration = new BMResilienceConfiguration();
		}

		return resilienceConfiguration;
	}

	private void writeResilienceConfiguration(BMResilienceConfiguration resilienceConfiguration) {
		BMProcessInformationExtension processInformation = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);
		writeResilienceConfiguration(processInformation, resilienceConfiguration);
	}

	private void writeResilienceConfiguration(BMProcessInformationExtension processInformation, BMResilienceConfiguration resilienceConfiguration) {
		processInformation.setResilienceConfiguration(resilienceConfiguration);
		processInformation.write(ivGeneralModelAccessor);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setModelAccessor(ModelAccessor modelAccessor) {
		if (modelAccessor != null) {
			ivModelAccessor = modelAccessor;
			ivGeneralModelAccessor = new GeneralModelAccessor(modelAccessor);

			if (resilienceParamsList == null) {

				tableViewerResilienceParams = new TableViewer(tableResilienceParams);
				tableViewerResilienceParams.setUseHashlookup(true);

				CellEditor[] editors = new CellEditor[2];
				editors[1] = new TextCellEditor(tableResilienceParams, SWT.NONE);
				tableViewerResilienceParams.setCellEditors(editors);

				resilienceParamsList = new ResilienceParamsList(tableViewerResilienceParams, ivGeneralModelAccessor);
			}

			resilienceParamsList.setIvGeneralModelAccessor(ivGeneralModelAccessor);

			if (resilienceFlagsList == null) {

				tableViewerResilienceFlags = new TableViewer(tableResilienceFlags);
				tableViewerResilienceFlags.setUseHashlookup(true);

				CellEditor[] editors = new CellEditor[2];
				editors[1] = new TextCellEditor(tableResilienceFlags, SWT.NONE);

				// Only Numeric
				((Text) editors[1].getControl()).addVerifyListener(new VerifyListener() {
					@Override
					public void verifyText(VerifyEvent e) {
						e.doit = "01".indexOf(e.text) >= 0;
					}
				});

				tableViewerResilienceFlags.setCellEditors(editors);

				resilienceFlagsList = new ResilienceFlagsList(tableViewerResilienceFlags, ivGeneralModelAccessor);
			}

			resilienceFlagsList.setIvGeneralModelAccessor(ivGeneralModelAccessor);

			updateValues();
		}
	}
}
