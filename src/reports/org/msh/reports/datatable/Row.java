package org.msh.reports.datatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Row {

	private Object[] values;
	private Object key;
	private DataTable dataTable;
	
	/**
	 * Indicate the parent row in a hierarchical order
	 */
	private Row parent;

	/**
	 * Indicate the children of this row
	 */
	private List<Row> children;

	
	public Row(DataTable dataTable) {
		super();
		this.dataTable = dataTable;
		if (dataTable.getColumnCount() > 0)
			resize(dataTable.getColumnCount());
	}

	
	/**
	 * Add a child row to this row. This method is protected because row insertion
	 * is controlled by the {@link DataTable} class
	 * @param row is the new child row of this row
	 */
	protected void addChildRow(Row row) {
		if (children == null)
			children = new ArrayList<Row>();
		children.add(row);
		row.setParent(this);
	}
	
	/**
	 * Level identifies the level of the row starting from 0. It allows rows to be
	 * grouped in a hierarchical order
	 * @return
	 */
	public int getLevel() {
		int level = 0;
		Row aux = parent;
		while (aux != null) {
			aux = aux.getParent();
			level++;
		}
		return level;
	}
	
	
	/**
	 * Render a list of cells to be displayed. Each call to this method is generated a list
	 * of {@link Cell} objects containing the value of the cell and information about its style
	 * @return
	 */
	public List<Cell> getCellsToRender() {
		if (values == null)
			return new ArrayList<Cell>();

		List<Cell> cells = new ArrayList<Cell>();
		
		CellStyle defaultStyle = dataTable.getDefaultCellStyle();

		int i = 0;
		int rowindex = getIndex();
		while (i < values.length) {
			CellStyle style = dataTable.findStyle(i, rowindex);
			if (style == null)
				style = defaultStyle;
			
			cells.add(new Cell(values[i], style));

			i += style.getColSpan();
		}
		
		return cells;
	}


	/**
	 * @param index
	 * @return
	 */
	public Object getValue(int index) {
		return values[index];
	}


	/**
	 * @param index
	 * @param value
	 */
	public void setValue(int index, Object value) {
		if (values == null)
			values = new Object[index+1];
		else {
			if (index >= values.length)
				resize(index + 1);
		}
		
		values[index] = value;
	}

	
	/**
	 * Resize the number of columns in the row
	 * @param numColumns
	 */
	protected void resize(int numColumns) {
		if (values == null) 
			 values = new Object[numColumns];
		else values = Arrays.copyOf(values, numColumns);
	}

	
	/**
	 * Remove the value of a column in the colindex position, reducing the size of the array of values
	 * @param colindex
	 */
	protected void removeCol(int colindex) {
		if ((colindex < 0) || (colindex >= getColumCount()))
			throw new IllegalArgumentException("Illegal index " + colindex);

		// shift values to the left, starting at the column to be removed
		int count = getColumCount();
		for (int i = colindex; i < count - 1; i++)
			values[i] = values[i + 1];
		
		// resize the values removing the last value
		resize(count - 1);
	}


	/**
	 * Insert a new blank column in the row values
	 * @param colindex
	 */
	protected void insertCol(int colindex) {
		// insert new column at the right side of the table
		int count = getColumCount() + 1;
		resize( count );
		// move values to the right
		for (int i = count - 1; i >= colindex; i-- ) {
			values[i] = values[i - 1];
		}
		
		// clear the value
		values[colindex] = null;
	}


	/**
	 * Return the number of columns in the row
	 * @return
	 */
	public int getColumCount() {
		return values == null? 0: values.length;
	}
	
	
	/**
	 * Return the list of values in a row
	 * @return
	 */
	public List getValues() {
		return values == null? null : Arrays.asList(values);
	}

	
	/**
	 * Get a sequence of cell values of the row from a list of column positions
	 * @param colindexes is an array containing a sequence of column positions (0-based index)
	 * indicating the columns to return its values
	 * @return array containing the values of each column
	 */
	public Object[] getValues(int[] colindexes) {
		Object[] vals = new Object[colindexes.length];
		int i = 0;
		for (Integer index: colindexes)
			vals[i++] = getValue(index);
		return vals;
	}


	/**
	 * Change the values
	 * @param colindexes
	 * @param values
	 */
	public void setValues(int[] colindexes, Object[] values) {
		int i = 0;
		for (Integer colindex: colindexes)
			this.values[colindex] = values[i++];
	}
	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return dataTable.getRows().indexOf(this);
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


	/**
	 * @return the parent
	 */
	public Row getParent() {
		return parent;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(Row parent) {
		this.parent = parent;
	}


	/**
	 * @return the children
	 */
	public List<Row> getChildren() {
		return children;
	}

}
