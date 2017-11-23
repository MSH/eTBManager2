package org.msh.utils.reportgen.highchart;

/**
 * Char type for highchart
 * @author Ricardo Memoria
 *
 */
public enum ChartType {
	
	 LINE("line"), 
	 SPLINE("spline"), 
	 AREA("area"), 
	 AREASPLINE("areaspline"), 
	 COLUMN("column"), 
	 BAR("bar"), 
	 PIE("pie"), 
	 SCATTER("scatter");

	 private final String jsonName;
	 
	 private ChartType(String jsonName) {
		 this.jsonName = jsonName;
	 }
	 
	 public String getJsonName() {
		 return jsonName;
	 }
}
