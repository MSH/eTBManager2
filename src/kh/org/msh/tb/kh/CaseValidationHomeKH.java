package org.msh.tb.kh;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.msh.tb.cases.CaseValidationHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.ValidationState;

@Name("caseValidationHomeKH")
@Scope(ScopeType.CONVERSATION)
public class CaseValidationHomeKH extends CaseValidationHome {


	public void initialize() {
		caseHome.getTbCase().setDiagnosisType(DiagnosisType.CONFIRMED);
	}
	
	/**
	 * Validate a case, generating a new patient number if it was not generated yet
	 * @return
	 */
	@Transactional
	@Restrict("#{caseHome.canValidate}")
	public String validate() { 
		TbCase tbcase = caseHome.getInstance();
		ValidationState vstate = tbcase.getValidationState();
		if (vstate == ValidationState.VALIDATED) 
			return "error";

		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);

		return super.validate();
	}

}
