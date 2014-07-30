package org.msh.tb.client.tableview;

import org.msh.tb.client.reports.ReportUtils;
import org.msh.tb.client.shared.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Store in-memory data about the table and its information
 * 
 * @author Ricardo Memoria
 *
 */
public class TableData {

	public static int CELL_TOTAL = -2;
	public static int CELL_TITLE = -1;

    /**
     * Indicator assigned to this table data
     */
    private CIndicator indicator;

	/**
	 * Table sent from the server
	 */
	private CIndicatorResponse table;
	/**
	 * Variables that compound the column of the table
	 */
	private List<CVariable> colVariables;

	/**
	 * Variables that compound the rows of the table
	 */
	private List<CVariable> rowVariables;
	/**
	 * List of columns that compound the header by row
	 */
	private List<List<CTableColumn>> rowsHeader;
	/**
	 * Indicate the maximum level of the row, i.e, the leaf level
	 */
	private int rowMaxLevel;
	/**
	 * Row containing total values. It's null if there is no total column
	 */
	private double[] totalRow;
	/**
	 * Column containing total values. It's null if there is no total column
	 */
	private double[] totalColumn;

    /**
     * Column with percentage values of each column in relation to the total
     */
    private double[] colPercentages;

    /**
     * Column with percentage values of each row in relation to the total
     */
    private double[] rowPercentages;

    /**
     * Sum of all values of the table
     */
    private double total;


    /**
     * Return a value of the table
     * @param c column index
     * @param r row index
     * @return double value
     */
    public double getValue(int c, int r) {
        CTableRow row = getTable().getRows().get(r);
        return row.getValues()[c];
    }

	/**
	 * Return the variable assigned to the column
	 * @param col is the column of the table
	 * @return instance of the {@link CVariable} assigned to the column
	 */
	public CVariable getVariable(CTableColumn col) {
		int index = col.getLevel();
		return colVariables.get(table.getColVarIndex()[index]);
	}

	/**
	 * Return the column of the last header row in the column position indicated by 
	 * the index argument
	 * @param index is the order of the column in the list of columns
	 * @return instance of {@link CTableColumn} representing the column
	 */
	public CTableColumn getColumn(int index) {
		return getHeaderColumns().get(index);
	}

	/**
	 * Return the number of columns in the table
	 * @return integer value
	 */
	public int getColumnCount() {
		return getHeaderColumns().size();
	}
	
	
	/**
	 * Return the number of rows in the table
	 * @return integer value
	 */
	public int getRowCount() {
		return table.getRows().size();
	}
	
	/**
	 * Return the last level of the columns of the header
	 * @return
	 */
	public List<CTableColumn> getHeaderColumns() {
		return rowsHeader.get(rowsHeader.size() - 1);
	}


	/**
	 * Return the name of the variable followed by its selection for the column
	 * referenced by index and its parent columns
	 * @param index
	 * @return
	 */
	public String getColumnDisplaySelection(int index) {
		CTableColumn col = getColumn(index);

		String res = "";
		String group = null;
		String name = null;

		CVariable prevVar = null;
		while (col != null) {
			CVariable var = getVariable(col);
			if (var == prevVar) {
				name = col.getTitle() + "-" + name;
			}
			else {
				if (prevVar != null)
					res = group + ": " + name + (res.isEmpty()? "": ", ") + res;
				
				group = var.getName();
				name = col.getTitle();
			}
			
			prevVar = var;
			col = col.getParent();
			
			if (col == null)
				res = group + ": " + name + (res.isEmpty()? "": ", ") + res;
		}
		
		return res;
	}
	
	
	/**
	 * Update the data with an instance of the {@link org.msh.tb.client.shared.model.CIndicatorResponse} containing
	 * data for a new report
	 * @param table contains data of a report
	 */
	public void update(CIndicator indicator, CIndicatorResponse table) {
        this.indicator = indicator;

        updateVariables(indicator);
		// initialize list of columns per row
		List<List<CTableColumn>> columnRows = new ArrayList<List<CTableColumn>>();
		rowsHeader = columnRows;

		this.table = table;
		
		for (int i = 0; i < table.getColVarIndex().length; i++)
			columnRows.add(new ArrayList<CTableColumn>());

		// update the parent relationship
		for (CTableColumn col: table.getColumns())
			updateChildColumn(col, 0);

		// calculate the maximum level of each row title
		rowMaxLevel = 0;
		for (CTableRow row: table.getRows())
			if (row.getLevel() > rowMaxLevel)
				rowMaxLevel = row.getLevel();
		
		// update total
		updateTotal();
	}

