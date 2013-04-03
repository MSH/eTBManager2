package org.msh.tb.client.shared.model;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a column of the table
 * 
 * @author Ricardo Memoria
 *
 */
public class CTableColumn implements IsSerializable {

	/**
	 * The title of this column
	 */
	private String title;
	
	/**
	 * Store the key of the column in string format
	 */
	private String key;
	
	/**
	 * List of child columns
	 */
	private ArrayList<CTableColumn> columns;
	
	/**
	 * The parent column, if available. This information is sent as
	 * null to the client and is mounted there when received from server
	 */
	private CTableColumn parent;


	/**
	 * Return the level of the column in the row header
	 * @return
	 */
	public int getLevel() {
		int level = 0;
		CTableColumn col = getParent();
		while (col != null) {
			col = col.getParent();
			level++;
		}
		return level;
	}

	/**
	 * Calculate the column span of this column based on the child columns
	 * @return integer number bigger than 0
	 */
	public int getSpan() {
		if (columns == null)
			return 1;
		int span = 0;
		for (CTableColumn col: columns)
			span += col.getSpan();
		return span;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the columns
	 */
	public ArrayList<CTableColumn> getColumns() {
		return columns;
	}
	/**
	 * @param columns the columns to set
	 */
	public void setColumns(ArrayList<CTableColumn> columns) {
		this.columns = columns;
	}
	/**
	 * @return the parent
	 */
	public CTableColumn getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(CTableColumn parent) {
		this.parent = parent;
	}
}
