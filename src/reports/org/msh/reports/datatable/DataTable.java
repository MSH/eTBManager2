package org.msh.reports.datatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a structured data representation in a table format, similar to the 
 * records stored in a database table or the result set of a database query, but
 * includes other features like:
 * <p/>
 * 1. The possibility to include new rows and columns, even with data already initialized;
 * <br/>
 * 2. Support key (objects) to be assigned to columns and rows
 * <br/>
 * 3. Support cell styles (column span, for example)
 * 
 * @author Ricardo Memoria
 *
 */
public class DataTable {

	private CellStyle defaultCellStyle = new CellStyle(-1, -1);
	private List<Row> rows = new ArrayList<Row>();
	private List<Column> columns = new ArrayList<Column>();
	private List<CellStyle> cellStyles;

	
	public DataTable() {
		super();
	}
	
	
	public DataTable(int colcount, int rowcount) {
		super();
		resize(colcount, rowcount);
	}

	/**
	 * Return the value of a cell
	 * @param colindex
	 * @param rowindex
	 * @return
	 */
	public Object getValue(int colindex, int rowindex) {
		Row row = rows.get(rowindex);
		return row.getValue(colindex);
	}


	/**
	 * Set the value of a cell in a specified row and col
	 * @param colindex
	 * @param rowindex
	 * @param value
	 */
	public void setValue(int colindex, int rowindex, Object value) {
		CellStyle st = findStyle(colindex, rowindex);
		if (st == null) {
			getRows().get(rowindex).setValue(colindex, value);
		}
		else {
			for (int r = st.getRow(); r < st.getRow() + st.getRowSpan(); r++)
				for (int c = st.getColumn(); c < st.getColumn() + st.getColSpan(); c++)
					getRows().get(r).setValue(c, value);
		}
	}

	
	/**
	 * Insert a new column in the table
	 * @param colindex
	 */
	public void insertCol(int colindex) {
		if ((colindex < 0) || (colindex >= getColumnCount()))
			throw new IllegalArgumentException("Illegal column index " + colindex);

		// add new column
		Column col = new Column(this);
		columns.add(colindex, col);

		for (Row row: rows)
			row.insertCol(colindex);
	}

	
	/**
	 * Insert a new column at the right side of the table
	 */
	public void insertCol() {
		Column col = new Column(this);
		columns.add(col);
	}


	/**
	 * Insert a new row in the data table at the position rowindex
	 * @param rowindex
	 */
	public Row insertRow(int rowindex) {
		if ((rowindex < 0) || (rowindex > getRowCount()))
			throw new IllegalArgumentException("Illegal column index " + rowindex);

		// add a new row
		Row row = new Row(this);
		
		// check if it's the last row to be included
		if (rowindex == getRowCount())
			 rows.add(row);
		else rows.add(rowindex, row);
		return row;
	}

	
	/**
	 * Insert a new row having another as his parent row
	 * @param parent
	 * @return
	 */
	public Row insertRow(Row parent) {
		// get the new index of the row
		int index = rows.indexOf(parent) + 1;
		int level = parent.getLevel();

		// get the next sibling row
		while ((index < rows.size()) && (rows.get(index).getLevel() > level))
			index++;
		
		Row row = insertRow(index);
		parent.addChildRow(row);
		
		return row;
	}
	
	/**
	 * Insert a row at the bottom of the table
	 */
	public Row insertRow() {
		Row row = new Row(this);
		rows.add(row);
		return row;
	}


