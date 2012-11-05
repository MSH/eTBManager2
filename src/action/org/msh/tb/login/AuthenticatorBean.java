package org.msh.tb.login;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.TimeZoneSelector;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserState;
import org.msh.utils.Passwords;



/**
 * Handle user authentication in the web UI and from a remote connection (web services, for example)
 * @author Ricardo Memoria
 *
 */
@Name("authenticator")
public class AuthenticatorBean {

    @In LocaleSelector localeSelector;
    
    @In(create=true) FacesMessages facesMessages;
    @In(create=true) TimeZoneSelector timeZoneSelector;
    @In(create=true) EntityManager entityManager;
    
    private String language;
    private User user;
    private List<UserWorkspace> userWorkspaces;


    /**
     * Select the workspace user must be logged in
     */
    private Integer loginWorkspaceId;
    


    /**
     * Authentication method used by WEB UI (login page) 
     * @return
     */
    public boolean webAuthenticate()
    {
    	Credentials credentials = Identity.instance().getCredentials();
    	if (credentials == null)
    		return false;

    	String pwd = Passwords.hashPassword(credentials.getPassword());

        try {
        	user = (User)entityManager.createQuery("from User u where u.login = :login " +
        						"and upper(u.password) = :pwd and u.state <> :blockstate")
        						.setParameter("login", credentials.getUsername().toUpperCase())
        						.setParameter("pwd", pwd.toUpperCase())
        						.setParameter("blockstate", UserState.BLOCKED)
        						.getSingleResult();
        	System.out.println("Login of " + user.getLogin());
        	
        	// check user workspace
        	loadUserWorkspace();
        	if (userWorkspaces.size() == 0) 
        		return false;

        	UserWorkspace userWorkspace = selectWorkspace();
        	if (userWorkspace == null)
        		return false;

        	if (userWorkspace.getProfile().getPermissions().size() == 0) {
            	facesMessages.addFromResourceBundle("login.norole");
        		return false;
        	}

        	// avoid lazy initialization problem
        	userWorkspace.getTbunit().getAdminUnit().getParentsTreeList(true);
        	if (userWorkspace.getAdminUnit() != null)
        		userWorkspace.getAdminUnit().getId();
        	if (userWorkspace.getHealthSystem() != null)
        		userWorkspace.getHealthSystem().getId();
        			
            // adjust time zone
        	String tm = user.getTimeZone();
        	if ((tm == null || (tm.isEmpty())))
        		tm = user.getDefaultWorkspace().getWorkspace().getDefaultTimeZone();

        	if (tm != null) {
            	timeZoneSelector.setTimeZoneId(tm);
            	timeZoneSelector.select();
            }
            
        	// adjust the language
//        	user.setLanguage(localeSelector.getLanguage());
//      		localeSelector.select();
      
            // register the user login
        	UserSession userSession = UserSession.instance();
        	userSession.registerLogin(userWorkspace);
            
            if (user.isPasswordExpired()) {
            	Redirect.instance().setViewId(null);
            }
            
            return true;
        }
        catch (NoResultException e) {
            return false;        	
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
     * Select the user workspace for using during the session
     */
    private UserWorkspace selectWorkspace() {
    	// there is a workspace to be used after authentication?
    	if (loginWorkspaceId != null) {
    		// search for user's workspace for login
    		for (UserWorkspace userWorkspace: userWorkspaces) {
    			if (userWorkspace.getWorkspace().getId().equals(loginWorkspaceId)) {
    				user.setDefaultWorkspace(userWorkspace);
    				return userWorkspace;
    			}
    		}
    		return null;
    	}
    	
    	// user has a default workspace ?
    	if (user.getDefaultWorkspace() == null) {
   			user.setDefaultWorkspace(user.getWorkspaces().get(0));
    	}
    	
    	return user.getDefaultWorkspace();
    }


    /**
     * Load user's workspaces
     */
    private void loadUserWorkspace() {
    	userWorkspaces = entityManager.createQuery("from UserWorkspace u " +
    			"join fetch u.workspace " +
    			"join fetch u.tbunit " +
    			"where u.user.id = :userid")
    			.setParameter("userid", user.getId())
    			.getResultList();
    }
    
    public void changeLanguage() {
    	localeSelector.setLocaleString(language);
    	localeSelector.select();
    }
    
	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}


	/**
	 * @return the loginWorkspaceId
	 */
	public Integer getLoginWorkspaceId() {
		return loginWorkspaceId;
	}


	/**
	 * @param loginWorkspaceId the loginWorkspaceId to set
	 */
	public void setLoginWorkspaceId(Integer loginWorkspaceId) {
		this.loginWorkspaceId = loginWorkspaceId;
	}
}
