package org.msh.tb.indicators.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Table containing a 2 dimensional indicators result<br>
 * Rows and columns are indicators and cells are the quantity by the two indicators
 * @author Ricardo Memoria
 *
 */
public class IndicatorTable {


	/**
	 * Stores data of a table column
	 * @author Ricardo Memoria
	 *
	 */
	public class TableColumn {
		public TableColumn(IndicatorTable table, String title, Object id) {
			super();
			this.title = title;
			this.table = table;
			this.id = id;
		}

		private IndicatorTable table;
		private String title;
		private Object id;
		private boolean highlight;
		private String numberPattern = "#,###,##0";
		private boolean rowTotal = true;

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
		 * @return the table of this column
		 */
		public IndicatorTable getTable() {
			return table;
		}
		
		/**
		 * Return the total of the column
		 * @return the total of the column
		 */
		public int getTotal() {
			int total = 0;
			for (TableRow row: table.getRows()) {
				TableCell cell = row.findCell(this);
				total += ((cell != null) && (cell.getValue() != null)? cell.getValue(): 0);
			}
			return total;
		}

		public Object getId() {
			return id;
		}

		public void setId(Object id) {
			this.id = id;
		}

		public String getNumberPattern() {
			return numberPattern;
		}

		public void setNumberPattern(String numberPattern) {
			this.numberPattern = numberPattern;
		}

		public void setTable(IndicatorTable table) {
			this.table = table;
		}

		public boolean isHighlight() {
			return highlight;
		}

		public void setHighlight(boolean highlight) {
			this.highlight = highlight;
		}

		public boolean isRowTotal() {
			return rowTotal;
		}

		public void setRowTotal(boolean rowTotal) {
			this.rowTotal = rowTotal;
		}
	}



	/**
	 * Stores a table row
	 * @author Ricardo Memoria
	 * 
	 */
	public class TableRow {
		private IndicatorTable table;
		private String title;
		private List<TableCell> values = new ArrayList<TableCell>();
		private List<TableCell> cells;
		private Object id;

		public TableRow(IndicatorTable table, String title, Object id) {
			super();
			this.table = table;
			this.title = title;
			this.id = id;
		}

		
		/**
		 * Return list of cell values. Most used for displaying values for the user
		 * @return list of {@link TableCell} instances
		 */
		public List<TableCell> getCells() {
			if (cells == null) {
				cells = new ArrayList<TableCell>();
				for (int i = 0; i < table.getColumnCount(); i++)
					cells.add(getCell(i));
			}
			return cells;
		}
		
	
		/**
		 * Add a new cell to the row
		 * @param col column of the cell
		 * @param val value of the cell
		 * @return instance of {@link TableCell} class
		 */
		public TableCell newValue(TableColumn col, Float val) {
			TableCell cell = new TableCell(col, this, val);
			values.add(cell);
			return cell;
		}


		/**
		 * Return the total of the row 
		 * @return total of the row
		 */
		public int getTotal() {
			int total = 0;
			for (TableCell cell: values) {
				if (cell.getColumn().isRowTotal())
					total += cell.getValue();
			}
			return total;
		}
		
		
		/**
		 * Search cell by its column ID. If there is no cell with the corresponding id, it returns null
		 * @param colId of the column in the table
		 * @return instance of {@link TableCell}
		 */
		public TableCell findCellByColumnId(Object colId) {
			for (TableCell cell: values) {
				Object id = cell.getColumn().getId();
				if ((id != null) && (id.equals(colId))) {
					return cell;
				}
			}
			return null;
		}
		
		/**
		 * Return the cell of the row
		 * @param index the index of the cell in the table starting in 0
		 * @return instance of {@link TableCell} containing cell information
		 */
		public TableCell getCell(int index) {
			TableColumn col = table.getColumns().get(index);
			return findCell(col);
		}
		
		/**
		 * Search for a cell by its column
		 * @param col column of the cell
		 * @return instance of {@link TableCell} containing the cell value
		 */
		public TableCell findCell(TableColumn col) {
			for (TableCell cell: values) {
				if (cell.getColumn() == col)
					return cell;
			}
			return null;
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
		 * @return the values
		 */
		public List<TableCell> getValues() {
			return values;
		}
		/**
		 * @param values the values to set
		 */
		public void setValues(List<TableCell> values) {
			this.values = values;
		}
		
		/**
		 * Returns the table of the row
		 * @return Instance of the {@link IndicatorTable} class
		 */
		public IndicatorTable getTable() {
			return table;
		}


		public Object getId() {
			return id;
		}


		public void setId(Object id) {
			this.id = id;
		}
	}



	/**
	 * Stores a table cell value
	 * @author Ricardo Memoria
	 *
	 */
	public class TableCell {
		private TableColumn column;
		private TableRow row;
		private Float value;

		public TableCell(TableColumn column, TableRow row, Float value) {
			super();
			this.column = column;
			this.row = row;
			this.value = value;
		}
		/**
		 * @return the column
		 */
		public TableColumn getColumn() {
			return column;
		}

		/**
		 * @param column the column to set
		 */
		public void setColumn(TableColumn column) {
			this.column = column;
		}

		/**
		 * @return the row
		 */
		public TableRow getRow() {
			return row;
		}

		/**
		 * @param row the row to set
		 */
		public void setRow(TableRow row) {
			this.row = row;
		}

