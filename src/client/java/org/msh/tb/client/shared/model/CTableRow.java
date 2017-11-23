package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a row of the table
 * 
 * @author Ricardo Memoria
 *
 */
public class CTableRow implements IsSerializable{

	/**
	 * The title of the row
	 */
	private String title;
	/**
	 * The level of this row, when grouped
	 */
	private int level;
	/**
	 * The variable index of the row
	 */
	private int varIndex;
	/**
	 * The values that compound the row
	 */
	private Double[] values;
	
	/**
	 * Store the key of the row in string format
	 */
	private String key;

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
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}
	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}
	/**
	 * @return the varIndex
	 */
	public int getVarIndex() {
		return varIndex;
	}
	/**
	 * @param varIndex the varIndex to set
	 */
	public void setVarIndex(int varIndex) {
		this.varIndex = varIndex;
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
	 * @param values the values to set
	 */
	public void setValues(Double[] values) {
		this.values = values;
	}
	/**
	 * @return the values
	 */
	public Double[] getValues() {
		return values;
	}

}
