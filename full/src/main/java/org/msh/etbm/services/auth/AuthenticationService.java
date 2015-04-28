package org.msh.etbm.services.auth;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.msh.tb.application.App;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.UserState;
import org.msh.tb.login.AuthenticatorBean;
import org.msh.tb.login.UserSession;
import org.msh.utils.UserUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

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


    /**
     * Return the list of workspaces from an user credential
     * @param username a valid user name
     * @param password the correct password of this user
     * @return
     */
    public List<AuthWorkspace> getWorkspaces(String username, String password) {
        EntityManager em = App.getEntityManager();

        // check if it is a valid user credential
        String pwd = UserUtils.hashPassword(password);
        List<User> users = em.createQuery("from User where upper(login) = :login " +
                "and upper(password) = :password " +
                "and state <> :st")
                .setParameter("login", username.toUpperCase())
                .setParameter("password", pwd.toUpperCase())
                .setParameter("st", UserState.BLOCKED)
                .getResultList();

        if (users.size() == 0) {
            throw new RuntimeException("Invalid username or password");
        }

        // select user
        User user = users.get(0);

        // get the list of workspaces
        List<UserWorkspace> lst = em.createQuery("from UserWorkspace uw join fetch uw.tbunit join fetch uw.workspace " +
                "where uw.user.id = :id")
                .setParameter("id", user.getId())
                .getResultList();

        List<AuthWorkspace> res = new ArrayList<AuthWorkspace>();
        for (UserWorkspace uw: lst) {
            Workspace ws = uw.getWorkspace();
            AuthWorkspace item = new AuthWorkspace();
            item.setName(ws.getName().getName1());
            item.setId(ws.getId());
            item.setDescription(ws.getDescription());
            item.setName2(ws.getName().getName2());
            item.setUnitName( uw.getTbunit().getName().getName1() );
            res.add(item);
        }

        return res;
    }
}
