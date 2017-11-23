package org.msh.tb.cases;


import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.application.App;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.cases.exams.MedicalExaminationHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.*;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.tb.tbunits.TBUnitType;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Handle TB and DR-TB cases editing and new notification
 * @author Ricardo Memoria
 *
 */
@Name("caseEditingHome")
@Scope(ScopeType.CONVERSATION)
@LogInfo(roleName="CASE_DATA", entityClass=TbCase.class)
public class CaseEditingHome {

	@In(create=true) protected CaseHome caseHome;
	@In(create=true)
	protected PatientHome patientHome;
	@In
	protected EntityManager entityManager;
	@In(create=true) protected PrevTBTreatmentHome prevTBTreatmentHome;
	@In(create=true) MedicalExaminationHome medicalExaminationHome;
	@In(create=true) protected FacesMessages facesMessages;
	
	@In(required=false) StartTreatmentHome startTreatmentHome;
	@In(required=false) ExamMicroscopyHome examMicroscopyHome;
	@In(required=false) ExamCultureHome examCultureHome;

	private TBUnitSelection tbunitselection;
	private AdminUnitSelection notifAdminUnit;
	private AdminUnitSelection currentAdminUnit;
	private boolean initialized;

    /*patientid: Identify the id of the patient DRTB case used to search its BMU Tb case
    * regdate: identify the registration date used to search its BMU Tb case
    * BMUTbcase: The TBcase found*/
    private HashMap<String, Object> BMUcase;

	/**
	 * 1 - Standard, 2 - Individualized
	 */
	protected int regimenType;
	
	
	public String selectPatientData() {
		initialized = false;
		if (initializeNewNotification().equals("initialized"))
			return "/cases/casenew.xhtml";
		return "error";
	}

	/**
	 * Initialize a new notification
	 */
	public String initializeNewNotification() {
		if (initialized)
			return "initialized";
		
		Patient p = patientHome.getInstance();

		patientHome.setTransactionLogActive(false);
        patientHome.setDisplayMessage(false);
		
		if (caseHome.getInstance().getClassification() == null)
			return "/cases/index.xhtml";
		
		if ((!patientHome.isManaged()) && 
			(p.getName() == null) && (p.getMiddleName() == null) && (p.getLastName() == null) &&
			(p.getBirthDate() == null))
		return "patient-searching";
		
		caseHome.getInstance().setPatient(patientHome.getInstance());
		
		if (p.getBirthDate() != null)
			updatePatientAge();
		
		// initialize default values
		CasesViewController ctrl = (CasesViewController) App.getComponent("casesViewController");
		if (ctrl != null && ctrl.getSelectedUnit() != null) {
			getTbunitselection().setSelected(ctrl.getSelectedUnit());
			getTbunitselection().setReadOnly(true);
		}

		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if (userWorkspace != null) {
/*
			if (userWorkspace.getTbunit().isNotifHealthUnit()) {
				getTbunitselection().setSelected(userWorkspace.getTbunit());
			}
*/

			AdministrativeUnit au = userWorkspace.getAdminUnit();
			if (au == null)
				au = userWorkspace.getTbunit().getAdminUnit();
			
			if (au != null) {
				au = entityManager.find(AdministrativeUnit.class, au.getId());
				
				getNotifAdminUnit().setSelectedUnit(au);

				if (getTbunitselection().getSelected() == null) {
					List<AdministrativeUnit> lst = getTbunitselection().getAdminUnits();
					
					if (lst != null)
					for (AdministrativeUnit adminUnit: lst) {
						if (adminUnit.isSameOrChildCode(au.getCode())) {
							getTbunitselection().setAdminUnit(adminUnit);
						}
					}					
				}
			}

            updateBMUinfo();
		}

		// initialize items with previous TB treatments
//		prevTBTreatmentHome.getTreatments();
		
		Events.instance().raiseEvent("new-notification");

		initialized = true;
		return "initialized";
	}


