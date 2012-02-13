package org.msh.utils.reportgen.data;

public class Cell {

	private TableItem column;
	private Row row;
	private long value = 0;
	
	
	public Cell(Row row, TableItem column) {
		super();
		this.row = row;
		this.column = column;
	}

	/**
	 * Add a value to a cell
	 * @param value
	 */
	public void addValue(long value) {
		this.value += value;
		// add the value to parent elements
		if ((row.getParent() != null) && (row.getParent() instanceof Row))
			((Row)row.getParent()).addValue(column, value);
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

	/**
	 * @return the row
	 */
	public Row getRow() {
		return row;
	}
}
