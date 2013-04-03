package org.msh.reports.datatable;

public class Column {

	private Object key;
	private DataTable dataTable;

	public Column(DataTable dataTable) {
		super();
		this.dataTable = dataTable;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return dataTable.getColumns().indexOf(this);
	}

	/**
	 * @return the dataTable
	 */
	public DataTable getDataTable() {
		return dataTable;
	}

	/**
	 * @return the key
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(Object key) {
		this.key = key;
	}

}
