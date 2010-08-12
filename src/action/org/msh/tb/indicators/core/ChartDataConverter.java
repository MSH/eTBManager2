package org.msh.tb.indicators.core;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;

@Name("chartDataConverter")
@org.jboss.seam.annotations.faces.Converter(id="chartDataConverter")
@BypassInterceptors
public class ChartDataConverter implements Converter{

//	@In(create=true) Map<String, String> messages;
//	@In(create=true) IndicatorFilters indicatorFilters;
	
	public Object getAsObject(FacesContext facesContext, UIComponent comp, String txt) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		
		if (!(obj instanceof IndicatorSeries))
			return null;
		
		IndicatorSeries series = (IndicatorSeries)obj;
		
//		String dsc = "";
//		String vals = "";
//		int max = 0;
//		int steps = 1;
		
		UIParameter param = findParam(comp, "title");
		String title;
		if (param != null)
			 title = param.getValue().toString();
		else title = "No title";
	
		Map<String, String> messages = Messages.instance();
		IndicatorFilters indicatorFilters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		
		JSONDataRender render = new JSONDataRender();
		render.setChartType(indicatorFilters.getChartType());
		render.setSeries(series);
		render.setTopTitle(messages.get("manag.forecast.numcasesreg"));
		render.setTitle(title);
		
		String s = render.render();
		return s;
	
/*		for (IndicatorItem item: series.getItems()) {
			if (!dsc.isEmpty()) {
				dsc = "," + dsc;
				vals = vals + ",";
			}
			dsc = "\"" + item.getKey() + "\"" + dsc;
			vals += "{\"right\":" + item.getValue() + "}";
			
			if (item.getValue() > max)
				max = item.getValue();
		}
		
		if (max > 10)
			steps = Math.round(max / 5) + 1;
		
		return "{ " +
		  "\"title\":{ \"text\":\"" + title + "\",  \"style\":\"{font-size: 18px; font-family: Verdana; text-align: center;}\"	  }, " +
		  "\"elements\":[   {     \"type\":      \"hbar\",     \"tip\":       \"#val#\", " + 
		     " \"colour\":    \"#3D829D\", " + 
		     " \"text\":      \"" + messages.get("manag.forecast.numcasesreg") + "\", " + 
		     " \"font-size\": 12, " + 
		    "  \"values\" :   [" + vals + "] " + 
		   " } " + 
		  "], " + 
		  "\"x_axis\":{ " +
		    "\"grid-colour\": \"#c0c0c0\"," + 
		    "\"min\":    0, " + 
		    "\"max\":    " + max + ", " + 
		    "\"offset\": false, " +
		    "\"steps\": " + steps +  
		  "}, " + 
		  "\"y_axis\":{ " + 
		    "\"grid-colour\": \"#c0c0c0\"," + 
		    "\"offset\":      true, " + 
		    "\"labels\": [" + dsc + "] " + 
		  "}, " + 
		  "\"bg_colour\": \"#fafcfa\", " + 
		  "\"tooltip\":{ " + 
		   " \"mouse\": 1 " + 
		  "} " + 
		"};";
*/	}

	public UIParameter findParam(UIComponent comp, String pname) {
		for (UIComponent c: comp.getChildren()) {
			if ((c instanceof UIParameter) && (((UIParameter)c).getName().equals(pname))) {
				return (UIParameter)c;
			}
		}
		return null;
	}
}
