package org.msh.reports.query;

import java.util.ArrayList;
import java.util.List;


public class TableJoin {

	private String tableName;
	private String tableField;
	private String parentField;
	private TableJoin parentJoin;
	private int aliasCounter;	// a unique number used to generate the table alias
	private List<TableJoin> joins;

	public TableJoin(String tableName, String tableField, TableJoin parentJoin, String parentField) {
		super();
		this.tableName = tableName;
		this.tableField = tableField;
		this.parentField = parentField;
		this.parentJoin = parentJoin;
		generateAliasCounter();
	}

	
	/**
	 * Add a join to the tree of joins
	 * @param table
	 * @param tableField
	 * @param parentTable
	 * @param parentField
	 * @return
	 */
	protected TableJoin addJoin(String table, String tableField, String parentTable, String parentField) {
		if (parentTable.equals(tableName)) {
			TableJoin join = new TableJoin(table, tableField, this, parentField);
			addJoin(join);
			return join;
		}

		if (joins != null) {
			for (TableJoin join: joins) {
				TableJoin aux = join.addJoin(table, tableField, parentTable, parentField);
				if (aux != null)
					return aux;
			}
		}
		
		throw new IllegalArgumentException("No join found for table " + parentTable);
	}
	
	/**
	 * Add a new join to this table
	 * @param join
	 */
	protected void addJoin(TableJoin join){
		if (joins == null)
			joins = new ArrayList<TableJoin>();
		
		joins.add(join);
	}

	
	/**
	 * Search for a join table with the arguments
	 * @param table
	 * @param tableFieldName
	 * @param parentTable
	 * @param parentFieldName
	 * @return
	 */
	protected TableJoin findJoin(String table, String tableFieldName, String parentTable, String parentFieldName) {
		if (tableName.equals(table)) {
			if (equalString(tableField, tableFieldName) && equalString(parentField, parentFieldName))
				return this;
		}
		
		if (joins != null) {
			for (TableJoin join: joins) {
				TableJoin tj = join.findJoin(table, tableFieldName, parentTable, parentFieldName);
				if (tj != null)
					return tj;
			}
		}
		return null;
	}
	
	
	/**
	 * Find join just by its table name
	 * @param tableName
	 * @return
	 */
	protected TableJoin findJoin(String tableName) {
		if (this.tableName.equals(tableName))
			return this;

		if (joins != null) {
			for (TableJoin join: joins) {
				TableJoin tj = join.findJoin(tableName);
				if (tj != null)
					return tj;
			}
		}
		return null;
	}
	
	protected boolean equalString(String val1, String val2) {
		// both are null or pointing to the same string?
		if (val1 == val2)
			return true;

		// one of them are null value
		if ((val1 == null) || (val2 == null))
			return false;

		return val1.equals(val2);
	}
	
	/**
	 * Generate a new alias counter for this join
	 */
	protected void generateAliasCounter() {
		// search for root table
		TableJoin root = this;
		while (root.getParentJoin() != null)
			root = root.getParentJoin();
		
		if (root == this)
			 aliasCounter = 1;
		else aliasCounter = root.calcMaxAliasCounter() + 1;
	}
	
	
	/**
	 * Calculate the biggest alias counter from a table join node
	 * @return
	 */
	protected int calcMaxAliasCounter() {
		int num = aliasCounter;
		
		// there are children ?
		if (joins != null)
			// search for a bigger number in the children
			for (TableJoin join: joins) {
				int n = join.calcMaxAliasCounter();
				if (n > num)
					num = n;
			}
		
		return num;
	}

	
	/**
	 * Create a new table alias based on the alias counter
	 * @return
	 */
	protected String createTableAlias() {
		int lo = aliasCounter % 26;
		int hi = aliasCounter / 26;
		
		String res = "" + (char)(97 + lo);
		if (hi > 0)
			res = (char)hi + res;

		return res;
	}

	

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return createTableAlias();
	}


	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the parentJoin
	 */
	public TableJoin getParentJoin() {
		return parentJoin;
	}


	/**
	 * @return the joins
	 */
	public List<TableJoin> getJoins() {
		return joins;
	}


	/**
	 * @return the tableField
	 */
	public String getTableField() {
		return tableField;
	}


	/**
	 * @return the parentField
	 */
	public String getParentField() {
		return parentField;
	}
}
