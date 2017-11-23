package org.msh.utils.reportgen;

/**
 * Define a variable to be used to group information in a report
 * @author Ricardo Memoria
 *
 */
public interface Variable {

	/**
	 * Return the display title of the variable
	 * @return
	 */
	String getTitle();
	
	/**
	 * Create the fields that will be included in the SQL select statement
	 * @param reportQuery
	 * @return array with list of fields to be included in SQL select statement
	 */
	String[] createSQLSelectFields(ReportQuery reportQuery);
	
	/**
	 * Create the fields that will be included in the SQL group by statement
	 * @param reportQuery
	 * @return
	 */
	String[] createSQLGroupByFields(ReportQuery reportQuery);
	
	/**
	 * Translate the values returned from database to another value used in the report.<p>
	 * This method is called if the variable included more than one field in the select clause of the query
	 * @param value[] array with values returned from the database
	 * @return new value, representing the variable value
	 */
	Object translateValues(Object value[]);
	
	/**
	 * Translate a value returned from the database to another value used in the report.<p>
	 * This method is called if the variable included one single field in the select clause of the query
	 * @param value
	 * @return
	 */
	Object translateSingleValue(Object value);
	
	
	/**
	 * Return the display text of a value supported by the variable
	 * @param value
	 * @return
	 */
	String getValueDisplayText(Object value);
	
	/**
	 * Compare two values for sorting purposes
	 * @param val1
	 * @param val2
	 * @return
	 */
	Integer compareValues(Object val1, Object val2);
	
	/**
	 * Toggle the grouping mode. If grouping is not enabled for the variable, this method shall return false
	 * @return
	 */
	boolean setGrouping(boolean value);
	
	/**
	 * Return grouping data, if existing. Grouping data is a way of displaying the information grouped by another data (example: Year and month)
	 * @param val1
	 * @return
	 */
	Object getGroupData(Object val1);
}
