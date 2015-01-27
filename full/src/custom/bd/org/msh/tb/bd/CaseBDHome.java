package org.msh.tb.bd;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.bd.cases.exams.MedicalExaminationBdHome;
import org.msh.tb.bd.entities.TbCaseBD;
import org.msh.tb.bd.entities.enums.PulmonaryTypesBD;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.TreatmentHome;



/**
 * Specific code to handle case management functionalities for Namibia workspace
 * @author Utkarsh Srivastava
 *
 */
@Name("caseBDHome")
public class CaseBDHome {

	@In CaseHome caseHome;
	@In(required=false) MedicalExaminationBdHome medicalExaminationBdHome;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(create=true) TreatmentHome treatmentHome;
	
	/**
	 * Return an instance of a {@link TbCaseBD} class
	 * @return
	 */
	@Factory("tbcasebd")
	public TbCaseBD getTbCaseBD() {
		return (TbCaseBD)caseHome.getInstance();
	}
	
	/**
	 * Save a new case for the Namibian workspace. Don't use the class {@link CaseEditingHome}, because this class
	 * already saves it using the {@link CaseEditingHome} component
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveNew() {
		String ret = caseEditingHome.saveNew();

        validatePulmonaryTypes();

		if(ret.equals("error"))
			return "error";
		
		saveMedicalExamination();
		
		return ret;
	}

    private void validatePulmonaryTypes(){
        if(getTbCaseBD().getPulmonaryTypesBD().equals(PulmonaryTypesBD.POSITIVE))
            getTbCaseBD().setPulmonaryType(null);
    }

	/**
	 * Save changes made to an already existing case in the Namibian workspace
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveEditing() {
        validatePulmonaryTypes();
        return caseEditingHome.saveEditing();
	}
	
	/**
	 * Save changes made to an already existing case in the Namibian workspace
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveTreatment() {
		return treatmentHome.saveChanges(); 
	}	
	
	
	/**
	 * Save medical examination
	 * @return
	 */
	public String saveMedicalExamination() {
		if (medicalExaminationBdHome == null)
			return "error";

		medicalExaminationBdHome.setDisplayMessage(false);
		return medicalExaminationBdHome.persist();
	}	
	
	
	
}
	

