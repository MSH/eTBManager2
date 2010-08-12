package org.msh.utils;

import java.util.Date;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;

@Name("futureDateValidator")
@org.jboss.seam.annotations.faces.Validator(id="futureDateValidator")
@BypassInterceptors
public class FutureDateValidator implements Validator {

	public void validate(FacesContext facesContext, UIComponent comp, Object val)
			throws ValidatorException {
		UIParameter param = findParam(comp.getParent(), "validatefuture");
		if (param != null) {
			Object p = param.getValue();
			if ((p == null) || (p.equals("true")))
				return;
		}
		
		Date dt = (Date)val;

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
