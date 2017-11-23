package org.msh.tb.cases;

import org.msh.tb.ETB;
import org.msh.tb.EntityHomeEx;

/**
 * Home for all entities (related to TB case data) that must be specialized (new fields) in 
 * a specific workspace. When trying to instantiate or load the entity, the system checks if there is
 * a class implemented following the rule bellow: <p/>
 * If there is an entity called <code>TbCase</code>, and the workspace has an extension xx, 
 * The system will try to use a class called <code>TbCaseXX</code>. <p/>
 * <b>Restrictions</b><p/>
 * Suppose the workspace has an extension 'xx'.<p/>
 * The original entity must be in the package <code>org.msh.tb.entities</code>.<p/>
 * The inherited class must be in the package <code>org.msh.tb.xx.entities</code><p/>
 * If original class is named <code>MyClass</code>, the inherited class must be called <code>MyClassXX</code>, ending with the workspace's
 * extension in upper case format.
 *  
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public class WsEntityHome<E> extends EntityHomeEx<E>{
	private static final long serialVersionUID = 4674924221364528253L;

	private Class workspaceEntityClass;
	
	
	/**
	 * Return the Class of the entity in this home class supported by the workspace
	 * @return
	 */
	protected Class getWorkspaceEntityClass() {
		if (workspaceEntityClass == null) {
			workspaceEntityClass = ETB.getWorkspaceClass(getEntityClass());
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
