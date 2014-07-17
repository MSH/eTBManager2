package org.msh.tb.ng;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.ng.entities.ExamHIV_Ng;


@Name("examHIVHomeNg")
public class ExamHIVHome extends ExamHome<ExamHIV_Ng> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7388048431788005218L;

	@Factory("examHIVNg")
	public ExamHIV_Ng getExamHIV_Ng() {
		return getInstance();
	}
	
}

