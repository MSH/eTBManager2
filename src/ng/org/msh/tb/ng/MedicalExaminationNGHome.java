package org.msh.tb.ng;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.ng.entities.MedicalExamination_Ng;
;
/**
 * Specific code to handle Medical Examination functionalities for Nigerian
 * workspace
 * 
 * @author Vani Rao
 * 
 */
@Name("medicalExaminationNGHome")
public class MedicalExaminationNGHome extends ExamHome<MedicalExamination_Ng>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8412334261581230829L;
	
	//private HealthFacility healthFacility;
	
	@Factory("medicalExamination_Ng")
	public MedicalExamination_Ng getMedicalExamination() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		return super.persist();
	}

//	public HealthFacility getHealthFacility() {
//		return healthFacility;
//	}
//	
//	public void setHealthFacility(HealthFacility healthFacility) {
//		this.healthFacility = healthFacility;
//	}


}
