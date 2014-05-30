package org.msh.utils.reportgen;

/**
 * Define a master table where report will be based on
 * @author Ricardo Memoria
 *
 */
public class MasterTable {

	// representative name of the master table (logical name)
	private String name;
	
	// physical name of the table in the database
	private String tableName;

	public MasterTable(String name, String tableName) {
		super();
		this.name = name;
		this.tableName = tableName;
	}

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
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
