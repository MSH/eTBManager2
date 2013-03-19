package org.msh.tb.ua.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.LocaleSelector;

@Name("numberConverter5D")
@org.jboss.seam.annotations.faces.Converter(id="currencyConverter5D")
@BypassInterceptors
public class NumberConverter5D implements Converter {

	public Object getAsObject(FacesContext facesContext, UIComponent comp, String txt) {
		Locale locale = LocaleSelector.instance().getLocale();
		NumberFormat df = DecimalFormat.getCurrencyInstance(locale);
		((DecimalFormat)df).applyPattern("#,###,###,##0.00000");

		Number val;
		try {
			val = df.parse(txt);
		} catch (ParseException e) {
			throw new ConverterException(e);
		}
		return val.floatValue();
	}

	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		Locale locale = LocaleSelector.instance().getLocale();
		NumberFormat df = DecimalFormat.getCurrencyInstance(locale);
		((DecimalFormat)df).applyPattern("#,###,###,##0.00000");

		Number val = (Number)obj;
		
		return df.format(val);
	}
}
