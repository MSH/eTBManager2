package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Specify the chart types and its corresponding 
 * name in the highchart library
 * 
 * @author Ricardo Memoria
 *
 */
public enum CChartType implements IsSerializable {
	CHART_LINE ("line"), 
	CHART_SPLINE ("spline"), 
	CHART_AREA ("area"), 
	CHART_AREASPLINE ("areaspline"), 
	CHART_COLUMN ("column"), 
	CHART_BAR ("bar"), 
	CHART_PIE ("pie");
	
	private String name;

	private CChartType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}