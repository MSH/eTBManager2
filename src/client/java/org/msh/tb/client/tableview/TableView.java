package org.msh.tb.client.tableview;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.App;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;
import org.msh.tb.client.shared.model.CVariable;

import java.util.List;

/**
 * Display a table with the indicator data on that
 * 
 * @author Ricardo Memoria
 *
 */
public class TableView extends Composite {

    public enum Event { COL_CLICK, ROW_CLICK, CELL_CLICK };

	private static final String STYLE_HEADER_TITLE = "tt-row-tot";
	private static final String STYLE_VAL_TOTAL = "vl-tot-v";

    private StandardEventHandler eventHandler;

	private FlexTable table;
	private TableData data;

	public TableView() {
		table = new FlexTable();

		table.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HTMLTable.Cell cell = table.getCellForEvent(event);
				tableCellClick(cell.getCellIndex(), cell.getRowIndex());
			}
		});
		
		initWidget(table);
	}


	/**
	 * Update the data of the table
	 * @param tableData
     */
	public void update(TableData tableData) {
		if (tableData == null)
			return;
		
		if (table.getStyleName().isEmpty()) {
			table.setStyleName("table-indicator");
		}

		boolean totCol = tableData.getTable().isColTotalAvailable();
		boolean totRow = tableData.getTable().isRowTotalAvailable();
		
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
				setHoverText(r, c, col.getTitle());
				int span = col.getSpan();
				setCellStyle(r, c, STYLE_HEADER_TITLE, 1, span);
				if (r == 1)
					numCols += span;
				c++;
			}
			r++;
		}

		// set column header titles
		String headerHtml = "";
        if (tableData.getColVariables() != null) {
            for (CVariable var: tableData.getColVariables()) {
                if (!headerHtml.isEmpty())
                    headerHtml += " / ";
                headerHtml += var.getName();
            }
        }

		setHoverText(0,1, headerHtml);
		setCellStyle(0, 1, STYLE_HEADER_TITLE, 1, numCols);
		
		int colheadersize = tableData.getColHeaderSize();

		// include the total column, if necessary
		if (totCol) {
			// set the label of the total column
			setHoverText(0, 2, App.messages.total());
			setCellStyle(0, 2, STYLE_HEADER_TITLE, colheadersize + 1, 1);
		}

/*
        if (totRow) {
            setHoverText(0, 3, "%");
            setCellStyle(0, 3, STYLE_HEADER_TITLE, colheadersize + 1, 1);
        }
*/

		// set row header titles
		headerHtml = "";
		for (CVariable var: tableData.getRowVariables()) {
			if (!headerHtml.isEmpty())
				headerHtml += "<br/>";
			headerHtml += var.getName();
		}
		setHoverText(0, 0, headerHtml);
//		table.setHTML(0, 0, headerHtml);
		setCellStyle(0, 0, STYLE_HEADER_TITLE, colheadersize + 1, 1);

		// get the maximum level of the rows
		int maxlevel = tableData.getRowMaxLevel();

		// create rows
		NumberFormat nf = NumberFormat.getDecimalFormat();
		r = colheadersize + 1;
		for (CTableRow row: tableData.getTable().getRows()) {
			setHoverText(r, 0, row.getTitle());
			String s;
			if (row.getLevel() != maxlevel)
				 s = "tt-row-grp";
			else s = "tt-row";
			table.getCellFormatter().setStyleName(r, 0, s);
			
			int c = 1;
			for (Double val: row.getValues()) {
				if (val != null)
					 setHoverText(r, c, nf.format(val));
				else table.setText(r, c, "");
				
				if (row.getLevel() != maxlevel)
					 s = "vl-grp";
				else s = "vl";
				setCellStyle(r, c, s, 1, 1);

				c++;
			}

			// include the total column cell
			if (totCol) {
				table.setText(r, c, nf.format(tableData.getTotalColumn()[r - colheadersize - 1]));
				setCellStyle(r, c, STYLE_VAL_TOTAL, 1, 1);
			}
			
			r++;
		}
		
		// include the total row, if available
		if (totRow) {
			setHoverText(r, 0, App.messages.total());
			setCellStyle(r, 0, STYLE_HEADER_TITLE, 1, 1);

			int c = 1;
			double sum = 0;
			for (double val: tableData.getTotalRow()) {
				table.setText(r, c, nf.format(val));
				setCellStyle(r, c, STYLE_VAL_TOTAL, 1, 1);
				sum += val;
				c++;
			}
			if (totCol) {
				table.setText(r, c, nf.format(sum));
				setCellStyle(r, c, STYLE_VAL_TOTAL, 1, 1);
			}
		}