	/**
	 * Update the patient's age according to his birth date and the diagnosis date 
	 */
	public void updatePatientAge() {
		Date dtBirth = caseHome.getInstance().getPatient().getBirthDate();
		if (dtBirth == null)
			return;
		
		Date dtDiag = caseHome.getInstance().getDiagnosisDate();
		if (dtDiag == null)
			dtDiag = new Date();
		
		int age = DateUtils.yearsBetween(dtBirth, dtDiag);
		caseHome.getInstance().setAge(age);
	}


	
	/**
	 * Initialize the caseHome object for editing
	 */
	public void initializeEditing() {
		if (initialized)
			return;
		
		prevTBTreatmentHome.setEditing(true);
//		prevTBTreatmentHome.setNumItems(prevTBTreatmentHome.getTreatments().size());
		
		TbCase tbcase = caseHome.getInstance();
		Address addr = tbcase.getCurrentAddress();
		if (addr == null)
			tbcase.setCurrentAddress(new Address());
		addr = tbcase.getNotifAddress();
		if (addr == null)
			tbcase.setNotifAddress(new Address());
		
		if (getTbunitselection().getSelected() == null)
			tbunitselection.setSelected(tbcase.getNotificationUnit());
		
		if (getNotifAdminUnit().getSelectedUnit() == null)
			notifAdminUnit.setSelectedUnit(tbcase.getNotifAddress().getAdminUnit());
		if (getCurrentAdminUnit().getSelectedUnit() == null)
			currentAdminUnit.setSelectedUnit(tbcase.getCurrentAddress().getAdminUnit());

		updatePatientAge();

		initialized = true;
	}


	
	/**
	 * Check if the register date is valid. If not, return the register date of the case that is already 
	 * registered in the period
	 * @return Register date of the case that is in the date registered
	 */
	public Date registerDateValid() {
		if (caseHome.getInstance().getPatient().getId() == null)
			return null;
		
		Date dt = caseHome.getInstance().getRegistrationDate();

		try {
			// uses a range of one week
			Date dt2 = DateUtils.incDays(dt, -7);
			Date dt3 = DateUtils.incDays(dt, 7);

			return (Date)entityManager.createQuery("select max(registerDate) from TbCase c " +
					"where c.registerDate = :dt or (c.registerDate <= :dt2 and c.treatmentPeriod.endDate >= :dt2) " +
					"and c.paitent.id = #{tbcase.patient.id}")
					.setParameter("dt", dt)
					.setParameter("dt2", dt2)
					.setParameter("dt3", dt3)
					.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}



	/**
	 * Save changes made to a case
	 * @return
	 */
	public String saveEditing() {
		if (!validateData())
			return "error";
		
		TbCase tbcase = caseHome.getInstance();

    	tbcase.setNotificationUnit(tbunitselection.getSelected());
		tbcase.getNotifAddress().setAdminUnit(notifAdminUnit.getSelectedUnit());
		tbcase.getCurrentAddress().setAdminUnit(currentAdminUnit.getSelectedUnit());

		if (!tbcase.isNotifAddressChanged()) {
			tbcase.getCurrentAddress().copy(tbcase.getNotifAddress());
		}

		prevTBTreatmentHome.persist();
		
		updatePatientAge();

		String s = caseHome.persist();
		
		if ("persisted".equals(s)) {
			caseHome.updateCaseTags();
			OwnerUnitChecker.checkOwnerId(tbcase);
		}

		return s;
	}

	/**
	 * Save changes made to a case
	 * @return
	 */
	public String saveEditingWithoutValidation() {
		TbCase tbcase = caseHome.getInstance();

		tbcase.setNotificationUnit(tbunitselection.getSelected());
		tbcase.getNotifAddress().setAdminUnit(notifAdminUnit.getSelectedUnit());
		tbcase.getCurrentAddress().setAdminUnit(currentAdminUnit.getSelectedUnit());

		if (!tbcase.isNotifAddressChanged()) {
			tbcase.getCurrentAddress().copy(tbcase.getNotifAddress());
		}

		prevTBTreatmentHome.persist();
		
		updatePatientAge();

		String s = caseHome.persist();
		
		if ("persisted".equals(s)) {
			caseHome.updateCaseTags();
			OwnerUnitChecker.checkOwnerId(tbcase);
		}

		return s;
	}


	/**
	 * Start a standard regimen for a case
	 */
	protected void startTreatment() {
		if (startTreatmentHome == null)
			return;
		
		TbCase tbcase = caseHome.getInstance();
		if (startTreatmentHome.getIniTreatmentDate() == null) {
			tbcase.setState(CaseState.WAITING_TREATMENT);
			return;
		}

		startTreatmentHome.getTbunitselection().setSelected(tbcase.getNotificationUnit());
		startTreatmentHome.setUseDefaultDoseUnit(true);
		startTreatmentHome.updatePhases();
		startTreatmentHome.startStandardRegimen();		
	}




	/**
	 * Register a new TB or MDR-TB case
	 * @return "persisted" if successfully registered
	 */
	@Transactional
	public String saveNew() {
		if (!validateData())
			return "error";
		
		TbCase tbcase = caseHome.getInstance();
		
		// is this a suspect case?
		if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT) {
			tbcase.setSuspectClassification(tbcase.getClassification());
		}
		
		// save the patient's data
		patientHome.persist();

		// get notification unit
		tbcase.setNotificationUnit(getTbunitselection().getSelected());
		tbcase.getNotifAddress().setAdminUnit(notifAdminUnit.getSelectedUnit());

		Address notifAddress = tbcase.getNotifAddress();
		Address curAddress = tbcase.getCurrentAddress();
		//curAddress.copy(notifAddress);

		tbcase.setNotifAddress(notifAddress);
		tbcase.setCurrentAddress(curAddress);

		if (tbcase.getValidationState() == null)
			tbcase.setValidationState(ValidationState.WAITING_VALIDATION);
		
		if (tbcase.getState() == null)
			tbcase.setState(CaseState.WAITING_TREATMENT);
		
		updatePatientAge();

		//if it is a new case need to set owner before save to avoid error.
		if(tbcase.getId() == null)
			tbcase.setOwnerUnit(tbcase.getNotificationUnit());

		// treatment was defined ?
		caseHome.setTransactionLogActive(true);
		if (!caseHome.persist().equals("persisted"))
			return "error";

		caseHome.setTransactionLogActive(false);

		// define the treatment regimen if it's not individualized (==2)
		if (regimenType != 2)
			startTreatment();
		
		if (medicalExaminationHome != null) {
			MedicalExamination medExa = medicalExaminationHome.getInstance();
			if (medExa.getDate() != null) {
				medExa.setAppointmentType(MedAppointmentType.SCHEDULLED);
				medExa.setUsingPrescMedicines(YesNoType.YES);
				medicalExaminationHome.persist();			
			}
		}

		// save additional information
		if (prevTBTreatmentHome != null)
			prevTBTreatmentHome.persist();

		caseHome.updateCaseTags();
		OwnerUnitChecker.checkOwnerId(tbcase);

		return (regimenType == 2? "individualized": "persisted");
	}

