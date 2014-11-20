package org.msh.tb.md;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.adminunits.CountryStructuresQuery;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;
import org.w3c.dom.Element;

import java.util.List;

public class LocalityImporting extends ImportingBase {
	/**
	 * Import localities from the XML node element
	 * @param xmlLocData
	 * @param workspace
	 * @return
	 */
	@Override
	public boolean importRecord(Element xmlLocData) {
		String id = getValue(xmlLocData, "second_id_in_table", true);
		String nameRo = getValue(xmlLocData, "LOCALITY_NAME_RO", true);
		String nameRu = getValue(xmlLocData, "LOCALITY_NAME_RU", false);
		String regionId = getValue(xmlLocData, "REGION_ID", true);

		if (isErrorOnCurrentImport())
			return false;
		
		AdministrativeUnit region = loadAdminUnit(regionId);
		
		if (region == null) {
			addError("Locality " + nameRo + ": region of id " + regionId + " not found");
			return false;
		}

		AdminUnitHome home = (AdminUnitHome)Component.getInstance("adminUnitHome");
		
		AdministrativeUnit locality = loadAdminUnit(id);
		if (locality != null) {
			locality.getName().setName1(nameRo);
			locality.getName().setName2(nameRu);
			locality.setLegacyId(id);

			int level = locality.getLevel();
			if (locality.getCountryStructure().getLevel() != level) {
				CountryStructure cs = findCountryStructure(locality);
				if (cs == null)
					return false;
				locality.setCountryStructure(cs);
			}
			
			getEntityManager().persist(locality);
			getEntityManager().flush();


			return false;
		}
		else {
			home.clearInstance();
			home.setParentId(region.getId());
			AdministrativeUnit adm = home.getInstance();
			adm.getName().setName1(nameRo);
			adm.getName().setName2(nameRu);
			adm.setLegacyId(id);

			CountryStructure cs = findCountryStructure(adm);

			adm.setCountryStructure(cs);
			adm.setWorkspace(getWorkspace());
			
			home.setCheckSecurityOnOpen(false);
			home.setDisplayMessage(false);
			home.setTransactionLogActive(true);

			home.persist();
			return true;
		}
	}
	
	
	protected CountryStructure findCountryStructure(AdministrativeUnit adm) {
		// find country structure for this locality
		CountryStructuresQuery qry = (CountryStructuresQuery)Component.getInstance("countryStructures");
		List<CountryStructure> lst = qry.getResultList();

		int level = (adm.getParent() == null? 1: adm.getParent().getLevel() + 1);
		CountryStructure cs = null;
		for (CountryStructure aux: lst)
			if (aux.getLevel() == level) {
				cs = aux;
				break;
			}
		
		if (cs == null) {
			addError("No country structure defined for level " + Integer.toString(level));
			return null;
		}
		
		return cs;
	}

}
