package org.msh.tb.client.chart;

/**
 * Specify the chart types and its corresponding 
 * name in the highchart library
 * 
 * @author Ricardo Memoria
 *
 */
public enum ChartType {
	CHART_LINE ("line"), 
	CHART_SPLINE ("spline"), 
	CHART_AREA ("area"), 
	CHART_AREASPLINE ("areaspline"), 
	CHART_COLUMN ("column"), 
	CHART_BAR ("bar"), 
	CHART_PIE ("pie");
	
	private String name;

	private ChartType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}