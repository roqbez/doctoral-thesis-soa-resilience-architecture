package br.ufsc.gsigma.catalog.plugin.modules.qos.process;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMQoSThresholds;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process.TaskServiceAssociationInfo;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;

public class ProcessQoSThresholdList extends AbstractBeanTableList<BMQoSThresholds> {

	public static final String NAME = "Name";

	private StructuredActivityNode processNode;

	private GeneralModelAccessor ivGeneralModelAccessor;

	private Map<String, Integer> mapPaddingSize = new HashMap<String, Integer>();

	public ProcessQoSThresholdList(TableViewer tableViewer, final Map<String, QoSAttribute> mapColumnNameToQoSAttribute) {

		super(tableViewer);

		addColumn(NAME);

		setRowColorProvider(new IColorProvider() {

			@Override
			public Color getForeground(Object element) {

				return null;
			}

			@Override
			public Color getBackground(Object element) {

				BMQoSThresholds qoSThreshold = (BMQoSThresholds) element;

				if (qoSThreshold != null && qoSThreshold.isProcess()) {
					Color color = new Color(Display.getCurrent(), 155, 220, 255);
					return color;
				} else
					return null;

			}
		});

		setColumnValueProvider(new ColumnValueProvider<BMQoSThresholds>() {

			@Override
			public String getColumnValue(BMQoSThresholds qoSThreshold, String columnName) {

				if (columnName.equals(NAME))
					return (qoSThreshold.isProcess() ? "P: " : "T: ") + qoSThreshold.getName();

				else if (mapColumnNameToQoSAttribute.get(columnName) != null) {

					QoSAttribute qoSAttribute = mapColumnNameToQoSAttribute.get(columnName);

					DecimalFormat df = new DecimalFormat("0.00");
					String qosKey = qoSAttribute.getKey();

					Integer n = mapPaddingSize.get(qosKey);

					return "min: " + padRightIfNecessary(qosKey, df.format(qoSThreshold.getQoSMinValues().get(qosKey)), n) + " max: " + df.format(qoSThreshold.getQoSMaxValues().get(qosKey));
				}

				return null;
			}
		});

	}

	private String padRightIfNecessary(String qoSKey, String s, int n) {

		if (s.length() >= n)
			return s;
		else {

			int w = n - s.length();

			StringBuilder sb = new StringBuilder(s);
			for (int i = 0; i <= w; i++)
				sb.append(" ");

			// gamb
			if (s.indexOf(',') == 1 || s.indexOf('.') == 1)
				sb.append(" ");

			String str = sb.toString();

			return str;
		}

	}

	@Override
	protected List<BMQoSThresholds> loadItems() {

		List<BMQoSThresholds> result = new ArrayList<BMQoSThresholds>();

		if (ivGeneralModelAccessor != null) {

			BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtension(ivGeneralModelAccessor);

			BMQoSThresholds qoSThresholds = processInformationExtension.getQoSThresholds();

			if (qoSThresholds != null) {
				if (processNode != null)
					qoSThresholds.setName(processNode.getName());

				qoSThresholds.setProcess(true);

				result.add(qoSThresholds);
			}

		}
		if (processNode != null) {

			for (Object o : processNode.getNodeContents()) {
				if (o instanceof StructuredActivityNode) {

					StructuredActivityNode taskNode = (StructuredActivityNode) o;

					TaskServiceAssociationInfo info = new TaskServiceAssociationInfo();
					info.setTaskName(taskNode.getName());

					BMTaskInformationExtension taskInformationExtension = ModelExtensionUtils.getTaskInformationExtension(taskNode);

					BMQoSThresholds qoSThresholds = taskInformationExtension.getQoSThresholds();

					if (qoSThresholds != null) {
						qoSThresholds.setName(taskNode.getName());
						result.add(qoSThresholds);
					}
				}
			}
		}

		if (mapPaddingSize != null)
			mapPaddingSize.clear();
		else
			mapPaddingSize = new HashMap<String, Integer>();

		DecimalFormat df = new DecimalFormat("0.00");

		for (BMQoSThresholds q : result) {

			for (Entry<String, Double> e : q.getQoSMinValues().entrySet()) {

				int n = df.format(e.getValue()).length();

				Integer v = mapPaddingSize.get(e.getKey());

				if (v == null)
					v = n;
				else
					v = Math.max(n, v);

				mapPaddingSize.put(e.getKey(), v);
			}
		}

		return result;
	}

	public void setIvGeneralModelAccessor(GeneralModelAccessor ivGeneralModelAccessor) {
		this.ivGeneralModelAccessor = ivGeneralModelAccessor;

		if (ivGeneralModelAccessor.getModel() instanceof StructuredActivityNode)
			this.processNode = (StructuredActivityNode) ivGeneralModelAccessor.getModel();
	}

}
