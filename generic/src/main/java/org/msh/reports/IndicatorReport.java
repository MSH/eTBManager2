package org.msh.reports;

import org.msh.reports.datatable.DataTable;
import org.msh.reports.datatable.impl.DataTableImpl;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.indicator.DataTableIndicator;
import org.msh.reports.indicator.DataTableIndicatorImpl;
import org.msh.reports.query.DataTableQuery;
import org.msh.reports.query.SQLQuery;
import org.msh.reports.query.SqlBuilder;
import org.msh.reports.query.TableJoin;
import org.msh.reports.tableoperations.ConcatTables;
import org.msh.reports.tableoperations.IndicatorTransform;
import org.msh.reports.tableoperations.KeyConverter;
import org.msh.reports.variables.Variable;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate an indicator report, consisting of variables compounding the rows and columns
 * @author Ricardo Memoria
 *
 */
public class IndicatorReport {

	private List<Variable> columnVariables = new ArrayList<Variable>();
	private List<Variable> rowVariables = new ArrayList<Variable>();
	private Map<Filter, FilterValue> filters = new HashMap<Filter, FilterValue>();
    private boolean limitToUserView;
	
	private SqlBuilder sqlBuilder;

	private DataTable res1, res2;
	private DataTableIndicator result;

	// new size after conversion
	private int rowsize;
	private int colsize;
	
	/**
	 * Number of records contained in the detailed report
	 */
	private Long recordCount;


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
	 * Return an instance of the {@link DataTableQuery} containing detailed list of cases
	 * @param fields is a list of comma separated fields to be returned
	 * @return instance of {@link DataTableQuery}
	 */
	public DataTableQuery getDetailedReport(String fields, String orderBy, int inipage, int recordsPerPage) {
		SqlBuilder builder = getSqlBuilder();


		for (Filter filter: filters.keySet()) {
			FilterValue fvalue = filters.get(filter);
			filter.prepareFilterQuery(builder, fvalue.getComparator(), fvalue.getValue());
		}

		// prepare to get counting
		builder.getVariables().clear();
		builder.setDetailed(false);

		// calculate record count
		DataTableQuery dt = createDataTableFromQuery(builder, null, null);
		recordCount = (Long)dt.getValue(0, 0);
		
		// prepare to generate detailed report
		builder.setDetailed(true);
		for (String fld: fields.split(",")) {
			builder.select(fld);
		}

		builder.setOrderBy(orderBy);
		
		return createDataTableFromQuery(builder, inipage * recordsPerPage, recordsPerPage);
	}
	
	/**
	 * Load data from the data base
	 */
	protected DataTableImpl loadData() {
		// create SQL instruction
		SqlBuilder sqlBuilder = getSqlBuilder();

		// add variables to SQL builder
		for (Variable v: columnVariables)
			sqlBuilder.addVariable(v);
		for (Variable v: rowVariables)
			sqlBuilder.addVariable(v);
		
		sqlBuilder.setFilters(filters);

        if (limitToUserView) {
            applyUserViewRestrictions(sqlBuilder);
        }

		// create an empty table
		DataTableImpl tbl = new DataTableImpl();

		// run the iteration over all variables
		runVariableIteration(tbl, sqlBuilder, 0);
		
		return tbl;
	}

    /**
     * Apply user view restrictions according to the user view configuration
     * @param sqlBuilder instance of SqlBuilder that will create the query
     */
    private void applyUserViewRestrictions(SqlBuilder sqlBuilder) {
        UserWorkspace user = UserSession.getUserWorkspace();

        // user is limited to just an administrative unit ?
        if (user.getView() == UserView.ADMINUNIT) {
            AdministrativeUnit au = user.getAdminUnit();
            if (au != null) {
                TableJoin join = sqlBuilder.join("tbunit", "tbcase.owner_unit_id").join("administrativeunit", "tbunit.id");
                sqlBuilder.addRestriction("administrativeunit.code like :code_1");
                sqlBuilder.addParameter("code_1", au.getCode() + "%");
            }
            return;
        }

        // user view is limited to just a health facility ?
        if (user.getView() == UserView.TBUNIT) {
            Tbunit unit = user.getTbunit();
            if (unit != null) {
                sqlBuilder.addRestriction("tbcase.owner_unit_id = " + unit.getId());
            }
            return;
        }
    }


    /**
	 * Run recursively the sequence of iteration over the variables that have more than 1 iteration
	 * @param target to do
	 * @param sqlBuilder to do
	 * @param varindex to do
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
				DataTable tbl = createDataTableFromQuery(sqlBuilder, null, null);
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
	 * @param iniResult the initial record to be returned, or null if the first record must be returned
	 * @param maxResults the maximum number of records to be returned, or null if all records should be returned
	 * @return instance of the {@link DataTableQuery} containing the result of the SQL executed in the server
	 */
	protected DataTableQuery createDataTableFromQuery(SqlBuilder builder, Integer iniResult, Integer maxResults) {
		String sql = builder.createSql();

		// load data
		SQLQuery qry = new SQLQuery();

		// include parameter values
		for (String paramname: sqlBuilder.getParameters().keySet())
			qry.setParameter(paramname, sqlBuilder.getParameters().get(paramname));

		qry.setIniResult(iniResult);
		qry.setMaxResults(maxResults);
		
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
	

	/**
	 * Create a new data table already converted to the variable key
	 * @param sourcedt
	 * @return
	 */
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
	 * @param tableField the field name
	 * @param foreignKey the foreign key representation
	 * @return
	 */
	public TableJoin addTableJoin(String tableField, String foreignKey) {
		return getSqlBuilder().table(sqlBuilder.getTableName()).join(tableField, foreignKey);
	}

	
	/**
	 * Add a table join to another table in the query
	 * @param tableName
	 * @param fieldName
	 * @param parentTable
	 * @param parentField
	 * @return
	 */
/*	public TableJoin addTableJoin(String tableName, String fieldName, String parentTable, String parentField) {
		return getSqlBuilder().addJoin(tableName, fieldName, parentTable, parentField);
	}
*/	
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
	 * @param sqlRestriction the sqlCondition to set
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


	/**
	 * @return the recordCount
	 */
	public Long getRecordCount() {
		return recordCount;
	}

    public boolean isLimitToUserView() {
        return limitToUserView;
    }

    public void setLimitToUserView(boolean limitToUserView) {
        this.limitToUserView = limitToUserView;
    }
}
