package org.msh.utils.reportgen.layouts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;
import org.msh.utils.reportgen.data.DataTableTransform;
import org.msh.utils.reportgen.data.Row;
import org.msh.utils.reportgen.data.TableItem;

public class ReportTableLayout implements HTMLLayoutGenerator {

	private ReportQuery reportQuery; 
	private String styleClass;
	private String id;
	
	private Variable columnVariable;
	private Variable rowVariable;
	
	private Variable groupColumnVariable;
	private Variable groupRowVariable;

	private DecimalFormat numberFormatter = new DecimalFormat("#,###,##0");

	private StringBuilder html;
	
	private DataTableTransform dataTable;


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

		dataTable = new DataTableTransform(reportQuery);
		dataTable.setColumnVariable(columnVariable);
		dataTable.setGroupColumnVariable(groupColumnVariable);
		dataTable.setRowVariable(rowVariable);
		dataTable.setGroupRowVariable(groupRowVariable);
		
		DecimalFormatSymbols symb = new DecimalFormatSymbols(LocaleSelector.instance().getLocale());
		numberFormatter.setDecimalFormatSymbols(symb);
		
		dataTable.execute();
		html = new StringBuilder(dataTable.getRows().size() * 20);

		String s = styleClass != null? "class=" + styleClass: "";
		s += id != null? " id=" + id: "";
		html.append("<table " + s + ">");
		
		// create table header
		html.append("<tr><th rowspan='");
		html.append(dataTable.isColumnGrouped()? "3": "2");
		html.append("'>" + rowVariable.getTitle() + "</th>");
		addTH(columnVariable.getTitle(), null, dataTable.getColumns().size() + 1);
		html.append("</tr>");

		createGroupingColumns();

		html.append("<tr>");
		for (TableItem col: dataTable.getColumns())
			addTH( col.getTitle(), "tt", 1);
		
		if (!dataTable.isColumnGrouped())
			addTH(Messages.instance().get("global.total"), "tt-col", 1);
		html.append("</tr>");
		
		TableItem rowGroup = null;
		// include rows
		for (Row row: dataTable.getRows()) {
			if ((dataTable.isRowGrouped()) && (rowGroup != row.getParent())) {
				addRow((Row)row.getParent(), "-grp");
				rowGroup = row.getParent();
			}

			addRow(row, "");
		}
		
		// include total line
		addRow(dataTable.getRowTotal(), "-tot");
		
		html.append("</table>");

		return html.toString();
	}

	
	/**
	 * Add a row to the table
	 * @param row
	 * @param th
	 */
	protected void addRow(Row row, String styleSuffix) {
		html.append("<tr>");
		addTD(row.getTitle(), "tt-row" + styleSuffix);
		
		for (TableItem col: dataTable.getColumns()) {
			Long val = row.getColumnValue(col);
			String s = (val != null? numberFormatter.format(val): null);
			 addTD(s, "vl" + styleSuffix);
		}
		long total = row.getTotal();
		addTD(total != 0? numberFormatter.format(total): null, "vl-tot-v" + styleSuffix);
		html.append("</tr>");
	}
	
	/**
	 * Add a TD tag to the HTML code
	 * @param s
	 */
	private void addTD(String s, String styleClass) {
		if (styleClass != null)
			 html.append("<td class='" + styleClass + "'>");
		else html.append("<td>");
		if (s != null)
			html.append(escapeString(s));
		html.append("</td>");
	}

	
	/**
	 * Add a TH tag to the HTML code
	 * @param s
	 */
	private void addTH(String s, String styleClass, int span) {
		html.append("<th");
		if (styleClass != null)
			html.append(" class='" + styleClass + "'");
		if (span > 1)
			html.append(" colspan='" + Integer.toString(span) + "'");
		html.append(">");
		if (s != null)
			html.append(escapeString(s));
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
	 * Create the HTML code with the table TH tags with the columns grouping 
	 */
	protected void createGroupingColumns() {
		if (!dataTable.isColumnGrouped())
			return;

		html.append("<tr>");
		for (TableItem grp: dataTable.getGroupColumns()) {
			addGroupingHeader(grp.getTitle(), grp.getChildren().size());
		}
		html.append("<th rowspan='2' class='tt-col-grp'>" + Messages.instance().get("global.total") + "</th>");
		html.append("</tr>");
	}

	
	/**
	 * Create the HTML code of the grouped header
	 * @param data
	 * @param span
	 */
	private void addGroupingHeader(String title, int span) {
		html.append("<th");
		if (span > 1)
			html.append(" colspan='" + Integer.toString(span) + "'");
		html.append('>');

		html.append( escapeString(title));
		html.append("</th>");
	}



	@Override
	public void clear() {
		reportQuery = null;
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
	 * @return the groupRowVariable
	 */
	public Variable getGroupRowVariable() {
		return groupRowVariable;
	}


	/**
	 * @param groupRowVariable the groupRowVariable to set
	 */
	public void setGroupRowVariable(Variable groupRowVariable) {
		this.groupRowVariable = groupRowVariable;
	}


	/**
	 * @return the dataTable
	 */
	public DataTableTransform getDataTable() {
		return dataTable;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
