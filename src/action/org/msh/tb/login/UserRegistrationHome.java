package org.msh.tb.login;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.msh.mdrtb.entities.SystemConfig;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.enums.UserState;
import org.msh.mdrtb.entities.enums.UserView;
import org.msh.tb.misc.DmSystemHome;
import org.msh.utils.Passwords;


@Name("userRegistrationHome")
public class UserRegistrationHome {

	@In EntityManager entityManager;
	@In(create=true) SystemConfig systemConfig;
	@In(create=true) Map<String, String> messages;
	@In(create=true) DmSystemHome dmsystem;
	
	private User user;
	
	public User getUser() {
		if (user == null)
			user = new User();
		return user;
	}
	
	public String register() {
		UserWorkspace userWs = new UserWorkspace();
		userWs.setPlayOtherUnits(false);
		userWs.setProfile(systemConfig.getUserProfile());
		userWs.setTbunit(systemConfig.getTbunit());
		userWs.setUser(user);
		userWs.setView(UserView.COUNTRY);
		userWs.setWorkspace(systemConfig.getWorkspace());

		user.setLogin(user.getLogin().toUpperCase());
		user.getWorkspaces().add(userWs);
		user.setRegistrationDate(new Date());
		user.setState(UserState.ACTIVE);

		String password = Passwords.generateNewPassword();
		user.setPassword(Passwords.hashPassword(password));
		Contexts.getEventContext().set("password", password);
		Contexts.getEventContext().set("user", user);
		
		entityManager.persist(user);
		entityManager.flush();
		
		dmsystem.enviarEmail("newuser.xhtml");
		
		return "success";
	}


	/**
	 * Valida a conta do usuário
	 * @param context
	 * @param compConta
	 * @param value
	 */
	public void validaConta(FacesContext context, UIComponent compConta, Object value) {
		String conta = (String) value;

		if ((conta.indexOf(" ") >= 0) || (conta.length() < 4)) {
			((UIInput)compConta).setValid(false);

			FacesMessage message = new FacesMessage(messages.get("admin.users.reqlogin"));
			context.addMessage(compConta.getClientId(context), message);			
		}
		
		List<User> lst = entityManager
				.createQuery("from User u where u.login = :conta")
				.setParameter("conta", conta.toUpperCase())
				.getResultList();

		if (lst.size() > 0) {
			((UIInput)compConta).setValid(false);

			FacesMessage message = new FacesMessage(messages.get("admin.users.uniquelogin"));
			context.addMessage(compConta.getClientId(context), message);
		}
	}

}
