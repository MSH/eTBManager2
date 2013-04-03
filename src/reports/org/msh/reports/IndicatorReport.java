package org.msh.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msh.reports.datatable.DataTable;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLQuery;
import org.msh.reports.query.SqlBuilder;
import org.msh.reports.query.TableJoin;
import org.msh.reports.tableoperations.CubeTransform;
import org.msh.reports.tableoperations.KeyConverter;
import org.msh.reports.tableoperations.TableTitleConverter;
import org.msh.reports.variables.Variable;

/**
 * Generate an indicator report, consisting of variables compounding the rows and columns
 * @author Ricardo Memoria
 *
 */
public class IndicatorReport {

	private List<Variable> columnVariables = new ArrayList<Variable>();
	private List<Variable> rowVariables = new ArrayList<Variable>();
	private Map<Filter, FilterValue> filters = new HashMap<Filter, FilterValue>();
	
	private SqlBuilder sqlBuilder;

	private DataTable res1, res2;
	private DataTable result;

	// new size after conversion
	private int rowsize;
	private int colsize;


	/**
	 * Create the indicator result
	 */
	protected void createResult() {
		DataTable data = loadData();
		res1 = data;

		// no data was returned from the database ?
		if (data.getRowCount() == 0) {
			// return an empty data table
			result = new DataTable();
			return;
		}

		data = convertData(data);
		res2 = data;
//		int colsize = convertData(data, columnVariables);
//		int rowsize = convertData(data, rowVariables);
		
		colsize = calcVariablesSize(columnVariables);
		rowsize = calcVariablesSize(rowVariables);
		
		result = cubeTransform(data, colsize, rowsize);
		
		TableTitleConverter conv = new TableTitleConverter();
		conv.convert(result, columnVariables, rowVariables, true);
		
		rowsize = 1;
	}
	
	
	/**
	 * Load data from the data base
	 * @return
	 */
	protected DataTable loadData() {
		// create SQL instruction
		SqlBuilder sqlBuilder = getSqlBuilder();
		
		// add variables to SQL builder
		for (Variable v: columnVariables)
			sqlBuilder.addVariable(v);
		for (Variable v: rowVariables)
			sqlBuilder.addVariable(v);
		
		// include filters in the SQL
		for (Filter filter: filters.keySet()) {
			FilterValue fvalue = filters.get(filter);
			filter.prepareFilterQuery(sqlBuilder, fvalue.getComparator(), fvalue.getValue());
		}
		
		String sql = sqlBuilder.createSql();

		// load data
		SQLQuery qry = new SQLQuery();
		
		// include parameter values
		for (String paramname: sqlBuilder.getParameters().keySet())
			qry.setParameter(paramname, sqlBuilder.getParameters().get(paramname));

		DataTable tbl = qry.execute(sql);

		return tbl;
	}

	
	protected SqlBuilder getSqlBuilder() {
		if (sqlBuilder == null)
			sqlBuilder = createSqlBuilder();
		return sqlBuilder;
	}

	
	/**
	 * Add another variable to the group of columns variables
	 * @param var
	 * @return
	 */
	public IndicatorReport addColumnVariable(Variable var) {
		columnVariables.add(var);
		return this;
	}
	
	
	/**
	 * Clear the list of column variables
	 */
	public void clearColumnVariables() {
		columnVariables.clear();
	}
	
	/**
	 * Add another variable to the group of row variables
	 * @param var
	 * @return
	 */
	public IndicatorReport addRowVariable(Variable var) {
		rowVariables.add(var);
		return this;
	}
	
	/**
	 * Clear all row variables
	 */
	public void clearRowVariables() {
		rowVariables.clear();
	}
	
	/**
	 * Add a new filter to the indicator report
	 * @param filter
	 * @param value
	 * @return
	 */
	public IndicatorReport addFilter(Filter filter, Object value) {
		FilterValue val = new FilterValue(filter, FilterOperation.EQUALS, value);
		filters.put(filter, val);
		return this;
	}

	
	/**
	 * Add a filter and its value to the report indicating the operation to be done
	 * @param filter
	 * @param oper
	 * @param value
	 * @return
	 */
	public IndicatorReport addFilter(Filter filter, FilterOperation oper, Object value) {
		FilterValue val = new FilterValue(filter, oper, value);
		filters.put(filter, val);
		return this;
	}
	
	/**
	 * Clear all filters applied to the indicator
	 */
	public void clearFilters() {
		filters.clear();
	}

	/**
	 * Create a new instance of a {@link SqlBuilder} class
	 * @return
	 */
	protected SqlBuilder createSqlBuilder() {
		return new SqlBuilder();
	}
	

