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
import br.ufsc.gsigma.catalog.plugin.modules.resilience.process.ResilienceParamsList.ResilienceParam;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ui.AbstractBeanTableList;

public class ResilienceParamsList extends AbstractBeanTableList<ResilienceParam> {

	protected static final String PARAM_NAME = "Name";
	protected static final String PARAM_VALUE = "Value";

	public ResilienceParamsList(TableViewer tableViewer, GeneralModelAccessor ivGeneralModelAccessor) {

		super(tableViewer, ivGeneralModelAccessor, new String[] { PARAM_NAME, PARAM_VALUE }, //
				new ColumnValueProvider<ResilienceParam>() {
					@Override
					public String getColumnValue(ResilienceParam q, String columnName) {

						if (PARAM_NAME.equals(columnName))
							return q.name;
						else if (PARAM_VALUE.equals(columnName))
							return q.value;

						return null;
					}
				});

		setBeanPropertyModifier(new BeanPropertyModifier<ResilienceParam>() {

			@Override
			public boolean modifyBeanPropertyValue(ResilienceParam q, String columnName, Object value) {

				if (PARAM_VALUE.equals(columnName)) {
					q.value = value != null ? String.valueOf(value) : null;
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
	protected List<ResilienceParam> loadItems() {

		if (ivGeneralModelAccessor == null)
			return Collections.EMPTY_LIST;

		Map<String, String> params = getResilienceParams();

		List<ResilienceParam> list = new ArrayList<ResilienceParam>(params.size());

		for (Entry<String, String> e : params.entrySet()) {
			list.add(new ResilienceParam(e.getKey(), e.getValue()));
		}

		Collections.sort(list, new Comparator<ResilienceParam>() {
			@Override
			public int compare(ResilienceParam w1, ResilienceParam w2) {
				return w1.name.compareTo(w2.name);
			}
		});

		return list;
	}

	protected Map<String, String> getResilienceParams() {
		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());
		return processInformationExtension.getResilienceConfiguration().getParams();
	}

	@Override
	protected void saveItems(List<ResilienceParam> itens) {

		BMProcessInformationExtension processInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());

		Collections.sort(itens, new Comparator<ResilienceParam>() {
			@Override
			public int compare(ResilienceParam o1, ResilienceParam o2) {
				return o1.name.compareTo(o2.name);
			}
		});

		for (ResilienceParam q : itens) {
			processInformationExtension.getResilienceConfiguration().getParams().put(q.name, q.value);
		}

		processInformationExtension.write(ivGeneralModelAccessor);
	}

	class ResilienceParam {

		private String name;

		private String value;

		ResilienceParam(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

	}
}