/*
        if (totCol) {
            setHoverText(r+1, 0, "%");
            setCellStyle(r+1, 0, STYLE_HEADER_TITLE, 1, 1);
        }
*/
	}


	/**
	 * Set a text in a cell of the table around a div tag to display a cell hover effect
	 * @param row
	 * @param col
	 * @param text
	 */
	private void setHoverText(int row, int col, String text) {
		table.setHTML(row, col, "<div class='cellhover'>" + text + "</div>");
	}

	/**
	 * Set the standard style of a cell. If there is no span for the row or the column,
	 * the value of 1 must be specified 
	 * @param row
	 * @param col
	 * @param styleClass
	 * @param rowSpan the span of the row, or 1 if no span must be applied
	 * @param colSpan the span of the column, or 1 if no span must be applied
	 */
	private void setCellStyle(int row, int col, String styleClass, int rowSpan, int colSpan) {
		if (rowSpan > 1)
			table.getFlexCellFormatter().setRowSpan(row, col, rowSpan);
		if (colSpan > 1)
			table.getFlexCellFormatter().setColSpan(row, col, colSpan);
		table.getCellFormatter().setStyleName(row, col, styleClass);
		table.getCellFormatter().setAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
	}


    /**
     * Notify the event handler about an event that happened in the table
     * @param event the table event
     * @param data extra information included in the event
     */
    private void fireTableEvent(Event event, Object data) {
        if (eventHandler != null) {
            eventHandler.handleEvent(event, data);
        }
    }


	/**
	 * Generates the chart according to the cell selected
	 * @param col
	 * @param row
	 */
	protected void tableCellClick(int col, int row) {
		int colheadersize = data.getColHeaderSize();
		
		int rowcount = data.getRowCount();
		int colcount = data.getHeaderColumns().size();
		
		// is total column cell ?
		if ((row == 0) && (col == 2)) {
            fireTableEvent(Event.COL_CLICK, TableData.CELL_TOTAL);
			return;
		}

		// is total column row ?
		if ((col == 0) && (row == rowcount + colheadersize + 1)) {
            fireTableEvent(Event.ROW_CLICK, TableData.CELL_TOTAL);
			return;
		}

		if ((row - colheadersize > rowcount) || (col > colcount)) {
			return;
		}

		// is cell containing value
		if ((row > colheadersize) && (col > 0)) {
            fireTableEvent(Event.CELL_CLICK, new Cell(col -1, row - colheadersize - 1));
		}
		
		// row title selection is just available if data is not grouped
		if ((!data.isRowGrouped()) && (!data.isColumnGrouped())) {
			// clicked on the row variable title ?
			if ((col == 0) && (row == 0)) {
                fireTableEvent(Event.ROW_CLICK, -1);
//				ReportMain.instance().setSelectedRow(-1);
				return;
			}
			
			// clicked on the column variable title ?
			if ((col == 1) && (row == 0)) {
                fireTableEvent(Event.COL_CLICK, -1);
				return;
			}
		}

		// row selected ?
		if (row == colheadersize) {
            fireTableEvent(Event.COL_CLICK, col);
        }
		else
			if ((col == 0) && (row > colheadersize)) {
                fireTableEvent(Event.ROW_CLICK, row - colheadersize - 1);
            }
	}

    public StandardEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(StandardEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}
