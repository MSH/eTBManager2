package org.msh.tb.cases.exams;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.tb.resistpattern.ResistancePatternService;
import org.msh.tb.transactionlog.LogInfo;

@Name("examXpertHome")
@LogInfo(roleName="EXAM_XPERT")
@Scope(ScopeType.CONVERSATION)
public class ExamXpertHome extends LaboratoryExamHome<ExamXpert> {
	private static final long serialVersionUID = -1014269108674534236L;

	private static final XpertResult results[] = {
		XpertResult.INVALID,
		XpertResult.ERROR,
		XpertResult.NO_RESULT,
		XpertResult.TB_NOT_DETECTED,
		XpertResult.TB_DETECTED,
		XpertResult.ONGOING
	};
	
	@Factory("examGenexpert")
	public ExamXpert getExamGenexpert() {
		return getInstance();
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.LaboratoryExamHome#persist()
	 */
	@Override
	public String persist() {
		ExamXpert exam = getInstance();
		
		if (!XpertResult.TB_DETECTED.equals( exam.getResult() ))
			exam.setRifResult(null);
		else {
			if (exam.getRifResult() == null) {
				FacesMessages.instance().addToControlFromResourceBundle("rifres", "javax.faces.component.UIInput.REQUIRED");
			}
		}
		
		// if it's on-going, some fields must be empty
		if (XpertResult.ONGOING.equals(exam.getResult())) {
			exam.setDateRelease(null);
		}
		
		ResistancePatternService srv = (ResistancePatternService)Component.getInstance("resistancePatternService");
		srv.updateCase(exam.getTbcase());
		
		return super.persist();
	}

	
	/**
	 * @return
	 */
	public XpertResult[] getGenexpertResults() {
		return results;
	}
	
	/**
	 * @return
	 */
	public XpertRifResult[] getRifResults() {
		return XpertRifResult.values();
	}
}
