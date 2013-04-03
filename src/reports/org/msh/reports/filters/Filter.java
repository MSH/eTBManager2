package org.msh.reports.filters;

import org.msh.reports.query.SQLDefs;

public interface Filter {

	/**
	 * Internal name of the filter. Usually it's the field name to be returned
	 * @return
	 */
	String getName();

	/**
	 * Display name of the filter
	 * @return
	 */
	String getLabel();

	/**
	 * Prepare the query to apply the filters
	 * @param def
	 * @param value the value to be applied to the filter
	 */
	void prepareFilterQuery(SQLDefs def, FilterOperation comp, Object value);

}
