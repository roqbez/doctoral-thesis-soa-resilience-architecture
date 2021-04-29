package br.ufsc.gsigma.catalog.plugin.modules.resilience.task;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;
import com.ibm.btools.bom.model.resources.Role;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskResilienceConfiguration;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;

public class TaskResilienceUI extends Composite {

	@SuppressWarnings("unused")
	private ModelAccessor ivModelAccessor;

	private GeneralModelAccessor ivGeneralModelAccessor;
	private Text txtTaskServiceReplicas;

	private Button chkResilienceSupport;

	private Button chckDeployAdhocReplicas;

	private static final VerifyListener onlyNumericVerifyListener = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
			e.doit = NumberUtils.isDigits(e.text);
		}
	};
	private Text txtPartner;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public TaskResilienceUI(Composite parent, int style) {
		super(parent, style);

		setBackground(parent.getBackground());

		setLayout(new GridLayout(1, false));

		Composite wrapperComposite = new Composite(this, SWT.NONE);
		wrapperComposite.setBackground(parent.getBackground());
		GridData gd_wrapperComposite = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_wrapperComposite.widthHint = 500;
		wrapperComposite.setLayoutData(gd_wrapperComposite);

		GridLayout gl_wrapperComposite = new GridLayout(1, false);
		gl_wrapperComposite.marginHeight = 0;
		gl_wrapperComposite.verticalSpacing = 0;
		gl_wrapperComposite.horizontalSpacing = 0;
		wrapperComposite.setLayout(gl_wrapperComposite);

		Composite composite = new Composite(wrapperComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		composite.setBackground(parent.getBackground());

		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setBackground(parent.getBackground());
		lblNewLabel.setText("Partner:");

		txtPartner = new Text(composite, SWT.BORDER);
		txtPartner.setEditable(false);
		txtPartner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		new Label(composite, SWT.NONE);
		
				chkResilienceSupport = new Button(composite, SWT.CHECK);
				chkResilienceSupport.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						BMTaskResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
						resilienceConfiguration.setEnableResilience(btn.getSelection());
						writeResilienceConfiguration(resilienceConfiguration);
						updateValues();
					}
				});
				chkResilienceSupport.setBackground(parent.getBackground());
				chkResilienceSupport.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
				chkResilienceSupport.setText("Enable resilience support");
		new Label(composite, SWT.NONE);

		chckDeployAdhocReplicas = new Button(composite, SWT.CHECK);
		chckDeployAdhocReplicas.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				BMTaskResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setEnableAdhocServicesReplication(btn.getSelection());
				writeResilienceConfiguration(resilienceConfiguration);
				updateValues();
			}
		});
		chckDeployAdhocReplicas.setBackground(parent.getBackground());
		chckDeployAdhocReplicas.setText("Deploy adhoc replicas if necessary");

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

		txtTaskServiceReplicas = new Text(composite_2, SWT.BORDER);
		GridData gd_txtTaskServiceReplicas = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtTaskServiceReplicas.widthHint = 10;
		txtTaskServiceReplicas.setLayoutData(gd_txtTaskServiceReplicas);
		txtTaskServiceReplicas.addVerifyListener(onlyNumericVerifyListener);
		txtTaskServiceReplicas.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				Text txt = (Text) evt.getSource();
				BMTaskResilienceConfiguration resilienceConfiguration = getResilienceConfiguration();
				resilienceConfiguration.setServiceReplicas(!StringUtils.isEmpty(txt.getText()) ? Integer.valueOf(txt.getText()) : 0);
				writeResilienceConfiguration(resilienceConfiguration);
			}
		});

	}

	private void updateValues() {

		if (ivGeneralModelAccessor.getModel() instanceof StructuredActivityNode) {

			StructuredActivityNode taskNode = (StructuredActivityNode) ivGeneralModelAccessor.getModel();

			Role participantRole = ModelExtensionUtils.getParticipantRole(taskNode);

			txtPartner.setText(participantRole != null ? participantRole.getName() : "");
		}

		BMTaskInformationExtension processInformation = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);

		BMTaskResilienceConfiguration resilienceConfiguration = processInformation.getResilienceConfiguration();

		if (resilienceConfiguration == null) {
			resilienceConfiguration = new BMTaskResilienceConfiguration();
			writeResilienceConfiguration(processInformation, resilienceConfiguration);
		}

		chkResilienceSupport.setSelection(resilienceConfiguration.isEnableResilience());
		chckDeployAdhocReplicas.setSelection(resilienceConfiguration.isEnableAdhocServicesReplication());
		txtTaskServiceReplicas.setText(String.valueOf(resilienceConfiguration.getServiceReplicas()));

		chckDeployAdhocReplicas.setEnabled(resilienceConfiguration.isEnableResilience());
		txtTaskServiceReplicas.setEnabled(resilienceConfiguration.isEnableResilience() && resilienceConfiguration.isEnableAdhocServicesReplication());

	}

	private BMTaskResilienceConfiguration getResilienceConfiguration() {

		BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);

		BMTaskResilienceConfiguration resilienceConfiguration = taskInformationExtension.getResilienceConfiguration();

		if (resilienceConfiguration == null) {
			resilienceConfiguration = new BMTaskResilienceConfiguration();
		}

		return resilienceConfiguration;
	}

	private void writeResilienceConfiguration(BMTaskResilienceConfiguration resilienceConfiguration) {
		BMTaskInformationExtension taskInformation = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);
		writeResilienceConfiguration(taskInformation, resilienceConfiguration);
	}

	private void writeResilienceConfiguration(BMTaskInformationExtension taskInformation, BMTaskResilienceConfiguration resilienceConfiguration) {
		taskInformation.setResilienceConfiguration(resilienceConfiguration);
		taskInformation.write(ivGeneralModelAccessor);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setModelAccessor(ModelAccessor modelAccessor) {
		if (modelAccessor != null) {
			ivModelAccessor = modelAccessor;
			ivGeneralModelAccessor = new GeneralModelAccessor(modelAccessor);
			updateValues();
		}
	}
}