	/**
	 * Register a new TB or MDR-TB case
	 * @return "persisted" if successfully registered
	 */
	@Transactional
	public String saveNewWithoutValidation() {
		TbCase tbcase = caseHome.getInstance();
		
		// is this a suspect case?
		if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT) {
			tbcase.setSuspectClassification(tbcase.getClassification());
		}
		
		// save the patient's data
		patientHome.persist();

		// get notification unit
		tbcase.setNotificationUnit(getTbunitselection().getSelected());
		tbcase.getNotifAddress().setAdminUnit(notifAdminUnit.getSelectedUnit());

		Address notifAddress = tbcase.getNotifAddress();
		Address curAddress = tbcase.getCurrentAddress();
		//curAddress.copy(notifAddress);

		tbcase.setNotifAddress(notifAddress);
		tbcase.setCurrentAddress(curAddress);

		if (tbcase.getValidationState() == null)
			tbcase.setValidationState(ValidationState.WAITING_VALIDATION);
		
		if (tbcase.getState() == null)
			tbcase.setState(CaseState.WAITING_TREATMENT);
		
		updatePatientAge();

		// treatment was defined ?
		caseHome.setTransactionLogActive(true);
		if (!caseHome.persist().equals("persisted"))
			return "error";

		caseHome.setTransactionLogActive(false);

		// define the treatment regimen if it's not individualized (==2)
		if (regimenType != 2)
			startTreatment();
		
		if (medicalExaminationHome != null) {
			MedicalExamination medExa = medicalExaminationHome.getInstance();
			if (medExa.getDate() != null) {
				medExa.setAppointmentType(MedAppointmentType.SCHEDULLED);
				medExa.setUsingPrescMedicines(YesNoType.YES);
				medicalExaminationHome.persist();			
			}
		}

		// save additional information
		if (prevTBTreatmentHome != null)
			prevTBTreatmentHome.persist();

