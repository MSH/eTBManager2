package org.msh.tb.adminunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.AdministrativeUnit;

/**
 * Wrapper class to hold administration unit lists for {@link AdminUnitSelection} class
 * @author Ricardo Memoria
 *
 */
@Name("adminListWrapper")
@BypassInterceptors
public class AdminListWrapper {

	private List<AdminUnitSelectionList> list;
	private Map<AdministrativeUnit, List<AdministrativeUnit>> cacheList = new HashMap<AdministrativeUnit, List<AdministrativeUnit>>();


	/**
	 * Return an instance of {@link AdminUnitSelectionList} to the reference object
	 * @param reference
	 * @return instance of {@link AdminUnitSelectionList}
	 */
	public AdminUnitSelectionList getReference(AdminUnitSelection reference) {
		if (list == null)
			list = new ArrayList<AdminUnitSelectionList>();
		
		for (AdminUnitSelectionList item: list) {
			if (item.getAdminUnitSelection().equals(reference))
				return item;
		}
		
		AdminUnitSelectionList item = new AdminUnitSelectionList(this);
		item.setAdminUnitSelection(reference);
		list.add(item);
		return item;
	}


	/**
	 * Cache list of administrative units during requests, avoiding reading the same list several times in the same form
	 * Example: Forms that has several administrative unit selections for different fields 
	 * @param parent
	 * @return {@link List} of {@link AdministrativeUnit} children of the parent parameter
	 */
	public List<AdministrativeUnit> getCacheList(AdministrativeUnit parent) {
		List<AdministrativeUnit> lst = cacheList.get(parent);
		
		if (lst == null) {
			String hql = "from AdministrativeUnit au join fetch au.countryStructure where au.workspace.id = #{defaultWorkspace.id}";

			if (parent != null)
				 hql = hql + " and au.parent.id = " + parent.getId().toString();
			else hql = hql + " and au.parent.id is null";
			
			hql = hql + " order by au.name.name1";

			lst = getEntityManager().createQuery(hql).getResultList();
			cacheList.put(parent, lst);
		}

		return lst;
	}


	/**
	 * Return the entity manager SEAM component
	 * @return {@link EntityManager} instance
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager", true);
	}
}
