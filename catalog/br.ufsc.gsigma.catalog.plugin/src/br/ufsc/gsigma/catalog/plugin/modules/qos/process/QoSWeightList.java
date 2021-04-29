package br.ufsc.gsigma.catalog.plugin.modules.qos.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.TableViewer;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.qos.process.QoSWeightList.QoSWeight;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.QoSUtil;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;

public class QoSWeightList extends AbstractBeanTableList<QoSWeight> {

	protected static final String QOS_ITEM = "QoS Item";
	protected static final String QOS_ATTRIBUTE = "QoS Attribute";
	protected static final String QOS_WEIGHT = "QoS Weight";

	private GeneralModelAccessor ivGeneralModelAccessor;

	public QoSWeightList(TableViewer tableViewer) {

		super(tableViewer);

		addColumn(QOS_ITEM);
		addColumn(QOS_ATTRIBUTE);
		addColumn(QOS_WEIGHT);

		setColumnValueProvider(new ColumnValueProvider<QoSWeight>() {

			@Override
			public String getColumnValue(QoSWeight q, String columnName) {

				if (QOS_ITEM.equals(columnName))
					return q.qoSItem;
				else if (QOS_ATTRIBUTE.equals(columnName))
					return q.qoSAttribute;
				else if (QOS_WEIGHT.equals(columnName))
					return q.weight.toString();

				return null;
			}
		});

		setBeanPropertyModifier(new BeanPropertyModifier<QoSWeight>() {

			@Override
			public boolean modifyBeanPropertyValue(QoSWeight q, String columnName, Object value) {

				if (QOS_WEIGHT.equals(columnName)) {
					q.weight = new Double((String) value);
					return true;
				}
				return false;
			}
		});
	}

	public void setIvGeneralModelAccessor(GeneralModelAccessor ivGeneralModelAccessor) {
		this.ivGeneralModelAccessor = ivGeneralModelAccessor;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<QoSWeight> loadItems() {

		if (ivGeneralModelAccessor == null)
			return Collections.EMPTY_LIST;

		Map<String, Double> qoSWeights = getQoSWeights();

		List<QoSWeight> list = new ArrayList<QoSWeight>(qoSWeights.size());

		for (Entry<String, Double> e : qoSWeights.entrySet()) {
			String[] s = e.getKey().split("\\.");
			list.add(new QoSWeight(s[0], s[1], e.getValue()));
		}

		Collections.sort(list, new Comparator<QoSWeight>() {
			@Override
			public int compare(QoSWeight w1, QoSWeight w2) {
				String k1 = w1.qoSItem + "." + w1.qoSAttribute;
				String k2 = w2.qoSItem + "." + w2.qoSAttribute;
				return k1.compareTo(k2);
			}
		});
		return list;
	}

	protected Map<String, Double> getQoSWeights() {
		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());

		Map<String, Double> qoSWeights = processInformationExtension.getQoSWeights();

		if (qoSWeights.isEmpty()) {
			for (QoSAttribute qoSAtt : QoSUtil.getEnabledQoSAttributes().values()) {
				qoSWeights.put(qoSAtt.getKey(), 1D);
			}
		}
		return qoSWeights;
	}

	@Override
	protected void saveItems(List<QoSWeight> itens) {

		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());

		for (QoSWeight q : itens) {
			processInformationExtension.getQoSWeights().put(q.qoSItem + "." + q.qoSAttribute, q.weight);
		}

		processInformationExtension.write(ivGeneralModelAccessor);
	}

	class QoSWeight {

		private String qoSItem;

		private String qoSAttribute;

		private Double weight = 1D;

		public QoSWeight(String qoSItem, String qoSAttribute, Double weight) {
			this.qoSItem = qoSItem;
			this.qoSAttribute = qoSAttribute;
			this.weight = weight;
		}
	}
}
