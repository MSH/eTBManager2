package org.msh.tb.ua;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;

@Name("userUAControl")
public class UserUAControl{
	@In(create=true) Map<String, String> messages;
	
	public void validateEmail(FacesContext context, UIComponent comp, Object value) {
		String email = (String)value;
		Pattern p = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$");
		Matcher m = p.matcher(email);
		
		if (!m.matches()){
			context.addMessage(comp.getClientId(context), new FacesMessage(getMessages().get("validator.email")));
			((UIInput)comp).setValid(false);
		}
	}
	protected Map<String, String> getMessages() {
		if (messages == null)
			messages = Messages.instance();
		return messages;
	}
}
