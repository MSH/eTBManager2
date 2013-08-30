package org.msh.tb.az.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.ExamXRayAZ;
import org.msh.tb.cases.exams.ExamHome;

@Name("examXRayAZHome")
public class ExamXRayAZHome extends ExamHome<ExamXRayAZ>{
	private static final long serialVersionUID = -2673853209390943540L;

	@Factory("examXRayAZ")
	public ExamXRayAZ getInstance() {
		return super.getInstance();
	}
}
