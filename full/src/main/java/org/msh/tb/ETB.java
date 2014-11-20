package org.msh.tb;

import org.jboss.seam.Component;
import org.msh.tb.entities.Workspace;

/**
 * A group of utilities functions of e-TB Manager
 * @author Ricardo Memoria
 *
 */
public class ETB {

	/**
	 * Return the specific subclass of the given class for the workspace
	 * @param clazz
	 * @return
	 */
	public static Class getWorkspaceClass(Class clazz) {
		Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");
		if (workspace == null)
			return clazz;

		String ext = workspace.getExtension();
		if (ext != null) {
			String className = clazz.getName();
			className = className.replace("org.msh.tb", "org.msh.tb." + ext);
			className += ext.toUpperCase();

			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				return clazz;
			}
		}
		else return clazz;
	}
	
	
	/**
	 * Return the display name of the sub class implemented for the given class 
	 * of the current workspace
	 * @param clazz
	 * @return
	 */
	public static String getWsClassName(Class clazz) {
		return ETB.getWorkspaceClass(clazz).getName();
	}
}