		caseHome.updateCaseTags();
		OwnerUnitChecker.checkOwnerId(tbcase);

		return (regimenType == 2? "individualized": "persisted");
	}

	/**
	 * Checks if information entered or modified is valid to be recorded
	 * @return true if successfully validated, otherwise returns false if fail
	 */
	public boolean validateData() {
		TbCase tbcase = caseHome.getInstance();

		if (tbcase.getRegistrationDate() == null) {
			facesMessages.addToControlFromResourceBundle("edtregdate", "javax.faces.component.UIInput.REQUIRED");
			return false;
		}
		
		if ((tbcase.getDiagnosisType() == DiagnosisType.CONFIRMED) && (tbcase.getDiagnosisDate() == null)) {
			facesMessages.addToControlFromResourceBundle("diagdateedt", "javax.faces.component.UIInput.REQUIRED");
			return false;
		}
		
		if ((regimenType == 1) && ((startTreatmentHome != null) && (startTreatmentHome.getRegimen() == null))) {
			facesMessages.addToControlFromResourceBundle("cbregimen", "javax.faces.component.UIInput.REQUIRED");
			return false;
		}
		
		//Checks if the treatment iniDate is before the diagnosis date
		//Workspace configuration allows it? 
		if (tbcase.getDiagnosisDate() != null) {
			//treatment has been defined?
			Date iniTreatmentDate = null;
			if(tbcase.getTreatmentPeriod() != null)
				iniTreatmentDate = tbcase.getTreatmentPeriod().getIniDate();
			else if (startTreatmentHome != null && startTreatmentHome.getIniTreatmentDate() != null)
				iniTreatmentDate = startTreatmentHome.getIniTreatmentDate();

            Workspace ws = UserSession.getWorkspace();

			//Validates if defined
			if ((!ws.isAllowDiagAfterTreatment()) && (iniTreatmentDate  != null) && (tbcase.getDiagnosisDate().after(iniTreatmentDate))) {
				facesMessages.addToControlFromResourceBundle("diagdateedt", "cases.treat.inidatemsg");
				return false;
			}

            if ((!ws.isAllowRegAfterDiagnosis()) && (tbcase.getRegistrationDate().after(tbcase.getDiagnosisDate()))) {
                facesMessages.addToControlFromResourceBundle("diagdateedt", "cases.details.valerror1");
                return false;
            }
		}

        if(tbcase.getPatientType() != null && !tbcase.getPatientType().equals(PatientType.PREVIOUSLY_TREATED)){
            tbcase.setPreviouslyTreatedType(null);
        }

        if(tbcase.getClassification().equals(CaseClassification.DRTB)){
            if(tbcase.getLastBmuDateTbRegister() != null && tbcase.getRegistrationDate() != null
                    && tbcase.getLastBmuDateTbRegister().after(tbcase.getRegistrationDate())){
                facesMessages.addToControlFromResourceBundle("bmudateInputDate", StatusMessage.Severity.FATAL ,"cases.details.valerror2");
                return false;
            }
        }

        if(tbcase.getClassification().equals(CaseClassification.TB) && tbcase.getPatientType() != null && tbcase.getPatientType().equals(PatientType.PREVIOUSLY_TREATED)
                && tbcase.getPreviouslyTreatedType() == null){
            facesMessages.addToControlFromResourceBundle("previouslyTreatedType", "javax.faces.component.UIInput.REQUIRED");
            return false;
        }

		return true;
	}
	
	/**
	 * Solve redirecting problem when the user changes diagnosis type but cancel the form
	 */
	public String cancel(){
		caseHome.setInstance(null);
		return "cancelcaseediting";
	}


	/**
	 * @return the currentAdminUnit
	 */
	public AdminUnitSelection getCurrentAdminUnit() {
		if (currentAdminUnit == null)
			currentAdminUnit = new AdminUnitSelection(false);
		return currentAdminUnit;
	}


	/**
	 * @return the notifAdminUnit
	 */
	public AdminUnitSelection getNotifAdminUnit() {
		if (notifAdminUnit == null)
			notifAdminUnit = new AdminUnitSelection(false);
		return notifAdminUnit;
	}



	public TbCase getTbcase() {
		return caseHome.getInstance();
	}


	
	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null) {
			tbunitselection = new TBUnitSelection("newuaid", true, TBUnitType.NOTIFICATION_UNITS);
		}
		return tbunitselection;
	}


	public int getRegimenType() {
		return regimenType;
	}


	public void setRegimenType(int regimenType) {
		this.regimenType = regimenType;
	}

	public PatientHome getPatientHome() {
		return patientHome;
	}

	public PrevTBTreatmentHome getPrevTBTreatmentHome() {
		return prevTBTreatmentHome;
	}

	public MedicalExaminationHome getMedicalExaminationHome() {
		return medicalExaminationHome;
	}

	/**
	 * @param tbunitselection the tbunitselection to set
	 */
	public void setTbunitselection(TBUnitSelection tbunitselection) {
		this.tbunitselection = tbunitselection;
	}

    public HashMap<String, Object> getBMUcase() {
        if(this.BMUcase == null){
            updateBMUTbCase();
        }else {
            Integer patientId = (Integer) this.BMUcase.get("patientid");
            Date regDate = (Date) this.BMUcase.get("regdate");
            TbCase DRTBcase = caseHome.getInstance();
            Patient DRTBpatient = patientHome.getInstance();

            if (DRTBcase == null)
                return null;

            if (!isSamePatientAndRegDate(DRTBpatient, DRTBcase.getRegistrationDate(), patientId, regDate))
                updateBMUTbCase();
        }
        return BMUcase;
    }

    private boolean isSamePatientAndRegDate(Patient patient, Date regDate, Integer patientIdSearched, Date regDateSearched){
        if(patient.getId() == null)
            return false;

        boolean isSamePatient = patient.getId().equals(patientIdSearched);

        boolean isSameRegDate = false;

        if(regDate == null && regDateSearched == null)
            isSameRegDate = true;
        else if (regDate != null)
            isSameRegDate = regDate.equals(regDateSearched);

        return isSamePatient && isSameRegDate;
    }

    public void setBMUcase(HashMap<String, Object> BMUcase) {
        this.BMUcase = BMUcase;
    }

    public TbCase getBMUTbCaseObject(){
        TbCase BMUcase = (TbCase) getBMUcase().get("BMUTbcase");

        return BMUcase;
    }

    public void updateBMUTbCase(){
        if(BMUcase == null)
            BMUcase = new HashMap<String, Object>();

        //If instance is null, patient is null, or it is not DRTB, no sense to calculate BMUTbcase
        if(caseHome.getInstance()==null || (!caseHome.getInstance().getClassification().equals(CaseClassification.DRTB))
                || caseHome.getInstance().getPatient() == null || caseHome.getInstance().getPatient().getId() == null || caseHome.getInstance().getPatient().getId().equals(0)){
            BMUcase.put("patientid", null);
            BMUcase.put("regdate", null);
            BMUcase.put("BMUTbcase", null);
            return;
        }

        BMUcase.put("patientid", caseHome.getInstance().getPatient().getId());
        BMUcase.put("regdate", caseHome.getInstance().getRegistrationDate());

        Date queryDate = caseHome.getInstance().getRegistrationDate();
        if(queryDate == null)
            queryDate = new Date();

        List<TbCase> cases = (List<TbCase>) entityManager.createQuery("from TbCase c where c.patient.id = :patientId and c.classification = :TB  and c.diagnosisType = :CONFIRMED " +
                                                                                        " and c.registrationDate < :DRTBRegDate order by c.registrationDate desc")
                                                         .setParameter("patientId", caseHome.getInstance().getPatient().getId())
                                                         .setParameter("TB", CaseClassification.TB)
                                                         .setParameter("CONFIRMED", DiagnosisType.CONFIRMED)
                                                         .setParameter("DRTBRegDate", queryDate)
                                                         .getResultList();

        if(cases == null || cases.size() < 1)
            BMUcase.put("BMUTbcase", null);
        else
            BMUcase.put("BMUTbcase", cases.get(0));
    }

    public void updateBMUinfo(){
        TbCase BMUcase = this.getBMUTbCaseObject();
        if(caseHome.getInstance().getClassification().equals(CaseClassification.DRTB) && BMUcase != null && caseHome.getId() == null){
            caseHome.getInstance().setLastBmuTbRegistNumber(BMUcase.getDisplayCaseNumber());
            caseHome.getInstance().setLastBmuDateTbRegister(BMUcase.getRegistrationDate());
        }
    }
}
