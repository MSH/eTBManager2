package org.msh.tb.login;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.etbm.services.pub.SendNewPasswordService;
import org.msh.tb.application.App;


/**
 * Sends a new password to the user by its e-mail address
 * @author Ricardo
 *
 */
@Name("sendPassword")
@BypassInterceptors
public class SendPasswordBean extends Controller {
	private static final long serialVersionUID = 5770134096915738398L;
	
	private String email;

	public String execute() {
        FacesMessages facesMessages = (FacesMessages) App.getComponent("facesMessages");
        SendNewPasswordService srv = (SendNewPasswordService) App.getComponent("sendNewPasswordService");

        boolean sent = srv.execute(email);

        if (sent) {
            facesMessages.addFromResourceBundle("login.pwdsent", email);
            return "success";
        }
        else {
            facesMessages.addFromResourceBundle("login.noemail");
            return "error";
        }
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
