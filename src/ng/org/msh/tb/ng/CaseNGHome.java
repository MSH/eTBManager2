package org.msh.tb.ng;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.TreatmentHome;
import org.msh.tb.ng.entities.TbCaseNG;


/**
 * Specific code to handle case management functionalities for Nigerian
 * workspace
 * 
 * @author Vani Rao
 * 
 */
@Name("caseNGHome")
public class CaseNGHome {
	@In(create = true)
	CaseHome caseHome;
	@In(create = true)
	CaseEditingHome caseEditingHome;
	@In(create = true)
	TreatmentHome treatmentHome;
	@In(required = false)
	MedicalExaminationNGHome medicalExaminationNGHome;

	/**
	 * Return an instance of a {@link TbCaseNA} class
	 * 
	 * @return
	 */
	@Factory("tbcaseng")
	public TbCaseNG getTbCaseNG() {
		return (TbCaseNG) caseHome.getTbCase();
	}

	/**
	 * Save a new case for the Nigerian workspace. Don't use the class
	 * {@link CaseEditingHome}, because this class already saves it using the
	 * {@link CaseEditingHome} component
	 * 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveNew() {
		String ret = caseEditingHome.saveNew();
		saveMedicalExamination();
		return ret;
	}

	/**
	 * Save changes made to an already existing case in the Nigerian workspace
	 * 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveEditing() {
		return caseEditingHome.saveEditing();
	}

	/**
	 * Save changes made to an already existing case in the Nigerian workspace
	 * 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveTreatment() {
		return treatmentHome.saveChanges();
	}

	@Transactional
	public String saveTBCase() {
		return "error";
	}

	/**
	 * Save medical examination
	 * 
	 * @return
	 */
	public String saveMedicalExamination() {
		if (medicalExaminationNGHome == null)
			return "error";

		medicalExaminationNGHome.setDisplayMessage(false);
		return medicalExaminationNGHome.persist();
	}

}
