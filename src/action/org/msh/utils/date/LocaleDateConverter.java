package org.msh.utils.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;

@Name("localeDateConverter")
@BypassInterceptors
@org.jboss.seam.annotations.faces.Converter(id="localeDateConverter")
public class LocaleDateConverter implements Converter{

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		return null;
	}

	
	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		return getDisplayDate((Date)obj);
	}

	
	/**
	 * Return a string representation of a date ready for displaying
	 * @param dt
	 * @return
	 */
	static public String getDisplayDate(Date dt) {
		String patt = Messages.instance().get("locale.outputDatePattern");
		Locale locale = LocaleSelector.instance().getLocale();
		SimpleDateFormat df = new SimpleDateFormat(patt, locale);
		
		return df.format(dt);
	}
}
