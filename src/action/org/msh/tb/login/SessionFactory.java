package org.msh.tb.login;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;

/**
 * Request scope component to serve as a factory of session scoped objects commonly used during user session,
 * like instances of the {@link User} class, {@link UserWorkspace} class, {@link Workspace} class and 
 * the {@link UserLogin} class
 * @author Ricardo Memoria
 *
 */
@Name("sessionFactory")
@BypassInterceptors
public class SessionFactory {

	public static final String workspaceId = "workspaceId";
	public static final String selectedUnitId = "selectedUnitId";
	public static final String userWorkspaceId = "userWorkspaceId";
	
	/**
	 * Factory to create an instance of the {@link Workspace} class in use by the current user
	 * @return
	 */
	@Factory(value="defaultWorkspace", scope=ScopeType.CONVERSATION, autoCreate=true)
	public Workspace createDefaultWorkspace() {
		Integer id = (Integer)Contexts.getSessionContext().get(workspaceId);
		return id == null? null : getEntityManager().find(Workspace.class, id);
	}
	

	/**
	 * Factory method to create the selected {@link Tbunit} of the user
	 * @return
	 */
	@Factory(value="selectedUnit", scope=ScopeType.CONVERSATION, autoCreate=true)
	public Tbunit createSelectedUnit() {
		Integer id = (Integer)Contexts.getSessionContext().get(selectedUnitId);
		return id == null? null : getEntityManager().find(Tbunit.class, id);
	}
	
	@Factory(value="userWorkspace", scope=ScopeType.CONVERSATION, autoCreate=true)
	public UserWorkspace createUserWorkspace() {
		Integer id = (Integer)Contexts.getSessionContext().get(userWorkspaceId);
		return id == null? null: getEntityManager().find(UserWorkspace.class, id);
	}
	
	/**
	 * Return the instance of the {@link EntityManager} in use
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}

}
