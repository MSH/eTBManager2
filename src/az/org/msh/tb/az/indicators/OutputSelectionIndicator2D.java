package org.msh.tb.az.indicators;

import java.util.List;

import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableCell;
/**
 * Duplicated Indicator2D which extend OutputSelectionIndicator
 * */
public abstract class OutputSelectionIndicator2D extends OutputSelectionIndicator {
	private static final long serialVersionUID = -8780796708312443230L;

	private IndicatorTable table;


	/**
	 * Add a value to the table
	 * @param columnTitle title of the column
	 * @param rowTitle title of the row
	 * @param value value to be set
	 * @return {@link TableCell} instance containing the cell data
	 */
	public TableCell addValue(String columnTitle, String rowTitle, Float value) {
		return table.addValue(columnTitle, rowTitle, value);
	}


	public TableCell addIdValue(Object columnId, Object rowId, Float val) {
		TableCell cell = table.addIdValue(columnId, rowId, val);
		if (cell.getColumn().getTitle() == null)
			cell.getColumn().setTitle(translateKey(columnId));
		if (cell.getRow().getTitle() == null)
			cell.getRow().setTitle(translateKey(rowId));
		return cell;
	}

	public void addColValues(String colTitle, Object id, List<Object[]> values) {
		if (values.size() == 0) {
			getTable().addColumn(colTitle, id);
			return;
		}
		
		for (Object[] vals: values) {
			Float val = ((Long)vals[1]).floatValue();
			if (val > 0) {
				addValue(colTitle, translateKey(vals[0]), val);
			}
		}		
	}

	/**
	 * Add values to a row with title rowTitle and column values in values parameter
	 * @param rowTitle - title of the row to insert values into
	 * @param values - list of values for each column, where first value is the column title and second value is the cell value
	 */
	public void addRowValues(String rowTitle, Object id, List<Object[]> values) {
		if (values.size() == 0) {
			getTable().addRow(rowTitle, id);
			return;
		}
		
		for (Object[] vals: values) {
			Float val = ((Long)vals[1]).floatValue();
			if (val > 0) {
				addValue(translateKey(vals[0]), rowTitle, val);
			}
		}
	}

	@Override
	public void createItems(List<Object[]> lst) {
		for (Object[] vals: lst) {
			Float val = ((Long)vals[2]).floatValue();
			if (val > 0) {
				addIdValue(vals[0], vals[1], val);
			}
		}
	}
	
	
	/**
	 * Return the table containing the data
	 * @return {@link IndicatorTable} instance
	 */
	public IndicatorTable getTable() {
		if (table == null)
			createTable();
		return table;
	}
	

	
	/**
	 * Create the table and fill it with the indicator values
	 */
	protected void createTable() {
		setConsolidated(true);
		table = new IndicatorTable();
		createIndicators();
	}
}
