package org.msh.utils.date;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Name("localeDateConverter")
@BypassInterceptors
@org.jboss.seam.annotations.faces.Converter(id="localeDateConverter")
public class LocaleDateConverter implements Converter{

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		return null;
	}

	
	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		UIParameter aux = (UIParameter)comp.findComponent("type");
		
		boolean includeTime = ((aux != null) && (aux.getValue().toString().equals("date-time")));
		return getDisplayDate((Date)obj, includeTime);
	}

	
	/**
	 * Return a string representation of a date ready for displaying
	 * @param dt
	 * @return
	 */
	static public String getDisplayDate(Date dt, boolean includeTime) {
		String patt = Messages.instance().get("locale.outputDatePattern");
		if (includeTime)
			patt += " HH:mm:ss";
		Locale locale = LocaleSelector.instance().getLocale();
		SimpleDateFormat df = new SimpleDateFormat(patt, locale);
		
		return df.format(dt);
	}
}
