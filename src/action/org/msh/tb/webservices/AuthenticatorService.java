package org.msh.tb.webservices;

import java.util.ArrayList;
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
	public Response login(@WebParam String username, @WebParam String password, @WebParam int workspaceid) {
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

		return resp;
	}
	
	
	@WebMethod
	public Response getUserWorkspaces(@WebParam String username, @WebParam String password) {
		Response resp = new Response();

		try {
			AuthenticatorBean authenticator = (AuthenticatorBean)Component.getInstance("authenticator");
			UserWorkspace userWorkspace = authenticator.validateUserPassword(username, password, null);

			if (userWorkspace == null)
				resp.setErrorno(Response.RESP_AUTHENTICATION_FAIL);

			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			List<UserWorkspace> lst = em.createQuery("from UserWorkspace uw join fetch uw.workspace w where uw.user.id = :id")
				.setParameter("id", userWorkspace.getUser().getId())
				.getResultList();
			
			List<WorkspaceInfo> res = new ArrayList<WorkspaceInfo>();
			for (UserWorkspace uw: lst) 
				res.add(new WorkspaceInfo(uw.getWorkspace().getId(), uw.getWorkspace().getName().getName1(), uw.getWorkspace().getName().getName2()));

			resp.setResult(ObjectSerializer.serializeToXml(res));
			
			resp.setErrorno(Response.RESP_SUCCESS);
			
		} catch (Exception e) {
			resp.setErrorno(Response.RESP_UNEXPECTED_ERROR);
			resp.setErrormsg(e.toString());
		}
		
		return resp;
	}
	
}
