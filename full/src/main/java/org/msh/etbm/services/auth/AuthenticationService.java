package org.msh.etbm.services.auth;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.msh.tb.login.AuthenticatorBean;
import org.msh.tb.login.UserSession;
import org.msh.tb.webservices.Response;

/**
 * Created by ricardo on 24/11/14.
 */
@Name("authenticationService")
@AutoCreate
public class AuthenticationService {

    /**
     * Try to login the user with the login, password and workspace ID. If login is
     * successfully done, a user token is returned
     * @param username the user name to enter in the system
     * @param password the user password
     * @param workspaceId the workspace ID (not required). If not informed, the system will use
     *                    the last workspace accessed by the user
     * @return token ID to be reused in other calls or null if the login failed
     */
    public String login(String username, String password, Integer workspaceId) {
        AuthenticatorBean authenticator = (AuthenticatorBean) Component.getInstance("authenticator");
        authenticator.setWorkspaceId(workspaceId);
        authenticator.setTryRestorePrevSession(true);

        Credentials credentials = Identity.instance().getCredentials();
        credentials.setUsername(username);
        credentials.setPassword(password);
        Identity.instance().login();

        if (Identity.instance().isLoggedIn()) {
            return UserSession.instance().getSessionId();
        }
        else {
            return null;
        }
    }

    /**
     * Log the user in the system by its token
     * @param token
     * @return
     */
    public boolean loginWithToken(String token) {
        AuthenticatorBean authenticator = (AuthenticatorBean) Component.getInstance("authenticator");
        authenticator.setSessionId(token);
        Identity.instance().login();

        return Identity.instance().isLoggedIn();
    }
}
