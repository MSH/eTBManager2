package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.MedicalExamination;


@Name("medicalExaminationHome")
public class MedicalExaminationHome extends ExamHome<MedicalExamination>{
	private static final long serialVersionUID = 4240214890485645788L;

	
	@Factory("medicalExamination")
	public MedicalExamination getMedicalExamination() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		return super.persist();
	}
}
