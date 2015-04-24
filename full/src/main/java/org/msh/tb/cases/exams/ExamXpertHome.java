package org.msh.tb.cases.exams;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.etbm.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.tb.resistpattern.ResistancePatternService;

@Name("examXpertHome")
@LogInfo(roleName="EXAM_XPERT", entityClass=ExamXpert.class)
@Scope(ScopeType.EVENT)
public class ExamXpertHome extends LaboratoryExamHome<ExamXpert> {
	private static final long serialVersionUID = -1014269108674534236L;

    @In
    FacesMessages facesMessages;

	private static final XpertResult results[] = {
        XpertResult.TB_DETECTED,
        XpertResult.TB_NOT_DETECTED,
        XpertResult.INVALID_NORESULT_ERROR
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

        if(exam.getStatus().equals(ExamStatus.PERFORMED) && exam.getResult() == null){
            facesMessages.addToControlFromResourceBundle("resultfield", "javax.faces.component.UIInput.REQUIRED");
            return "error";
        }else if(!exam.getStatus().equals(ExamStatus.PERFORMED)){
            exam.setResult(null);
            exam.setDateRelease(null);
            exam.setComments(null);
            exam.setRifResult(null);
        }

		if (!XpertResult.TB_DETECTED.equals( exam.getResult() ))
			exam.setRifResult(null);
		else {
			if (exam.getRifResult() == null) {
				FacesMessages.instance().addToControlFromResourceBundle("rifres", "javax.faces.component.UIInput.REQUIRED");
				return "error";
			}
		}
		
		// if it's on-going, some fields must be empty
		if (XpertResult.ONGOING.equals(exam.getResult())) {
			exam.setDateRelease(null);
		}
		
		String s = super.persist();
		
		if ("persisted".equals(s)) {
			// update resistance pattern
			ResistancePatternService srv = (ResistancePatternService)Component.getInstance("resistancePatternService");
			srv.updateCase(exam.getTbcase());
		}
		
		return  s;
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

	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.LaboratoryExamHome#remove()
	 */
	@Override
	public String remove() {
		String s = super.remove();
		if ("exam-removed".equals(s)) {
			ResistancePatternService srv = (ResistancePatternService)Component.getInstance("resistancePatternService");
			srv.updateCase(getInstance().getTbcase());
		}
		
		return s;
	}
}
