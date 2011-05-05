package org.msh.tb.cases;

import org.jboss.seam.Component;
import org.msh.mdrtb.entities.Workspace;
import org.msh.tb.EntityHomeEx;

/**
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public class WorkspaceSensitiveEntityHome<E> extends EntityHomeEx<E>{
	private static final long serialVersionUID = 4674924221364528253L;

	private Class workspaceEntityClass;
	
	
	public Class getWorkspaceEntityClass() {
		if (workspaceEntityClass == null) {
			Class clazz = getEntityClass();
			Workspace defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace");
			String ext = defaultWorkspace.getExtension();
			if (ext != null) {
				String className = clazz.getSimpleName() + ext.toUpperCase();
				try {
					workspaceEntityClass = Class.forName(className);
				} catch (ClassNotFoundException e) {
					workspaceEntityClass = clazz;
				}
			}
			else workspaceEntityClass = clazz;
		}
		
		return workspaceEntityClass;
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.EntityHome#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return getWorkspaceEntityClass().toString();
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.EntityHome#loadInstance()
	 */
	@Override
	protected E loadInstance() {
		return (E)getEntityManager().find(getWorkspaceEntityClass(), getId());
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Home#createInstance()
	 */
	@Override
	protected E createInstance() {
		E instance = null;
		Class clazz = getWorkspaceEntityClass();
		try {
			instance = (E)clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return instance;
	}

}
