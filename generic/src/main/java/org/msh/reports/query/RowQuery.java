package org.msh.reports.query;

import org.msh.reports.datatable.impl.DataTableImpl;
import org.msh.reports.datatable.impl.RowImpl;

public class RowQuery extends RowImpl {

	public RowQuery(DataTableImpl dataTable) {
		super(dataTable);
	}

	/**
	 * @param fieldName
	 * @return
	 */
	public Object getValue(String fieldName) {
		for (ColumnQuery col: ((DataTableQuery)getDataTable()).getQueryColumns())
			if (col.getFieldName().equals(fieldName))
				return getValue(getIndex());
		throw new IllegalArgumentException("Field name not found: " + fieldName);
	}
}
