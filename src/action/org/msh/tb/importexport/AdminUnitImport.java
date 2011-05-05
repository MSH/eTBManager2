package org.msh.tb.importexport;

import java.util.List;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;



/**
 * Import data from an external file into the administrative unit table
 * @author Ricardo Memoria
 *
 */
public class AdminUnitImport extends ImportBase {

	private List<CountryStructure> cslist;

	// store names of previous parents
	private String[] parentNames = new String[5];
	private String[] parentTypes = new String[5];
	
	private AdministrativeUnit parentUnit;
	
	/**
	 * Index in the array of the column representing the name of the country structure  
	 */
	private int typeIndex;


	@Override
	public boolean initialize() {
		if (super.initialize()) {
			typeIndex = getColumnIndex("type");
			return true;
		}
		else return false;
	}
	
	
	@Override
	public void importRecord(String[] values) {
		// get custom id
		String customId = getId();
		int colId = getColumnId();

		// get type name
		String typeName = null;
		if (typeIndex >= 0) {
  			typeName = values[typeIndex].trim();
  			if (typeName.isEmpty())
  				typeName = null;
		}
		
		int index = 0;
		int sindex = 0;
		int nameLevel = -1;
		for (String s: values) {
			if ((index != colId) && (index != typeIndex) ) {
				if (!s.isEmpty()) {
					parentNames[sindex] = s;
					parentTypes[sindex] = getColumns().get(index);
					nameLevel = sindex;
				}
				sindex++;
			}
			index++;
		}

		for (int i = nameLevel + 1 ; i < 5; i++) {
			parentNames[i] = null;
		}

		// if there is no name, so leave it!
		if (nameLevel < 0)
			return;

		String name = parentNames[nameLevel];
		if (typeName == null)
			typeName = parentTypes[nameLevel];
		
		String parentName;
		if (nameLevel > 0)
			 parentName = parentNames[nameLevel - 1];
		else parentName = null;
		
		saveAdminUnit(parentName, typeName, name, customId, nameLevel + 1);
	}

	
	/**
	 * Save a new administrative unit in the table of administrative units
	 * @param parentName name of the parent administrative unit
	 * @param type the name of the administrative unit structure (ex. Region, Locality)
	 * @param name the name of the administrative unit
	 * @param customId the standard code of the unit (not required)
	 * @param nameLevel the level of the administrative unit in the country
	 * @return 
	 */
	protected AdministrativeUnit saveAdminUnit(String parentName, String type, String name, String customId, int nameLevel) {
		// check if administrative unit already exists
		AdministrativeUnit adminUnit = getAdminUnit(name, nameLevel);

		AdministrativeUnit parent;
		if (nameLevel > 1) {
			 parent = getParentUnit(parentName, nameLevel - 1);
			 if (parent == null)
				 throw new RuntimeException("The administrative unit " + parentName + " at level " + Integer.toString(nameLevel - 1) + " was not found in the system");
		}
		else parent = null;

		// create a new administrative unit
		AdminUnitHome adminUnitHome = (AdminUnitHome)Component.getInstance("adminUnitHome", true);
		adminUnitHome.clearInstance();
		adminUnit = adminUnitHome.getInstance();

		adminUnitHome.setInstance(adminUnit);
		adminUnit.getName().setName1(name);
		adminUnit.setLegacyId(customId);
		CountryStructure cs = getCountryStructure(type, nameLevel);
		if (cs == null)
			throw new RuntimeException("The country structure for " + type + " in the level " + nameLevel + " was not found");
		adminUnit.setCountryStructure(cs);
		adminUnit.setWorkspace(getWorkspace());
		
		adminUnit.setParent(parent);

		adminUnitHome.setTransactionLogActive(false);
		adminUnitHome.persist();
		
		return adminUnit;
	}

	
	/**
	 * Return country structure by its type and level
	 * @param type
	 * @param level
	 * @return
	 */
	protected CountryStructure getCountryStructure(String type, int level) {
		List<CountryStructure> lst = getCountryStructureList();
		for (CountryStructure cs: lst) {
			if (cs.getLevel() == level) {
				if (type == null)
					return cs;
				if (type.equalsIgnoreCase(cs.getName().getName1())) {
					return cs;
				}
			}
		}
		
		if (type == null)
			return null;
		
		CountryStructure cs = new CountryStructure();
		cs.setLevel(level);
		cs.getName().setName1(type);
		cs.setWorkspace(getWorkspace());
		getEntityManager().persist(cs);
		getEntityManager().flush();
		
		lst.add(cs);
		
		return cs;
	}


	/**
	 * Search for an administrative unit by its name and level
	 * @param name
	 * @param level
	 * @return
	 */
	protected AdministrativeUnit getAdminUnit(String name, int level) {
		List<AdministrativeUnit> lst = getEntityManager().createQuery("from AdministrativeUnit a " +
				"where upper(a.name.name1) = :name " +
				"and a.countryStructure.level = :level " +
				"and a.workspace.id = :id")
			.setParameter("id", getWorkspace().getId())
			.setParameter("name", name.toUpperCase())
			.setParameter("level", level)
			.getResultList();
		if (lst.size() > 0) {
			return lst.get(0);
		}
		return null;
	}

	/**
	 * Return list of country structure
	 * @return
	 */
	protected List<CountryStructure> getCountryStructureList() {
		if (cslist == null) {
			int id = getWorkspace().getId();
			cslist = getEntityManager().createQuery("from CountryStructure where workspace.id = " + id).getResultList();
		}
		return cslist;
	}
	
	
	protected AdministrativeUnit getParentUnit(String name, int level) {
		if ((parentUnit == null) || (!parentUnit.getName().toString().equalsIgnoreCase(name))) {
			parentUnit = getAdminUnit(name, level);
			
			if (parentUnit == null) {
				String parentName;
				if (level > 0)
					 parentName = parentNames[level - 1];
				else parentName = null;
				parentUnit = saveAdminUnit(parentName, parentTypes[level], name, null, level);
			}
/*			List<AdministrativeUnit> lst = getEntityManager().createQuery("from AdministrativeUnit a where a.workspace.id = :ws " +
					"and a.countryStructure.level = :level and upper(a.name.name1) = :name")
					.setParameter("ws", getWorkspace().getId())
					.setParameter("level", level)
					.setParameter("name", name.toUpperCase())
					.getResultList();
			if (lst.size() > 0)
				 parentUnit = lst.get(0);
			else parentUnit = null;
*/		}
		
		return parentUnit;
	}
	
	@Override
	public void clearEntityManager() {
		cslist = null;
	}
}
