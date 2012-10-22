package org.msh.tb.application;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;

/**
 * Base controller where business components can use to execute common operations dependent of containers. 
 * This class will make it easier to share source code between web application and desktop application
 * @author Ricardo Memoria
 *
 */
@Name("app")
public class App {

	/**
	 * Return an instance of the {@link EntityManager} in use
	 * @return
	 */
	public static EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}
	
	
	public static String getMessage(String key) {
		return Messages.instance().get(key);
	}
	
	/**
	 * Return a component of a specified name
	 * @param name
	 * @return
	 */
	public static Object getComponent(String name) {
		return Component.getInstance(name);
	}
	
	/**
	 * Return a component of a specified class
	 * @param clazz
	 * @return
	 */
	public static <E> E getComponent(Class<E> clazz) {
		return (E)Component.getInstance(clazz);
	}
	
	/**
	 * Return the workspace in use
	 * @return
	 */
	public static Workspace getDefaultWorkspace() {
		return (Workspace)Component.getInstance("defaultWorkspace");
	}
	
	/**
	 * Return the user logged into the current session
	 * @return
	 */
	public static User getUser() {
		return ((UserLogin)Component.getInstance("user")).getUser();
	}
	
	
	/**
	 * Return the instance of {@link UserWorkspace} assigned to the current session
	 * @return
	 */
	public static UserWorkspace getUserWorkspace() {
		return (UserWorkspace)Component.getInstance("userWorkspace");
	}
}
