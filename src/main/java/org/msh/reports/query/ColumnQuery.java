package org.msh.reports.query;

import org.msh.reports.datatable.impl.ColumnImpl;
import org.msh.reports.datatable.impl.DataTableImpl;

/**
 * Represent a column of the {@link DataTableQuery}
 * 
 * @author Ricardo Memoria
 *
 */
public class ColumnQuery extends ColumnImpl {
	public ColumnQuery(DataTableImpl dataTable) {
		super(dataTable);
	}

	private String fieldName;

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + getIndex() + ") " + fieldName;
	}
}