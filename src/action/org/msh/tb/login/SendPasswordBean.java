package org.msh.tb.login;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.mdrtb.entities.User;
import org.msh.tb.misc.DmSystemHome;
import org.msh.utils.Passwords;


/**
 * Sends a new password to the user by its e-mail address
 * @author Ricardo
 *
 */
@Name("sendPassword")
public class SendPasswordBean extends Controller {
	private static final long serialVersionUID = 5770134096915738398L;
	
	private String email;
	private String password;
	private User user;
	
	@In(create=true) EntityManager entityManager;
	@In(create=true) DmSystemHome dmsystem;
	@In(create=true) FacesMessages facesMessages;
	
	public String execute() {
		List<User> users = entityManager.createQuery("from User u where u.email = :email")
			.setParameter("email", email)
			.getResultList();
		
		if (users.size() == 0) {
			facesMessages.addFromResourceBundle("login.noemail");
			return "error";
		}
		
		user = users.get(0);
		
		password = Passwords.generateNewPassword();
		user.setPassword(Passwords.hashPassword(password));
		Contexts.getEventContext().set("password", password);
		
		if (!dmsystem.enviarEmail("forgotpassword.xhtml")) {
			facesMessages.addFromResourceBundle("mail.fail", user.getEmail());
			return "error";
		}

		entityManager.persist(user);
		entityManager.flush();
		
		facesMessages.addFromResourceBundle("login.pwdsent", user.getEmail());
		
		return "success";
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public User getUser() {
		return user;
	}
}
