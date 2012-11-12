package org.msh.tb.webservices;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.login.AuthenticatorBean;
import org.msh.tb.login.UserSession;

/**
 * Web service to provide authentication
 * @author Ricardo Memoria
 *
 */
@WebService(name="authenticatorService", serviceName="authenticatorService")
@SOAPBinding(style=Style.RPC)
public class AuthenticatorService {

	@WebMethod
	public String login(@WebParam String username, @WebParam String password, @WebParam int workspaceid) {
		Response resp = new Response();
		try {
			AuthenticatorBean authenticator = (AuthenticatorBean)Component.getInstance("authenticator");
			authenticator.setWorkspaceId(workspaceid);
			authenticator.setTryRestorePrevSession(true);

			Credentials credentials = Identity.instance().getCredentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			Identity.instance().login();

			if (Identity.instance().isLoggedIn()) {
				resp.setResult(UserSession.instance().getSessionId());
				resp.setErrorno(Response.RESP_SUCCESS);
			}
			else resp.setErrorno(Response.RESP_AUTHENTICATION_FAIL);

		} catch (Exception e) {
			resp.setErrormsg(e.getMessage());
			resp.setErrorno(Response.RESP_UNEXPECTED_ERROR);
		}

		return ObjectSerializer.serializeToXml(resp);
	}
	
	
	@WebMethod
	public String getUserWorkspaces(@WebParam String username, @WebParam String password) {
		Response resp = new Response();

		try {
			AuthenticatorBean authenticator = (AuthenticatorBean)Component.getInstance("authenticator");
			UserWorkspace userWorkspace = authenticator.validateUserPassword(username, password, null);

			if (userWorkspace == null)
				resp.setErrorno(Response.RESP_AUTHENTICATION_FAIL);

			EntityManager em = (EntityManager)Component.getInstance("EntityManager");
			List<Workspace> lst = em.createQuery("select w from UserWorkspace uw join fetch uw.workspace w where uw.user.id = :id")
				.setParameter("id", userWorkspace.getUser().getId())
				.getResultList();
			
			resp.setErrorno(Response.RESP_SUCCESS);
			
		} catch (Exception e) {
			resp.setErrorno(Response.RESP_UNEXPECTED_ERROR);
			resp.setErrormsg(e.getMessage());
		}
		
		return ObjectSerializer.serializeToXml(resp);
	}
	
}
