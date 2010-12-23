package org.msh.tb.cases;


import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.Address;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.MedicalExamination;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.DiagnosisType;
import org.msh.mdrtb.entities.enums.MedAppointmentType;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.cases.exams.MedicalExaminationHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.date.DateUtils;


/**
 * Handle TB and DR-TB cases editing and new notification
 * @author Ricardo Memoria
 *
 */
@Name("caseEditingHome")
@Scope(ScopeType.CONVERSATION)
public class CaseEditingHome {

	@In(create=true) CaseHome caseHome;
	@In(create=true) PatientHome patientHome;
	@In EntityManager entityManager;
	@In(create=true) PrevTBTreatmentHome prevTBTreatmentHome;
	@In(create=true) MedicalExaminationHome medicalExaminationHome;
	@In(create=true) FacesMessages facesMessages;
	
	@In(required=false) StartTreatmentHome startTreatmentHome;
	@In(required=false) ExamMicroscopyHome examMicroscopyHome;
	@In(required=false) ExamCultureHome examCultureHome;

	private TBUnitSelection tbunitselection;
	private AdminUnitSelection notifAdminUnit;
	private AdminUnitSelection currentAdminUnit;
	private boolean initialized;

	/**
	 * 1 - Standard, 2 - Individualized
	 */
	private int regimenType;
	
	
	public String selectPatientData() {
		initialized = false;
		if (initializeNewNotification().equals("initialized"))
			return "/cases/casenew.xhtml";
		return "error";
	}

	/**
	 * Initialize a new notification
	 */
	@RaiseEvent("new-notification")
	public String initializeNewNotification() {
		if (initialized)
			return "initialized";
		
		Patient p = patientHome.getInstance();
		
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
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if (userWorkspace != null) {
			if (userWorkspace.getTbunit().isNotifHealthUnit())
				getTbunitselection().setTbunit(userWorkspace.getTbunit());
			
			AdministrativeUnit au = userWorkspace.getAdminUnit();
			if (au == null)
				au = userWorkspace.getTbunit().getAdminUnit();
			
			if (au != null) {
				au = entityManager.find(AdministrativeUnit.class, au.getId());
				
				getNotifAdminUnit().setSelectedUnit(au);

				if (getTbunitselection().getTbunit() == null) {
					List<AdministrativeUnit> lst = getTbunitselection().getAdminUnits();
					
					if (lst != null)
					for (AdministrativeUnit adminUnit: lst) {
						if (adminUnit.isSameOrChildCode(au.getCode())) {
							getTbunitselection().setAdminUnit(adminUnit);
						}
					}					
				}
			}
		}
		
		initialized = true;
		return "initialized";
	}


	/**
	 * Update the patient's age according to his birth date and the diagnosis date 
	 */
	protected void updatePatientAge() {
		Date dtBirth = patientHome.getInstance().getBirthDate();
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
		
		prevTBTreatmentHome.setNumItems(prevTBTreatmentHome.getItems().size());
		
		TbCase tbcase = caseHome.getInstance();
		Address addr = tbcase.getCurrentAddress();
		if (addr == null)
			tbcase.setCurrentAddress(new Address());
		addr = tbcase.getNotifAddress();
		if (addr == null)
			tbcase.setNotifAddress(new Address());
		
		if (getTbunitselection().getTbunit() == null)
			tbunitselection.setTbunit(tbcase.getNotificationUnit());
		
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

		tbcase.setNotificationUnit(tbunitselection.getTbunit());
		tbcase.getNotifAddress().setAdminUnit(notifAdminUnit.getSelectedUnit());
		tbcase.getCurrentAddress().setAdminUnit(currentAdminUnit.getSelectedUnit());
		
		if (!tbcase.isNotifAddressChanged()) {
			tbcase.getCurrentAddress().copy(tbcase.getNotifAddress());
		}

		prevTBTreatmentHome.persist();

		return caseHome.persist();
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

		startTreatmentHome.getTbunitselection().setTbunit(tbcase.getNotificationUnit());
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

		// save the patient's data
		patientHome.persist();

		// get notification unit
		tbcase.setNotificationUnit(getTbunitselection().getTbunit());
		tbcase.getNotifAddress().setAdminUnit(notifAdminUnit.getSelectedUnit());
		
		Address notifAddress = tbcase.getNotifAddress();
		Address curAddress = tbcase.getCurrentAddress();
		curAddress.copy(notifAddress);

		tbcase.setNotifAddress(notifAddress);
		tbcase.setCurrentAddress(curAddress);

		if (tbcase.getValidationState() == null)
			tbcase.setValidationState(ValidationState.WAITING_VALIDATION);
		
//		if (tbcase.getClassification() == CaseClassification.DRTB)
//			tbcase.setCategory(TbCategory.CATEGORY_IV);
		
		tbcase.setState(CaseState.WAITING_TREATMENT);

		// treatment was defined ?
		if (!caseHome.persist().equals("persisted"))
			return "error";

		caseHome.setTransactionLogActive(false);

		// define the treatment regimen if it's not individualized (==2)
		if (regimenType != 2)
			startTreatment();
		
		MedicalExamination medExa = medicalExaminationHome.getInstance();
		if (medExa.getDate() != null) {
			medExa.setAppointmentType(MedAppointmentType.SCHEDULLED);
			medExa.setUsingPrescMedicines(YesNoType.YES);
			medicalExaminationHome.persist();			
		}

		// save additional information
		prevTBTreatmentHome.persist();
		
		return (regimenType == 2? "individualized": "persisted");
	}




	/**
	 * Checks if information entered or modified is valid to be recorded
	 * @return
	 */
	public boolean validateData() {
		TbCase tbcase = caseHome.getInstance();

		if ((tbcase.getDiagnosisType() == DiagnosisType.CONFIRMED) && (tbcase.getDiagnosisDate() == null)) {
			facesMessages.addToControlFromResourceBundle("reghidden", "javax.faces.component.UIInput.REQUIRED");
			return false;
		}
		
		if ((regimenType == 1) && ((startTreatmentHome != null) && (startTreatmentHome.getRegimen() == null))) {
			facesMessages.addToControlFromResourceBundle("cbregimen", "javax.faces.component.UIInput.REQUIRED");
			return false;
		}
		
		return true;
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
			tbunitselection = new TBUnitSelection(true, TBUnitFilter.NOTIFICATION_UNITS);
		}
		return tbunitselection;
	}


	public int getRegimenType() {
		return regimenType;
	}


	public void setRegimenType(int regimenType) {
		this.regimenType = regimenType;
	}
}
