package org.msh.utils.reportgen.data;

import java.util.ArrayList;
import java.util.List;


public class Row extends TableItem {

	private List<Cell> cells = new ArrayList<Cell>();

	public Row() {
		super();
	}

	public Row(TableItem parent) {
		super(parent);
	}

	/**
	 * Find a cell by its column. If cell doesn't exist, a new one is created
	 * @param col
	 * @return
	 */
	public Cell findOrCreateCell(TableItem col) {
		Cell cell = getCellByColumn(col); 
		
		if (cell != null)
			return cell;
		cell = new Cell(col);
		cells.add(cell);
		return cell;
	}
	
	/**
	 * Return the value of a column
	 * @param c
	 * @return
	 */
	public Long getColumnValue(TableItem col) {
		for (Cell cell: cells)
			if (cell.getColumn().equals(col))
				return cell.getValue();
		return null;
	}
	
	/**
	 * Search for a cell by its column. If cell doesn't exist create a new one
	 * @param Col
	 * @return
	 */
	public Cell getCellByColumn(TableItem col) {
		for (Cell cell: cells)
			if (cell.getColumn().equals(col))
				return cell;
		return null;
	}

	/**
	 * @return the cells
	 */
	public List<Cell> getCells() {
		return cells;
	}

	/**
	 * @param cells the cells to set
	 */
	public void setCells(List<Cell> cells) {
		this.cells = cells;
	}
}
