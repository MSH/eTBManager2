/**
 * 
 */
package org.msh.tb;

import org.jboss.seam.annotations.Name;

/**
 * Factory of {@link EntityHomeEx} classes based on the given entity class
 * @author Ricardo Memoria
 *
 */
@Name("entityHomeFactory")
public class EntityHomeFactory {

	public EntityHomeEx getEntityHome(Class entityClass) {
		return null;
//		if (TbCase.class.isAssignableFrom(cls))
	}
}
