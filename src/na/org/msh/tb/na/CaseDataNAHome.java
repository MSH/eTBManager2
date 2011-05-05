package org.msh.tb.na;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseValidationHome;
import org.msh.tb.cases.exams.MedicalExaminationHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.cases.treatment.TreatmentHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.na.entities.CaseDataNA;


/**
 * Specific code to handle case management functionalities for Namibia workspace
 * @author Utkarsh Srivastava
 *
 */
@Name("caseDataNAHome")
public class CaseDataNAHome extends EntityHomeEx<CaseDataNA> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6017786643816951651L;
	@In EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	@In(create=true) StartTreatmentHome startTreatmentHome;
	@In(create=true) CaseEditingHome caseEditingHome;
//	@In(create=true) TreatmentHealthUnitHome treatmentHealthUnitHome;
	@In(create=true) CaseValidationHome caseValidationHome;
	@In(create=true) TreatmentHome treatmentHome;
	@In(required=false) MedicalExaminationNAHome medicalExaminationNAHome;
	
	

	/**
	 * Return object with specific fields for Namibia workspace
	 * @return instance of {@link CaseDataNA} class
	 */
	@Factory("caseDataNA")
	public CaseDataNA getCaseDataNA() {
			if (caseHome.getId() != null)
				setId(caseHome.getId());
/*			if (caseHome.getId() != null) {
				entityManager.createQuery("from CaseDataBR c where c.tbcase.id = #{caseHome.id}").getSingleResult();
				getAuselection().setSelectedUnit(getInstance().getAdminUnitUsOrigem());
			}
			setId(caseHome.getId());
*/
		return getInstance();
	}


	/**
	 * Save a new case for the Namibian workspace. Don't use the class {@link CaseEditingHome}, because this class
	 * already saves it using the {@link CaseEditingHome} component
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveNew() {
		String ret = caseEditingHome.saveNew();

		if (!ret.equals("persisted"))
			return ret;

		TbCase tbcase = caseEditingHome.getTbcase();
		CaseDataNA data = getInstance();
		
		data.setTbcase(tbcase);
		data.setId(tbcase.getId());
		
		setDisplayMessage(false);
		return persist();
	}




	/**
	 * Save changes made to an already existing case in the Namibian workspace
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveEditing() {
		if (!caseEditingHome.saveEditing().equals("persisted"))
			return "error";
		
		CaseDataNA data = getInstance();
		TbCase tbcase = caseEditingHome.getTbcase();
		
		if (data.getId() == null)
			data.setId(tbcase.getId());

		setDisplayMessage(false);
		return persist(); 
	}
	
	/**
	 * Save changes made to an already existing case in the Namibian workspace
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveTreatment() {
		if (!treatmentHome.saveChanges().equals("persisted"))
			return "error";
		
		CaseDataNA data = getInstance();
		TbCase tbcase = caseEditingHome.getTbcase();
				
		if (data.getId() == null)
			data.setId(tbcase.getId());

		setDisplayMessage(false);
		return persist(); 
	}	


	@Transactional
	public String saveTBCase() {
		return "error";
	}
	
	
	/**
	 * Validate a case and save the data specific for Philippines
	 * @return "validated" if successfully validated
	 */
	protected String validate() {
		String ret = caseValidationHome.validate();
		if (!("validated".equals(ret)))
			return ret;
		
		persist();
		return ret;
	}
	
	
	/**
	 * Save medical examination
	 * @return
	 */
	public String saveMedicalExamination() {
		if (medicalExaminationNAHome == null)
			return "error";

		medicalExaminationNAHome.setDisplayMessage(false);
		return medicalExaminationNAHome.persist();
	}	
	

	
}
	

