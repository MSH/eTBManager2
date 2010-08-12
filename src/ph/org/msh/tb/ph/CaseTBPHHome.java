package org.msh.tb.ph;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Regimen;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;

@Name("caseTBPHHome")
public class CaseTBPHHome {

	@In(create=true) CaseEditingHome caseEditingHome;
	@In CaseHome caseHome;
	
	private TBUnitSelection healthFacilitySelection = new TBUnitSelection(true, TBUnitFilter.TBHEALTH_UNITS);
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
