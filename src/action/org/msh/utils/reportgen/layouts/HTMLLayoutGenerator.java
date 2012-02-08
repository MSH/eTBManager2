package org.msh.utils.reportgen.layouts;

import org.msh.utils.reportgen.ReportQuery;

/**
 * Generate HTML representation of a report query
 * @author Ricardo Memoria
 *
 */
public interface HTMLLayoutGenerator {

	/**
	 * Add queries to a layout generator. A layout generator may support several queries (complex layouts)
	 * or just one single query
	 * @param reportQuery
	 */
	void addQuery(ReportQuery reportQuery);

	/**
	 * Generate HTML representation of the report
	 * @return
	 */
	String generateHtml();
	
	/**
	 * Clear all data in the layout, including all queries 
	 */
	void clear();
}
