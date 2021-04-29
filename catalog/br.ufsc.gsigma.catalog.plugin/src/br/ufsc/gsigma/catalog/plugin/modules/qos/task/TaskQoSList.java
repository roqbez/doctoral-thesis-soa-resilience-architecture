package br.ufsc.gsigma.catalog.plugin.modules.qos.task;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.QoSUtil;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute.QoSValueUtilityDirection;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;

public class TaskQoSList extends AbstractBeanTableList<QoSCriterion> {

	public static final String QOS_ITEM = "QoS Item";
	public static final String QOS_ATTRIBUTE = "QoS Attribute";
	public static final String QOS_COMPARISION = "Comparision";
	public static final String QOS_VALUE = "Value";
	public static final String QOS_UNIT = "Unit";
	public static final String QOS_DERIVED_FROM_GLOBAL = "Derived from Global";

	protected GeneralModelAccessor ivGeneralModelAccessor;

	public TaskQoSList(TableViewer tableViewer) {
		super(tableViewer);

		setRowColorProvider(new IColorProvider() {

			@Override
			public Color getForeground(Object element) {

				QoSCriterion qoSCriterion = (QoSCriterion) element;

				if (qoSCriterion != null && qoSCriterion.isManaged())
					return Display.getDefault().getSystemColor(SWT.COLOR_RED);
				else
					return null;
			}

			@Override
			public Color getBackground(Object paramObject) {
				return null;
			}
		});

		setColumnValueProvider(new ColumnValueProvider<QoSCriterion>() {

			@Override
			public String getColumnValue(QoSCriterion qoSCriterion, String columnName) {

				if (columnName.equals(QOS_ITEM))
					return qoSCriterion.getQoSItem();

				else if (columnName.equals(QOS_ATTRIBUTE))
					return qoSCriterion.getQoSAttribute();

				else if (columnName.equals(QOS_COMPARISION))
					return getComparisionTypeLabel(qoSCriterion.getComparisionType());

				else if (columnName.equals(QOS_VALUE))
					return (qoSCriterion.getQoSValue() != null) ? qoSCriterion.getQoSValue().toString() : "";

				else if (columnName.equals(QOS_UNIT)) {
					QoSAttribute qoSAttribute = QoSUtil.getEnabledQoSAttribute(qoSCriterion.getQoSKey());
					return qoSAttribute != null ? qoSAttribute.getUnit() : "N/A";

				} else if (columnName.equals(QOS_DERIVED_FROM_GLOBAL)) {
					return qoSCriterion.isManaged() ? "yes" : "no";
				}

				return null;
			}
		});

		setBeanPropertyModifier(new BeanPropertyModifier<QoSCriterion>() {

			@Override
			public boolean modifyBeanPropertyValue(QoSCriterion qoSCriterion, String columnName, Object value) {

				if (columnName.equals(QOS_COMPARISION)) {
					qoSCriterion.setComparisionType(getComparisionTypeValue(getComparisionTypeChoices()[(Integer) value]));
					return true;

				} else if (columnName.equals(QOS_VALUE)) {
					if (NumberUtils.isNumber(value.toString())) {
						qoSCriterion.setQoSValue(new Double(value.toString()));
						return true;
					}
					return false;
				}

				return false;
			}
		});
	}

	public void setIvGeneralModelAccessor(GeneralModelAccessor ivGeneralModelAccessor) {
		this.ivGeneralModelAccessor = ivGeneralModelAccessor;
	}

	public void add(QoSAttribute qoSAttribute) {

		QoSCriterion qoSCriterion = new QoSCriterion(qoSAttribute.getQoSItem(), qoSAttribute.getName());

		if (qoSAttribute.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE)
			qoSCriterion.setComparisionType("le");
		else if (qoSAttribute.getValueUtilityDirection() == QoSValueUtilityDirection.POSITIVE)
			qoSCriterion.setComparisionType("ge");

		add(qoSCriterion);
	}

	public boolean isAttributeAlreadySelected(QoSAttribute qoSAttribute) {

		for (QoSCriterion q : getItens()) {
			if (q.getQoSItem().equals(qoSAttribute.getQoSItem()) && q.getQoSAttribute().equals(qoSAttribute.getName()))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<QoSCriterion> loadItems() {

		if (ivGeneralModelAccessor == null)
			return Collections.EMPTY_LIST;

		BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);

		return taskInformationExtension.getQoSCriterions();
	}

	@Override
	protected void saveItems(List<QoSCriterion> itens) {
		if (ivGeneralModelAccessor != null) {
			BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(ivGeneralModelAccessor);
			taskInformationExtension.setQoSCriterions(itens);
			taskInformationExtension.write(ivGeneralModelAccessor);
		}
	}

	private static String[] getComparisionTypeChoices() {
		return new String[] { "=", ">=", "<=", ">", "<" };
	}

	private static String getComparisionTypeLabel(String comparisionType) {
		if (comparisionType == null)
			return "=";
		else if (comparisionType.equalsIgnoreCase("eq"))
			return "=";
		else if (comparisionType.equalsIgnoreCase("lt"))
			return "<";
		else if (comparisionType.equalsIgnoreCase("le"))
			return "<=";
		else if (comparisionType.equalsIgnoreCase("gt"))
			return ">";
		else if (comparisionType.equalsIgnoreCase("ge"))
			return ">=";
		else
			return "=";
	}

	private String getComparisionTypeValue(String comparisionTypeLabel) {

		if (comparisionTypeLabel == null)
			return "eq";
		else if (comparisionTypeLabel.equals("="))
			return "eq";
		else if (comparisionTypeLabel.equals("<"))
			return "lt";
		else if (comparisionTypeLabel.equals("<="))
			return "le";
		else if (comparisionTypeLabel.equals(">"))
			return "gt";
		else if (comparisionTypeLabel.equals(">="))
			return "ge";
		else
			return "eq";
	}
}
