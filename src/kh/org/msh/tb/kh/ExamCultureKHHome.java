package org.msh.tb.kh;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.LaboratoryExamHome;
import org.msh.tb.kh.entities.ExamCulture_Kh;

import org.msh.tb.transactionlog.LogInfo;

@Name("examCultureHomeKh")
@LogInfo(roleName="EXAM_HIV")
public class ExamCultureKHHome extends LaboratoryExamHome<ExamCulture_Kh>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1298829141838748484L;
	
	@Factory("examCulturekh")
	public ExamCulture_Kh getExamCulture_Kh() {
		return getInstance();
	}
}