	protected DataTable convertData(DataTable sourcedt) {
		KeyConverter conv = new KeyConverter();
		
		// mount list of variables and columns
		List<int[]> cols = new ArrayList<int[]>();
		List<Variable> vars = new ArrayList<Variable>();
		
		SqlBuilder sqlbuilder = getSqlBuilder();
		for (Variable var: sqlbuilder.getVariables()) {
			cols.add(sqlbuilder.getColumnsVariable(var));
			vars.add(var);
		}
		
		DataTable dt = conv.execute(sourcedt, vars, cols);
		
/*		// calculate size of new data table for rows and columns
		colsize = calcConvertedSize(sourcedt, columnVariables);
		rowsize = calcConvertedSize(sourcedt, rowVariables);
		
		DataTable dt = new DataTable(colsize + rowsize + 1, sourcedt.getRowCount());

		convertVariables(sourcedt, dt, 0, columnVariables);
		convertVariables(sourcedt, dt, colsize, rowVariables);

		// move column with value
		int colori = sourcedt.getColumnCount() - 1;
		int coldst = dt.getColumnCount() - 1;
		for (int i = 0; i < sourcedt.getRowCount(); i++)
			dt.setValue(coldst, i, sourcedt.getValue(colori, i));
*/		
		return dt;
	}

	
	/**
	 * @param lst
	 * @return
	 */
	protected int calcVariablesSize(List<Variable> lst) {
		int len = 0;
		for (Variable var: lst) {
			len += var.isGrouped()? 2: 1;
		}
		return len;
	}
	
	
	/**
	 * Transform a data table into a cube using specific columns 
	 * to generate the new columns and rows.
	 * @param tbl the table to be transformed
	 * @param colsize is the number of columns to be used as the 
	 * new columns, counting from the first column
	 * @param rowsize is the number of column to be used as the new rows,
	 * counting from the next column after colsize 
	 * @return a new instance of the {@link DataTable} transformed in cube
	 */
	protected DataTable cubeTransform(DataTable tbl, int colsize, int rowsize) {
		CubeTransform trans = new CubeTransform();
		int cols[] = new int[colsize];
		int rows[] = new int[rowsize];
		for (int i = 0; i < colsize; i++)
			cols[i] = i;
		for (int i = 0; i < rowsize; i++)
			rows[i] = colsize + i;

		trans.setRowGroupping(true);

		return trans.transform(tbl, cols, rows, colsize + rowsize);
	}

	
	/**
	 * Add a table join to the main table
	 * @param tableName
	 * @param fieldName
	 * @param foreignKey
	 * @return
	 */
	public TableJoin addTableJoin(String tableName, String fieldName, String foreignKey) {
		return getSqlBuilder().addJoin(tableName, fieldName, sqlBuilder.getTableName(), foreignKey);
	}

	
	/**
	 * Add a table join to another table in the query
	 * @param tableName
	 * @param fieldName
	 * @param parentTable
	 * @param parentField
	 * @return
	 */
	public TableJoin addTableJoin(String tableName, String fieldName, String parentTable, String parentField) {
		return getSqlBuilder().addJoin(tableName, fieldName, parentTable, parentField);
	}
	
	/**
	 * Return the result of the report
	 * @return
	 */
	public DataTable getResult() {
		if (result == null)
			createResult();
		return result;
	}

	/**
	 * @return the columnVariables
	 */
	public List<Variable> getColumnVariables() {
		return columnVariables;
	}

	/**
	 * @return the rowVariables
	 */
	public List<Variable> getRowVariables() {
		return rowVariables;
	}

	/**
	 * @return the filters
	 */
	public Map<Filter, FilterValue> getFilters() {
		return filters;
	}


	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return getSqlBuilder().getTableName();
	}


	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		getSqlBuilder().setTableName(tableName);
	}


	/**
	 * @param sqlCondition the sqlCondition to set
	 */
	public void addRestriction(String sqlRestriction) {
		getSqlBuilder().addRestriction(sqlRestriction);
	}


	/**
	 * @return the res1
	 */
	public DataTable getRes1() {
		return res1;
	}


	/**
	 * @return the res2
	 */
	public DataTable getRes2() {
		return res2;
	}
	
	
	/**
	 * Return the number of rows that the column header uses in the indicator table
	 * @return
	 */
	public int getColumnHeaderSize() {
		return colsize;
	}

	/**
	 * Return the number of columns that the row header uses in the indicator table
	 * @return
	 */
	public int getRowHeaderSize() {
		return rowsize;
	}
}
