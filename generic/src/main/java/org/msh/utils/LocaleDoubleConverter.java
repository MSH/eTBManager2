package org.msh.utils;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.LocaleSelector;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@Name("localeDoubleConverter")
@BypassInterceptors
@org.jboss.seam.annotations.faces.Converter(id="localeDoubleConverter")
public class LocaleDoubleConverter implements Converter {

	public Object getAsObject(FacesContext facesContext, UIComponent comp, String value) {
		if (value == null)
			return null;
		
		value = value.trim();
		
		Locale locale = LocaleSelector.instance().getLocale();
		try {
			double val = NumberFormat.getNumberInstance(locale).parse(value).doubleValue();
			return val;
		} catch (ParseException e) {
            throw new ConverterException();
		}
	}

	public String getAsString(FacesContext facesContext, UIComponent comp, Object value) {
		Locale locale = LocaleSelector.instance().getLocale();
		String res = NumberFormat.getNumberInstance(locale).format(value);
		return res;
	}

}
