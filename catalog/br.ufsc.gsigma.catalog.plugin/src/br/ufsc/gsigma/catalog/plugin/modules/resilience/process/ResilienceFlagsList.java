package br.ufsc.gsigma.catalog.plugin.modules.resilience.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.TableViewer;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.resilience.process.ResilienceFlagsList.ResilienceFlag;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;

public class ResilienceFlagsList extends AbstractBeanTableList<ResilienceFlag> {

	protected static final String FLAG_NAME = "Name";
	protected static final String FLAG_VALUE = "Value";

	public ResilienceFlagsList(TableViewer tableViewer, GeneralModelAccessor ivGeneralModelAccessor) {

		super(tableViewer, ivGeneralModelAccessor, new String[] { FLAG_NAME, FLAG_VALUE }, //
				new ColumnValueProvider<ResilienceFlag>() {
					@Override
					public String getColumnValue(ResilienceFlag q, String columnName) {

						if (FLAG_NAME.equals(columnName))
							return q.name;
						else if (FLAG_VALUE.equals(columnName))
							return String.valueOf(q.value);

						return null;
					}
				});

		setBeanPropertyModifier(new BeanPropertyModifier<ResilienceFlag>() {

			@Override
			public boolean modifyBeanPropertyValue(ResilienceFlag q, String columnName, Object value) {

				if (FLAG_VALUE.equals(columnName)) {
					q.value = value != null ? "1".equals(String.valueOf(value)) : null;
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
	protected List<ResilienceFlag> loadItems() {

		if (ivGeneralModelAccessor == null)
			return Collections.EMPTY_LIST;

		Map<String, Boolean> flags = getResilienceFlags();

		List<ResilienceFlag> list = new ArrayList<ResilienceFlag>(flags.size());

		for (Entry<String, Boolean> e : flags.entrySet()) {
			list.add(new ResilienceFlag(e.getKey(), e.getValue()));
		}

		Collections.sort(list, new Comparator<ResilienceFlag>() {
			@Override
			public int compare(ResilienceFlag w1, ResilienceFlag w2) {
				return w1.name.compareTo(w2.name);
			}
		});

		return list;
	}

	protected Map<String, Boolean> getResilienceFlags() {
		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());
		return processInformationExtension.getResilienceConfiguration().getFlags();
	}

	@Override
	protected void saveItems(List<ResilienceFlag> itens) {

		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());

		Collections.sort(itens, new Comparator<ResilienceFlag>() {
			@Override
			public int compare(ResilienceFlag o1, ResilienceFlag o2) {
				return o1.name.compareTo(o2.name);
			}
		});

		for (ResilienceFlag q : itens) {
			processInformationExtension.getResilienceConfiguration().getFlags().put(q.name, q.value);
		}

		processInformationExtension.write(ivGeneralModelAccessor);
	}

	class ResilienceFlag {

		private String name;

		private boolean value;

		ResilienceFlag(String name, boolean value) {
			super();
			this.name = name;
			this.value = value;
		}

	}
}
