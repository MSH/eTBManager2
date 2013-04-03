package org.msh.reports.datatable;

/**
 * A temporary representation of a cell of the table to be rendered 
 * @author Ricardo Memoria
 *
 */
public class Cell {

	private Object value;
	private CellStyle style;

	public Cell(Object value, CellStyle style) {
		super();
		this.value = value;
		this.style = style;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @return the style
	 */
	public CellStyle getStyle() {
		return style;
	}
}
