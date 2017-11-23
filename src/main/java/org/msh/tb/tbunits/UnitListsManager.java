/**
 * 
 */
package org.msh.tb.tbunits;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component must have event scope view and it'll return the list of TB units for
 * a given filter. This list must be discharged when the request is completed.
 *  
 * @author Ricardo Memoria
 *
 */
@Name("unitListsManager")
@BypassInterceptors
public class UnitListsManager {

	private Map<UnitListFilter, List<Tbunit>> lists = new HashMap<UnitListFilter, List<Tbunit>>();
	
	/**
	 * Get a reference to a list of {@link Tbunit} objects based on its filter
	 * @param filter
	 * @return
	 */
	public List<Tbunit> getUnits(UnitListFilter filter) {
		List<Tbunit> lst = lists.get(filter);
		if (lst == null) {
			lst = createListUnits(filter);
			if (lst != null)
				lists.put(filter, lst);
		}
		return lst;
	}

	/**
	 * Create a list of {@link Tbunit} objects based on the given filters
	 * @return list of {@link Tbunit} objects
	 */
	private List<Tbunit> createListUnits(UnitListFilter filter) {
		if (filter.getAdminUnitId() == null)
			return null;
		
		// add dynamic condition by health system
		Integer healthSystemID = null;
		if (filter.isApplyHealthSystemRestriction()) {
			UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
			if (userWorkspace.getHealthSystem() != null)
				healthSystemID = userWorkspace.getHealthSystem().getId();
		}
		String cond;
		if (healthSystemID != null)
			 cond = "and u.healthSystem.id = " + healthSystemID.toString();
		else cond = null;
		
		String hql = "select u from Tbunit u " +
				"where u.adminUnit.code like :code " +
				"and u.workspace.id = #{defaultWorkspace.id} " +
				"and u.active = :active " +
				(cond != null? cond: ""); 

		// check if there is any restriction
		String restriction = createHQLUnitFilterByType(filter.getType());
		if ((restriction != null) && (!restriction.isEmpty()))
			hql = hql.concat(" and " + restriction);
		
		hql = hql + " order by u.name.name1";

		EntityManager em = (EntityManager)Component.getInstance("entityManager");
		
		AdministrativeUnit adminUnit = em.find(AdministrativeUnit.class, filter.getAdminUnitId());
		
		return em.createQuery(hql)
				.setParameter("code", adminUnit.getCode() + "%")
				.setParameter("active", true)
				.getResultList();
	}

	/**
	 * Create HQL unit filter by type
	 * @param unitType
	 * @return
	 */
	protected String createHQLUnitFilterByType(TBUnitType unitType) {
		if (unitType == null)
			return null;
		
		switch (unitType) {
			case NOTIFICATION_UNITS: return "u.notifHealthUnit = true";
			case HEALTH_UNITS: return "u.treatmentHealthUnit = true";
			case MDRHEALTH_UNITS: return "u.mdrHealthUnit = true";
			case TBHEALTH_UNITS: return "u.tbHealthUnit = true";
			case MEDICINE_ORDER_UNITS: return "u.treatmentHealthUnit = true and (u.firstLineSupplier != null or u.secondLineSupplier != null)";
			case MEDICINE_SUPPLIERS: return "u.medicineSupplier = true";
			case MEDICINE_WAREHOUSES: return "u.medicineStorage = true and (u.medManStartDate is not null)";
		}
		return null;
	}

}
