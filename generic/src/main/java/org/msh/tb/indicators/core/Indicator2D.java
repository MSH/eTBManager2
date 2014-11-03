package org.msh.tb.indicators.core;

import org.msh.tb.indicators.core.IndicatorTable.TableCell;

import java.util.List;

/**
 * Base class supporting TB/MDR-TB indicator generation in a 2-dimension table
 * <br/>
 * A base class must be created and override the abstract method {@link #createIndicators()}
 * @author Ricardo Memoria
 *
 */
public abstract class Indicator2D extends Indicator {
	private static final long serialVersionUID = -1390002507473748173L;
	
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

    /**
     * Add a value to the table without checking if the column already exists, a new cell will always be created.
     * @param columnTitle title of the column
     * @param rowTitle title of the row
     * @param value value to be set
     * @return {@link TableCell} instance containing the cell data
     */
    public TableCell addValue(String columnTitle, String columnTitleDisplay, String rowTitle, Float value) {
        return table.addValue(columnTitle, columnTitleDisplay, rowTitle, value);
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
