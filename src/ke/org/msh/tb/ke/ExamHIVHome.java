package org.msh.tb.ke;


import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.ke.entities.ExamHIV_Ke;


@Name("examHIVHomeKe")
public class ExamHIVHome extends ExamHome<ExamHIV_Ke> {
	private static final long serialVersionUID = 5431512237255765820L;

	@Factory("examHIVKe")
	public ExamHIV_Ke getExamHIV_Ke() {
		return getInstance();
	}
	
}
