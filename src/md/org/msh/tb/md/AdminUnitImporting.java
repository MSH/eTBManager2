package org.msh.tb.md;

import java.util.List;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.entities.Workspace;
import org.w3c.dom.Element;

/**
 * Import regions and localities to the administrative unit
 * @author Ricardo Memoria
 *
 */
public class AdminUnitImporting extends ImportingBase {

	/**
	 * Import localities from the XML node element
	 * @param xmlLocData
	 * @param workspace
	 * @return
	 */
	public boolean importLocality(Element xmlLocData, Workspace workspace) {
		setWorkspace(workspace);
		int numWarnings = getWarning().size();

		String id = getValue(xmlLocData, "second_id_in_table", true);
		String nameRo = getValue(xmlLocData, "LOCALITY_NAME_RO", true);
		String nameRu = getValue(xmlLocData, "LOCALITY_NAME_RU", false);
		String regionId = getValue(xmlLocData, "REGION_ID", true);

		if (getWarning().size() > numWarnings)
			return false;
		
		AdministrativeUnit region = loadAdminUnit(regionId);
		
		if (region == null) {
			addMessage("Locality " + nameRo + ": region of id " + regionId + " not found");
			return false;
		}
		
		AdministrativeUnit locality = loadAdminUnit(id);
		if (locality != null) {
			locality.getName().setName1(nameRo);
			locality.getName().setName2(nameRu);
			locality.setLegacyId(id);
			getEntityManager().persist(locality);
			getEntityManager().flush();
		}
		else {
			AdminUnitHome home = (AdminUnitHome)Component.getInstance("adminUnitHome");
			home.clearInstance();
			home.setParentId(region.getId());
			AdministrativeUnit adm = home.getInstance();
			adm.getName().setName1(nameRo);
			adm.getName().setName2(nameRu);
			adm.setLegacyId(id);
			List<CountryStructure> lst = home.getStructures();
			if (lst.size() == 0) {
				addMessage("No country structure defined for level " + Integer.toString(region.getLevel() + 1));
				return false;
			}
			adm.setCountryStructure(lst.get(0));
			adm.setWorkspace(getWorkspace());
			
			home.setCheckSecurityOnOpen(false);
			home.setDisplayMessage(false);
			home.setTransactionLogActive(false);

			home.persist();
			addMessage("New locality included: (" + id + ") " + nameRo);
		}
		
		return true;
	}

	
	/**
	 * Import a region from the XML node element
	 * @param xmlLocData
	 * @param workspace
	 * @return
	 */
	public boolean importRegion(Element xmlLocData, Workspace workspace) {
		setWorkspace(workspace);
		int numWarnings = getWarning().size();
		
		String id = getValue(xmlLocData, "second_id_in_table", true);
		String nameRo = getValue(xmlLocData, "REGION_NAME_RO", true);
		String nameRu = getValue(xmlLocData, "REGION_NAME_RU", false);
		
		if (getWarning().size() > numWarnings)
			return false;
		
		AdministrativeUnit locality = loadAdminUnit(id);
		if (locality != null) {
			locality.getName().setName1(nameRo);
			locality.getName().setName2(nameRu);
			locality.setLegacyId(id);
			getEntityManager().persist(locality);
			getEntityManager().flush();
		}
		else {
			AdminUnitHome home = (AdminUnitHome)Component.getInstance("adminUnitHome");
			home.clearInstance();
			AdministrativeUnit adm = home.getInstance();
			adm.getName().setName1(nameRo);
			adm.getName().setName2(nameRu);
			adm.setLegacyId(id);
			List<CountryStructure> lst = home.getStructures();
			if (lst.size() == 0) {
				addMessage("No country structure defined for level 1");
				return false;
			}
			adm.setCountryStructure(lst.get(0));
			adm.setWorkspace(getWorkspace());
			
			home.setCheckSecurityOnOpen(false);
			home.setDisplayMessage(false);
			home.setTransactionLogActive(false);
			
			home.persist();
			
			addMessage("New region included: (" + id + ") " + nameRo);
		}
		
		return true;
	}
	
	
/*	*//**
	 * Load administrative unit by its name or legacy id
	 * @param name
	 * @param legacyId
	 * @return
	 *//*
	protected AdministrativeUnit loadAdminUnitByNameOrLegacyId(String name, String legacyId) {
		EntityManager em = getEntityManager();
		List<AdministrativeUnit> lst = em.createQuery("from AdministrativeUnit adm where (upper(adm.name) = :name) or (adm.legacyId = :id)")
			.setParameter("name", name.toUpperCase())
			.setParameter("id", legacyId)
			.getResultList();
		
		return (lst.size() > 0? lst.get(0): null);
	}
*/}
