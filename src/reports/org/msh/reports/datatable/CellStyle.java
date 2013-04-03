package org.msh.reports.datatable;

public class CellStyle {

	private int column;
	private int row;
	private int rowSpan = 1;
	private int colSpan = 1;


	public CellStyle(int column, int row) {
		super();
		this.column = column;
		this.row = row;
	}


	/**
	 * Check if the cell position is inside the style
	 * @param col
	 * @param row
	 * @return
	 */
	public boolean isCellInside(int colindex, int rowindex) {
		return (colindex >= column) && (rowindex >= row) && (colindex < column + colSpan) && (rowindex < row + rowSpan);
	}
	
	
	
	/**
	 * @return the rowSpan
	 */
	public int getRowSpan() {
		return rowSpan;
	}
	/**
	 * @param rowSpan the rowSpan to set
	 */
	protected void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}
	/**
	 * @return the colSpan
	 */
	public int getColSpan() {
		return colSpan;
	}
	/**
	 * @param colSpan the colSpan to set
	 */
	protected void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}
	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}
	/**
	 * @param column the column to set
	 */
	public void setColumn(int column) {
		this.column = column;
	}
	/**
	 * @return the row
	 */
	public int getRow() {
		return row;
	}
	/**
	 * @param row the row to set
	 */
	public void setRow(int row) {
		this.row = row;
	}
}
