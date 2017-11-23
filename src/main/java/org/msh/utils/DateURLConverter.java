package org.msh.utils;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.LocaleSelector;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Name("dateURLConverter")
@org.jboss.seam.annotations.faces.Converter(id="dateURLConverter")
@BypassInterceptors
public class DateURLConverter implements Converter {

	public Object getAsObject(FacesContext facesContext, UIComponent comp, String txt) {
		Locale locale = LocaleSelector.instance().getLocale();
		Calendar c = Calendar.getInstance(locale);

		int ano = Integer.parseInt(txt.substring(0, 4));
		int mes = Integer.parseInt(txt.substring(4, 6));
		int dia = Integer.parseInt(txt.substring(6, 8));
		c.set(Calendar.YEAR, ano);
		c.set(Calendar.MONTH, mes);
		c.set(Calendar.DAY_OF_MONTH, dia);
			
		return c.getTime();
	}

	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		Date dt = (Date)obj;
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		
		NumberFormat df = DecimalFormat.getCurrencyInstance();
		((DecimalFormat)df).applyPattern("0000");
		String s = df.format(c.get(Calendar.YEAR));
		
		((DecimalFormat)df).applyPattern("00");
		s += df.format(c.get(Calendar.MONTH)) + df.format(c.get(Calendar.DAY_OF_MONTH));
		
		return s;
	}
}
