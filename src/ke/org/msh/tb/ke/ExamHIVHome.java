package org.msh.tb.ke;


import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ExamHIV_Ke;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.log.LogInfo;

@Name("examHIVHomeKe")
@LogInfo(roleName="EXAM_HIV")
public class ExamHIVHome extends ExamHome<ExamHIV_Ke> {
	private static final long serialVersionUID = 5431512237255765820L;

	@Factory("examHIVKe")
	public ExamHIV_Ke getExamHIV_Ke() {
		return getInstance();
	}
	
}
