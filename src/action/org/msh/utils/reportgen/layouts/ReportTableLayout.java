package org.msh.utils.reportgen.layouts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.msh.utils.reportgen.ReportData;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

public class ReportTableLayout implements HTMLLayoutGenerator {

	private ReportQuery reportQuery; 
	private String styleClass;
	
	private Variable columnVariable;
	private Variable rowVariable;
	
	private Variable colGroupingVariable;
	private Variable rowGroupingVariable;
	
	private List<Row> rows;
	private List<Column> columns;
	
	private StringBuilder html;
	
	private boolean colgrouped;
	private boolean rowgrouped;


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.layouts.HTMLLayoutGenerator#addQuery(org.msh.utils.reportgen.ReportQuery)
	 */
	@Override
	public void addQuery(ReportQuery reportQuery) {
		if (this.reportQuery != null)
			throw new RuntimeException("Report query has already been set for this layout");
		this.reportQuery = reportQuery;
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.layouts.HTMLLayoutGenerator#generateHtml()
	 */
	@Override
	public String generateHtml() {
		if (reportQuery == null)
			throw new RuntimeException("ReportQuery not specified for layout generator");

		List<ReportData> values = reportQuery.getResultList();

		colgrouped = colGroupingVariable != null;
		if (!colgrouped)
			colgrouped = columnVariable.setGrouping(true);

		rowgrouped = rowGroupingVariable != null;
		if (!rowgrouped)
			rowgrouped = rowVariable.setGrouping(true);
		
		groupData(values);

		html = new StringBuilder(values.size() * 10);

		String s = styleClass != null? "class=" + styleClass: "";
		html.append("<table " + s + "><tr>");
		
		// create table header
		html.append("<th rowspan='");
		html.append(colgrouped? "3": "2");
		html.append("'>" + rowVariable.getTitle() + "</th>");
		html.append("<th colspan='");
		html.append(Integer.toString(columns.size() + 1));
		html.append("'>");
		html.append(escapeString(columnVariable.getTitle()));
		html.append("</th></tr>");

		createGroupingColumns();

		html.append("<tr>");
		for (Column col: columns) {
			Object val = col.getData();
			s = escapeString( columnVariable.getValueDisplayText(val) );
			addTH(s);
		}
		
		if (!colgrouped)
			html.append("<th>TOTAL</th></tr>");
		
		Object rowgrpvalue = (rowgrouped? -1: null);
		// include rows
		DecimalFormat f = new DecimalFormat("#,###,##0");
		for (Row row: rows) {
			if ((rowgrouped) && (rowgrpvalue != row.getGroupData())) {
				int span = columns.size() + 2;
				html.append("<tr><th colspan='" + span + "'>");
				if (rowGroupingVariable != null)
					 s = rowGroupingVariable.getValueDisplayText(row.getGroupData());
				else s = rowVariable.getValueDisplayText(row.getGroupData());
				html.append( escapeString(s) );
				html.append("</th></tr>");
				rowgrpvalue = row.getGroupData();
			}
	
			html.append("<tr>");
			s = escapeString( rowVariable.getValueDisplayText( row.getData() ));
			addTH(s);
			
			for (Column col: columns) {
				Long val = row.getColumnValue(col);
				s = (val != null? f.format(val): null);
				addTD(s);
				
				if (val != null)
					col.addTotal(val);
			}
			Long total = row.getTotal();
			addTH(total != null? f.format(total): null);
			html.append("</tr>");
		}
		
		// include total line
		html.append("<tr>");
		addTH("TOTAL");
		long val = 0;
		for (Column col: columns) {
			Long total = col.getTotal();
			addTH(total != null? f.format(total): null);
			
			if (total != null)
				val += total;
		}
		addTH(val > 0? f.format(val): null);
		html.append("</tr>");
		
		return html.toString();
	}

	
	/**
	 * Add a TD tag to the HTML code
	 * @param s
	 */
	private void addTD(String s) {
		html.append("<td>");
		if (s != null)
			html.append("<span class='text-small'>" + s + "</span>");
		html.append("</td>");
	}

	
	/**
	 * Add a TH tag to the HTML code
	 * @param s
	 */
	private void addTH(String s) {
		html.append("<th>");
		if (s != null)
			html.append("<span class='text-small'>" + s + "</span>");
		html.append("</th>");
	}
	
	/**
	 * @param s
	 * @return
	 */
	protected String escapeString(String s) {
		return StringEscapeUtils.escapeHtml(s);
	}


	/**
	 * Group value data creating the columns and rows of the table
	 * @param values
	 */
	protected void groupData(List<ReportData> values) {
		int colindex = reportQuery.getVariables().indexOf(columnVariable);
		int rowindex = reportQuery.getVariables().indexOf(rowVariable);
		int colgrpindex;
		if (colGroupingVariable != null)
			 colgrpindex = reportQuery.getVariables().indexOf(colGroupingVariable);
		else colgrpindex = -1;
		
		int rowgrpindex;
		if (rowGroupingVariable != null)
			 rowgrpindex = reportQuery.getVariables().indexOf(rowGroupingVariable);
		else rowgrpindex = -1;
		
		for (ReportData data: values) {
			Object vals[] = data.getValues();
			Object coldata = vals[colindex];
			Object rowdata = vals[rowindex];
			
			Object colgrpdata;
			if ((colgrpindex < 0) && (colgrouped))
				 colgrpdata = columnVariable.getGroupData(coldata);
			else colgrpdata = (colgrpindex >= 0? vals[colgrpindex]: null);
			
			Object rowgrpdata = (rowgrpindex >= 0? vals[rowgrpindex]: null);
			if ((rowgrpindex < 0) && (rowgrouped))
				 rowgrpdata = rowVariable.getGroupData(rowdata);
			else rowgrpdata = (rowgrpindex >= 0? vals[rowgrpindex]: null);
			
			Column col = findColumn(colgrpdata, coldata);
			Row row = findRow(rowgrpdata, rowdata);
			
			Cell cell = row.findOrCreateCell(col);
			cell.addValue(data.getTotal());
		}

		sortData();
	}

	
	protected void sortData() {
		// sort columns
		Collections.sort(columns, new Comparator<Column>() {
			@Override
			public int compare(Column o1, Column o2) {
				Integer comp1 = null;
				Integer comp2 = null;
				if (colGroupingVariable != null)
					comp1 = colGroupingVariable.compareValues(o1.getGroupData(), o2.getGroupData());

				if ((comp1 == null) || (comp1 == 0)) {
					comp2 = columnVariable.compareValues(o1.getData(), o2.getData());
					return (comp2 == null? 0: comp2);
				}

				return (comp1 == null? 0: comp1);
			}
		});
		
		// sort rows
		Collections.sort(rows, new Comparator<Row>() {
			@Override
			public int compare(Row o1, Row o2) {
				Integer comp1 = null;
				Integer comp2 = null;
				if (rowGroupingVariable != null)
					comp1 = rowGroupingVariable.compareValues(o1.getGroupData(), o2.getGroupData());

				if ((comp1 == null) || (comp1 == 0)) {
					comp2 = rowVariable.compareValues(o1.getData(), o2.getData());
					return (comp2 == null? 0: comp2);
				}

				return (comp1 == null? 0: comp1);
			}
		});
	}
	
	/**
	 * Create the HTML code with the table TH tags with the columns grouping 
	 */
	protected void createGroupingColumns() {
		if (!colgrouped)
			return;

		Object val = columns.get(0).getGroupData();
		int span = 0;
		for (int i = 0; i < columns.size(); i++) {
			Column c = columns.get(i);
			if ((c.getGroupData() == val) || ((c.getGroupData() != null) && (c.getGroupData().equals(val)))) {
				span++;
			}
			else {
				addGroupingHeader(val, span);
				span = 1;
				val = c.getGroupData();
			}
		}
		
		addGroupingHeader(val, span);
		
		html.append("<th rowspan='2'>TOTAL</th>");
	}

	
	/**
	 * Create the HTML code of the grouped header
	 * @param data
	 * @param span
	 */
	private void addGroupingHeader(Object data, int span) {
		html.append("<th");
		if (span > 1)
			html.append(" colspan='" + Integer.toString(span) + "'");
		html.append('>');
		
		String s;
		if (colGroupingVariable != null)
			s = colGroupingVariable.getValueDisplayText(data);
		else s = columnVariable.getValueDisplayText(data);

		html.append( escapeString(s));
		html.append("</th>");
	}


	/**
	 * Search for a column by its data. If column doesn't exists, create a new one as the last column and return it
	 * @param data
	 * @return
	 */
	protected Column findColumn(Object datagrp, Object data) {
		if (columns == null) 
			columns = new ArrayList<ReportTableLayout.Column>();

		for (Column col: columns) {
			if (((col.getGroupData() == datagrp) || ((col.getGroupData() != null) && (col.getGroupData().equals(datagrp)) )) &&
				((col.getData() == data) || ((col.getData() != null) && (col.getData().equals(data))))) {
				return col;
			}
		}

		Column col = new Column(datagrp, data);
		columns.add(col);
		return col;
	}

	
	/**
	 * Find a row by its data. If row doesn't exists, create a new one at the last row and return it
	 * @param data
	 * @return
	 */
	protected Row findRow(Object datagrp, Object data) {
		if (rows == null) 
			rows = new ArrayList<ReportTableLayout.Row>();

		for (Row row: rows) {
			if (((row.getGroupData() == datagrp) || ((row.getGroupData() != null) && (row.getGroupData().equals(datagrp)) )) &&
				((row.getData() == data) || ((row.getData() != null) && (row.getData().equals(data))))) {
				return row;
			}
		}

		Row row = new Row(datagrp, data);
		rows.add(row);
		return row;
	}


	@Override
	public void clear() {
		reportQuery = null;
	}

	/**
	 * Store information about a column in the table
	 * @author Ricardo Memoria
	 *
	 */
	protected class Column {
		private Object groupData;
		private Object data;
		private Long total;

		public Column(Object groupData, Object data) {
			super();
			this.groupData = groupData;
			this.data = data;
		}
		/**
		 * @return the total
		 */
		public Long getTotal() {
			return total;
		}
		/**
		 * @param total the total to set
		 */
		public void setTotal(Long total) {
			this.total = total;
		}
		
		/**
		 * Increment the total value
		 * @param value
		 */
		public void addTotal(Long value) {
			if (total == null)
				 total = value;
			else total += value;
		}
		/**
		 * @return the groupData
		 */
		public Object getGroupData() {
			return groupData;
		}
		/**
		 * @param groupData the groupData to set
		 */
		public void setGroupData(Object groupData) {
			this.groupData = groupData;
		}
		/**
		 * @return the data
		 */
		public Object getData() {
			return data;
		}
		/**
		 * @param data the data to set
		 */
		public void setData(Object data) {
			this.data = data;
		}
	}
	
	/**
	 * Store information about a row in the table
	 * @author Ricardo Memoria
	 *
	 */
	protected class Row {
		private Object groupData;
		private Object data;
		private List<Cell> cells = new ArrayList<ReportTableLayout.Cell>();

		public Row(Object groupData, Object data) {
			super();
			this.groupData = groupData;
			this.data = data;
		}
		/**
		 * Find a cell by its column. If cell doesn't exist, a new one is created
		 * @param col
		 * @return
		 */
		public Cell findOrCreateCell(Column col) {
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
		public Long getColumnValue(Column col) {
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
		public Cell getCellByColumn(Column col) {
			for (Cell cell: cells)
				if (cell.getColumn().equals(col))
					return cell;
			return null;
		}
		/**
		 * @return the data
		 */
		public Object getData() {
			return data;
		}
		
		/**
		 * Return the total value
		 * @return
		 */
		public Long getTotal() {
			Long total = 0L;
			for (Cell cell: cells) {
				total += cell.getValue();
			}
			
			return total == 0? null: total;
		}
		/**
		 * @return the groupData
		 */
		public Object getGroupData() {
			return groupData;
		}
	}

	
	/**
	 * @return the columnVariable
	 */
	public Variable getColumnVariable() {
		return columnVariable;
	}


	/**
	 * @param columnVariable the columnVariable to set
	 */
	public void setColumnVariable(Variable columnVariable) {
		this.columnVariable = columnVariable;
	}


	/**
	 * @return the rowVariable
	 */
	public Variable getRowVariable() {
		return rowVariable;
	}


	/**
	 * @param rowVariable the rowVariable to set
	 */
	public void setRowVariable(Variable rowVariable) {
		this.rowVariable = rowVariable;
	}


	protected class Cell {
		private Column column;
		private Long value;
		public Cell(Column column) {
			super();
			this.column = column;
		}
		/**
		 * @return the column
		 */
		public Column getColumn() {
			return column;
		}
		/**
		 * @return the value
		 */
		public Long getValue() {
			return value;
		}
		
		public void setValue(Long value) {
			this.value = value;
		}
		
		public void addValue(Long value) {
			if (this.value == null)
				 this.value = value;
			else this.value += value;
		}
	}


	/**
	 * @return the styleClass
	 */
	public String getStyleClass() {
		return styleClass;
	}


	/**
	 * @param styleClass the styleClass to set
	 */
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}


	/**
	 * @return the colGroupingVariable
	 */
	public Variable getColGroupingVariable() {
		return colGroupingVariable;
	}


	/**
	 * @param colGroupingVariable the colGroupingVariable to set
	 */
	public void setColGroupingVariable(Variable colGroupingVariable) {
		this.colGroupingVariable = colGroupingVariable;
	}


	/**
	 * @return the rowGroupingVariable
	 */
	public Variable getRowGroupingVariable() {
		return rowGroupingVariable;
	}


	/**
	 * @param rowGroupingVariable the rowGroupingVariable to set
	 */
	public void setRowGroupingVariable(Variable rowGroupingVariable) {
		this.rowGroupingVariable = rowGroupingVariable;
	}
}
