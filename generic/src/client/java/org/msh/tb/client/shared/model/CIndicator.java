/**
 * 
 */
package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contain information about an indicator to be displayed in the page
 * 
 * @author Ricardo Memoria
 *
 */
public class CIndicator implements IsSerializable {

	private String title;
	private Integer chartType;
	private Integer tblSelection;
	private Integer tblSelectedCell;
	private ArrayList<String> colVariables;
	private ArrayList<String> rowVariables;
    private HashMap<String, String> filters;

    public HashMap<String, String> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, String> filters) {
        this.filters = filters;
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
	 * @return the chartType
	 */
	public Integer getChartType() {
		return chartType;
	}
	/**
	 * @param chartType the chartType to set
	 */
	public void setChartType(Integer chartType) {
		this.chartType = chartType;
	}
	/**
	 * @return the colVariables
	 */
	public ArrayList<String> getColVariables() {
		return colVariables;
	}
	/**
	 * @param colVariables the colVariables to set
	 */
	public void setColVariables(ArrayList<String> colVariables) {
		this.colVariables = colVariables;
	}
	/**
	 * @return the rowVariables
	 */
	public ArrayList<String> getRowVariables() {
		return rowVariables;
	}
	/**
	 * @param rowVariables the rowVariables to set
	 */
	public void setRowVariables(ArrayList<String> rowVariables) {
		this.rowVariables = rowVariables;
	}
	/**
	 * @return the tblSelection
	 */
	public Integer getTblSelection() {
		return tblSelection;
	}
	/**
	 * @param tblSelection the tblSelection to set
	 */
	public void setTblSelection(Integer tblSelection) {
		this.tblSelection = tblSelection;
	}
	/**
	 * @return the tblSelectedCell
	 */
	public Integer getTblSelectedCell() {
		return tblSelectedCell;
	}
	/**
	 * @param tblSelectedCell the tblSelectedCell to set
	 */
	public void setTblSelectedCell(Integer tblSelectedCell) {
		this.tblSelectedCell = tblSelectedCell;
	}
}
