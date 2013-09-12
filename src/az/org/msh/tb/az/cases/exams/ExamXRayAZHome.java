package org.msh.tb.az.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.ExamXRayAZ;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.ua.entities.MedicineReceivingUA;

@Name("examXRayAZHome")
@LogInfo(roleName="EXAM_XRAY",entityClass=ExamXRayAZ.class)
public class ExamXRayAZHome extends ExamHome<ExamXRayAZ>{
	private static final long serialVersionUID = -2673853209390943540L;

	@Factory("examXRayAZ")
	public ExamXRayAZ getInstance() {
		return super.getInstance();
	}
}
