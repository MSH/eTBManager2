package org.msh.tb.cases.exams;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.ExamGenexpert;
import org.msh.tb.entities.enums.GenexpertResult;
import org.msh.tb.transactionlog.LogInfo;

@Name("examGenexpertHome")
@LogInfo(roleName="EXAM_GENEXPERT")
@Scope(ScopeType.CONVERSATION)
public class ExamGenexpertHome extends LaboratoryExamHome<ExamGenexpert> {
	private static final long serialVersionUID = -1014269108674534236L;

	private static final GenexpertResult results[] = {
		GenexpertResult.INVALID,
		GenexpertResult.ERROR,
		GenexpertResult.NO_RESULT,
		GenexpertResult.TB_NOT_DETECTED,
		GenexpertResult.TB_DETECTED,
		GenexpertResult.ONGOING
	};
	
	private static final GenexpertResult rifResults[] = {
		GenexpertResult.RIF_DETECTED,
		GenexpertResult.RIF_NOT_DETECTED,
		GenexpertResult.RIF_INDETERMINATE
	};

	@Factory("examGenexpert")
	public ExamGenexpert getExamGenexpert() {
		return getInstance();
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.LaboratoryExamHome#persist()
	 */
	@Override
	public String persist() {
		ExamGenexpert exam = getInstance();
		
		if (!GenexpertResult.TB_DETECTED.equals( exam.getResult() ))
			exam.setRifResult(null);
		else {
			if (exam.getRifResult() == null) {
				FacesMessages.instance().addToControlFromResourceBundle("rifres", "javax.faces.component.UIInput.REQUIRED");
			}
		}
		
		// if it's on-going, some fields must be empty
		if (GenexpertResult.ONGOING.equals(exam.getResult())) {
			exam.setDateRelease(null);
		}
		
		return super.persist();
	}

	
	/**
	 * @return
	 */
	public GenexpertResult[] getGenexpertResults() {
		return results;
	}
	
	/**
	 * @return
	 */
	public GenexpertResult[] getRifResults() {
		return rifResults;
	}
}
