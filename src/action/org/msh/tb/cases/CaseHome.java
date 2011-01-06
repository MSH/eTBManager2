package org.msh.tb.cases;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Identity;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.log.LogInfo;


/**
 * Handle basic operations with a TB/MDR case. Check {@link CaseEditingHome} for notification and editing of case data
 * Specific operations concerning exams, case regimes, heath units and medical consultations are handled by other classes.
 * @author Ricardo Memória
 *
 */
@Name("caseHome")
@LogInfo(roleName="CASE_DATA")
@BypassInterceptors
public class CaseHome extends EntityHomeEx<TbCase>{
	private static final long serialVersionUID = -8072727373977321407L;

	private List<TbCase> otherCases;
	private Boolean hasIssues;
	private List<TreatmentHealthUnit> treatmentHealthUnits;


	/**
	 * Return an instance of a {@link TbCase} class
	 * @return
	 */
	@Factory("tbcase")
	public TbCase getTbCase() {
		return getInstance();
	}


	
	/**
	 * Removes a TB/MDR-TB case from the system according to the case id set in the property <i>id</i>.
	 * All information about the case is removed, like exams, treatments, regimes and health units.
	 **/
	@Override
	public String remove() {
		String ret = super.remove();
		if (!ret.equals("removed"))
			return ret;
		
		Patient patient = getInstance().getPatient();
		
		Long count = (Long)getEntityManager()
			.createQuery("select count(*) from TbCase c where c.patient.id = :id")
			.setParameter("id", patient.getId())
			.getSingleResult();
		
		if (count == 0) {
			getEntityManager().remove(patient);
		}
		return ret;
	}


	/**
	 * Return if case is under treatment
	 * @return
	 */
	public boolean isInTreatment() {
		return (getInstance().getHealthUnits().size() > 0);
	}


	/**
	 * Return list of treatment health units of the case (exclude the health unit that is being transferred);
	 * @return
	 */
	public List<TreatmentHealthUnit> getTreatmentHealthUnits() {
		if (treatmentHealthUnits == null) {
			treatmentHealthUnits = new ArrayList<TreatmentHealthUnit>();
			for (TreatmentHealthUnit hu: getInstance().getHealthUnits()) {
				if (!hu.isTransferring()) {
					treatmentHealthUnits.add(hu);
				}
			}
		}
		return treatmentHealthUnits;
	}

	
	/**
	 * Return the health unit where patient is being transferred to
	 * @return
	 */
	public TreatmentHealthUnit getTransferInHealthUnit() {
		for (TreatmentHealthUnit hu: getInstance().getHealthUnits()) {
			if (hu.isTransferring())
				return hu;
		}
		return null;
	}

	
	/**
	 * Check if role name suffix is allowed to the case according to its classification
	 * @param sufixName
	 * @return
	 */
	protected boolean checkRoleBySuffix(String suffixName) {
		TbCase tbcase = getInstance();

		return Identity.instance().hasRole(tbcase.getClassification().toString() + "_" + suffixName);
	}



	/**
	 * Check if the case can be validated
	 * @return true if can be validated
	 */
	public boolean isCanValidate() {
		ValidationState vs = getInstance().getValidationState();
		return ((vs == ValidationState.WAITING_VALIDATION) || (vs == ValidationState.PENDING_ANSWERED)) && (checkRoleBySuffix("CASE_VALIDATE"));
	}

	
	/**
	 * Check if user can create a new issue for the case
	 * @return
	 */
	public boolean isCanCreateIssue() {
		return (getInstance().getValidationState() != ValidationState.VALIDATED);
	}

	
	/**
	 * Check if user can answer a pending case
	 * @return
	 */
	public boolean isCanAnswerIssue() {
		return (getInstance().getValidationState() == ValidationState.PENDING);
	}
	
	/**
	 * Check if the case can be transfered to another health unit
	 * @return true if can be transfered
	 */
	public boolean isCanTransferOut() {
		TbCase tbcase = getInstance();
		return (tbcase.isOpen()) && (tbcase.getState() == CaseState.ONTREATMENT) && (checkRoleBySuffix("CASE_TRANSFER"));
	}
	
	public boolean isCanTransferIn() {
		TbCase tbcase = getInstance();
		return (tbcase.isOpen()) && (tbcase.getState() == CaseState.TRANSFERRING) && (checkRoleBySuffix("CASE_TRANSFER"));
	}
	
	public boolean isCanViewExams() {
		return checkRoleBySuffix("CASE_EXAMS");
	}
	
