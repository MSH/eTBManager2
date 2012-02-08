package org.msh.utils.reportgen;

import java.util.List;

/**
 * Define a filter to be applied to a report
 * @author Ricardo Memoria
 *
 */
public interface Filter {

	/**
	 * Displayed title of the filter
	 * @return
	 */
	String getTitle();
	
	/**
	 * Return the id of the filter. If it returns null, the filter has no id to be identified
	 * @return
	 */
	String getId();
	
	/**
	 * Generate SQL Condition for the {@link ReportQuery}
	 * @param reportQuery
	 * @return
	 */
	String createSQLCondition(Object value, ReportQuery reportQuery);
	

	/**
	 * Return the type of control to be used to get information from user
	 * @return
	 */
	FilterInputType getInputType();
	
	/**
	 * Return the list of options to be selected by the user. This method depends on the return of getInputType() method.
	 * @return
	 */
	List<FilterOption> getOptions();
}
