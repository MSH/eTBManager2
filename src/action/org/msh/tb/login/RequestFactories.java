package org.msh.tb.login;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.Workspace;

/**
 * Declared factories to be called during initialization of contexts variables available in an event context
 * @author Ricardo Memoria
 *
 */
@Name("requestFactories")
@BypassInterceptors
public class RequestFactories {

	/**
	 * Return the default workspace in use by the current user connected
	 * @return
	 */
	@Factory(value="defaultWorkspace", autoCreate=true)
	public Workspace getDefaultWorkspace() {
		UserSession userSession = (UserSession)Component.getInstance("userSession");

		if ((userSession == null) || (userSession.getWorkspaceId() == null))
			return null;

		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		
		if (entityManager == null)
			return null;
		
		return (Workspace)entityManager.createQuery("from Workspace where id = :id")
			.setParameter("id", userSession.getWorkspaceId())
			.getSingleResult();
	}
}
