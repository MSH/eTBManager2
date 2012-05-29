package org.msh.tb.kh;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.kh.entities.ExamHIV_Kh;
import org.msh.tb.transactionlog.LogInfo;

@Name("examHIVHomeKh")
@LogInfo(roleName="EXAM_HIV")
public class ExamHIVHome extends ExamHome<ExamHIV_Kh>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1153689428198606825L;
	
	@Factory("examHIVKh")
	public ExamHIV_Kh getExamHIV_Kh() {
		return getInstance();
	}

}
