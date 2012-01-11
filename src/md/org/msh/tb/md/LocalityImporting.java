package org.msh.tb.md;

import java.util.List;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;
import org.w3c.dom.Element;

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
		
		AdministrativeUnit locality = loadAdminUnit(id);
		if (locality != null) {
			locality.getName().setName1(nameRo);
			locality.getName().setName2(nameRu);
			locality.setLegacyId(id);
			getEntityManager().persist(locality);
			getEntityManager().flush();
			return false;
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
				addError("No country structure defined for level " + Integer.toString(region.getLevel() + 1));
				return false;
			}
			adm.setCountryStructure(lst.get(0));
			adm.setWorkspace(getWorkspace());
			
			home.setCheckSecurityOnOpen(false);
			home.setDisplayMessage(false);
			home.setTransactionLogActive(true);

			home.persist();
			return true;
		}
	}

}
