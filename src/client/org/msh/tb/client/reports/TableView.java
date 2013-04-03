package org.msh.tb.client.reports;

import java.util.List;

import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;
import org.msh.tb.client.shared.model.CVariable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

public class TableView extends Composite{

	private FlexTable table;
	private TableData data;
	
	public TableView() {
		table = new FlexTable();
		table.setStyleName("table2");

		table.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Cell cell = table.getCellForEvent(event);
				tableCellClick(cell.getCellIndex(), cell.getRowIndex());
			}
		});
		
		initWidget(table);
	}


	/**
	 * Update the data of the table
	 * @param tableData
	 * @param rowVars
	 * @param colVars
	 */
	public void update(TableData tableData) {
		if (tableData == null)
			return;

		this.data = tableData;
		table.removeAllRows();
		table.setVisible(true);

		// create header
		int r = 1;
		// calculate the number of columns
		int numCols = 0;
		for (List<CTableColumn> lst: tableData.getRowsHeader()) {
			int c = 0;
			for (CTableColumn col: lst) {
				table.setText(r, c, col.getTitle());
				table.getCellFormatter().setStyleName(r, c, "tt-row-tot");
				table.getCellFormatter().setAlignment(r, c, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
				int span = col.getSpan();
				if (span > 1) {
					table.getFlexCellFormatter().setColSpan(r, c, span);
				}

				if (r == 1)
					numCols += span;
				c++;
			}
			r++;
		}

		// set column header titles
		String headerHtml = "";
		for (CVariable var: tableData.getColVariables()) {
			if (!headerHtml.isEmpty())
				headerHtml += " / ";
			headerHtml += var.getName();
		}
		table.setText(0, 1, headerHtml);
		table.getFlexCellFormatter().setColSpan(0, 1, numCols);
		table.getCellFormatter().setStyleName(0, 1, "tt-row-tot");
		table.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		
		int colheadersize = tableData.getColHeaderSize();
		
		// set row header titles
		headerHtml = "";
		for (CVariable var: tableData.getRowVariables()) {
			if (!headerHtml.isEmpty())
				headerHtml += "<br/>";
			headerHtml += var.getName();
		}
		table.setHTML(0, 0, headerHtml);
		table.getFlexCellFormatter().setRowSpan(0, 0, colheadersize + 1);
		table.getCellFormatter().setStyleName(0, 0, "tt-row-tot");
		table.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);

		// get the maximum level of the rows
		int maxlevel = tableData.getRowMaxLevel();
		
		// create rows
		r = colheadersize + 1;
		for (CTableRow row: tableData.getTable().getRows()) {
			table.setText(r, 0, row.getTitle());
			String s;
			if (row.getLevel() != maxlevel)
				 s = "tt-row-grp";
			else s = "tt-row";
			table.getCellFormatter().setStyleName(r, 0, s);
			
			int c = 1;
			NumberFormat nf = NumberFormat.getDecimalFormat();
			for (Double val: row.getValues()) {
				table.setText(r, c, val != null? nf.format(val): "");
				if (row.getLevel() != maxlevel)
					 s = "vl-grp";
				else s = "vl";
				table.getCellFormatter().setStyleName(r,  c, s);
				table.getCellFormatter().setAlignment(r, c, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
				c++;
			}
			
			r++;
		}
	}

	

	/**
	 * Generates the chart according to the cell selected
	 * @param cellIndex
	 * @param rowIndex
	 */
	protected void tableCellClick(int col, int row) {
		// row title selection is just available if data is not grouped
		if ((!data.isRowGrouped()) && (!data.isColumnGrouped())) {
			// clicked on the row variable title ?
			if ((col == 0) && (row == 0)) {
				MainPage.instance().setSelectedRow(-1);
				return;
			}
			
			// clicked on the column variable title ?
			if ((col == 1) && (row == 0)) {
				MainPage.instance().setSelectedCol(-1);
				return;
			}
		}

		int colheadersize = data.getColHeaderSize();
		// row selected ?
		if (row == colheadersize)
			MainPage.instance().setSelectedCol(col);
		else
			if ((col == 0) && (row > colheadersize))
					MainPage.instance().setSelectedRow(row - colheadersize - 1);
	}

}
