package org.msh.tb.md;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Execute importing of a region from the SYMETB in a xml representation
 * @author Ricardo Memoria
 *
 */
public class RegionImporting extends ImportingBase {

	@Override
	protected boolean importRecord(Element xmlLocData) {
		String id = getValue(xmlLocData, "second_id_in_table", true);
		String nameRo = getValue(xmlLocData, "REGION_NAME_RO", true);
		String nameRu = getValue(xmlLocData, "REGION_NAME_RU", false);
		
		if (isErrorOnCurrentImport())
			return false;
		
		AdministrativeUnit region = loadAdminUnit(id);
		if (region != null) {
			region.getName().setName1(nameRo);
			region.getName().setName2(nameRu);
			region.setLegacyId(id);
			getEntityManager().persist(region);
			getEntityManager().flush();
			return false;
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
				addError("No country structure defined for level 1");
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