    /**
     * Update the variables by replacing its IDs by the CVariable instance
     * @param indicator instance of CIndicator
     */
    private void updateVariables(CIndicator indicator) {
        if (indicator.getColVariablesCount() > 0) {
            colVariables = new ArrayList<CVariable>();
            convertVarIds(indicator.getColVariables(), colVariables);
        }

        if (indicator.getRowVariablesCount() > 0) {
            rowVariables = new ArrayList<CVariable>();
            convertVarIds(indicator.getRowVariables(), rowVariables);
        }
    }

    /**
     * Convert variables IDs to CVariable instances
     * @param ids list of string ids
     * @param vars list of CVariable instance
     */
    private void convertVarIds(List<String> ids, List<CVariable> vars) {
        for (String id: ids) {
            CVariable var = ReportUtils.findVariableById(id);
            if (var != null) {
                vars.add(var);
            }
        }
    }


    /**
	 * Update the total values displayed in the table
	 */
	protected void updateTotal() {
        total = 0;

		// update column total
		if (table.isColTotalAvailable()) {
			totalColumn = new double[table.getRows().size()];
			int index = 0;
			for (CTableRow row: table.getRows()) {
				for (Double val: row.getValues()) {
					if (val != null) {
                        totalColumn[index] += val;
                        total += val;
                    }
				}
				index++;
			}
		}
		else totalColumn = null;

		// update row total
		if (table.isRowTotalAvailable()) {
			totalRow = new double[getColumnCount()];
			for (CTableRow row: table.getRows()) {
				if (row.getLevel() == 0) {
					int index = 0;
					for (Double val: row.getValues()) {
						if (val != null) 
							totalRow[index] += val;
						index++;
					}
				}
			}
		}
		else totalRow = null;

        colPercentages = null;
        rowPercentages = null;
	}


    /**
     * Return the column percentages. If not calculated, calculate it on time
     * @return array of column percentages
     */
    protected double[] getColumnPerc() {
        if (!table.isColTotalAvailable()) {
            return null;
        }

        if (colPercentages == null) {
            colPercentages = new double[getColumnCount()];
            if (total > 0) {
                int index = 0;
                for (double val: totalColumn) {
                    colPercentages[index] = val/total;
                }
            }
        }
        return colPercentages;
    }

	/**
	 * Update the parent link of the child columns using recursion
	 * @param col
	 */
	protected void updateChildColumn(CTableColumn col, int level) {
		rowsHeader.get(level).add(col);

		if (col.getColumns() == null)
			return;

		// update children
		for (CTableColumn aux: col.getColumns()) {
			aux.setParent(col);
			updateChildColumn(aux, level + 1);
		}
	}


	/**
	 * Return the number of rows that compound the column headers of the table
	 * @return the colHeaderSize
	 */
	public int getColHeaderSize() {
		return table.getColVarIndex().length;
	}

	/**
	 * Check if the columns are grouped in more than 1 variable
	 * @return true if the columns are grouped
	 */
	public boolean isColumnGrouped() {
		return (colVariables.size() > 1);
	}
	
	/**
	 * Check if rows are grouped in more than 1 variable
	 * @return true if rows are grouped
	 */
	public boolean isRowGrouped() {
		return (rowVariables.size() > 1);
	}
	
