/**
 * 
 */
package org.msh.tb.client.chart;

import org.msh.tb.client.App;
import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;
import org.msh.tb.client.shared.model.CTableSelection;
import org.msh.tb.client.tableview.TableData;
import org.msh.tb.client.tableview.TableData.HeaderLabel;

import java.util.List;


/**
 * Update a chart based on the data from the report response
 * @author Ricardo Memoria
 *
 */
public class ChartReport {

	/**
	 * Update the given chart
	 * @param chart
	 * @param tableData
	 */
	public static void update(ChartView chart, TableData tableData) {
		if (tableData.getTable() == null)
			return;

		chart.clear();

		String title = tableData.getTable().getUnitTypeLabel();
		if (title == null)
			title = App.messages.numberOfCases();
		chart.setyAxisTitle(title);

		// is row selected ?
		if (tableData.getSelection() == CTableSelection.ROW) {
			// clicked on the position of the table 0,0 ?
			if (tableData.getSelectedCell() == TableData.CELL_TITLE) {
                if ((tableData.getRowVariables() != null) && (tableData.getColVariables() != null)) {
                    chart.setTitle(tableData.getRowVariables().get(0).getName() + " x " + tableData.getColVariables().get(0).getName());
                }
                else {
                    chart.setTitle("");
                }
				chart.setSubTitle(null);
				
				List<CTableRow> rows = tableData.getTable().getRows();
				for (CTableRow row: rows) {
					ChartSeries series = chart.addSeries(row.getTitle());
					for (int i = 0; i < row.getValues().length; i++) {
						series.addValue(tableData.getColumn(i).getTitle(), row.getValues()[i]);
					}
				}
			}
			else if (tableData.getSelectedCell() == TableData.CELL_TOTAL) {
				chart.setTitle(tableData.getColVariables().get(0).getName());
				chart.setSubTitle(App.messages.total());
				ChartSeries series = chart.addSeries(App.messages.total());
				int index = 0;
				for (double val: tableData.getTotalRow()) {
					series.addValue(tableData.getColumn(index++).getTitle(), val);
				}
			}
			else {
				// create title of the report
				CTableRow row = tableData.getTable().getRows().get(tableData.getSelectedCell());
				String colTitle = tableData.getColVariables().get(0).getName();
				chart.setTitle(App.messages.numberOfCasesBy() + " " + colTitle);
				chart.setSubTitle( tableData.getRowVariables().get(row.getVarIndex()).getName() + ": " + row.getTitle());

				// is grouped ?
				if (tableData.isColumnGrouped()) {
					List<HeaderLabel> labels = tableData.mountColumnHeaderLabels();
					
					String prevLabel = "";
					ChartSeries series = null;
					int index = 0;
					for (HeaderLabel label: labels) {
						if ((series == null) || (!label.getGroupLabel().equals(prevLabel))) {
							series = chart.addSeries(label.getGroupLabel());
							prevLabel = label.getGroupLabel();
						}
						series.addValue(label.getItemLabel(), row.getValues()[index]);
						index++;
					}
				}
				else {
					// handle single row selection
					List<CTableColumn> cols = tableData.getRowsHeader().get(0);
					ChartSeries series = chart.addSeries(row.getTitle());
					int i = 0;
					for (CTableColumn col: cols) {
						series.addValue(col.getTitle(), row.getValues()[i++]);
					}
				}
			}
		}
		else {
			// clicked on the position of the table 0,0 ?
			if (tableData.getSelectedCell() == TableData.CELL_TITLE) {
				chart.setTitle(tableData.getRowVariables().get(0).getName() + " x " + tableData.getColVariables().get(0).getName());
				chart.setSubTitle(null);
				
				List<CTableRow> rows = tableData.getTable().getRows();
				int colindex = 0;
				for (CTableColumn col: tableData.getHeaderColumns()) {
					ChartSeries series = chart.addSeries(col.getTitle());
					for (CTableRow row: rows) {
						series.addValue(row.getTitle(), row.getValues()[colindex]);
					}
					colindex++;
				}
			}
			else 
			if (tableData.getSelectedCell() == TableData.CELL_TOTAL) {
				chart.setTitle(tableData.getRowVariables().get(0).getName());
				chart.setSubTitle(App.messages.total());
				ChartSeries series = chart.addSeries(App.messages.total());
				int index = 0;
				for (double val: tableData.getTotalColumn()) {
					series.addValue(tableData.getTable().getRows().get(index++).getTitle(), val);
				}
			}
			else {
				// column was selected
				CTableColumn col = tableData.getColumn(tableData.getSelectedCell());
				String s = col.getTitle();
				
				String colTitle = tableData.getRowVariables().get(0).getName();
				chart.setTitle(App.messages.numberOfCasesBy() + " " + colTitle);
				chart.setSubTitle(tableData.getColumnDisplaySelection(tableData.getSelectedCell()));

				// the rows are grouped ?
				if (tableData.isRowGrouped()) {
					List<HeaderLabel> lst = tableData.mountRowHeaderLabels();

					ChartSeries series = null;
					String prevName = null;
					for (HeaderLabel lbl: lst) {
						if ((prevName == null) || (!prevName.equals(lbl.getGroupLabel()))) {
							series = chart.addSeries(lbl.getGroupLabel());
							prevName = lbl.getGroupLabel();
						}
						series.addValue(lbl.getItemLabel(), lbl.getRow().getValues()[tableData.getSelectedCell()]);
					}
				}
				else {
					// just one variable selected for the row
					ChartSeries series = chart.addSeries(s);
					for (CTableRow row: tableData.getTable().getRows()) {
						series.addValue(row.getTitle(), row.getValues()[tableData.getSelectedCell()]);
					}
				}
			}
		}

		chart.update();
	}
}
