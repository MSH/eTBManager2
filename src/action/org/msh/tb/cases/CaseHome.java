package org.msh.tb.cases;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.Identity;
import org.msh.tb.ETB;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.transactionlog.LogInfo;



/**
 * Handle basic operations with a TB/MDR case. Check {@link CaseEditingHome} for notification and editing of case data
 * Specific operations concerning exams, case regimes, heath units and medical consultations are handled by other classes.
 * @author Ricardo Memória
 *
 */
@Name("caseHome")
@LogInfo(roleName="CASE_DATA")
@BypassInterceptors
@AutoCreate
public class CaseHome extends WsEntityHome<TbCase>{
	private static final long serialVersionUID = -8072727373977321407L;

	private List<TbCase> otherCases;
	private Boolean hasIssues;
	private List<TreatmentHealthUnit> treatmentHealthUnits;
	
	private Integer newPatientNumber;
	private Integer newCaseNumber;


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
	 * Update tags of the case
	 */
	public void updateCaseTags() {
		TagsCasesHome.instance().updateTags(getInstance());
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
	public boolean checkRoleBySuffix(String suffixName) {
		TbCase tbcase = getInstance();

		CaseClassification cla = tbcase.getClassification();
		
		if (cla == null)
			 return false;
		else return Identity.instance().hasRole(cla.toString() + "_" + suffixName);
	}


	/**
	 * Check if user is working on its working unit. It depends on the case state and the user profile.
	 * 1) If user can play activities of all other units, so it's the working unit;
	 * 2) If case is waiting for treatment, the user unit is compared to the case notification unit;
	 * 3) If case is on treatment, the user unit is compared to the case treatment unit;
	 * @return
	 */
	public boolean isWorkingUnit() {
		UserWorkspace ws = (UserWorkspace)Component.getInstance("userWorkspace");
		if (ws.isPlayOtherUnits())
			return true;

		TbCase tbcase = getInstance();
		Tbunit treatmentUnit = tbcase.getOwnerUnit();

		if (treatmentUnit != null)
			return (treatmentUnit.getId().equals(ws.getTbunit().getId()));

		Tbunit unit = tbcase.getNotificationUnit();
		if (unit != null)
			return ((unit != null) && (unit.getId().equals(ws.getTbunit().getId())));

		return true;
	}
	

	/**
	 * Check if the case can be validated
	 * @return true if can be validated
	 */
	public boolean isCanValidate() {
		if (!isManaged())
			return false;
		ValidationState vs = getInstance().getValidationState();
		return ((vs == ValidationState.WAITING_VALIDATION) || (vs == ValidationState.PENDING_ANSWERED)) && (checkRoleBySuffix("CASE_VALIDATE") && (isWorkingUnit()));
	}

	
	/**
	 * Check if user can create a new issue for the case
	 * @return
	 */
	public boolean isCanCreateIssue() {
		return (checkRoleBySuffix("CASE_VALIDATE") && (getInstance().getValidationState() != ValidationState.VALIDATED) && (isWorkingUnit()));
	}

	
	/**
	 * Check if user can answer a pending case
	 * @return
	 */
	public boolean isCanAnswerIssue() {
		return ((isManaged()) && (getInstance().getValidationState() == ValidationState.PENDING) && (isWorkingUnit()));
	}
	
	/**
	 * Check if the case can be transfered to another health unit
	 * @return true if can be transfered
	 */
	public boolean isCanTransferOut() {
		TbCase tbcase = getInstance();
		return (tbcase.isOpen()) && (tbcase.getState() == CaseState.ONTREATMENT) && (checkRoleBySuffix("CASE_TRANSFER")) && (isWorkingUnit());
	}
	
	public boolean isCanTransferIn() {
		TbCase tbcase = getInstance();
		return (tbcase.isOpen()) && (tbcase.getState() == CaseState.TRANSFERRING) && (checkRoleBySuffix("CASE_TRANSFER")) && (isWorkingUnit());
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
		return (getInstance().isOpen()) && (checkRoleBySuffix("CASE_CLOSE")) && isWorkingUnit();
	}
	
	public boolean isCanReopen() {
		return (!getInstance().isOpen()) && (checkRoleBySuffix("CASE_REOPEN")) && isWorkingUnit();
	}

	
	public boolean isCanViewCase() {
		if (!isManaged())
			 return false;
		else return checkRoleBySuffix("CASE_VIEW");
	}
	
	@Override
	public boolean isCanOpen() {
		return true;
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
		return (getTbCase().isOpen()) && checkRoleBySuffix("CASE_DATA_EDT") && (isWorkingUnit());
	}
	
	public boolean isCanRemoveCaseData() {
		if(getTbCase().getValidationState().equals(ValidationState.VALIDATED))
			return (getTbCase().isOpen()) && checkRoleBySuffix("CASE_DEL_VAL") && (isWorkingUnit());
		else
			return (getTbCase().isOpen()) && checkRoleBySuffix("CASE_DATA_EDT") && (isWorkingUnit());
	}
	
	public boolean isCanStartTreatment() {
		CaseState st = getInstance().getState();
		return getTbCase().isOpen() && (isManaged()) && (st != null) && (st.ordinal() < CaseState.ONTREATMENT.ordinal()) && (isCanEditTreatment());
	}
	
	public boolean isCanEditTreatment() {
		return getTbCase().isOpen() && (getInstance().isOpen()) && (checkRoleBySuffix("CASE_TREAT_EDT") && (isWorkingUnit()));
	}

	public boolean isCanEditTreatmentCalendar() {
		return getTbCase().isOpen() && (getInstance().isOpen()) && (checkRoleBySuffix("CASE_INTAKEMED_EDT") && (isWorkingUnit()));
	}
	
	public boolean isCanEditExams() {
		return getTbCase().isOpen() && checkRoleBySuffix("CASE_EXAMS_EDT") && isWorkingUnit();
	}
	/**
	 * Several exams results may be add to closed cases, because of long term to get such results
	 * AK - 05/07/2012
	 * @return
	 */
	public boolean isCanEditExamsInClosedCases() {
		return checkRoleBySuffix("CASE_EXAMS_EDT") && isWorkingUnit();
	}
	
	public boolean isCanEditAditionalInfo() {
		return getTbCase().isOpen() && checkRoleBySuffix("CASE_ADDINFO_EDT") && isWorkingUnit();
	}
	
	public boolean isCanAddComments() {
		return checkRoleBySuffix("CASE_COMMENTS") && isWorkingUnit();
	}
	
	public boolean isCanTagCase() {
		return checkRoleBySuffix("CASE_TAG") && isWorkingUnit();
	}
	
	public boolean isCanInsertFollowUpForm() {
		return (isCanEditCaseData() || isCanEditExams() || isCanEditTreatment() || isCanEditTreatmentCalendar()) 
					&& isWorkingUnit();
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
			otherCases = getEntityManager().createQuery("from " + ETB.getWsClassName(TbCase.class) + " c " +
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



	/**
	 * @return the newPatientNumber
	 */
	public Integer getNewPatientNumber() {
		return newPatientNumber;
	}



	/**
	 * @param newPatientNumber the newPatientNumber to set
	 */
	public void setNewPatientNumber(Integer newPatientNumber) {
		this.newPatientNumber = newPatientNumber;
	}



	/**
	 * @return the newCaseNumber
	 */
	public Integer getNewCaseNumber() {
		return newCaseNumber;
	}



	/**
	 * @param newCaseNumber the newCaseNumber to set
	 */
	public void setNewCaseNumber(Integer newCaseNumber) {
		this.newCaseNumber = newCaseNumber;
	}

	public String changeNumber() {
		if (!isManaged())
			return "error";
		
		if (newPatientNumber != null)
			getInstance().getPatient().setRecordNumber(newPatientNumber);
		
		if (newCaseNumber != null)
			getInstance().setCaseNumber(newCaseNumber);
		
		getEntityManager().persist(getInstance().getPatient());
		persist();
		return "number-changed";
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#getRoleName()
	 */
	@Override
	public String getRoleName(RoleAction action) {
		if (action != RoleAction.NEW)
			return super.getRoleName(action);
		
		if (getInstance().getDiagnosisType() == DiagnosisType.CONFIRMED)
			 return "NEWCASE";
		else return "NEWSUSP";
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#saveTransactionLog(org.msh.tb.entities.enums.RoleAction)
	 */
	@Override
	protected void saveTransactionLog(RoleAction action) {
		if (action == RoleAction.NEW) {
			String s = "#{" + getInstance().getClassification().getKey() + "}";
			getLogService().setTitleSuffix(s);
		}
		super.saveTransactionLog(action);
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#getLogEntityClass()
	 */
	@Override
	public String getLogEntityClass() {
		return TbCase.class.getSimpleName();
	}
	
	
	/**
	 * Return status string to be displayed to the user about the main status of the case
	 * @param tbcase
	 * @return
	 */
	public String getStatusString(TbCase tbcase) {
		Map<String, String> msgs = Messages.instance();

		SimpleDateFormat f = new SimpleDateFormat("MMM-yyyy");

		// is suspect and waiting for treatment ?
		if ((tbcase.getState() == CaseState.WAITING_TREATMENT) && (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT)) {
			if (tbcase.getRegistrationDate() != null)
				 return MessageFormat.format(msgs.get("cases.sit.SUSP.date"), f.format(tbcase.getRegistrationDate()));
			else return null;
		}

		// is confirmed waiting for treatment ?
		if (tbcase.getState() == CaseState.WAITING_TREATMENT) {
			if (tbcase.getDiagnosisDate() != null)
				return MessageFormat.format(msgs.get("cases.sit.CONF.date"), f.format( tbcase.getDiagnosisDate() ));
		}

		if ((tbcase.getState() == CaseState.ONTREATMENT) || (tbcase.getState() == CaseState.TRANSFERRING)) {
			if (tbcase.getTreatmentPeriod().getIniDate() != null)
				return MessageFormat.format(msgs.get("cases.sit.ONTREAT.date"), f.format( tbcase.getTreatmentPeriod().getIniDate() ));
		}

		if (tbcase.getState().ordinal() > CaseState.TRANSFERRING.ordinal()) {
			if (tbcase.getOutcomeDate() != null)
				return MessageFormat.format(msgs.get("cases.sit.OUTCOME.date"), f.format( tbcase.getOutcomeDate() ));
		}
		
		return null;		
	}
	
	
	/**
	 * Return second status string to be displayed to the user about the case
	 * @param tbcase
	 * @return
	 */
	public String getStatusString2(TbCase tbcase) {
		if (tbcase.getState() == CaseState.WAITING_TREATMENT) {
			if (tbcase.getDiagnosisType() == DiagnosisType.CONFIRMED)
				return Messages.instance().get(CaseState.WAITING_TREATMENT.getKey());
			else return Messages.instance().get("cases.sit.SUSP");
		}
		
		if (tbcase.getState() == CaseState.ONTREATMENT)
			return null;
		
		return Messages.instance().get(tbcase.getState().getKey());
	}
}
