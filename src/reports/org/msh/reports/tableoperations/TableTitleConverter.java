package org.msh.reports.tableoperations;

import java.util.List;

import org.msh.reports.datatable.DataTable;
import org.msh.reports.datatable.Row;
import org.msh.reports.variables.Variable;

/**
 * This class replaces all key fields of title cells of an indicator table into texts to be displayed,
 * and also store the keys in the row or column
 * 
 * @author Ricardo Memoria
 *
 */
public class TableTitleConverter {

	public void convert(DataTable tbl, List<Variable> columns, List<Variable> rows, boolean rowgrouped) {
		int ini;
		if (rowgrouped)
			 ini = 1;
		else ini = rows.size();
		
		// convert column titles
		for (int colindex = ini; colindex < tbl.getColumnCount(); colindex++) {
			int rowindex = 0;
			// create the keys to be attached to the column
			Object[] keys = new Object[columns.size()];
			for (Variable var: columns) {
				Object key = tbl.getValue(colindex, rowindex);
				String title = var.getDisplayText(key);
				tbl.setValue(colindex, rowindex, title);
				keys[rowindex] = key;
				rowindex++;
			}
			tbl.getColumns().get(colindex).setKey(keys);
		}

		// convert row titles
		if (rowgrouped) {
			replaceGroupedRowTitles(tbl, rows);
		}
		else {
			ini = columns.size();

			for (int rowindex = ini; rowindex < tbl.getRowCount(); rowindex++) {
				int colindex = 0;

				// create the keys to be attached to the column
				Object[] keys = new Object[rows.size()];
				for (Variable var: rows) {
					Object key = tbl.getValue(colindex, rowindex);
					String title = var.getDisplayText(key);
					tbl.setValue(colindex, rowindex, title);
					keys[colindex] = key;
					colindex++;
				}
				tbl.getColumns().get(colindex).setKey(keys);
			}
		}
	}


	/**
	 * If cube table was generated with grouped rows, i.e, parent and children rows,
	 * this method replaces the row keys by titles
	 * 
	 * @param tbl
	 * @param rows
	 */
	private void replaceGroupedRowTitles(DataTable tbl, List<Variable> rows) {
		int level = 0;
		for (Variable var: rows) {
			// is variable grouped
			if (var.isGrouped()) {
				for (int i = 1; i < tbl.getRowCount(); i++) {
					Row row = tbl.getRows().get(i);
					if (row.getKey() != null) {
						String title = null;
						Object key = row.getKey();
						if (row.getLevel() == level)
							title = var.getGroupDisplayText(key);
						else
						if (row.getLevel() == level + 1)
							title = var.getDisplayText(key);
						
						if (title != null) {
							row.setValue(0, title);
							row.setKey(key);
						}
					}
				}
				level += 2;
			}
			else {
				// variable is not grouped
				for (int i = 1; i < tbl.getRowCount(); i++) {
					Row row = tbl.getRows().get(i);
					if (row.getLevel() == level) {
						Object key = row.getValue(0);
						String title = var.getDisplayText(key);
						row.setValue(0, title);
						row.setKey(key);
					}
				}
				level++;
			}
		}
	}
}
