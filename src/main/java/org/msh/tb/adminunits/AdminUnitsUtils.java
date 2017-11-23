package org.msh.tb.adminunits;

import org.msh.tb.entities.AdministrativeUnit;

/**
 * Administrative unit utilities functions
 * @author Ricardo
 *
 */
public class AdminUnitsUtils {

	/**
	 * Generate HQL instruction to return all children ids of a parent administrative unit
	 * @param parent
	 * @return
	 */
	static public String childrenHQLCriteria(AdministrativeUnit parent, boolean includeSelf) {
		if (parent.getLevel() == 5) {
			if (includeSelf)
				 return "select au0.id from AdministrativeUnit au0 where au0.id = " + parent.getId().toString();
			else return "";
		}
		
		String parentId = parent.getId().toString();
		
		String crit = " where au1.id = " + parentId;
		String join = "from AdministrativeUnit au0 left join au0.parent au1";
		
		if (includeSelf)
			crit = crit + " or au0.id = " + parentId;
		
		if (parent.getLevel() <= 3) {
			join += " left join au1.parent au2";
			crit += " or au2.id = " + parentId;
		}

		if (parent.getLevel() <= 2) {
			join += " left join au2.parent au3";
			crit += " or au3.id = " + parentId;
		}

		if (parent.getLevel() == 1) {
			crit += " or au4.id = " + parentId;
			join += " left join au3.parent au4";
		}

		return "select au0.id " + join + crit;
	}
}
