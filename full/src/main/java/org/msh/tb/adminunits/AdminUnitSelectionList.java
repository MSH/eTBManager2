package org.msh.tb.adminunits;

import org.jboss.seam.Component;
import org.msh.tb.entities.AdministrativeUnit;

import javax.persistence.EntityManager;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminUnitSelectionList {

	private AdminUnitSelection adminUnitSelection;

	private AdminListWrapper adminListWrapper;
	
	private List<AdministrativeUnit> unitsLevel1;
	private List<AdministrativeUnit> unitsLevel2;
	private List<AdministrativeUnit> unitsLevel3;
	private List<AdministrativeUnit> unitsLevel4;
	private List<AdministrativeUnit> unitsLevel5;
	
	public AdminUnitSelectionList(AdminListWrapper adminListWrapper) {
		super();
		this.adminListWrapper = adminListWrapper;
	}

	public List<AdministrativeUnit> getUnitsLevel1() {
		if (unitsLevel1 == null)
			unitsLevel1 = createUnits(null);
		return unitsLevel1;
	}
	
	public List<AdministrativeUnit> getUnitsLevel2() {
		if (unitsLevel2 == null)
			unitsLevel2 = createUnits(adminUnitSelection.getUnitLevel1());
		return unitsLevel2;
	}
	
	public List<AdministrativeUnit> getUnitsLevel3() {
		if (unitsLevel3 == null)
			unitsLevel3 = createUnits(adminUnitSelection.getUnitLevel2());
		return unitsLevel3;
	}
	
	public List<AdministrativeUnit> getUnitsLevel4() {
		if (unitsLevel4 == null)
			unitsLevel4 = createUnits(adminUnitSelection.getUnitLevel3());
		return unitsLevel4;
	}
	
	public List<AdministrativeUnit> getUnitsLevel5() {
		if (unitsLevel5 == null)
			unitsLevel5 = createUnits(adminUnitSelection.getUnitLevel4());
		return unitsLevel5;
	}


	/**
	 * Create the children units according to the parent. If parent is null, it's considered to be the root level 
	 * @param parent
	 * @return List of {@link AdministrativeUnit} instances children of the parent
	 */
	private List<AdministrativeUnit> createUnits(AdministrativeUnit parent) {
		List<AdministrativeUnit> alw = adminListWrapper.getCacheList(parent);
		
		Collections.sort(alw, new Comparator<AdministrativeUnit>() {
			  public int compare(AdministrativeUnit o1, AdministrativeUnit o2) {
				String name1, name2;
				name1 = o1.getName().getName1();
				name2 = o2.getName().getName1();
				
				if (name1.equals(name2)){
					name2 = name1+"_"+o2.getCode();
				}
				Collator myCollator = Collator.getInstance();			    
				return myCollator.compare(name1,name2);
			  }
		});
		
		return alw;
/*		String hql = "from AdministrativeUnit au join fetch au.countryStructure where au.workspace.id = #{defaultWorkspace.id}";

		if (parent != null)
			 hql = hql + " and au.parent.id = " + parent.getId().toString();
		else hql = hql + " and au.parent.id is null";
		
		hql = hql + " order by au.name.name1";

		return getEntityManager().createQuery(hql).getResultList();
*/	}



	/**
	 * Return the entity manager SEAM component
	 * @return {@link EntityManager} instance
	 */
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager", true);
	}
	
	/**
	 * @return the adminUnitSelection
	 */
	public AdminUnitSelection getAdminUnitSelection() {
		return adminUnitSelection;
	}

	/**
	 * @param adminUnitSelection the adminUnitSelection to set
	 */
	public void setAdminUnitSelection(AdminUnitSelection adminUnitSelection) {
		this.adminUnitSelection = adminUnitSelection;
	}

	public void refresh() {
		unitsLevel1 = null;
		unitsLevel2 = null;
		unitsLevel3 = null;
		unitsLevel4 = null;
		unitsLevel5 = null;
	}
}
