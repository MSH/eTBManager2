package org.msh.reports.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msh.reports.variables.Variable;

/**
 * Create a SQL declaration to be used in indicator reports
 * @author Ricardo Memoria
 *
 */
public class SqlBuilder implements SQLDefs {

	private TableJoin masterTable;
	private StringBuilder sql;
	private List<Field> fields = new ArrayList<SqlBuilder.Field>();
	private List<Variable> variables = new ArrayList<Variable>();
	private Variable currentVariable;
	private String fieldList;
	private List<String> restrictions = new ArrayList<String>();
	private List<String> varRestrictions = new ArrayList<String>();
	private HashMap<String, Object> parameters = new HashMap<String, Object>();
	private Map<Variable, Integer> varIteration = new HashMap<Variable, Integer>();
	private boolean detailed;
	private String orderBy;

	public SqlBuilder(String tableName) {
		super();
		masterTable = new TableJoin(tableName, null, null, null);
	}

	
	public SqlBuilder() {
		super();
	}

	
	/**
	 * Clear the data to generate a new SQL from scratch
	 */
	public void clear() {
		sql = null;
		fields.clear();
		variables.clear();
		currentVariable = null;
		restrictions.clear();
		fieldList = null;
		varIteration.clear();
	}

	
	/**
	 * Select the iteration of the variable to be executed
	 * @param var the variable that is already available in the list of variables
	 * @param iteration the iteration index to be executed, starting at 0 index
	 */
	public void setVariableIteration(Variable var, Integer iteration) {
		varIteration.put(var, iteration);
	}


	/**
	 * Change the table name and clear all joins
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		masterTable = new TableJoin(tableName, null, null, null);
	}

	
	/**
	 * Return the main table name used in this query
	 * @return
	 */
	public String getTableName() {
		return masterTable != null? masterTable.getTableName(): null;
	}

	
	/**
	 * Create the SQL instruction
	 * @return SQL instruction to be executed in the database server in order to return
	 * the specific fields and its indicators
	 */
	public String createSql() {
		sql = new StringBuilder();
		fieldList = null;
		varRestrictions.clear();

		if (variables.size() > 0) {
			fields.clear();
			try{
				for (Variable var: variables) {
					currentVariable = var;
					// get the current variable iteration
					Integer iteration = varIteration.get(var);
					if (iteration == null)
						iteration = 0;
					// prepare the variable
					var.prepareVariableQuery(this, iteration);
				}
			}
			finally {
				currentVariable = null;
			}
		}
		
		createSQLSelect(sql);

		createSQLFrom(sql);

		createSQLJoins(sql);
		
		createSQLWhere(sql);
		
		createSQLGroupBy(sql);
		
		createSQLOrderBy(sql);
		
		return sql.toString();
	}

	
	/**
	 * Create SQL Where
	 */
	protected void createSQLWhere(StringBuilder builder) {
		boolean first = true;

		// include restrictions defined in the SQL builder
		if (restrictions.size() > 0) {
			for (String s: restrictions) {
				if (first) {
					builder.append("\nwhere ");
					first = false;
				}
				else builder.append("\nand ");

				s = parseTableNames(s);
				builder.append("\n" + s);
			}
		}

		// include restrictions defined by the variables
		if (varRestrictions.size() > 0) {
			for (String s: varRestrictions) {
				if (first) {
					builder.append("\nwhere ");
					first = false;
				}
				else builder.append("\nand ");

				s = parseTableNames(s);
				builder.append("\n" + s);
			}
		}
	}
	
	
	/**
	 * Return the list of column for a given variable used to build the query
	 * @param var
	 * @return
	 */
	public int[] getColumnsVariable(Variable var) {
		List<Integer> cols = new ArrayList<Integer>();
		int index = 0;
		for (Field field: fields) {
			if (field.getVariable() == var) {
				cols.add(index);
			}
			index++;
		}

		int[] res = new int[cols.size()];
		index = 0;
		for (Integer n: cols)
			res[index++] = n;

		return res;
	}


	/**
	 * Create SELECT SQL instruction
	 */
	protected void createSQLSelect(StringBuilder builder) {
		builder.append("select ");

		if (detailed) {
			builder.append( getFieldList() );
		}
		else {
			if (variables.size() == 0)
				builder.append("count(*)");
			else {
				builder.append(getFieldList());
				builder.append(", count(*)");
			}
		}
	}

	
	/**
	 * Create SQL FROM
	 */
	protected void createSQLFrom(StringBuilder builder) {
		builder.append("\nfrom " + masterTable.getTableName() + " " + masterTable.getAlias());
	}
	
	/**
	 * Create SQL join with other tables
	 */
	protected void createSQLJoins(StringBuilder builder) {
		if (masterTable.getJoins() != null)
			for (TableJoin join: masterTable.getJoins())
				createSQLJoin(builder, join);
	}

