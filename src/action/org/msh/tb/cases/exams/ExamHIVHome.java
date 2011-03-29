package org.msh.tb.cases.exams;


import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.enums.HIVResult;
import org.msh.tb.log.LogInfo;

@Name("examHIVHome")
@LogInfo(roleName="EXAM_HIV")
public class ExamHIVHome extends ExamHome<ExamHIV> {
	private static final long serialVersionUID = 5431512237255765820L;

	@In(create=true) FacesMessages facesMessages;
	
	@Factory("examHIV")
	public ExamHIV getExamHIV() {
		return getInstance();
	}


	@Override
	public String persist() {
		ExamHIV exam = getInstance();
		if ((exam.getDate() == null) && (exam.getResult() != HIVResult.ONGOING)) {
			facesMessages.addToControlFromResourceBundle("edtdate", "javax.faces.component.UIInput.REQUIRED");
			return "error";
		}
		return super.persist();
	}
}
