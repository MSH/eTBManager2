package org.msh.utils.reportgen.data;

import org.jboss.seam.international.Messages;
import org.msh.utils.reportgen.ReportData;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataTableTransform {

	private ReportQuery reportQuery;
	private List<TableItem> columns;
	private List<Row> rows;
	private List<TableItem> groupColumns;
	private List<TableItem> groupRows;
	private Row rowTotal;
	
	private Variable columnVariable;
	private Variable rowVariable;
	private Variable groupColumnVariable;
	private Variable groupRowVariable;
	
	private boolean columnGrouped;
	private boolean rowGrouped;
	

	public DataTableTransform(ReportQuery reportQuery) {
		this.reportQuery = reportQuery;
	}

	/**
	 * 
	 */
	public void execute() {
		columns = new ArrayList<TableItem>();
		rows = new ArrayList<Row>();
		groupColumns = new ArrayList<TableItem>();
		groupRows = new ArrayList<TableItem>();
		rowTotal = new Row();
		rowTotal.setTitle(Messages.instance().get("global.total"));

		List<ReportData> values = reportQuery.getResultList();

		columnGrouped = groupColumnVariable != null;
		if (!columnGrouped)
			columnGrouped = columnVariable.setGrouping(true);

		rowGrouped = groupRowVariable != null;
		if (!rowGrouped)
			rowGrouped = rowVariable.setGrouping(true);
		
		groupData(values);
	}


	/**
	 * Group value data creating the columns and rows of the table
	 * @param values
	 */
	protected void groupData(List<ReportData> values) {
		for (ReportData data: values) {
			Object coldata = data.getValue(columnVariable);
			Object rowdata = data.getValue(rowVariable);
			
			Object colgrpdata;
			if (columnGrouped) {
				if (groupColumnVariable == null)
					 colgrpdata = columnVariable.getGroupData(coldata);
				else colgrpdata = data.getValue(groupColumnVariable);
			}
			else colgrpdata = null;
			
			Object rowgrpdata;
			if (rowGrouped) {
				if (groupRowVariable == null)
					 rowgrpdata = rowVariable.getGroupData(rowdata);
				else rowgrpdata = data.getValue(groupRowVariable);
			}
			else rowgrpdata = null;
			
			TableItem grp;
			if (columnGrouped)
				 grp = findOrCreateGroupColumn(colgrpdata);
			else grp = null;
			
			TableItem col = findOrCreateColumn(grp, coldata);

			if (rowGrouped)
				 grp = findOrCreateGroupRow(rowgrpdata);
			else grp = null;
			Row row = findOrCreateRow(grp, rowdata);
			
			long val = data.getTotal();
			row.addValue(col, val);
			rowTotal.addValue(col, val);
		}

		sortData();
	}

	
	/**
	 * Sort the data in the table
	 */
	protected void sortData() {
		Collections.sort(groupColumns);
		Collections.sort(columns);
		Collections.sort(groupRows);
		Collections.sort(rows);
	}


	/**
	 * Search for a group column by its data
	 * @param data
	 * @return
	 */
	protected TableItem findOrCreateGroupColumn(Object data) {
		for (TableItem grp: groupColumns) {
			if (grp.isSameData(data))
				return grp;
		}
		
		TableItem grp = new TableItem();
		grp.setData(data);
		if (groupColumnVariable != null)
			 grp.setVariable( groupColumnVariable );
		else grp.setVariable( columnVariable );
		groupColumns.add(grp);
		return grp;
	}


	/**
	 * Search for a group column by its data
	 * @param data
	 * @return
	 */
	protected TableItem findOrCreateGroupRow(Object data) {
		for (TableItem grp: groupRows) {
			if (grp.isSameData(data))
				return grp;
		}
		
		TableItem grp = new Row();
		grp.setData(data);
		if (groupRowVariable != null)
			 grp.setVariable( groupRowVariable );
		else grp.setVariable( rowVariable );
		groupRows.add(grp);
		return grp;
	}


	/**
	 * Search for a column by its data. If column doesn't exists, create a new one as the last column and return it
	 * @param data
	 * @return
	 */
	protected TableItem findOrCreateColumn(TableItem groupColumn, Object data) {
		if (columns == null) 
			columns = new ArrayList<TableItem>();

		for (TableItem col: columns) {
			if (col.equals(groupColumn, data))
				return col;
		}

		TableItem col = new TableItem(groupColumn);
		col.setData(data);
		col.setVariable( columnVariable );
		columns.add(col);
		return col;
	}

	
	/**
	 * Find a row by its data. If row doesn't exists, create a new one at the last row and return it
	 * @param data
	 * @return
	 */
	protected Row findOrCreateRow(TableItem groupRow, Object data) {
		if (rows == null) 
			rows = new ArrayList<Row>();

		for (Row row: rows) {
			if (row.equals(groupRow, data))
				return row;
		}

		Row row = new Row(groupRow);
		row.setData(data);
		row.setVariable( rowVariable );
		rows.add(row);
		return row;
	}


	/**
	 * @return the reportQuery
	 */
	public ReportQuery getReportQuery() {
		return reportQuery;
	}

	/**
	 * @return the columns
	 */
	public List<TableItem> getColumns() {
		return columns;
	}

	/**
	 * @return the rows
	 */
	public List<Row> getRows() {
		return rows;
	}

	/**
	 * @return the groupColumns
	 */
	public List<TableItem> getGroupColumns() {
		return groupColumns;
	}

	/**
	 * @return the groupRows
	 */
	public List<TableItem> getGroupRows() {
		return groupRows;
	}

	/**
	 * @return the columnVariable
	 */
	public Variable getColumnVariable() {
		return columnVariable;
	}

	/**
	 * @param columnVariable the columnVariable to set
	 */
	public void setColumnVariable(Variable columnVariable) {
		this.columnVariable = columnVariable;
	}

	/**
	 * @return the rowVariable
	 */
	public Variable getRowVariable() {
		return rowVariable;
	}

	/**
	 * @param rowVariable the rowVariable to set
	 */
	public void setRowVariable(Variable rowVariable) {
		this.rowVariable = rowVariable;
	}

	/**
	 * @return the groupColumnVariable
	 */
	public Variable getGroupColumnVariable() {
		return groupColumnVariable;
	}

	/**
	 * @param groupColumnVariable the groupColumnVariable to set
	 */
	public void setGroupColumnVariable(Variable groupColumnVariable) {
		this.groupColumnVariable = groupColumnVariable;
	}

	/**
	 * @return the groupRowVariable
	 */
	public Variable getGroupRowVariable() {
		return groupRowVariable;
	}

	/**
	 * @param groupRowVariable the groupRowVariable to set
	 */
	public void setGroupRowVariable(Variable groupRowVariable) {
		this.groupRowVariable = groupRowVariable;
	}

	/**
	 * @return the columnGrouped
	 */
	public boolean isColumnGrouped() {
		return columnGrouped;
	}

	/**
	 * @return the rowGrouped
	 */
	public boolean isRowGrouped() {
		return rowGrouped;
	}

	/**
	 * @return the rowTotal
	 */
	public Row getRowTotal() {
		return rowTotal;
	}
}
