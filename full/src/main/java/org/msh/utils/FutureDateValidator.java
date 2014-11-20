package org.msh.utils;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.msh.utils.date.DateUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Date;
import java.util.Map;

@Name("futureDateValidator")
@org.jboss.seam.annotations.faces.Validator(id="futureDateValidator")
@BypassInterceptors
public class FutureDateValidator implements Validator {

	public void validate(FacesContext facesContext, UIComponent comp, Object val)
			throws ValidatorException {
		Date dt = (Date)val;
		
		if (dt == null)
			return;

		// limit the minimum value of a date
		if (dt.before(DateUtils.newDate(1850, 1, 1))) {
			Map<String,String> messages = Messages.instance();
			
			FacesMessage message = new FacesMessage();
			message.setDetail(messages.get("javax.faces.component.UISelectOne.INVALID"));
			message.setSummary(messages.get("javax.faces.component.UISelectOne.INVALID"));
			message.setSeverity(FacesMessage.SEVERITY_ERROR); 
			throw new ValidatorException(message);
		}

		if (dt.after(new Date())) {
			Map<String,String> messages = Messages.instance();
			
			FacesMessage message = new FacesMessage();
			message.setDetail(messages.get("validator.notfuture"));
			message.setSummary(messages.get("validator.notfuture"));
			message.setSeverity(FacesMessage.SEVERITY_ERROR); 
			throw new ValidatorException(message);
		}
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
