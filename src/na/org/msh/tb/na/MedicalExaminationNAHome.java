package org.msh.tb.na;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.na.entities.MedicalExaminationNA;

@Name("medicalExaminationNAHome")
public class MedicalExaminationNAHome extends ExamHome<MedicalExaminationNA> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7854010740906578780L;

	@Factory("medicalExaminationNA")
	public MedicalExaminationNA getMedicalExamination() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		return super.persist();
	}
}
