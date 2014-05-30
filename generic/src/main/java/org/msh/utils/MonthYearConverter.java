package org.msh.utils;

import java.text.DateFormatSymbols;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.LocaleSelector;

@Name("monthYearConverter")
@org.jboss.seam.annotations.faces.Converter(id="monthYearConverter")
@BypassInterceptors
public class MonthYearConverter implements Converter {

	public Object getAsObject(FacesContext facesContext, UIComponent comp, String txt) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		Integer month = (Integer)obj;
		if (month == null)
			return null;

		Locale locale = LocaleSelector.instance().getLocale();
		DateFormatSymbols symbols = new DateFormatSymbols(locale);
		String smonth = symbols.getShortMonths()[month];

		Integer year = null;
		UIParameter paramYear = findParam(comp, "year");
		if (paramYear != null)
			year = (Integer)paramYear.getValue();

		if (year != null)
			smonth += '-' + year.toString();
		
		return smonth;
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
