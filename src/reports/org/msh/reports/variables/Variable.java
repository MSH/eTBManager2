package org.msh.reports.variables;

import org.msh.reports.query.SQLDefs;

/**
 * Interface that must be implemented by classes that want to expose itself as a 
 * report variable. A report variable is displayed in the report resulting table.
 * 
 * @author Ricardo Memoria
 *
 */
public interface Variable {

	/**
	 * Internal name of the variable. Usually it's the field name to be returned
	 * @return
	 */
	String getName();
	
	/**
	 * Display name of the variable
	 * @return
	 */
	String getLabel();

	/**
	 * Include the fields that must be returned
	 * @param def
	 */
	void prepareVariableQuery(SQLDefs def);

	/**
	 * Convert a value returned from the database to another value. For example, a number that must be displayed as a string.
	 * if more than 1 value is expected from the argument, the value represents an array, so a cast to Object[] must be done.
	 * @param values
	 * @return
	 */
	Object createKey(Object values);

	
	/**
	 * From the key created by the method <code>createKey()</code>, return the text to be displayed in the table cell
	 * @param key is the object key created by the method <code>createKey()</code>
	 * @return the text to be displayed for the key
	 */
	String getDisplayText(Object key);

	/**
	 * Compare two values of the variable. It follows the implementation of the {@link Comparable} interface. If more than 1 field is specified
	 * to be returned from the data base, the parameters objects are actually an array of objects, so a proper cast must be done to the right value.
	 * <p>
	 * If just one field is specified, so val1 and val2 will point to this value
	 * @param val1
	 * @param val2
	 * @return
	 */
	int compareValues(Object val1, Object val2);

	/**
	 * Return the possible list of values of the variable. This list is used to initialize the data table
	 * @return list of object keys that will be used to create a fix list of values in the indicator
	 */
	Object[] getDomain();
	
	/**
	 * If the variable must display information in a two-level structure, so this method must return true
	 * @return true if variable is grouped
	 */
	boolean isGrouped();
	
	/**
	 * If the variable is grouped in two levels (by the method <code>isGrouped()</code>, this method is called
	 * for each value to be converted to a group key 
	 * @param values
	 * @return Object instance representing the group key
	 */
	Object createGroupKey(Object values);
	
	/**
	 * Convert a group object key to a text to be displayed to the user
	 * @param key is the group key created before by <code>createGroupKey()</code> method
	 * @return a text ready for displaying representing the key
	 */
	String getGroupDisplayText(Object key);
}