	/**
	 * Create a join of the table and its joins
	 * @param join
	 */
	protected void createSQLJoin(StringBuilder builder, TableJoin join) {
		if (join.isLeftJoin())
			 builder.append("\nleft join ");
		else builder.append("\ninner join ");
		builder.append(join.getTableName());
		builder.append(' ');
		builder.append(join.getAlias());
		builder.append(" on ");
		TableJoin p = join.getParentJoin();
		builder.append(join.getAlias() + "." + join.getTableField() + "=" + p.getAlias() + "." + join.getParentField());
		
		if (join.getJoins() != null)
			for (TableJoin j: join.getJoins())
				createSQLJoin(builder, j);
	}

	
	/**
	 * Create SQL Group By instruction
	 */
	protected void createSQLGroupBy(StringBuilder builder) {
		if ((detailed) || (variables.size() == 0))
			return;
		builder.append("\ngroup by ");
		builder.append(getFieldList());
	}
	
	
	/**
	 * Create order by clause. If the query is detailed, so the order by
	 * will just be included if the <code>orderBy</code> is defined.
	 * @param builder
	 */
	protected void createSQLOrderBy(StringBuilder builder) {
		if ((detailed && orderBy == null) || ((!detailed) && (variables.size() == 0)))
			return;
		
		builder.append("\norder by ");
		
		if (orderBy != null)
			 builder.append(orderBy);
		else builder.append(getFieldList());
	}

	
	/**
	 * Return the field list separated by commas
	 * @return
	 */
	protected String getFieldList() {
		if (fieldList == null) {
			fieldList = "";
			for (Field field: fields) {
				if (!fieldList.isEmpty())
					fieldList += ", ";
				fieldList += parseTableNames(field.getName());
			}
		}
		
		return fieldList;
	}
	
	/**
	 * Replace table names by its alias
	 * @param s
	 * @return
	 */
	protected String parseTableNames(String s) {
		int index = 0;
		while (index < s.length()) {
			index = s.indexOf('.', index);
			if (index == -1)
				break;

			int ini = index - 1;
			while (ini >= 0) {
				char c = s.charAt(ini);
				if (!Character.isJavaIdentifierPart(c))
					break;
				ini--;
			}
			if (ini < 0)
				ini = 0;
			else ini++;
			String tbl = s.substring(ini, index);
			TableJoin join = masterTable.findJoin(tbl);
			
			if (join != null) {
				String alias = join.getAlias();
				s = s.substring(0, ini) + alias + s.substring(index, s.length());
				index = ini + alias.length() + 1;
			}
			else index++;
		}
		
		return s;
	}
	
	/**
	 * Add a variable to the SQL Builder
	 * @param var
	 */
	public void addVariable(Variable var) {
		variables.add(var);
	}
	
	@Override
	public TableJoin getMasterTable() {
		return masterTable;
	}

	@Override
	public TableJoin addJoin(String table, String field, String parentTable, String parentField) {
		return masterTable.addJoin(table, field, parentTable, parentField);
	}

	@Override
	public void addField(String field, TableJoin table) {
		Field fld = new Field();
		fld.setName(field);
		fld.setTable(table);
		fld.setVariable(currentVariable);
		fields.add(fld);
	}


	@Override
	public void addField(String field) {
		addField(field, masterTable);
	}

	
	public class Field {
		private String name;
		private TableJoin table;
		private Variable variable;
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the table
		 */
		public TableJoin getTable() {
			return table;
		}
		/**
		 * @param table the table to set
		 */
		public void setTable(TableJoin table) {
			this.table = table;
		}
		/**
		 * @return the variable
		 */
		public Variable getVariable() {
			return variable;
		}
		/**
		 * @param variable the variable to set
		 */
		public void setVariable(Variable variable) {
			this.variable = variable;
		}
	}


	@Override
	public void addRestriction(String restriction) {
		List<String> restr;
		
		if (currentVariable != null)
			 restr = varRestrictions;
		else restr = restrictions;

		if (!restr.contains(restriction))
				restr.add(restriction);
	}


	/**
	 * @return the parameters
	 */
	public HashMap<String, Object> getParameters() {
		return parameters;
	}


	/* (non-Javadoc)
	 * @see org.msh.reports.query.SQLDefs#addParameter(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addParameter(String paramname, Object value) {
		parameters.put(paramname, value);
	}


	/**
	 * @return the variables
	 */
	public List<Variable> getVariables() {
		return variables;
	}


	/* (non-Javadoc)
	 * @see org.msh.reports.query.SQLDefs#addLeftJoin(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public TableJoin addLeftJoin(String table, String field,
			String parentTable, String parentField) {
		TableJoin join = addJoin(table, field, parentTable, parentField);
		join.setLeftJoin(true);
		return join;
	}


	/**
	 * @return the detailed
	 */
	public boolean isDetailed() {
		return detailed;
	}


	/**
	 * @param detailed the detailed to set
	 */
	public void setDetailed(boolean detailed) {
		this.detailed = detailed;
	}


	/**
	 * @return the orderBy
	 */
	public String getOrderBy() {
		return orderBy;
	}


	/**
	 * @param orderBy the orderBy to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	
}
