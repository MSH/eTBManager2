package org.msh.tb.login;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.TimeZoneSelector;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserState;
import org.msh.utils.Passwords;



/**
 * Handle user authentication from the web UI or from a remote connection (web services, for example).
 * Authentication is required in order to access most of the business classes, and this component
 * is not accessed directly, but thought the {@link Identity} class.
 * <p/>
 * @param workspaceid - not required. Use it if you know the user workspace you want to 
 * connect to
 * @param restorePrevSession - If this parameter is true, the authentication will try to use 
 * the previous session id. This is important if you don't want to register several logins from
 * the same user (for example, an automatic program connecting through web services) 
 * @author Ricardo Memoria
 *
 */
@Name("authenticator")
public class AuthenticatorBean {

	// indicate the workspace that user will log into. This parameter is not required
    private Integer workspaceId;

    // indicate if the authentication procedure will try to restore the previous 
    // user login (under certain conditions)
    private boolean tryRestorePrevSession;
    
    private String sessionId;


    /**
     * Authenticate a user name, its password, and the workspace he is trying to log into.
     * If the workspace ID is not informed, the system will authenticate the user and will try
     * to find a workspace available (which is initially the last workspace logged or the first
     * in the list of available workspaces).
     * <p/>
     * @param username - User name (or login)
     * @param password - User password
     * @return
     */
    public boolean authenticate() {
    	// a session id was informed ?
    	if (sessionId != null) {
        	// trying to log using a session Id
    		return UserSession.instance().registerLogin(sessionId, tryRestorePrevSession) != null;
    	}
   
    	Credentials credentials = Identity.instance().getCredentials();

    	String username = credentials.getUsername();
    	String password = credentials.getPassword();
    	
    	// create variable to point to authenticated user in a workspace
    	UserWorkspace userWorkspace = validateUserPassword(username, password, workspaceId);
    	    	
    	// no user and its workspace was authenticated ?
    	if (userWorkspace == null)
    		return false;

    	selectUserTimeZone(userWorkspace);

    	// register the login
    	UserSession.instance().registerLogin(userWorkspace, tryRestorePrevSession);
    	
    	// if user password is expired, clear information about redirection
        if (userWorkspace.getUser().isPasswordExpired())
        	Redirect.instance().setViewId(null);
    	
    	return true;
    }



	/**
     * Validate a user name and its password. Workspace is not required, and if not given, the most appropriate
     * workspace will be selected 
     * @param username
     * @param password
     * @param workspaceId
     * @return instance of the {@link UserWorkspace} class representing the validated user, or null if it's not found
     */
    public UserWorkspace validateUserPassword(String username, String password, Integer workspaceId) {
    	String pwdhash = Passwords.hashPassword(password);

    	// no workspace was defined ?
    	if (workspaceId == null) {
    		// authenticate user and password
        	List<User> lst = getEntityManager().createQuery("from User u where u.login = :login " +
    				"and upper(u.password) = :pwd and u.state <> :blockstate")
    				.setParameter("login", username.toUpperCase())
    				.setParameter("pwd", pwdhash.toUpperCase())
    				.setParameter("blockstate", UserState.BLOCKED)
    				.getResultList();
        	
        	if (lst.size() == 0)
        		return null;

        	User user = lst.get(0);
        	return selectUserWorkspace(user);
    	}
    	else {
    		// authenticate user, password and workspace
        	List<UserWorkspace> lst = getEntityManager().createQuery("from UserWorkspace uw " +
        			"join fetch uw.user u " +
        			"join fetch uw.workspace w " +
        			"where u.login = :login " +
    				"and upper(u.password) = :pwd " +
    				"and u.state <> :blockstate " +
    				"and w.id = :wsid")
    				.setParameter("login", username.toUpperCase())
    				.setParameter("pwd", pwdhash.toUpperCase())
    				.setParameter("wsid", workspaceId)
    				.setParameter("blockstate", UserState.BLOCKED)
    				.getResultList();

        	if (lst.size() == 0)
        		return null;
        	
        	return lst.get(0);
    	}
    }

    /**
     * Select the user time zone in order to display properly the date and time in the system.
     * If no time zone was assigned to the user, the system time zone will be used to the 
     * user session
     * @param userWorkspace object containing information about the user and its workspace
     */
    private void selectUserTimeZone(UserWorkspace userWorkspace) {
    	User user = userWorkspace.getUser();
   
        // adjust time zone
    	String tm = user.getTimeZone();
    	if ((tm == null || (tm.isEmpty())))
    		tm = userWorkspace.getWorkspace().getDefaultTimeZone();

    	if (tm != null) {
    		TimeZoneSelector.instance().setTimeZoneId(tm);
    		TimeZoneSelector.instance().select();
        }
	}


	/**
     * Select the best workspace for a user. Initially the user default workspace is selected 
     * (which is the last one logged in or the one assigned when user is registered).
     * <p/>
     * If there is no default workspace, the system will select the first from the list of
     * workspaces available for the user.
     * <p/>
     * @param user instance of {@link User}
     * @return An instance of {@link UserWorkspace} representing the selected workspace of the user,
     * or null if no workspace was found.
     */
    private UserWorkspace selectUserWorkspace(User user) {
    	if (user.getDefaultWorkspace() != null)
    		return user.getDefaultWorkspace();

    	try {
			return (UserWorkspace)getEntityManager().createQuery("from UserWorkspace where id = (select min(id) " +
	    			"from UserWorkspace aux where aux.user.id = :userid)")
	    			.setParameter("userid", user.getId())
	    			.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

    
    /**
     * Called from the command Exit in the user menu of the web page
     * @return
     */
    public String logout() {
    	Identity.instance().logout();
    	return "logged-out";
    }

    
    

	/**
	 * @return the workspaceId
	 */
	public Integer getWorkspaceId() {
		return workspaceId;
	}


	/**
	 * @param workspaceId the workspaceId to set
	 */
	public void setWorkspaceId(Integer workspaceId) {
		this.workspaceId = workspaceId;
	}


	/**
	 * @return the tryRestorePrevSession
	 */
	public boolean isTryRestorePrevSession() {
		return tryRestorePrevSession;
	}


	/**
	 * @param tryRestorePrevSession the tryRestorePrevSession to set
	 */
	public void setTryRestorePrevSession(boolean tryRestorePrevSession) {
		this.tryRestorePrevSession = tryRestorePrevSession;
	}
	
	
	/**
	 * Return the instance of {@link EntityManager} in use
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}


	/**
	 * @return the sessionId
	 */
	public String isSessionId() {
		return sessionId;
	}


	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
