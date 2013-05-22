package org.msh.reports.query;

import java.util.List;

import org.msh.reports.datatable.DataTable;

/**
 * Standard implementation of a data table that contains
 * data from a query
 * @author Ricardo Memoria
 *
 */
public interface DataTableQuery extends DataTable {

	/**
	 * Return the field name of a column
	 * @param index
	 * @return
	 */
	public String getColumnFieldName(int index);

	/**
	 * Return the columns of the data table query
	 * @return
	 */
	List<ColumnQuery> getQueryColumns();
	
	/**
	 * Return the rows of the data table query
	 * @return
	 */
	List<RowQuery> getQueryRows();

}
