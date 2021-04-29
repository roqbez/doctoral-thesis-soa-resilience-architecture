package br.ufsc.gsigma.catalog.plugin.util.ui;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;

public abstract class AbstractBeanTableList<T> {

	private TableViewer tableViewer;

	private ColumnValueProvider<T> columnValueProvider;

	private IColorProvider rowColorProvider;

	private List<T> itens;

	protected GeneralModelAccessor ivGeneralModelAccessor;

	public AbstractBeanTableList(TableViewer tableViewer) {
		this(tableViewer, null, null, null);
	}

	public AbstractBeanTableList(TableViewer tableViewer, GeneralModelAccessor ivGeneralModelAccessor, String[] columns, ColumnValueProvider<T> columnValueProvider) {
		this.tableViewer = tableViewer;
		this.ivGeneralModelAccessor = ivGeneralModelAccessor;
		this.columnValueProvider = columnValueProvider;

		if (columns != null) {
			for (String col : columns) {
				addColumn(col);
			}
		}

		tableViewer.setContentProvider(new BeanTableContentProvider());
		tableViewer.setLabelProvider(new BeanTableLabelProvider());
		tableViewer.setInput(this);
	}

	protected abstract List<T> loadItems();

	protected void saveItems(List<T> itens) {
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public void refresh() {
		this.tableViewer.refresh();
		for (TableColumn c : this.tableViewer.getTable().getColumns()) {
			c.pack();
		}
	}

	public ColumnValueProvider<T> getColumnValueProvider() {
		return columnValueProvider;
	}

	public void setColumnValueProvider(ColumnValueProvider<T> columnValueProvider) {
		this.columnValueProvider = columnValueProvider;
	}

	public IColorProvider getRowColorProvider() {
		return rowColorProvider;
	}

	public void setRowColorProvider(IColorProvider rowColorProvider) {
		this.rowColorProvider = rowColorProvider;
	}

	public void setBeanPropertyModifier(BeanPropertyModifier<T> beanPropertyModifier) {
		this.tableViewer.setCellModifier(new BeanTableCellModifier(beanPropertyModifier));
	}

	public interface ColumnValueProvider<T> {
		public String getColumnValue(T object, String columnName);
	}

	public interface BeanPropertyModifier<T> {
		public boolean modifyBeanPropertyValue(T object, String columnName, Object value);
	}

	public void addColumn(String name) {
		String[] columns = ArrayUtils.add((String[]) tableViewer.getColumnProperties(), name);
		tableViewer.setColumnProperties(columns);
	}

	public List<T> getItens() {
		itens = loadItems();
		return itens;
	}

	public void add(T obj) {
		tableViewer.add(obj);
		itens = loadItems();
		itens.add(obj);
		saveItems(itens);
	}

	public void remove(T obj) {
		tableViewer.remove(obj);
		itens = loadItems();
		itens.remove(obj);
		saveItems(itens);
	}

	public void clear() {

		itens = loadItems();

		tableViewer.getTable().removeAll();

		itens.clear();
		saveItems(itens);
	}

	public void set(List<T> list) {

		itens = loadItems();

		tableViewer.getTable().removeAll();

		itens.clear();
		itens = new LinkedList<T>(list);

		for (T obj : list)
			tableViewer.add(obj);

		saveItems(itens);
	}

	private class BeanTableContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object paramObject) {
			itens = loadItems();
			return itens.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer paramViewer, Object paramObject1, Object paramObject2) {
		}

	}

	private class BeanTableLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

		@SuppressWarnings("unchecked")
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (tableViewer.getColumnProperties() != null) {
				String columnName = (String) tableViewer.getColumnProperties()[columnIndex];
				return columnValueProvider.getColumnValue((T) element, columnName);
			} else {
				return null;
			}
		}

		@Override
		public Image getColumnImage(Object paramObject, int paramInt) {
			return null;
		}

		@Override
		public Color getForeground(Object paramObject) {
			if (rowColorProvider != null)
				return rowColorProvider.getForeground(paramObject);
			else
				return null;
		}

		@Override
		public Color getBackground(Object paramObject) {
			if (rowColorProvider != null)
				return rowColorProvider.getBackground(paramObject);
			else
				return null;
		}
	}

	private class BeanTableCellModifier implements ICellModifier {

		BeanPropertyModifier<T> beanPropertyModifier;

		public BeanTableCellModifier(BeanPropertyModifier<T> beanPropertyModifier) {
			this.beanPropertyModifier = beanPropertyModifier;
		}

		@Override
		public boolean canModify(Object element, String property) {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object getValue(Object element, String property) {
			return columnValueProvider.getColumnValue((T) element, property);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void modify(Object element, String columnName, Object value) {

			T object = (T) ((TableItem) element).getData();

			boolean changed = beanPropertyModifier.modifyBeanPropertyValue(object, columnName, value);

			if (changed) {
				tableViewer.update(object, null);
				saveItems(itens);
			}
		}

	}
}
