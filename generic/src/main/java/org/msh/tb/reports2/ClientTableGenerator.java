package org.msh.tb.reports2;

import org.msh.reports.IndicatorReport;
import org.msh.reports.datatable.impl.DataTableImpl;
import org.msh.reports.indicator.DataTableIndicator;
import org.msh.reports.indicator.HeaderRow;
import org.msh.reports.indicator.IndicatorColumn;
import org.msh.reports.indicator.IndicatorRow;
import org.msh.reports.variables.Variable;
import org.msh.tb.client.shared.model.CIndicatorResponse;
import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate the table to be sent to the client side from 
 * an instance of the {@link DataTableImpl} and the variables
 * used to generate the indicator
 * 
 * @author Ricardo Memoria
 *
 */
public class ClientTableGenerator {

//	private int colHeaderSize;
	private DataTableIndicator tbl;
	
	/**
	 * Generate an instance of the {@link org.msh.tb.client.shared.model.CIndicatorResponse} class containing information about an
	 * indicator report to be sent back to the client side
	 *  
	 * @param rep instance of the {@link org.msh.reports.IndicatorReport} containing the data of the report
	 * @return instance of the {@link org.msh.tb.client.shared.model.CIndicatorResponse} containing the indicator table to be sent to the client
	 */
	public CIndicatorResponse execute(IndicatorReport rep) {
		// create the client table to be returned to the client
		CIndicatorResponse ctable = new CIndicatorResponse();

		// execute the report
		tbl = rep.getResult();
		
		if (tbl.getRowCount() == 0)
			return ctable;

		// convert the columns to the client data
		HeaderRow headerRow = tbl.getHeaderRow(0);
		ArrayList<CTableColumn> cols = createColumns(headerRow.getColumns());
		ctable.setColumns(cols);

		// create index of the variables used in the columns
		int[] varcols = new int[tbl.getHeaderRowsCount()]; 
		for (int i = 0; i < tbl.getHeaderRowsCount(); i++) {
			Variable var = tbl.getHeaderRow(i).getVariable();
			varcols[i] = rep.getColumnVariables().indexOf(var);
		}
		ctable.setColVarIndex(varcols);

		// create list of rows and its values
		ArrayList<CTableRow> rows = new ArrayList<CTableRow>();
		for (IndicatorRow row: tbl.getIndicatorRows()) {
			CTableRow cr = new CTableRow();
			String key = row.getKey() != null? row.getKey().toString(): null;
			cr.setKey(key);
			cr.setLevel(row.getLevel());
			cr.setTitle(row.getTitle());
			int index = rep.getRowVariables().indexOf(row.getVariable());
			cr.setVarIndex(index);

			Double[] values = new Double[tbl.getColumnCount()];
			for (int i = 0; i < tbl.getColumnCount(); i++) {
				Double val = (Double)row.getValue(i);
				if ((val != null) && (val != 0.0D)) {
					values[i] = val;
				}
			}
			cr.setValues(values);
			rows.add(cr);
		}
		ctable.setRows(rows);

		// check if total is available for the rows
		boolean total = true;
		for (Variable var: rep.getRowVariables())
			if (!var.isTotalEnabled()) {
				total = false;
				break;
			}
		ctable.setRowTotalAvailable(total);

		// check if total is available for the columns
		total = true;
		for (Variable var: rep.getColumnVariables())
			if (!var.isTotalEnabled()) {
				total = false;
				break;
			}
		ctable.setColTotalAvailable(total);

		return ctable;
	}
	
	/**
	 * Create the client columns from an {@link IndicatorColumn} instance and
	 * all its children
	 * @param col instance of {@link IndicatorColumn} that will provide the children columns
	 * @return
	 */
	private ArrayList<CTableColumn> createColumns(List<IndicatorColumn> cols) {
		ArrayList<CTableColumn> lst = new ArrayList<CTableColumn>();
		for (IndicatorColumn col: cols) {
			CTableColumn cc = new CTableColumn();
			String key = col.getKey() != null? col.getKey().toString() : null;
			cc.setKey(key);
			cc.setTitle(col.getTitle());
			if (!col.isEndPointColumn())
				cc.setColumns(createColumns(col.getColumns()));
			lst.add(cc);
		}
		return lst;
	}

	/**
	 * Update the variable index that is included in both columns and rows to be sent to the client side 
	 * @param ctable
	 * @param rowVars
	 * @param colVars
	 */
/*	private void updateVariableIndex(CTable ctable, List<Variable> rowVars, List<Variable> colVars) {
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
*/	
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
/*	protected int calcSpan(DataTableIndicator tbl, int r, int c) {
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
*/	
	
/*	protected int addChildColumn(CTableColumn parent, int c, int r) {
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
*/	
	
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
