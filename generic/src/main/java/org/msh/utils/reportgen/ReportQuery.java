package org.msh.utils.reportgen;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;

/**
 * Generate a single query to the database returning consolidated data based on filters, variables and a master table
 * @author Ricardo Memoria
 *
 */
public class ReportQuery {

	private static final String mainTableAlias = "a";
	
	private ReportFilters filters;
	private List<Variable> variables;
	private List<ReportData> resultList;
	private String masterTable;
	private List<VarInfo> varsinfo;
	private List<JoinTable> joins;
	private Map<String, Object> parameters;
	private int aliasCounter;
	private int paramCounter;
	private List<String> restrictions;

	/**
	 * Create a new query informing the main table where report will be generated
	 * @param mastertable
	 */
	public ReportQuery(String mastertable) {
		this.masterTable = mastertable;
	}

	
	/**
	 * Force execution of the query again on asking for the result list
	 */
	public void refresh() {
		resultList = null;
	}


	/**
	 * Select master table to be used to generate report
	 * @param name
	 */
	public void setMasterTable(String tablename) {
		this.masterTable = tablename;
	}

	
	/**
	 * Add a variable to be returned by the report
	 * @param variable
	 */
	public void addVariable(Variable variable) {
		if (variables == null)
			variables = new ArrayList<Variable>();
		variables.add(variable);
	}


	/**
	 * Add static restriction to the query
	 * @param sql
	 */
	public void addRestriction(String sql) {
		if (restrictions == null)
			restrictions = new ArrayList<String>();
		restrictions.add(sql);
	}

	/**
	 * Create the filters to be applied to this report
	 * @return
	 */
	protected ReportFilters createFilters() {
		return new ReportFilters();
	}
	
	/**
	 * Create the filters to be used in this report
	 * @return
	 */
	public ReportFilters getFilters() {
		if (filters == null)
			filters = createFilters();
		return filters;
	}
	
	
	public String getMainTableAlias() {
		return mainTableAlias; 
	}

	
	/**
	 * Create the query that will return the data from the database to generate the report
	 * @return
	 */
	protected Query createQuery() {
		String sql = createSQL();
		
		EntityManager em = getEntityManager();
		Query query = em.createNativeQuery(sql);
		
		if (parameters != null) {
			for (String param: parameters.keySet())
				query.setParameter(param, parameters.get(param));
		}
		
		return query;
	}

	
	/**
	 * Return the instance of the {@link EntityManager}
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}


	/**
	 * Add a parameter to be included in the query
	 * @param param
	 * @param value
	 */
	protected void setParameter(String param, Object value) {
		if (parameters == null)
			parameters = new HashMap<String, Object>();
		
		parameters.put(param, value);
	}
	
	/**
	 * Add a query parameter. The parameter name is automatically assigned by the query and returned by the method
	 * @param value
	 * @return
	 */
	public String createParameter(Object value) {
		String paramName = "p" + Integer.toString(paramCounter);
		paramCounter++;
		
		setParameter(paramName, value);
		
		return paramName;
	}


	/**
	 * Create the SQL declaration
	 * @return
	 */
	protected String createSQL() {
		if (masterTable == null)
			throw new RuntimeException("No master table defined to the ReportQuery");

		JoinTable join = new JoinTable(masterTable, null, null, null);
		join.setAlias(mainTableAlias);
		joins = new ArrayList<ReportQuery.JoinTable>();
		joins.add(join);
		
		// reset the alias counter, in order to feed other tables
		aliasCounter = 1;
		paramCounter = 1;

		SQLCreator creator = new SQLCreator(this);
		
		return creator.createSQL();
	}

	
	/**
	 * Search for join table by its table name
	 * @param tablename
	 * @return
	 */
	protected JoinTable joinTableByTableName(String tablename) {
		for (JoinTable tbl: joins) {
			if (tbl.getTableName().equals(tablename))
				return tbl;
		}
		return null;
	}

	
	/**
	 * Create a new table alias, incrementing the alias counter. So, the next time this method is called, a new alias is created
	 * @return
	 */
	protected String createTableAlias() {
		int lo = aliasCounter % 26;
		int hi = aliasCounter / 26;
		
		String res = "" + (char)(97 + lo);
		if (hi > 0)
			res = (char)hi + res;

		aliasCounter++;
		
		return res;
	}

	/**
	 * @param table
	 * @param keyfield
	 * @param fortable
	 * @param forfield
	 */
	public JoinTable addJoinTable(String table, String keyfield, String forfield, String fortable) {
		JoinTable master = joinTableByTableName(fortable);
		if (master == null)
			throw new RuntimeException("Master table for table " + fortable + " was not found");

		JoinTable info = new JoinTable(table, keyfield, master, forfield);

		// check if join is already in the list
		for (JoinTable aux: joins) {
			if (aux.equals(info)) {
				return aux;
			}
		}
		
		String alias = createTableAlias();
		info.setAlias(alias);
		
		joins.add(info);
		return info;
	}