	/**
	 * Return the style of a specific cell in the table
	 * @param colindex
	 * @param rowindex
	 * @return
	 */
	public CellStyle getCellStyle(int colindex, int rowindex) {
		CellStyle style = findStyle(colindex, rowindex);

		if (style == null) 
			style = createNewStyle(colindex, rowindex);

		return style;
	}

	
	/**
	 * Resize the table to the specified values
	 * @param columns
	 * @param rows
	 */
	public void resize(int numcols, int numrows) {
		if ((numcols == columns.size()) && (numrows == rows.size()))
			return;

		// update number of rows
		if (numrows > rows.size()) {
			while (rows.size() < numrows)
				rows.add(new Row(this));
		}
		else {
			while (rows.size() > numrows)
				rows.remove(rows.size() - 1);
		}
		
		if (columns.size() != numcols) {
			for (Row row: rows) {
				row.resize(numcols);
			}

			// update number of columns
			if (numcols > columns.size()) {
				while (columns.size() < numcols)
					columns.add(new Column(this));
			}
			else {
				while (columns.size() > numcols)
					columns.remove(columns.size() - 1);
			}
		}
		
		// update styles
		if (cellStyles != null) {
			int i = 0;
			while (i < cellStyles.size()) {
				CellStyle style = cellStyles.get(i);
				// style is out of range ?
				if ((style.getRow() > rows.size()) || (style.getColumn() > columns.size())) {
					cellStyles.remove(style);
					i--;
				}
				else {
					if (style.getRow() + style.getRowSpan() > rows.size())
						style.setRowSpan(rows.size() - style.getRow());

					if (style.getColumn() + style.getColSpan() > columns.size())
						style.setColSpan(columns.size() - style.getColumn());
				}

				i++;
			}
		}
	}

	
	/**
	 * Search for a style in the specified colindex and rowindex in the table
	 * @param colindex
	 * @param rowindex
	 * @return
	 */
	public CellStyle findStyle(int colindex, int rowindex) {
		if (cellStyles == null)
			return null;

		for (CellStyle style: cellStyles) {
			if (style.isCellInside(colindex, rowindex))
				return style;
		}
		
		return null;
	}

	
	/**
	 * Span a cell in the position specified by colindex and rowindex
	 * @param col
	 * @param row
	 * @param colspan
	 * @param rowspan
	 */
	public void spanCell(int colindex, int rowindex, int colspan, int rowspan) {
		CellStyle style = findStyle(colindex, rowindex);
		
		if (style == null)
			style = createNewStyle(colindex, rowindex);
		else {
			if ((style.getColumn() != colindex) || (style.getRow() != rowindex)) {
				style.setColSpan(1);
				style.setRowSpan(1);
				style = createNewStyle(colindex, rowindex);
			}
		}
		style.setColSpan(colspan);
		style.setRowSpan(rowspan);
		
		// span the same value to all cells
		Object value = getValue(colindex, rowindex);
		setValue(colindex, rowindex, value);
	}

	
	/**
	 * Remove a row in the rowindex position
	 * @param rowindex
	 */
	public void removeRow(int rowindex) {
		rows.remove(rowindex);

		// check all styles that this row affects
		if (cellStyles == null)
			return;

		int i = 0;
		while (i < cellStyles.size()) {
			CellStyle style = cellStyles.get(i);
			int r = style.getRow();
			int dx = style.getRowSpan();
			if ((rowindex >= r)  &&  (rowindex < r + dx)) {
				dx--;
				// this style is useless
				if (dx == 0)
					cellStyles.remove(r);
				else {
					style.setRowSpan(dx);
					i++;
				}
			}
			else i++;
		}
	}
	
	
	/**
	 * Remove a column of the data table at the colindex position
	 * @param colindex
	 */
	public void removeColumn(int colindex) {
		columns.remove(colindex);
		
		for (Row row: rows)
			row.removeCol(colindex);

		// check all styles that this column affects
		if (cellStyles == null)
			return;

		int i = 0;
		while (i < cellStyles.size()) {
			CellStyle style = cellStyles.get(i);
			int c = style.getColumn();
			int dx = style.getColSpan();
			if ((colindex >= c)  &&  (colindex < c + dx)) {
				dx--;
				// this style is useless
				if (dx == 0)
					cellStyles.remove(c);
				else {
					style.setColSpan(dx);
					i++;
				}
			}
			else i++;
		}
	}

	
	/**
	 * Swap content of two rows indicated in their positions by rowindex1 and rowindex2
	 * @param rowindex1
	 * @param rowindex2
	 */
	public void swapRow(int rowindex1, int rowindex2) {
		Row row1 = rows.get(rowindex1);
		Row row2 = rows.get(rowindex2);

		rows.set(rowindex2, row1);
		rows.set(rowindex1, row2);
		// TO DO: update the cell style
	}

	
	/**
	 * Search for a column in a range of rows with a specific value
	 * @param rowindexes
	 * @param values
	 * @return
	 */
	public Column findColumn(int[] rowindexes, Object[] values, int colini) { 
		for (int c = colini; c < columns.size(); c++) {
			boolean equals = true;
			int i = 0;

			for (int r: rowindexes) {
				Object val = getValue(c, r);
				if (!equalObjects(val, values[i++])) {
					equals = false;
					break;
				}
			}
			
			if (equals)
				return columns.get(c);
		}
		
		return null;
	}


	/**
	 * Search for a row in a range of columns with a specific value
	 * @param colindexes
	 * @param values
	 * @return
	 */
	public Row findRow(int[] colindexes, Object[] values, int rowini) {
		for (int rindex = rowini; rindex < rows.size(); rindex++) {
			int i = 0;
			boolean equals = true;
			Row row = rows.get(rindex);
			
			for (int index: colindexes) {
				Object val = row.getValue(index);
				if (!equalObjects(val, values[i++])) {
					equals = false;
					break;
				}
			}
			
			if (equals)
				return row;
		}
		
		return null;
	}


	/**
	 * Compare two objects
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	protected boolean equalObjects(Object obj1, Object obj2) {
		if (obj1 == obj2)
			return true;
		
		if ((obj1 == null) || (obj2 == null))
			return false;
		
		return obj1.equals(obj2);
	}


	/**
	 * Create a new style
	 * @param colindex
	 * @param rowindex
	 * @return
	 */
	protected CellStyle createNewStyle(int colindex, int rowindex) {
		CellStyle style = new CellStyle(colindex, rowindex);

		if (cellStyles == null)
			cellStyles = new ArrayList<CellStyle>();
		cellStyles.add(style);

		return style;
	}


	/**
	 * Return the number of columns in the table
	 * @return
	 */
	public int getColumnCount() {
		return columns.size();
	}

	
	/**
	 * Return the number of rows in the table
	 * @return
	 */
	public int getRowCount() {
		return rows.size();
	}
	
	/**
	 * @return
	 */
	public List<Row> getRows() {
		return rows;
	}


	/**
	 * @return the columns
	 */
	public List<Column> getColumns() {
		return columns;
	}


	/**
	 * @return the defaultCellStyle
	 */
	public CellStyle getDefaultCellStyle() {
		return defaultCellStyle;
	}


	/**
	 * @return the cellStyles
	 */
	public List<CellStyle> getCellStyles() {
		return cellStyles;
	}
}