	public boolean isCanViewTreatment() {
		return checkRoleBySuffix("CASE_TREAT");
	}
	
	public boolean isCanViewTreatmentCalendar() {
		return checkRoleBySuffix("CASE_INTAKEMED");
	}
	
	public boolean isCanViewDrugogram() {
		return checkRoleBySuffix("CASE_DRUGOGRAM");
	}
	
	public boolean isCanClose() {
		return (getInstance().isOpen()) && (checkRoleBySuffix("CASE_CLOSE"));
	}
	
	public boolean isCanReopen() {
		return (!getInstance().isOpen()) && (checkRoleBySuffix("CASE_REOPEN"));
	}
	
	@Override
	public boolean isCanOpen() {
		return checkRoleBySuffix("CASE_VIEW");
	}
	
	/**
	 * Check if case data can be displayed (patient data and clinical view)
	 * @return
	 */
	public boolean isCanViewCaseData() {
		return checkRoleBySuffix("CASE_DATA");
	}
		
	public boolean isCanViewAditionalInfo() {
		return checkRoleBySuffix("CASE_ADDINFO");
	}
	
	public boolean isCanEditCaseData() {
		return (getInstance().isOpen()) && checkRoleBySuffix("CASE_DATA_EDT");
	}
	
	public boolean isCanEditTreatment() {
		return (getInstance().isOpen()) && (checkRoleBySuffix("CASE_TREAT_EDT"));
	}

	public boolean isCanEditTreatmentCalendar() {
		return (getInstance().isOpen()) && (checkRoleBySuffix("CASE_INTAKEMED_EDT"));		
	}
	
	public boolean isCanEditExams() {
		return getInstance().isOpen() && checkRoleBySuffix("CASE_EXAMS_EDT");
	}
	
	public boolean isCanEditAditionalInfo() {
		return getInstance().isOpen() && checkRoleBySuffix("CASE_ADDINFO_EDT");
	}
	
	@Override
	public Workspace getInstanceWorkspace() {
		Patient p = getInstance().getPatient();
		return (p != null? p.getWorkspace(): null);
	}
	
	
	@Override
	public CaseClassification getCaseClassificationForLog() {
		return getInstance().getClassification();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#initTransactionLog()
	 */
/*	@Override
	public void initTransactionLog() {
		super.initTransactionLog();
	}
*/	
	public List<TbCase> getOtherCases() {
		if (otherCases == null) {
			otherCases = getEntityManager().createQuery("from TbCase c " +
					"where c.patient.id = #{tbcase.patient.id} and c.id <> #{tbcase.id} " +
					"order by c.registrationDate")
					.getResultList();
		}
		return otherCases;
	}
	
	/**
	 * Return if the case has issues reported during validation phase
	 * @return true - if has issues
	 */
	public boolean isHasIssue() {
		if (hasIssues == null) {
			Long numIssues = (Long)getEntityManager().createQuery("select count(*) from CaseIssue c where c.case.id = :id")
				.setParameter("id", getInstance().getId())
				.getSingleResult();
			hasIssues = numIssues > 0;
		}
		return hasIssues;
	}


	/**
	 * Initialize view of the detail page of the case
	 */
	public void initView() {
		CaseFilters filters = (CaseFilters)Component.getInstance("caseFilters", true);
		CaseView view = filters.getCaseView();

		boolean bOK = ((CaseView.DATA.equals(view) || (CaseView.MEDEXAMS.equals(view))) && (isCanViewCaseData())) ||
		((CaseView.TREATMENT.equals(view)) && (isCanViewTreatment())) ||
		((CaseView.EXAMS.equals(view)) && (isCanViewExams())) ||
		((CaseView.ADDINFO.equals(view)) && (isCanViewAditionalInfo())) ||
		((CaseView.RESUME.equals(view)) && (isCanViewDrugogram()));
		
		if (bOK)
			return;

		// find available view
		if (isCanViewCaseData())
			filters.setCaseView(CaseView.DATA);
		else
		if (isCanViewExams())
			filters.setCaseView(CaseView.EXAMS);
		else
		if (isCanViewTreatment())
			filters.setCaseView(CaseView.TREATMENT);
		else
		if (isCanViewAditionalInfo())
			filters.setCaseView(CaseView.ADDINFO);
		else
		if (isCanViewDrugogram())
			filters.setCaseView(CaseView.RESUME);
		else filters.setCaseView(null);
	}
}
