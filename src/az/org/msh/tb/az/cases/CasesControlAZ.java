package org.msh.tb.az.cases;


import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.CaseSeverityMark;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.cases.treatment.StartTreatmentIndivHome;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.tbunits.TBUnitSelection;

/**
 * Class-controller for cases-unit
 * @author A.M.
 */

@Name("casesControlAZ")
public class CasesControlAZ {
	
	//==========================CONFIRM FOR START TREATMENT==================================
	/**
	 * Return true, if select tb-unit not where user from
	 */
	public boolean notOwnTBUnit(){
		TBUnitSelection nTbUSel;
		StartTreatmentHome th = (StartTreatmentHome)App.getComponent("startTreatmentHome",false);
		if (th!=null)
			nTbUSel = th.getTbunitselection();
		else{
			StartTreatmentIndivHome tih = (StartTreatmentIndivHome)App.getComponent("startTreatmentIndivHome",false);
			nTbUSel = tih.getTbunitselection();
		}
		Tbunit nTbU = nTbUSel.getTbunit();
		Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		if (nTbU==null || nTbUUser==null) return false;
		if (nTbU.getId().intValue() != nTbUUser.getId().intValue())
			return true;
		return false;
	}
	/**
	 * Set tb-unit from database
	 */
	public void setUserTBUnitDefault(){
		//Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		TBUnitSelection nTbUSel;
		CaseHome caseHome = (CaseHome)App.getComponent("caseHome");
		Tbunit nTbUUser = caseHome.getTbCase().getOwnerUnit();
		StartTreatmentHome th = (StartTreatmentHome)App.getComponent("startTreatmentHome",false);
		if (th!=null)
			nTbUSel = th.getTbunitselection();
		else{
			StartTreatmentIndivHome tih = (StartTreatmentIndivHome)App.getComponent("startTreatmentIndivHome",false);
			nTbUSel = tih.getTbunitselection();
		}
		nTbUSel.setTbunitWithOptions(nTbUUser);
	}
	
	/**
	 * Override the method save() CaseSeverityMarksHome class. Add flush after remove
	 */
	public static void CaseSeverityMarksHomeSave() {
		CaseSeverityMarksHome csmh = (CaseSeverityMarksHome) App.getComponent(CaseSeverityMarksHome.class);
		if (csmh.getSeverityMarks() == null)
			return;
		
		if (!csmh.isEditing())
			throw new RuntimeException("SymptomsHome not in edit mode");
		
		for (CaseSeverityMark sym: csmh.getSeverityMarks()) {
			if (!sym.isChecked()) {
				if (App.getEntityManager().contains(sym)){
					App.getEntityManager().remove(sym);
					App.getEntityManager().flush();
				}
			}
			else App.getEntityManager().persist(sym);
		}
	}
}
