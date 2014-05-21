package org.msh.reports.filters;

import java.util.List;

import org.msh.reports.ReportElement;
import org.msh.reports.query.SQLDefs;

public interface Filter extends ReportElement {

	/**
	 * Prepare the query to apply the filters
	 * @param def
	 * @param value the value to be applied to the filter
	 */
	void prepareFilterQuery(SQLDefs def, FilterOperation comp, Object value);

	/**
	 * Return a String identification of the filter's type
	 * @return filter's type
	 */
	String getFilterType();
	
	/**
	 * Return the options of the filter, or null, if no option is available for this filter
	 * @param param is a parameter recognized by the filter to return specific options
	 * @return instance of the {@link List} interface containing the {@link FilterOperation}
	 */
	List<FilterOption> getFilterOptions(Object param);

	/**
	 * Identifies if the options of the filter will be initialized just on demand
	 * or filled immediately when the filter is created 
	 * @return
	 */
	boolean isFilterLazyInitialized();
	
	/**
	 * Convert the given string to a value acceptable by the filter
	 * @param value is the string containing the filter value
	 * @return is the value converted from string
	 */
	Object filterValueFromString(String value);
}
