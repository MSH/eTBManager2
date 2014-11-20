package org.msh.tb.bd.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;


@Name("medicalExaminationBdHome")
public class MedicalExaminationBdHome extends ExamHome<MedicalExaminationBd>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2680118113742531608L;

	@Factory("medicalExaminationBd")
	public MedicalExaminationBd getMedicalExaminationBd() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		return super.persist();
	}
}
