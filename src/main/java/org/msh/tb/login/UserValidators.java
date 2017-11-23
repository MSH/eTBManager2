/**
 * 
 */
package org.msh.tb.login;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.App;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common validators used in user registration
 * @author Ricardo Memoria
 *
 */
@Name("userValidators")
public class UserValidators {
	
	/**
	 * Validate the user login
	 * @param context
	 * @param compConta
	 * @param value
	 */
	public void validateLogin(FacesContext context, UIComponent compConta, Object value, Integer userId) {
		String conta = (String) value;

		if ((conta.indexOf(" ") >= 0) || (conta.length() < 4)) {
			((UIInput)compConta).setValid(false);

			FacesMessage message = new FacesMessage(Messages.instance().get("admin.users.reqlogin"));
			context.addMessage(compConta.getClientId(context), message);			
		}
		
		String hql= "select count(u.id) from User u where u.login = :conta";
		if (userId != null)
			hql += " and u.id <> " + userId;
		
		Long count = (Long)App.getEntityManager()
				.createQuery(hql)
				.setParameter("conta", conta.toUpperCase())
				.getSingleResult();

		if (count > 0) {
			((UIInput)compConta).setValid(false);

			FacesMessage message = new FacesMessage(Messages.instance().get("admin.users.uniquelogin"));
			context.addMessage(compConta.getClientId(context), message);
		}
	}
	
	/**
	 * Validate the e-mail address entered in a JSF input text. The input text must use the
	 * validator property pointing to this method. If the e-mail is not valid, a {@link FacesMessage}
	 * will be generated to the component and the component will be invalidated
	 *  
	 * @param context instance of {@link FacesContext}
	 * @param comp instance of the {@link UIComponent} where e-mail was entered
	 * @param value object value entered by the user (string value)
	 */
	public void validateEmail(FacesContext context, UIComponent comp, Object value) {
		
		String email = (String)value;
		
		email = email.trim();

		Pattern p = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$");
		
		Matcher m = p.matcher(email);

		if (!m.matches()) {
			context.addMessage(comp.getClientId(context), new FacesMessage(Messages.instance().get("validator.email")));
			((UIInput)comp).setValid(false);
		}


	}

}
