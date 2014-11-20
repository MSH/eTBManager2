package org.msh.utils.reportgen;

import org.msh.utils.reportgen.ReportQuery.JoinTable;
import org.msh.utils.reportgen.ReportQuery.VarInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructs the SQL instruction based on the {@link ReportQuery} parameters
 * @author Ricardo Memoria
 *
 */
public class SQLCreator {

	private ReportQuery reportQuery;
	private List<String> groupflds;
	private List<VarInfo> varsinfo;
	private List<String> conditions;


	public SQLCreator(ReportQuery reportQuery) {
		super();
		this.reportQuery = reportQuery;
	}


	/**
	 * Create SQL declaration
	 * @return
	 */
	public String createSQL() {
		conditions = new ArrayList<String>();

		// create list of conditions
		for (FilterValue fval: reportQuery.getFilters().getFilterValues()) {
			if (!fval.isNull()) {
				String s = fval.createSQLCondition(reportQuery);
				if ((s != null) && (!s.isEmpty()))
					conditions.add( s );
			}
		}
		
		if (reportQuery.getRestrictions() != null)
			for (String s: reportQuery.getRestrictions())
				conditions.add( s );
		
		// create list of fields for the select clause from the variable list
		if (reportQuery.getVariables() == null)
			throw new RuntimeException("No variable was defined to the report");

		varsinfo = reportQuery.getVarInfos();
		groupflds = new ArrayList<String>();

		for (Variable var: reportQuery.getVariables()) {
			String[] fields = var.createSQLSelectFields(reportQuery);
			VarInfo varinfo = reportQuery.new VarInfo(var, fields);
			varsinfo.add(varinfo);
			
			String f[] = var.createSQLGroupByFields(reportQuery);
			for (String s: f)
				groupflds.add( translateField(s) );
		}
		
		StringBuilder sql = new StringBuilder(100);
		// create select
		createSelect(sql);

		// create from and joins
		createFromAndJoins(sql);
		
		// create conditions
		createWhere(sql);

		// create group by clause
		createGroupBy(sql);
		
		createOrderBy(sql);
		
		return sql.toString();
	}
	
	
	protected String translateField(String field) {
		String s;

		if (field.indexOf('.') > 0) {
			String vals[] = field.split("\\.");
			JoinTable tbl = reportQuery.joinTableByTableName(vals[0]);
			if (tbl != null) {
				s = tbl.getAlias() + '.' + vals[1];
			}
			else s = field;
		}
		else s = field;
		
		return s;
	}
	
	
	/**
	 * Create SQL SELECT declaration
	 * @param sql
	 */
	protected void createSelect(StringBuilder sql) {
		List<VarInfo> varsinfo = reportQuery.getVarInfos();

		sql.append("select ");
		for (VarInfo var: varsinfo) {
			for (String s: var.getFields()) {
				sql.append( translateField(s) );
				sql.append(",");
			}
		}
		sql.append("count(*)");
		sql.append('\n');
	}


	/**
	 * Create SQL FROM and JOINs declarations
	 * @param sql
	 */
	protected void createFromAndJoins(StringBuilder sql) {
		List<JoinTable> joins = reportQuery.getJoins();
		sql.append("from ");
		sql.append(reportQuery.getMasterTable());
		sql.append(" " + reportQuery.getMainTableAlias());
		sql.append('\n');
		for (int i = 1; i < joins.size(); i++) {
			JoinTable join = joins.get(i);
			sql.append(" join ");
			sql.append(join.getTableName());
			sql.append(" " + join.getAlias() + " on ");
			sql.append(join.getAlias() + "." + join.getKey() + "=" + join.getMasterJoin().getAlias() + "." + join.getForeignKey());
			sql.append('\n');
		}
	}
	
	
	/**
	 * Create SQL WHERE declaration
	 * @param sql
	 */
	protected void createWhere(StringBuilder sql) {
		for (int i = 0; i < conditions.size(); i++) {
			if (i == 0)
				 sql.append("where ");
			else sql.append("and ");
			sql.append(conditions.get(i));
			sql.append('\n');
		}
	}
	
	
	/**
	 * Create SQL GROUP BY declaration
	 * @param sql
	 */
	protected void createGroupBy(StringBuilder sql) {
		if (groupflds.size() > 0) {
			sql.append("group by ");
			for (int i = 0; i < groupflds.size(); i++) {
				sql.append(groupflds.get(i));
				if (i < groupflds.size() - 1)
					sql.append(',');
			}
		}
	}
	
	
	/**
	 * Create SQL ORDER BY declaration
	 * @param sql
	 */
	protected void createOrderBy(StringBuilder sql) {
		sql.append("\norder by count(*) desc");
	}
}
