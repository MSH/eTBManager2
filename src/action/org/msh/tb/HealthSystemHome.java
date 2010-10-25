package org.msh.tb;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.tb.log.LogInfo;
import org.msh.utils.EntityQuery;


/**
 * Home class to handle health system CRUD operations
 * @author Ricardo Memoria
 *
 */
@Name("healthSystemHome")
@LogInfo(roleName="HEALTHSYS")
public class HealthSystemHome extends EntityHomeEx<HealthSystem> {
	private static final long serialVersionUID = -4524994233700353060L;

	@In(create=true) FacesMessages facesMessages;
	@In(required=false) HealthSystemsQuery healthSystems;
	
	@Factory("healthSystem")
	public HealthSystem getHealthSystem() {
		return getInstance();
	}


	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityHomeEx#remove()
	 */
	@Override
	public String remove() {
		UserWorkspace userWorkspace = getUserWorkspace();
		if (userWorkspace.getTbunit().getHealthSystem().equals(getInstance())) {
			facesMessages.addFromResourceBundle("admin.healthsys.delerror1");
			return "error";
		}
		
		if (healthSystems != null)
			healthSystems.getResultList().remove(getInstance());
			
		return super.remove();
	}

	@Override
	public EntityQuery<HealthSystem> getEntityQuery() {
		return (HealthSystemsQuery)Component.getInstance("healthSystems", false);
	}
}
