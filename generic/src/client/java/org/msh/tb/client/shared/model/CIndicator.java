/**
 * 
 */
package org.msh.tb.client.shared.model;

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
}
