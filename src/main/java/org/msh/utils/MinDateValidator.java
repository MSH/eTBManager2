package org.msh.utils;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Name("minDateValidator")
@org.jboss.seam.annotations.faces.Validator(id="minDateValidator")
@BypassInterceptors
public class MinDateValidator implements Validator {

	public void validate(FacesContext facesContext, UIComponent comp, Object val) throws ValidatorException {
		Date dt = (Date)val;
		
		Calendar minDate  = Calendar.getInstance();
		minDate.set(Calendar.DAY_OF_MONTH, 1);
		minDate.set(Calendar.MONTH, Calendar.JANUARY);
		minDate.set(Calendar.YEAR, 1900);
		
		if(minDate != null && dt != null && minDate.getTime().compareTo(dt) > 0 ){
			Map<String,String> messages = Messages.instance();
			
			FacesMessage message = new FacesMessage();
			message.setDetail(messages.get("cases.regimens.splitvalmsg"));
			message.setSummary(messages.get("cases.regimens.splitvalmsg"));
			message.setSeverity(FacesMessage.SEVERITY_ERROR); 
			throw new ValidatorException(message);
		}
	}

}
