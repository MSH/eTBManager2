package org.msh.tb.ph;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Regimen;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.tb.tbunits.TBUnitType;

@Name("caseTBPHHome")
public class CaseTBPHHome {

	@In(create=true) CaseEditingHome caseEditingHome;
	@In CaseHome caseHome;
	
	private TBUnitSelection healthFacilitySelection = new TBUnitSelection("unitid", true, TBUnitType.TBHEALTH_UNITS);
	private Regimen regimen;
	
	public String saveTBCase() {
		return "persisted";
	}
	
	/**
	 * @return the regimen
	 */
	public Regimen getRegimen() {
		return regimen;
	}

	/**
	 * @param regimen the regimen to set
	 */
	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}

	public TBUnitSelection getHealthFacilitySelection() {
		return healthFacilitySelection;
	}
	
}