	/**
	 * Create a new join between a table and the master table of the report 
	 * @param table
	 * @param keyfield
	 * @param foreignfield
	 * @return
	 */
	public JoinTable addJoinMasterTable(String table, String keyfield, String foreignfield) {
		return addJoinTable(table, keyfield, foreignfield, getMasterTable());
	}
	
	/**
	 * Create result list of the report
	 * @return
	 */
	protected List<ReportData> createResultList() {
		Query query = createQuery();
	
		List<Object[]> lst = query.getResultList();
		resultList = new ArrayList<ReportData>();
		

		for (Object[] vals: lst) {
			addData(vals);
		}
		
		return resultList;
	}

	
	/**
	 * Add a record data from the query to the report data, translating the information
	 * @param vals
	 */
	protected void addData(Object[] vals) {
		// last column is the total of the consolidated record
		Long total = ((BigInteger)vals[vals.length - 1]).longValue();
		Object vars[] = translateVariableData(vals);
		ReportData repData = searchSimiliarData(vars);

		if (repData == null) {
			repData = new ReportData(this, vars, total);
			resultList.add(repData);
		}
		else {
			repData.addTotal(total);
		}
	}


	/**
	 * Translate data from the database to data to the report
	 * @param vals
	 * @return
	 */
	protected Object[] translateVariableData(Object[] vals) {
		// translate data from database into report data
		int index = 0;
		int resindex = 0;

		Object res[] = new Object[varsinfo.size()];
		
		for (VarInfo varinfo: varsinfo) {
			int numValues = varinfo.getFields().length;
			Object value;
			
			// variable just returned one single value ?
			if (numValues == 1) {
				// translate this single value
				value = varinfo.getVariable().translateSingleValue(vals[index]);
				index++;
			}
			else {
				// create a temporary array with values from the variable
				Object fieldValues[] = Arrays.copyOfRange(vals, index, index + numValues);
				
				value = varinfo.getVariable().translateValues(fieldValues);
				index += numValues;
			}

			res[resindex] = value;
			
			// increment indexes for the loop
			resindex++;
		}
		
		return res;
	}
	
	
	/**
	 * Search in the list for records with the same variable values
	 * @param vals
	 * @return
	 */
	protected ReportData searchSimiliarData(Object[] vals) {
		for (ReportData rec: resultList) {
			Object vs[] = rec.getValues();
			if (Arrays.equals(vs, vals))
				return rec;
		}
		
		return null;
	}


	/**
	 * Return the result from database based on filters, variables and master table 
	 * @return
	 */
	public List<ReportData> getResultList() {
		if (resultList == null)
			resultList = createResultList();
		
		return resultList;
	}

	/**
	 * @return the masterTable
	 */
	public String getMasterTable() {
		return masterTable;
	}
	
	
	protected List<JoinTable> getJoins() {
		return joins;
	}
	
	protected List<VarInfo> getVarInfos() {
		if (varsinfo == null)
			varsinfo = new ArrayList<ReportQuery.VarInfo>();
		return varsinfo;
	}
	
	/**
	 * Store information about a variable
	 * @author Ricardo Memoria
	 *
	 */
	protected class VarInfo {
		private Variable variable;
		private String[] fields;

		public VarInfo(Variable variable, String[] fields) {
			super();
			this.variable = variable;
			this.fields = fields;
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
		/**
		 * @return the fields
		 */
		public String[] getFields() {
			return fields;
		}
		/**
		 * @param fields the fields to set
		 */
		public void setFields(String[] fields) {
			this.fields = fields;
		}
	}
	
	
	/**
	 * Store information about a join between tables
	 * @author Ricardo Memoria
	 *
	 */
	protected class JoinTable {
		private String tableName;
		private String key;
		private String foreignKey;
		private JoinTable masterJoin;
		private String alias;

		public JoinTable(String tableName, String key, JoinTable masterJoin, String foreignKey) {
			super();
			this.tableName = tableName;
			this.key = key;
			this.foreignKey = foreignKey;
			this.masterJoin = masterJoin;
		}

		/**
		 * @return the tableName
		 */
		public String getTableName() {
			return tableName;
		}

		/**
		 * @return the key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * @return the foreignKey
		 */
		public String getForeignKey() {
			return foreignKey;
		}

		/**
		 * @return the masterJoin
		 */
		public JoinTable getMasterJoin() {
			return masterJoin;
		}

		/**
		 * @return the alias
		 */
		public String getAlias() {
			return alias;
		}

		/**
		 * @param alias the alias to set
		 */
		public void setAlias(String alias) {
			this.alias = alias;
		}
	}


	/**
	 * @return the variables
	 */
	public List<Variable> getVariables() {
		return variables;
	}


	/**
	 * @return the restrictions
	 */
	public List<String> getRestrictions() {
		return restrictions;
	}
	
}
