package org.msh.tb.webservices;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.jboss.seam.Component;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.msh.tb.login.AuthenticatorBean;

/**
 * Web service to provide authentication
 * @author Ricardo Memoria
 *
 */
@WebService(name="authenticatorService", serviceName="authenticatorService")
@SOAPBinding(style=Style.RPC)
public class AuthenticatorService {

	@WebMethod
	public String login(String username, String password, int workspaceid) {
		try {
			AuthenticatorBean authenticator = (AuthenticatorBean)Component.getInstance("authenticator");
			authenticator.setWorkspaceId(workspaceid);
			authenticator.setTryRestorePrevSession(true);

			Credentials credentials = Identity.instance().getCredentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			Identity.instance().login();
			
			if (Identity.instance().isLoggedIn()) {
				return "success";
			}
			else return "fail";

		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	@WebMethod
	public boolean isLoggedIn() {
		return Identity.instance().isLoggedIn();
	}
}
