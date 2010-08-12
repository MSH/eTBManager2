package org.msh.tb.login;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.utils.Passwords;



@Name("changePwd")
public class ChangePwdBean {

    @In(create=true) FacesMessages facesMessages;
    @In(create=true) EntityManager entityManager; 

    @In(required=false)
    UserLogin userLogin;
	
	private String password;
    private String newPassword1;
    private String newPassword2;

	public String execute() {
		// verifica se as 2 novas senhas são iguais
		if (!newPassword1.equals(newPassword2)) {
			facesMessages.addFromResourceBundle("changepwd.wrongpass2");
			return "error";
		}
		
		String pwd = Passwords.hashPassword(password);
		
		User user = (User)entityManager.merge(userLogin.getUser());
		
		// verifica se a senha atual é igual a senha do usuário
		if (!pwd.equalsIgnoreCase(user.getPassword())) {
			facesMessages.addFromResourceBundle("changepwd.wrongpass1");
			return "error";
		}
		
		pwd = Passwords.hashPassword(newPassword1);
		user.setPassword(pwd);
		entityManager.persist(user);
		userLogin.setUser(user);
		
		return "pwdchanged";
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
