package org.msh.tb.application;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Workspace;

import javax.persistence.EntityManager;

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
	
	
	/**
	 * Translate a key message to a message in the current locale
	 * @param key
	 * @return
	 */
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
	 * Return a component of a specified name 
	 * @param name
	 * @return
	 */
	public static Object getComponent(String name, boolean create) {
		return Component.getInstance(name, create);
	}

    /**
     * Return an instance of a component. The name of the component must be composed of the name parameter
     * plus the suffix _ws, where _ws is the extension of the default workspace on upper case letters.
     * @param name
     * @return
     */
    public static Object getComponentFromDefaultWorkspaceOrGeneric(String name) {
        Object ret;
        Workspace ws = (Workspace) App.getComponent("defaultWorkspace",true);

        String s = name + "_" + ws.getExtension();
        ret = Component.getInstance(s, true);

        if(ret == null)
            ret = Component.getInstance(name, true);

        return ret;
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
	 * Return the temporary directory used by the JVM
	 * @return String containing the temporary folder
	 */
	public static String getTempDir() {
		return System.getProperty("java.io.tmpdir");
	}
}
