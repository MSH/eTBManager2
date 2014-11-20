package org.msh.tb.indicators.core;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

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
		
		if ((obj == null) || (!(obj instanceof IndicatorSeries)))
			return null;
		
		IndicatorSeries series = (IndicatorSeries)obj;
		
//		String dsc = "";
//		String vals = "";
//		int max = 0;
//		int steps = 1;
		
		UIParameter param = findParam(comp, "title");
		String title;
		if ((param != null) && (param.getValue() != null))
			 title = param.getValue().toString();
		else title = "No title";
		
		UIParameter param2 = findParam(comp,"title2");
		String title2;
		if ((param2 != null) && (param2.getValue() != null))
			title2 = param2.getValue().toString();
		else title2 = "Number of cases";
	
		IndicatorFilters indicatorFilters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		
		JSONDataRender render = new JSONDataRender();
		render.setChartType(indicatorFilters.getChartType());
		render.setSeries(series);
		//render.setTopTitle(messages.get("manag.forecast.numcasesreg"));
		render.setTopTitle(title2);
		render.setTitle(title);
		
		String s = render.render();
		return s;
	}

	public UIParameter findParam(UIComponent comp, String pname) {
		for (UIComponent c: comp.getChildren()) {
			if ((c instanceof UIParameter) && (((UIParameter)c).getName().equals(pname))) {
				return (UIParameter)c;
			}
		}
		return null;
	}
}
