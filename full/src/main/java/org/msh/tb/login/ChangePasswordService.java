package org.msh.tb.login;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.enums.UserState;
import org.msh.utils.UserUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.util.regex.Pattern;



@Name("changePasswordService")
public class ChangePasswordService {

	private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-zA-Z]).{6,20})";

    @In(create=true) FacesMessages facesMessages;
    @In(create=true) EntityManager entityManager; 

    @In(required=false)
    UserLogin userLogin;
	
	private String password;
    private String newPassword1;
    private String newPassword2;

	public String execute() {
		String pwd = UserUtils.hashPassword(password);
		
		User user = entityManager.find(User.class, userLogin.getUser().getId());

		// verifica se a senha atual � igual a senha do usu�rio
		if (!pwd.equalsIgnoreCase(user.getPassword())) {
			facesMessages.addFromResourceBundle("changepwd.wrongpass1");
			return "error";
		}
		
		return updatePassword();
	}

	
	/**
	 * Update the user password (doesn't check if current password)
	 * @return
	 */
	public String updatePassword() {
		// verifica se as 2 novas senhas s�o iguais
		if (!newPassword1.equals(newPassword2)) {
			facesMessages.addFromResourceBundle("changepwd.wrongpass2");
			return "error";
		}
		
		if (!validadePassword(newPassword1)) {
			facesMessages.addToControlFromResourceBundle("edtpassword1", "Invalid password");
			return "error";
		}

		User user = entityManager.find(User.class, userLogin.getUser().getId());
		
		if (user.getState() == UserState.BLOCKED)
			throw new RuntimeException("User is blocked");

		String pwd = UserUtils.hashPassword(newPassword1);
		user.setPassword(pwd);
		user.setState(UserState.ACTIVE);
		entityManager.persist(user);
		userLogin.setUser(user);
		
		entityManager.flush();
		
		return "pwdchanged";
	}
	
	
	public void passwordValidator(FacesContext context, UIComponent edtPassword, Object value) { 
		String password = (String) value;
		
		if ((password == null) || (!validadePassword(password))) {
			((UIInput)edtPassword).setValid(false);

			FacesMessage message = new FacesMessage(Messages.instance().get("changepwd.invalidpassword"));
			context.addMessage(edtPassword.getClientId(context), message);
		}
	}
	
	/**
	 * Static method to check if the given password is valid according to security rules
	 * @param password
	 * @return
	 */
	public static boolean validadePassword(String password) {
		Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
		return pattern.matcher(password).matches();
	}


	public String getNewPassword1() {
		return newPassword1;
	}
	public void setNewPassword1(String newPassword1) {
		this.newPassword1 = newPassword1;
	}
	public String getNewPassword2() {
		return newPassword2;
	}
	public void setNewPassword2(String newPassword2) {
		this.newPassword2 = newPassword2;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
