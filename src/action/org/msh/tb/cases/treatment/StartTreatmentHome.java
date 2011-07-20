package org.msh.tb.cases.treatment;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;


/**
 * Handle beginning of standard treatment for a case
 * @author Ricardo Memoria
 *
 */
@Name("startTreatmentHome")
@Scope(ScopeType.CONVERSATION)
public class StartTreatmentHome {

	@In(required=true) CaseHome caseHome;
	@In(create=true) CaseRegimenHome caseRegimenHome;
	@In EntityManager entityManager;
	
	private TBUnitSelection tbunitselection;
	private Date iniTreatmentDate;
	
	private boolean saveChages = true;
	
	/**
	 * End of the treatment. This field is not required and is used if the case has already an outcome date
	 */
	private Date endTreatmentDate;
	
	/**
	 * Indicate, when creating a standard script, if the default dose unit defined in the regimen will be applied to the case regimen 
	 */
	private boolean useDefaultDoseUnit;
	

	
	/**
	 * Start a standard regimen for a case
	 * @return "treatment-started" if successfully started, otherwise return "error"
	 */
	public String startStandardRegimen() { 
		if ((getTbunitselection().getTbunit() == null) || (iniTreatmentDate == null))
			return "error";

		TbCase tbcase = caseHome.getInstance();
		
		if (!CaseState.WAITING_TREATMENT.equals(tbcase.getState()))
			return "error";

		entityManager.createQuery("delete from PrescribedMedicine pm where pm.tbcase.id = " + tbcase.getId()).executeUpdate();
		entityManager.createQuery("delete from TreatmentHealthUnit hu where hu.tbcase.id = " + tbcase.getId()).executeUpdate();
		
		caseRegimenHome.setUseDefaultDoseUnit(useDefaultDoseUnit);
		caseRegimenHome.startNewRegimen(iniTreatmentDate, endTreatmentDate, tbunitselection.getTbunit());
		
		// save the initial regimen information
		tbcase.setRegimenIni(tbcase.getRegimen());

		// initialize case data
		tbcase.setState(CaseState.ONTREATMENT);

		if (saveChages)
			caseHome.persist();
		
		return "treatment-started";
	}


	/**
	 * Treatment health unit
	 * @return
	 */
	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null)
			tbunitselection = new TBUnitSelection(true, TBUnitFilter.HEALTH_UNITS);
		return tbunitselection;
	}

	public Date getIniTreatmentDate() {
		return iniTreatmentDate;
	}

	public void setIniTreatmentDate(Date iniTreatmentDate) {
		this.iniTreatmentDate = iniTreatmentDate;
	}


	/**
	 * Update the list of phases and its medicines
	 */
	public void updatePhases() {
		caseRegimenHome.refresh();
	}



	public boolean isUseDefaultDoseUnit() {
		return useDefaultDoseUnit;
	}


	public void setUseDefaultDoseUnit(boolean useDefaultDoseUnit) {
		this.useDefaultDoseUnit = useDefaultDoseUnit;
	}


	public Date getEndTreatmentDate() {
		return endTreatmentDate;
	}


	public void setEndTreatmentDate(Date endTreatmentDate) {
		this.endTreatmentDate = endTreatmentDate;
	}


	public boolean isSaveChages() {
		return saveChages;
	}


	public void setSaveChages(boolean saveChages) {
		this.saveChages = saveChages;
	}


	/**
	 * @return the regimen
	 */
	public Regimen getRegimen() {
		return caseRegimenHome.getRegimen();
	}


	/**
	 * @param regimen the regimen to set
	 */
	public void setRegimen(Regimen regimen) {
		caseRegimenHome.setRegimen( regimen );
	}

}
