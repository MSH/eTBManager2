package org.msh.reports.query;

import org.msh.reports.filters.Filter;
import org.msh.reports.variables.Variable;


/**
 * Generic SQL definitions used by report instances of {@link Filter} and {@link Variable} to 
 * construct the SQL query to be sent to the database
 * @author Ricardo Memoria
 *
 */
public interface SQLDefs {

	/**
	 * Return the name of the master table of the query
	 * @return
	 */
	TableJoin getMasterTable();
	
	/**
	 * Add a table join to the query
	 * @param table is the name of the table
	 * @param field is the field in the table that joins to the parent table
	 * @param parentTable is the parent table to where this join will connect
	 * @param parentField is the field in the parent table where the field of the table is related
	 * @return instance of the {@link TableJoin} class representing the join
	 */
	TableJoin addJoin(String table, String field, String parentTable, String parentField);
	
	
	/**
	 * Add a table left join to the query
	 * @param table is the name of the table
	 * @param field is the field in the table that joins to the parent table
	 * @param parentTable is the parent table to where this join will connect
	 * @param parentField is the field in the parent table where the field of the table is related
	 * @return instance of the {@link TableJoin} class representing the join
	 */
	TableJoin addLeftJoin(String table, String field, String parentTable, String parentField);

	/**
	 * Add a field to be returned by the query
	 * @param field
	 * @param table
	 */
	void addField(String field, TableJoin table);
	
	/**
	 * Add a field of the master table to be returned by the query
	 * @param field
	 */
	void addField(String field);

	/**
	 * Add a SQL restriction to the query
	 * @param restriction
	 */
	void addRestriction(String restriction);
	
	/**
	 * Add a value to a parameter in the query 
	 * @param paramname
	 * @param value
	 */
	void addParameter(String paramname, Object value);
}
