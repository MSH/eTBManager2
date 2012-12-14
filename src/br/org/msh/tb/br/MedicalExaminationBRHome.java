package org.msh.tb.br;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.br.entities.MedicalExaminationBR;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.MedicalExaminationHome;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.utils.TextUtils;

/**
 * Implementação específica do {@link MedicalExaminationBR} para a versão Brasileira (SITETB)
 * @author Ricardo Memoria
 *
 */
@Name("medicalExaminationBRHome")
@Scope(ScopeType.CONVERSATION)
public class MedicalExaminationBRHome  {

	@In(required=true) MedicalExaminationHome medicalExaminationHome;
	@In(required=true) CaseHome caseHome;

	/**
	 * Persist changes of a medical examination
	 * @return
	 */
	public String persist() {
		if (!validate())
			return "error";

		adjustMedicalExaminationFields();
		return medicalExaminationHome.persist();
	}

	
	/**
	 * Validate the data of the medical examination for the Brazilian version
	 * @return
	 */
	public boolean validate() {
		boolean validated = true;

		CaseHome caseHome = (CaseHome)Component.getInstance("caseHome");
		
		MedicalExaminationBR medexam = getInstance();
		if(medexam.getDate().before(caseHome.getTbCase().getDiagnosisDate())){
			FacesMessages.instance().addToControlFromResourceBundle("medexamdate", "cases.medexamdate.val1");
			validated = false;
		}
		
		// check if the nextappointment if before the examination
		if(getInstance().getNextAppointment().before(getInstance().getDate())){
			FacesMessages.instance().addToControlFromResourceBundle("nextexam", "cases.medexamdate.val2");
			validated = false;
		}
		
		return validated;
	}
	
	/**
	 * Adjust values of the medical examination fields
	 */
	protected void adjustMedicalExaminationFields() {
		MedicalExaminationBR medExam = (MedicalExaminationBR)medicalExaminationHome.getInstance();

		TextUtils.setPropertyUpperCase(medExam, "positionResponsible");
		TextUtils.setPropertyUpperCase(medExam, "responsible");
		
		if (!YesNoType.YES.equals(medExam.getSupervisedTreatment()))
			medExam.setSupervisionUnitName(null);
		
		TextUtils.setPropertyUpperCase(medExam, "supervisionUnitName");

		if(medExam.getSupervisedTreatment().equals(YesNoType.NO))
			medExam.setSupervisionUnitName(null);
	}

	/**
	 * @return
	 */
	public MedicalExaminationBR getInstance() {
		return (MedicalExaminationBR)medicalExaminationHome.getInstance();
	}

	public MedicalExaminationHome getHome() {
		return medicalExaminationHome;
	}
	
	public boolean isFirstExam(Integer examId){
		MedicalExamination firstExam = null;
		
		if(examId == null || caseHome.getTbCase().getExaminations() == null || caseHome.getTbCase().getExaminations().size() <= 0){
			return true;
		}else{
			firstExam = caseHome.getInstance().getExaminations().get(caseHome.getInstance().getExaminations().size()-1);
		}
		
		if(firstExam != null && firstExam.getId().equals(examId)){
			return true;
		}else{
			return false;
		}
	}
}
