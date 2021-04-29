package br.ufsc.gsigma.catalog.plugin.util.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public abstract class UIUtil {

	private static final ExecutorService pool = Executors.newCachedThreadPool();

	public static AtomicBoolean tickProgressMonitor(final IProgressMonitor monitor) {

		if (monitor == null)
			return null;

		final AtomicBoolean isDone = new AtomicBoolean(false);

		pool.submit(new Runnable() {
			@Override
			public void run() {
				while (!isDone.get()) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							monitor.worked(1);
						}
					});
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		return isDone;
	}

	public static GridLayout createGridLayout(int numColumns) {
		GridLayout gridLayout = new GridLayout(numColumns, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginRight = 5;
		return gridLayout;
	}

	public static GridData createGridData(int style) {
		GridData gridData = new GridData(style);
		return gridData;
	}

	public static Color getColor(int color) {
		return Display.getCurrent().getSystemColor(color);
	}

	public static Composite createComposite(Composite parent, int numColumns) {
		return createComposite(parent, numColumns, GridData.FILL_HORIZONTAL);
	}

	public static Composite createComposite(Composite parent, int numColumns, int layoutDataStyle) {
		Composite composite = new Composite(parent, 0);
		composite.setBackground(parent.getBackground());
		composite.setLayout(createGridLayout(numColumns));
		composite.setLayoutData(createGridData(layoutDataStyle));
		return composite;
	}

	public static TabFolder createTabFolder(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.BORDER);
		tabFolder.setLayout(new GridLayout(1, false));
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		return tabFolder;
	}

	public static Composite createTabComposite(TabFolder tabFolder, String label) {

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(label);
		Composite tabItemComposite = new Composite(tabFolder, SWT.NONE);
		tabItemComposite.setBackground(tabFolder.getBackground());

		tabItemComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		tabItemComposite.setLayout(new GridLayout(1, false));
		tabItemComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(tabItemComposite);

		return tabItemComposite;
	}

	public static Table createTable(Composite parent) {

		Table table = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);

		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		return table;
	}

	public static void autoSizeColumns(Table table) {

		Map<Integer, Integer> fixedColumns = new HashMap<Integer, Integer>();
		fixedColumns.put(0, 280);

		Map<Integer, Integer> mapWeight = new HashMap<Integer, Integer>();
		mapWeight.put(5, 2);

		autoSizeColumns(table, fixedColumns, mapWeight);
	}

	public static void autoSizeColumns(Table table, Map<Integer, Integer> fixedColumns, Map<Integer, Integer> mapWeight) {

		for (int k = 0; k < table.getColumnCount(); k++) {
			table.getColumn(k).pack();
		}

		int width = table.getClientArea().width + 2 * table.getBorderWidth();

		if (width <= 1)
			return;

		TableColumn[] columns = table.getColumns();

		int numberOfColumns = columns.length;

		int[] widths = new int[numberOfColumns];
		int fixedWidth = 0;
		int numberOfFixedColumns = 0;
		int numberOfWeightColumns = 0;

		int totalWeight = 0;

		for (int i = 0; i < numberOfColumns; i++) {
			if (!fixedColumns.containsKey(i)) {
				Integer weight = mapWeight.get(i);
				weight = weight != null ? weight : 1;
				totalWeight += weight;
			}
		}

		for (int i = 0; i < numberOfColumns; i++) {

			// TableColumn col = (TableColumn) columns[i];

			if (fixedColumns.containsKey(i)) {
				// int pixels = col.getWidth();
				int w = fixedColumns.get(i);
				widths[i] = w;
				fixedWidth += w;
				numberOfFixedColumns++;
			}
		}

		numberOfWeightColumns = numberOfColumns - numberOfFixedColumns;

		if (numberOfWeightColumns > 0) {

			int rest = width - fixedWidth;
			int totalDistributed = 0;

			for (int i = 0; i < numberOfColumns; i++) {

				if (!fixedColumns.containsKey(i)) {

					Integer weight = mapWeight.get(i);
					weight = weight != null ? weight : 1;

					int pixels = totalWeight == 0 ? 0 : weight * rest / totalWeight;
					totalDistributed += pixels;
					widths[i] = pixels;
				}
			}

			int diff = rest - totalDistributed;
			for (int i = 0; diff > 0; i++) {
				if (i == numberOfColumns)
					i = 0;

				if (!fixedColumns.containsKey(i)) {
					++widths[i];
					--diff;
				}
			}
		}

		for (int i = 0; i < numberOfColumns; i++) {
			if (columns[i].getWidth() != widths[i])
				columns[i].setWidth(widths[i]);
		}
	}

}
