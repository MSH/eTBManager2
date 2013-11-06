/**
 * 
 */
package org.msh.tb.laboratories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.UserWorkspace;


/**
 * Manage an in-memory and request scope list of laboratories
 * @author Ricardo Memoria
 *
 */
@Name("labListsManager")
@BypassInterceptors
public class LabListsManager {

	private Map<LabListFilter, List<Laboratory>> lists = new HashMap<LabListFilter, List<Laboratory>>();
	
	/**
	 * Get a reference to a list of {@link Laboratory} objects based on its filter
	 * @param filter
	 * @return
	 */
	public List<Laboratory> getLaboratories(LabListFilter filter) {
		List<Laboratory> lst = lists.get(filter);
		if (lst == null) {
			lst = createListLabs(filter);
			if (lst != null)
				lists.put(filter, lst);
		}
		return lst;
	}

	/**
	 * Create a list of {@link Laboratory} objects based on the given filters
	 * @return list of {@link Laboratory} objects
	 */
	private List<Laboratory> createListLabs(LabListFilter filter) {
		if (filter.getAdminUnitId() == null)
			return null;
		
		// add dynamic condition by health system
		Integer healthSystemID = null;
		if (filter.isApplyHealthSystemRestrictions()) {
			UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
			if (userWorkspace.getHealthSystem() != null)
				healthSystemID = userWorkspace.getHealthSystem().getId();
		}
		String cond;
		if (healthSystemID != null)
			 cond = "and u.healthSystem.id = " + healthSystemID.toString();
		else cond = null;
		
		String hql = "select u from Laboratory u " +
				"where u.adminUnit.code like :code " +
				"and u.workspace.id = #{defaultWorkspace.id} " +
				(cond != null? cond: ""); 

		hql = hql + " order by u.name";

		EntityManager em = (EntityManager)Component.getInstance("entityManager");
		
		AdministrativeUnit adminUnit = em.find(AdministrativeUnit.class, filter.getAdminUnitId());
		
		return em.createQuery(hql)
				.setParameter("code", adminUnit.getCode() + "%")
				.getResultList();
	}
}

