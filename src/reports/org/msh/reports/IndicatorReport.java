package org.msh.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msh.reports.datatable.DataTable;
import org.msh.reports.datatable.impl.DataTableImpl;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.indicator.DataTableIndicator;
import org.msh.reports.indicator.DataTableIndicatorImpl;
import org.msh.reports.query.SQLQuery;
import org.msh.reports.query.SqlBuilder;
import org.msh.reports.query.TableJoin;
import org.msh.reports.tableoperations.ConcatTables;
import org.msh.reports.tableoperations.IndicatorTransform;
import org.msh.reports.tableoperations.KeyConverter;
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
	private DataTableIndicator result;

	// new size after conversion
	private int rowsize;
	private int colsize;


	/**
	 * Create the indicator result
	 */
	public void execute() {
		DataTable data = loadData();
		res1 = data;

		// no data was returned from the database ?
		if (data.getRowCount() == 0) {
			// return an empty data table
			result = new DataTableIndicatorImpl();
			return;
		}

		data = convertData(data);
		res2 = data;
		
		colsize = calcVariablesSize(columnVariables);
		rowsize = calcVariablesSize(rowVariables);

		result = indicatorTransform(data, columnVariables, rowVariables);

		rowsize = 1;
	}
	
	
	/**
	 * Load data from the data base
	 * @return
	 */
	protected DataTableImpl loadData() {
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

		// create an empty table
		DataTableImpl tbl = new DataTableImpl();

		// run the iteration over all variables
		runVariableIteration(tbl, sqlBuilder, 0);
		
		return tbl;
	}
	
	
	/**
	 * Run recursively the sequence of iteration over the variables that have more than 1 iteration
	 * @param target
	 * @param sqlBuilder
	 * @param varindex
	 */
	protected void runVariableIteration(DataTable target, SqlBuilder sqlBuilder, int varindex) {
		Variable var = sqlBuilder.getVariables().get(varindex);
		int num = var.getIteractionCount();

		// consider at least 1 iteration for each variable
		if (num == 0)
			num = 1;

		for (int i = 0; i < num; i++) {
			sqlBuilder.setVariableIteration(var, i);

			// is the last item?
			if (varindex == sqlBuilder.getVariables().size() - 1) {
				DataTable tbl = createDataTableFromQuery(sqlBuilder);
				ConcatTables.insertRows(target, tbl);
			}
			else runVariableIteration(target, sqlBuilder, varindex + 1);
		}
	}
	
	
	/**
	 * Execute the database query from the SQL builder and loads its result in a {@link DataTableImpl}
	 * instance. If a variable is defined, it'll be called in the specific iteration. Usually, the
	 * variable is defined just for iteration bigger than 1, since the iteration 1 is generally
	 * called to all variables
	 * 
	 * @param builder the SQL builder that contains the variables and filters
	 * @param var the variable to run the specific iteration
	 * @param iteration
	 * @return
	 */
	protected DataTable createDataTableFromQuery(SqlBuilder builder) {
		String sql = sqlBuilder.createSql();

		// load data
		SQLQuery qry = new SQLQuery();
		
		// include parameter values
		for (String paramname: sqlBuilder.getParameters().keySet())
			qry.setParameter(paramname, sqlBuilder.getParameters().get(paramname));

		return qry.execute(sql);
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
		List<Variable> vars = new ArrayList<Variable>();
		List<int[]> varcols = new ArrayList<int[]>();
		
		SqlBuilder sqlbuilder = getSqlBuilder();
		for (Variable var: sqlbuilder.getVariables()) {
			vars.add(var);
			varcols.add(sqlBuilder.getColumnsVariable(var));
		}

		DataTable dt = conv.execute(sourcedt, vars, varcols);
		
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
	 * @param varColumns is the list of variables that compound the columns 
	 * @param varRows is the list of variables that compound the rows 
	 * @return a new instance of the {@link DataTableIndicator} 
	 */
	protected DataTableIndicator indicatorTransform(DataTable tbl, List<Variable> varColumns, List<Variable> varRows) {
		IndicatorTransform trans = new IndicatorTransform();
		return trans.excute(tbl, varColumns, varRows, tbl.getColumnCount() - 1);
/*		CubeTransform trans = new CubeTransform();
		int cols[] = new int[colsize];
		int rows[] = new int[rowsize];
		for (int i = 0; i < colsize; i++)
			cols[i] = i;
		for (int i = 0; i < rowsize; i++)
			rows[i] = colsize + i;

		trans.setRowGroupping(true);

		return trans.transform(tbl, cols, rows, colsize + rowsize);
*/	}

	
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
	public DataTableIndicator getResult() {
		if (result == null)
			execute();
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
