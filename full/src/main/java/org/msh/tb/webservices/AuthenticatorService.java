package org.msh.tb.webservices;

import org.jboss.seam.Component;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.msh.etbm.services.auth.AuthenticationService;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.login.AuthenticatorBean;
import org.msh.tb.login.UserSession;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Web service to provide authentication
 * @author Ricardo Memoria
 *
 */
@WebService(name="authenticatorService", serviceName="authenticatorService", targetNamespace="http://etbmanager.org/authenticator")
@SOAPBinding(style=Style.RPC)
public class AuthenticatorService {

	/**
	 * Validate user and password under the provided workspace, and return a token to be reused in future requests
	 * @param username the user login
	 * @param password the user password
	 * @param workspaceid the ID of the workspace
	 * @return instance of {@link Response} class containing the result of the operation
	 */
	@WebMethod
	public Response login(@WebParam(name="username") String username, @WebParam(name="password") String password, @WebParam(name="workspaceId") int workspaceid) {
		Response resp = new Response();
		try {
            AuthenticationService srv = (AuthenticationService)Component.getInstance("authenticationService");
            String token = srv.login(username, password, workspaceid);

			if (token != null) {
				resp.setResult(UserSession.instance().getSessionId());
				resp.setErrorno(Response.RESP_SUCCESS);
			}
			else {
                resp.setErrorno(Response.RESP_AUTHENTICATION_FAIL);
            }

		} catch (Exception e) {
			resp.setErrormsg(e.getMessage());
			resp.setErrorno(Response.RESP_UNEXPECTED_ERROR);
		}

		return resp;
	}
	
	
	/**
	 * Return the list of workspaces available for the given user login
	 * @param username is the user login
	 * @param password is the user password
	 * @return instance of {@link Response} class containing the list of workspaces in the result 
	 * property as an XML document
	 */
	@WebMethod
	public Response getUserWorkspaces(@WebParam(name="username") String username, @WebParam(name="password") String password) {
		Response resp = new Response();

		try {
			AuthenticatorBean authenticator = (AuthenticatorBean)Component.getInstance("authenticator");
			UserWorkspace userWorkspace = authenticator.validateUserPassword(username, password, null);

			if (userWorkspace == null) {
				resp.setErrorno(Response.RESP_AUTHENTICATION_FAIL);
				return resp;
			}

			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			List<Object[]> lst = em.createQuery("select w.id, w.name.name1, w.name.name2, uw.tbunit.name.name1 " +
					"from UserWorkspace uw join uw.workspace w where uw.user.id = :id " +
					"order by w.name.name1")
				.setParameter("id", userWorkspace.getUser().getId())
				.getResultList();
			
			List<WorkspaceInfo> res = new ArrayList<WorkspaceInfo>();
			for (Object[] vals: lst) 
				res.add(new WorkspaceInfo((Integer)vals[0],
						(String)vals[1],
						(String)vals[2],
						(String)vals[3]));

			resp.setResult(ObjectSerializer.serializeToXml(res));
			
			resp.setErrorno(Response.RESP_SUCCESS);
			
		} catch (Exception e) {
			resp.setErrorno(Response.RESP_UNEXPECTED_ERROR);
			resp.setErrormsg(e.toString());
			e.printStackTrace();
		}
		
		return resp;
	}
	
}
