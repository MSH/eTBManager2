package org.msh.tb.br;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.MedicalExamination;
import org.msh.tb.br.entities.MedicalExaminationBR;
import org.msh.tb.cases.exams.ExamHome;

@Name("medicalExaminationBRHome")
public class MedicalExaminationBRHome extends ExamHome<MedicalExaminationBR> {
	private static final long serialVersionUID = -6049339629755904897L;

	
	@Factory("medicalExaminationBR")
	public MedicalExamination getMedicalExamination() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		return super.persist();
	}
}
