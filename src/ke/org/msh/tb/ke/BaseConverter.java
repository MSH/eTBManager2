package org.msh.tb.ke;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;

@Name("baseConverter")
@BypassInterceptors
@org.jboss.seam.annotations.faces.Converter(id="baseConverter")
public class BaseConverter implements Converter{

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		return null;
	}
	
	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		String patt = Messages.instance().get("locale.outputDatePattern");
		SimpleDateFormat df = new SimpleDateFormat(patt);
		
		return df.format((Date)obj);
	}
}
