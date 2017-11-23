package org.msh.tb.reportgen;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

import java.util.List;

public class AdminUnitVariable implements Variable {

	private static final String fields[] = {"administrativeunit.code"};
	
	private List<AdministrativeUnit> adminUnits;
	
	private InfoCountryLevels levelInfo;
	
	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#getTitle()
	 */
	@Override
	public String getTitle() {
		return getInfoCountryLevels().getNameLevel1().toString();
	}

	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#createSQLSelectFields(org.msh.utils.reportgen.ReportQuery)
	 */
	@Override
	public String[] createSQLSelectFields(ReportQuery reportQuery) {
		reportQuery.addJoinMasterTable("administrativeunit", "id", "adminunit_id");
		return fields;
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#createSQLGroupByFields(org.msh.utils.reportgen.ReportQuery)
	 */
	@Override
	public String[] createSQLGroupByFields(ReportQuery reportQuery) {
		return fields;
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#translateValues(java.lang.Object[])
	 */
	@Override
	public Object translateValues(Object[] value) {
		return null;
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#translateSingleValue(java.lang.Object)
	 */
	@Override
	public Object translateSingleValue(Object value) {
		for (AdministrativeUnit adm: getAdminUnits()) {
			if (adm.isSameOrChildCode(value.toString()))
				return adm;
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#getValueDisplayText(java.lang.Object)
	 */
	@Override
	public String getValueDisplayText(Object value) {
		if (value == null)
			return null;
		return ((AdministrativeUnit)value).getName().toString();
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Integer compareValues(Object val1, Object val2) {
		if ((val1 == null) && (val2 == null))
			return null;
		
		if (val1 == null)
			return -1;

		if (val2 == null)
			return 1;

		AdministrativeUnit adm1 = (AdministrativeUnit)val1;
		AdministrativeUnit adm2 = (AdministrativeUnit)val2;
		return adm1.getName().toString().compareTo(adm2.getName().toString());
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.Variable#setGrouping(boolean)
	 */
	@Override
	public boolean setGrouping(boolean value) {
		return false;
	}

	@Override
	public Object getGroupData(Object val1) {
		return null;
	}

	
	protected List<AdministrativeUnit> getAdminUnits() {
		if (adminUnits == null) {
			AdminUnitSelection sel = new AdminUnitSelection();
			adminUnits = sel.getOptionsLevel1();
		}
			
		return adminUnits;
	}
	
	/**
	 * Return the list of the structure of the country
	 * @return
	 */
	protected InfoCountryLevels getInfoCountryLevels() {
		if (levelInfo == null)
			levelInfo = (InfoCountryLevels)Component.getInstance("levelInfo");
		return levelInfo;
	}
}
