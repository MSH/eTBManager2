package org.msh.tb.az.admin;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.UserWorkspace;

@Name("userAZControl")
public class UserAZControl{
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
	
	public boolean canViewJusticeFields(){
		UserWorkspace userWorkspace = (UserWorkspace) Component.getInstance("userWorkspace");
		if (userWorkspace.getHealthSystem()==null) return true;
		if (userWorkspace.getHealthSystem().getId()!=903) return true;
		return false;
	}
}
