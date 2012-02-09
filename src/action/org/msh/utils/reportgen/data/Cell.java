package org.msh.utils.reportgen.data;

public class Cell {

	private TableItem column;
	private long value = 0;
	
	
	public Cell(TableItem column) {
		super();
		this.column = column;
	}

	/**
	 * Add a value to a cell
	 * @param value
	 */
	public void addValue(long value) {
		value += value;
	}

	/**
	 * @return the value
	 */
	public long getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(long value) {
		this.value = value;
	}
	/**
	 * @return the column
	 */
	public TableItem getColumn() {
		return column;
	}
}
