package org.msh.tb.kh;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.kh.entities.MedicalExamination_Kh;


/**
 * Specific code to handle Medical Examination functionalities for Cambodian
 * workspace
 * 
 * @author Vani Rao
 * 
 */
@Name("medicalExaminationKHHome")
public class MedicalExaminationKHHome extends ExamHome<MedicalExamination_Kh>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7014730842973918428L;
	@Factory("medicalExamination_Kh")
	public MedicalExamination_Kh getMedicalExamination() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		return super.persist();
	}
}

