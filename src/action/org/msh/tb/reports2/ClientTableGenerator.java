package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.List;

import org.msh.reports.IndicatorReport;
import org.msh.reports.datatable.DataTable;
import org.msh.reports.datatable.Row;
import org.msh.reports.variables.Variable;
import org.msh.tb.client.shared.model.CTable;
import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;

/**
 * Generate the table to be sent to the client side from 
 * an instance of the {@link DataTable} and the variables
 * used to generate the indicator
 * 
 * @author Ricardo Memoria
 *
 */
public class ClientTableGenerator {

	private int colHeaderSize;
	private DataTable tbl;
	
	/**
	 * Generate an instance of the {@link CTable} class containing information about an 
	 * indicator report to be sent back to the client side
	 *  
	 * @param tbl instance of the {@link DataTable} containing the data of the report
	 * @return instance of the {@link CTable} containing the indicator table to be sent to the client
	 */
	public CTable execute(IndicatorReport rep) {
		// create the client table to be returned to the client
		CTable ctable = new CTable();
		
		// execute the report
		tbl = rep.getResult();

		// convert the data to a client data
		ArrayList<CTableRow> rows = new ArrayList<CTableRow>();
		ArrayList<CTableColumn> cols = new ArrayList<CTableColumn>();

		colHeaderSize = rep.getColumnHeaderSize();
		int rowheadersize = rep.getRowHeaderSize();

		// create the columns
		int cindex = 1;
		while (cindex < tbl.getColumnCount()) {
			CTableColumn col = new CTableColumn();
			String s = (String)tbl.getValue(cindex, 0);
			col.setTitle(s);
			int end = calcSpan(tbl, 0, cindex) + cindex;
			while (cindex < end) 
				cindex += addChildColumn(col, cindex, 1);
			cols.add(col);
		}
		ctable.setColumns(cols);

		// create the list of variables used to mount the columns
		int[] varindex = new int[colHeaderSize];
		int index = 0;
		int i = 0;
		for (Variable var: rep.getColumnVariables()) {
			if (var.isGrouped()) {
				varindex[i++] = index;
			}
			varindex[i++] = index;
			index++;
		}
		ctable.setColVarIndex(varindex);
		
/*
		for (int r = 0; r < colheadersize; r++) {
			int c = rowheadersize;
			CTableRowColumn rc = new CTableRowColumn();
			
			ArrayList<CTableColumn> lst = new ArrayList<CTableColumn>();
			int index = 0;
			while (c < tbl.getColumnCount()) {
				CTableColumn col = new CTableColumn();
				String title = (String)tbl.getValue(c, r);
				int span = calcSpan(tbl, r, c);
				col.setTitle(title);
				if (span > 1)
					col.setSpan(span);
				if (rowCols.size() > 0) {
					col.setParentColumn( getParentIndex(rowCols.get(rowCols.size() - 1).getColumns(), index));
					index += (col.getSpan() != null? col.getSpan(): 1);
				}
				lst.add(col);
				c += span;
			}
			rc.setColumns(lst);
			rowCols.add(rc);
		}
*/
		// create the rows with title and values
		for (int r = colHeaderSize; r < tbl.getRowCount(); r++) {
			Row row = tbl.getRows().get(r);

			CTableRow crow = new CTableRow();
			String title = (String)row.getValue(0);

			Double[] vals = new Double[tbl.getColumnCount() - rowheadersize];
			for (int c = rowheadersize; c < tbl.getColumnCount(); c++) {
				Long val = (Long)row.getValue(c);
				vals[c - rowheadersize] = val != null? val.doubleValue(): null;
			}
			
			crow.setTitle(title);
			crow.setValues(vals);
			crow.setLevel(row.getLevel());
			crow.setKey(row.getKey() != null? row.getKey().toString(): null);
			rows.add(crow);
		}
		
		ctable.setRows(rows);

		// set the variables used in columns and rows
		updateVariableIndex(ctable, rep.getRowVariables(), rep.getColumnVariables());
		
		return ctable;
	}
	
	/**
	 * Update the variable index that is included in both columns and rows to be sent to the client side 
	 * @param ctable
	 * @param rowVars
	 * @param colVars
	 */
	private void updateVariableIndex(CTable ctable, List<Variable> rowVars, List<Variable> colVars) {
		List<Integer> rowIndexes = new ArrayList<Integer>();
		List<Integer> colIndexes = new ArrayList<Integer>();

		// create indexes for rows
		int index = 0;
		for (Variable v: rowVars) {
			rowIndexes.add(index);
			if (v.isGrouped())
				rowIndexes.add(index);
			index++;
		}
		
		index = 0;
		for (Variable v: colVars) {
			colIndexes.add(index);
			if (v.isGrouped())
				colIndexes.add(index);
			index++;
		}

		// update variable index of the rows
		for (CTableRow row: ctable.getRows()) {
			if (row.getLevel() < rowIndexes.size())
				row.setVarIndex(rowIndexes.get(row.getLevel()));
		}
	}
	
	/**
	 * Return the parent index of the column index
	 * @param cols
	 * @param childIndex
	 * @return
	 */
/*	public Integer getParentIndex(ArrayList<CTableColumn> cols, int childIndex) {
		int index = 0;
		for (int i = 0; i < cols.size(); i++) {
			CTableColumn col = cols.get(i);
			int span = col.getSpan() != null? col.getSpan(): 1;
			if ((childIndex >= index) && (childIndex < index + span - 1))
				return i;
		}
		return null;
	}
*/
	/**
	 * Calculate column span of the given cell
	 * @param cells
	 * @param index
	 * @return
	 */
	protected int calcSpan(DataTable tbl, int r, int c) {
		Object val1 = tbl.getValue(c, r);
		Object par1 = r > 0? tbl.getValue(c, r - 1): null;
		
		int span = 1;
		for (int i = c + 1; i < tbl.getColumnCount(); i++) {
			Object val2 = tbl.getValue(i, r);
			Object par2 = r > 0? tbl.getValue(i, r - 1): null;
			if (eqobj(val1, val2) && eqobj(par1, par2)) {
				span++;
			}
			else break;
		}
		
		return span;
	}
	
	
	protected int addChildColumn(CTableColumn parent, int c, int r) {
		if (r >= colHeaderSize)
			return 1;

		int len = calcSpan(tbl, r, c);
		String s = (String)tbl.getValue(c, r);

		CTableColumn col = new CTableColumn();
		col.setTitle(s);
		ArrayList<CTableColumn> cols = parent.getColumns();
		if (cols == null) {
			cols = new ArrayList<CTableColumn>();
			parent.setColumns(cols);
		}
		cols.add(col);

		int i = 0;
		while (i < len) {
			i += addChildColumn(col, c, r + 1);
		}
		
		return len;
	}
	
	
	/**
	 * Check if two objects are equals
	 * @param val1
	 * @param val2
	 * @return
	 */
	protected boolean eqobj(Object val1, Object val2) {
		return ((val1 == val2) || ((val1 != null) && (val1.equals(val2))));
	}
}