		/**
		 * @return the value
		 */
		public Float getValue() {
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(Float value) {
			this.value = value;
		}
		
		public void addValue(Float value) {
			if (value == null)
				return;
			if (this.value != null)
				 this.value += value;
			else this.value = value;
		}
	}
	
	
	private List<TableColumn> columns = new ArrayList<TableColumn>();
	private List<TableRow> rows = new ArrayList<TableRow>();
	

	/**
	 * Set the value of a cell
	 * @param columnTitle column title, if no column is found with the title, a new one is created
	 * @param rowTitle row title, if no row is found with the title, a new one is created
	 * @param value value of the cell
	 * @return instance of {@link TableCell} class
	 */
	public TableCell addValue(String columnTitle, String rowTitle, Float value) {
		TableColumn col = findColumnByTitle(columnTitle);
		if (col == null) {
			col = new TableColumn(this, columnTitle, null);
			columns.add(col);
		}
		
		TableRow row = findRowByTitle(rowTitle);
		if (row == null) {
			row = new TableRow(this, rowTitle, null);
			rows.add(row);
		}
		
		TableCell cell = row.findCell(col);
		if (cell == null)
			 cell = row.newValue(col, value);
		else cell.addValue(value);
		
		return cell;
	}
	

	/**
	 * Set the value of a cell
	 * @param columnTitle column title, if no column is found with the title, a new one is created
	 * @param rowTitle row title, if no row is found with the title, a new one is created
	 * @param value value of the cell
	 * @return instance of {@link TableCell} class
	 */
	public TableCell addIdValue(Object columnId, Object rowId, Float value) {
		TableColumn col = findColumnById(columnId);
		if (col == null) {
			col = new TableColumn(this, null, columnId);
			columns.add(col);
		}
		
		TableRow row = findRowById(rowId);
		if (row == null) {
			row = new TableRow(this, null, rowId);
			rows.add(row);
		}
		
		TableCell cell = row.findCell(col);
		if (cell == null)
			 cell = row.newValue(col, value);
		else cell.addValue(value);
		
		return cell;
	}
	

	
	/**
	 * Add a new column to the table
	 * @param colTitle
	 * @return
	 */
	public TableColumn addColumn(String colTitle, Object id) {
		TableColumn col = findColumnByTitle(colTitle);
		
		if (col == null) {
			col = new TableColumn(this, colTitle, id);
			columns.add(col);
		}
		return col;
	}
	
	
	/**
	 * Add a new row to the table
	 * @param rowTitle
	 * @return
	 */
	public TableRow addRow(String rowTitle, Object id) {
		TableRow row = findRowByTitle(rowTitle);
		
		if (row == null) {
			row = new TableRow(this, rowTitle, id);
			rows.add(row);			
		}
		return row;
	}
	
	
	/**
	 * Search for a row by its title
	 * @param rowTitle title of the row in the table
	 * @return instance of {@link TableRow} class representing the row, or <b>null</b> if the row doesn't exist
	 */
	public TableRow findRowByTitle(String rowTitle) {
		for (TableRow row: rows) {
			if (row.getTitle().equals(rowTitle))
				return row;
		}
		return null;
	}

	
	/**
	 * Search for a row by its id
	 * @param id of the row in the table
	 * @return instance of {@link TableRow} class representing the row, or <b>null</b> if the row doesn't exist
	 */
	public TableRow findRowById(Object id) {
		for (TableRow row: rows) {
			Object rid = row.getId();
			if ((rid != null) && (rid.equals(id)))
				return row;
		}
		return null;
	}


	/**
	 * Search for a specific cell in the table by its IDs
	 * @param columnId id of the column
	 * @param rowId id of the row
	 * @return cell of the table that matches id of column and row
	 */
	public TableCell findCellByIds(Object columnId, Object rowId) {
		TableRow row = findRowById(rowId);
		if (row == null)
			return null;
		
		TableCell cell = row.findCellByColumnId(columnId);
		if (cell == null)
			return null;
		return cell;
	}
	
	/**
	 * Search for a column by its title
	 * @param columnTitle title of the column in the table
	 * @return instance of {@link TableColumn} class representing the column, or <b>null</b> if the column doesn't exist
	 */
	public TableColumn findColumnByTitle(String columnTitle) {
		for (TableColumn col: columns) {
			if (col.getTitle().equals(columnTitle))
				return col;
		}
		return null;
	}
	
	
	/**
	 * Search for a column by its id
	 * @param id of the column
	 * @return instance of {@link TableColumn} class representing the column, or <b>null</b> if the column doesn't exist
	 */
	public TableColumn findColumnById(Object id) {
		for (TableColumn col: columns) {
			Object cid = col.getId();
			if ((cid != null) && (cid.equals(id)))
				return col;
		}
		return null;		
	}


	/**
	 * Returns the columns of the table
	 * @return {@link TableColumn} instance
	 */
	public List<TableColumn> getColumns() {
		return columns;
	}

	
	/**
	 * Return the rows of the table
	 * @return {@link TableRow} instance
	 */
	public List<TableRow> getRows() {
		return rows;
	}

	
	/**
	 * Return the number of rows in the table 
	 * @return number of rows
	 */
	public int getRowCount() {
		return rows.size();
	}

	
	/**
	 * Return the number of columns in the table
	 * @return number of columns
	 */
	public int getColumnCount() {
		return columns.size();
	}
	
	
	/**
	 * Return the total of all values in the table
	 * @return total value
	 */
	public int getTotal() {
		int total = 0;
		for (TableRow row: getRows()) {
			total += row.getTotal();
		}
		return total;
	}
}
