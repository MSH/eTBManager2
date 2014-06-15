/**
 * 
 */
package org.msh.tb.client.shared.model;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contain information about an indicator to be displayed in the page
 * 
 * @author Ricardo Memoria
 *
 */
public class CIndicator implements IsSerializable {

	private String title;
	private Integer chartType;
	private CReportResponse reportResponse;
	private ArrayList<CVariable> colVariables;
	private ArrayList<CVariable> rowVariables;

	/**
	 * @return the reportResponse
	 */
	public CReportResponse getReportResponse() {
		return reportResponse;
	}
	/**
	 * @param reportResponse the reportResponse to set
	 */
	public void setReportResponse(CReportResponse reportResponse) {
		this.reportResponse = reportResponse;
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
	public ArrayList<CVariable> getColVariables() {
		return colVariables;
	}
	/**
	 * @param colVariables the colVariables to set
	 */
	public void setColVariables(ArrayList<CVariable> colVariables) {
		this.colVariables = colVariables;
	}
	/**
	 * @return the rowVariables
	 */
	public ArrayList<CVariable> getRowVariables() {
		return rowVariables;
	}
	/**
	 * @param rowVariables the rowVariables to set
	 */
	public void setRowVariables(ArrayList<CVariable> rowVariables) {
		this.rowVariables = rowVariables;
	}
}
