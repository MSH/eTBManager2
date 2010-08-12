package org.msh.tb.cases;


import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.tb.log.LogInfo;

@Name("examHIVHome")
@LogInfo(roleName="EXAM_HIV")
public class ExamHIVHome extends ExamHome<ExamHIV> {
	private static final long serialVersionUID = 5431512237255765820L;

	@Factory("examHIV")
	public ExamHIV getExamHIV() {
		return getInstance();
	}
	
}
