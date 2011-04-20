package org.msh.tb.na;


import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.log.LogInfo;
import org.msh.tb.na.entities.ExamHIV_NA;

@Name("examHIVHomeNA")
@LogInfo(roleName="EXAM_HIV")
public class ExamHIVHome extends ExamHome<ExamHIV_NA> {
	private static final long serialVersionUID = 5431512237255765820L;

	@Factory("examHIVNA")
	public ExamHIV_NA getExamHIV_NA() {
		return getInstance();
	}
	
}