	/**
	 * Mount the list of header labels of the columns containing the title and group title, if available
	 * @return list of objects of {@link HeaderLabel} class
	 */
	public List<HeaderLabel> mountColumnHeaderLabels() {
		List<CTableColumn> cols = rowsHeader.get(rowsHeader.size() - 1);
		List<HeaderLabel> lst = new ArrayList<TableData.HeaderLabel>();

		int[] varsIndex = table.getColVarIndex();

		int varItemIndex = varsIndex[varsIndex.length - 1];

		// create list of item and group labels
		for (CTableColumn col: cols) {
			CTableColumn aux = col;
			String itemTitle = "";
			String groupTitle = "";
			int index = varsIndex.length - 1;

			while (aux != null) {
				// is the same variable as in the item label ?
				if (varItemIndex == varsIndex[index]) {
					// mount the item label
					if (!itemTitle.isEmpty())
						itemTitle += "-";
					itemTitle += aux.getTitle();
				}
				else {
					// mount the group label
					if (!groupTitle.isEmpty())
						groupTitle += "-";
					groupTitle += aux.getTitle();
				}
				aux = aux.getParent();
				index--;
			}
			
			lst.add(new HeaderLabel(itemTitle, groupTitle));
		}
		
		return lst;
	}

	
	/**
	 * Mount the list of headers for the rows
	 * @return
	 */
	public List<HeaderLabel> mountRowHeaderLabels() {
		// get the variable index of the item rows
		int itemVarIndex = 0;
		for (CTableRow row: table.getRows())
			if (row.getLevel() == rowMaxLevel) {
				itemVarIndex = row.getVarIndex();
				break;
			}
		
		List<HeaderLabel> lst = new ArrayList<TableData.HeaderLabel>();
		String groupName = "";
		String itemName1 = "";
		for (CTableRow row: table.getRows()) {
			if (row.getLevel() == rowMaxLevel) {
				String name = row.getTitle();
				if (!itemName1.isEmpty())
					name = itemName1 + "-" + name;
				HeaderLabel lbl = new HeaderLabel(name, groupName);
				lbl.setRow(row);
				lst.add(lbl);
			}
			else {
				if (row.getVarIndex() == itemVarIndex)
					itemName1 = row.getTitle();
				else {
					if (row.getLevel() == 0)
						groupName = row.getTitle();
					else groupName += "-" + row.getTitle();
				}
			}
		}
		
		return lst;
	}
	
	/**
	 * @return the table
	 */
	public CIndicatorResponse getTable() {
		return table;
	}
	/**
	 * @return the colVariables
	 */
	public List<CVariable> getColVariables() {
		return colVariables;
	}
	/**
	 * @param colVariables the colVariables to set
	 */
	public void setColVariables(List<CVariable> colVariables) {
		this.colVariables = colVariables;
	}
	/**
	 * @return the rowVariables
	 */
	public List<CVariable> getRowVariables() {
		return rowVariables;
	}
	/**
	 * @param rowVariables the rowVariables to set
	 */
	public void setRowVariables(List<CVariable> rowVariables) {
		this.rowVariables = rowVariables;
	}
	/**
	 * @return the rowsHeader
	 */
	public List<List<CTableColumn>> getRowsHeader() {
		return rowsHeader;
	}
	/**
	 * @param rowsHeader the rowsHeader to set
	 */
	public void setRowsHeader(List<List<CTableColumn>> rowsHeader) {
		this.rowsHeader = rowsHeader;
	}

	
	/**
	 * Store temporary information about the header names
	 * @author Ricardo Memoria
	 *
	 */
	public class HeaderLabel {
		private String itemLabel;
		private String groupLabel;
		private CTableRow row;
		public HeaderLabel(String itemLabel, String groupLabel) {
			super();
			this.itemLabel = itemLabel;
			this.groupLabel = groupLabel;
		}
		/**
		 * @return the itemLabel
		 */
		public String getItemLabel() {
			return itemLabel;
		}
		/**
		 * @return the groupLabel
		 */
		public String getGroupLabel() {
			return groupLabel;
		}
		/**
		 * @return the row
		 */
		public CTableRow getRow() {
			return row;
		}
		/**
		 * @param row the row to set
		 */
		public void setRow(CTableRow row) {
			this.row = row;
		}
		
	}


	/**
	 * @return the rowMaxLevel
	 */
	public int getRowMaxLevel() {
		return rowMaxLevel;
	}

	/**
	 * @param rowMaxLevel the rowMaxLevel to set
	 */
	public void setRowMaxLevel(int rowMaxLevel) {
		this.rowMaxLevel = rowMaxLevel;
	}

	/**
	 * @return the totalRow
	 */
	public double[] getTotalRow() {
		return totalRow;
	}

	/**
	 * @return the totalColumn
	 */
	public double[] getTotalColumn() {
		return totalColumn;
	}

    /**
     * Return the indicator in use
     * @return instance of CIndicator
     */
    public CIndicator getIndicator() {
        return indicator;
    }

    /**
     * Return the sum of all values in the table
     * @return
     */
    public double getTotal() {
        return total;
    }
}
