package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.log.LogInfo;

@Name("examXRayHome")
@LogInfo(roleName="EXAM_XRAY")
public class ExamXRayHome extends ExamHome<ExamXRay> {
	private static final long serialVersionUID = 7241763132119792759L;

	@Factory("examXRay")
	public ExamXRay getExamXRay() {
		return getInstance();
	}
}
