package org.msh.reports.tableoperations;

import java.util.List;

import org.msh.reports.datatable.DataTable;
import org.msh.reports.datatable.Row;
import org.msh.reports.variables.Variable;

/**
 * Converts a data table loaded from a query to another data table with its
 * variables values converted to the corresponding variable keys.
 * <p/>
 * This class is used to support indicator generation. It considers that the last
 * column of the source table contains a numeric value that will be used
 * to generate the values of the indicator
 * 
 * @author Ricardo Memoria
 *
 */
public class KeyConverter {

	// list of columns where keys are stored
	private int[] colindexes;
	
	// the result table generated by this class
	private DataTable resultTable;
	
	/**
	 * Convert the values of a database table to a key-valued table based on its variables.
	 * The method considers that the last column of the <code>tblsource</code> contains 
	 * a numeric value that will be used to generate the values of the indicator, and
	 * the other columns contains values necessary to generate the key of the variables
	 *  
	 * @param tblsource the table containing data returned from the database query
	 * @param variables the list of variables that generated the database table
	 * @param varcols the list of column (0-based) indexes pointing to the position
	 * of the tblsource where the variable values are stored
	 * @return instance of the {@link DataTable} containing the new table converted to keys
	 */
	public DataTable execute(DataTable tblsource, List<Variable> variables, List<int[]> varcols) {
		// calculate the number of columns of the new table
		int varcolcount = 0;
		for (Variable var: variables) {
			// if variable is grouped, so it'll need two columns
			varcolcount += var.isGrouped()? 2: 1;
		}

		// create the new table
		// add 1 to include the indicator value
		resultTable = new DataTable(varcolcount + 1, 0);

		// this is the list of column with keys in the new table
		colindexes = new int[varcolcount];
		for (int i = 0; i < varcolcount; i++)
			colindexes[i] = i;
		
		// loop in each row of the table
		for (Row row: tblsource.getRows()) {
			int index = 0;
			boolean isMultikey = false;

			// create temporary row that will store keys
			Object[] keys = new Object[varcolcount];
			int rowindex = 0;

			for (Variable var: variables) {
				// get the values of the columns related to the variable
				int[] cols = varcols.get(index);
				Object[] vals = row.getValues(cols);

				// support for key creation
				if (var.isGrouped()) {
					Object groupkey;
					if (vals.length == 1)
						 groupkey = var.createGroupKey(vals[0]);
					else groupkey = var.createGroupKey(vals);
					keys[rowindex] = groupkey;
					rowindex++;
				}

				// create the key
				Object key;
				if (vals.length == 1)
					 key = var.createKey(vals[0]);
				else key = var.createKey(vals);

				// check if there is a multiple key
				if ((!isMultikey) && (key != null))
					isMultikey = key.getClass().isArray();
				keys[rowindex] = key;
				rowindex++;
				
				index++;
			}

			// get the last column as being the indicator
			Long val = (Long)row.getValue(row.getColumCount() - 1);

			if (isMultikey)
				 handleMultikeysRow(keys, val);
			else updateRow(keys, val);
		}

		return resultTable;
	}

	/**
	 * @param keys
	 * @param val
	 */
	private void handleMultikeysRow(Object[] keys, Long val) {
		Object[] temprow = new Object[keys.length];
		readRow(0, keys, temprow, val);
	}

	/**
	 * @param index
	 * @param keys
	 * @param newrow
	 * @param val
	 */
	private void readRow(int index, Object[] keys, Object[] newrow, Long val) {
		Object key = keys[index];

		// key store multiple keys ?
		if (key.getClass().isArray()) {
			Object[] multkeys = (Object[])key;
			for (Object k: multkeys) {
				newrow[index] = k;
				if (index >= newrow.length - 1)
					updateRow(newrow, val);
				else readRow(index + 1, keys, newrow, val);
			}
		}
		else {
			newrow[index] = key;
			if (index >= newrow.length - 1)
				updateRow(newrow, val);
			else readRow(index + 1, keys, newrow, val);
		}
	}


	/**
	 * Update the value of the row based on the keys and indicator value. If 
	 * the row in the table already exists, the indicator cell is increased
	 * by the value
	 * 
	 * @param keys
	 * @param val
	 */
	private void updateRow(Object[] keys, Long val) {
		// position in the table of indicator value
		int valindex = resultTable.getColumnCount() - 1;

		// search for row 
		Row row = resultTable.findRow(colindexes, keys, 0);
		if (row == null) {
			row = resultTable.insertRow();
			for (int i = 0; i < keys.length; i++)
				row.setValue(i, keys[i]);
		}
		else val += (Long)row.getValue(valindex);
		
		row.setValue(valindex, val);
	}

}