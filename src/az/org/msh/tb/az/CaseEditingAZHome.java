package org.msh.tb.az;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.MedAppointmentType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.tbunits.TBUnitSelection;


@Name("caseEditingAZHome")
public class CaseEditingAZHome extends CaseEditingHome{
	
	private TBUnitSelection referTBUnit;
	
	@Override
	@Transactional
	public String saveNew() {
		if (!validateData())
			return "error";
		
		TbCaseAZ tbcase = getTbCase();
		// save the patient's data
		getPatientHome().persist();

		// get notification unit
		tbcase.setNotificationUnit(getTbunitselection().getTbunit());
		tbcase.getNotifAddress().setAdminUnit(getNotifAdminUnit().getSelectedUnit());
		
		Address notifAddress = tbcase.getNotifAddress();
		Address curAddress = tbcase.getCurrentAddress();
		//curAddress.copy(notifAddress);

		tbcase.setNotifAddress(notifAddress);
		tbcase.setCurrentAddress(curAddress);
		
		tbcase.setReferToTBUnit(getReferTBUnit().getTbunit());

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
		
		if (getMedicalExaminationHome() != null) {
			MedicalExamination medExa = getMedicalExaminationHome().getInstance();
			if (medExa.getDate() != null) {
				medExa.setAppointmentType(MedAppointmentType.SCHEDULLED);
				medExa.setUsingPrescMedicines(YesNoType.YES);
				getMedicalExaminationHome().persist();			
			}
		}

		// save additional information
		if (getPrevTBTreatmentHome() != null)
			getPrevTBTreatmentHome().persist();

		caseHome.updateCaseTags();

		return (regimenType == 2? "individualized": "persisted");
	}
	
	/**
	 * Another way to init new notification, if exist one EDIDSS notification
	 */
	@Override
	public String initializeNewNotification() {
		Patient p = patientHome.getInstance();
		List<TbCase> cases = p.getCases();
		TbCase caseEIDSS = null;
		for (TbCase tbCase : cases){
			if(tbCase.getLegacyId() != null){
				caseEIDSS = tbCase;
			}
		}
		if (caseEIDSS != null){
			caseHome.setId(caseEIDSS.getId());
			caseHome.find();
			Contexts.getConversationContext().set("tbcase", caseHome.getInstance());
			if (p.getBirthDate() != null)
				updatePatientAge();
			String ret = initWithoutNotifications();
			caseHome.getInstance().setNotificationUnit(new Tbunit());
			caseHome.getInstance().setNotifAddress(new Address());
			caseHome.getInstance().setCurrentAddress(new Address());
			Contexts.getConversationContext().set("tbcase", caseHome.getInstance());
			return ret;
		}
		return super.initializeNewNotification();
	}
	
	/**
	 * Initialize a new notification
	 */
	public String initWithoutNotifications() {
		if (initialized)
			return "initialized";
		
		Patient p = patientHome.getInstance();
		
		if (caseHome.getInstance().getClassification() == null)
			return "/cases/index.xhtml";
		
		if ((!patientHome.isManaged()) && 
			(p.getName() == null) && (p.getMiddleName() == null) && (p.getLastName() == null) &&
			(p.getBirthDate() == null))
		return "patient-searching";
		
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

	@Override
	public String saveEditing() {
		if (!validateData())
			return "error";
		TbCaseAZ tbcase = getTbCase();
		tbcase.setReferToTBUnit(getReferTBUnit().getTbunit());
		
		return super.saveEditing();
	}
	
	public TBUnitSelection getReferTBUnit() {
		if (referTBUnit == null){
			referTBUnit = new TBUnitSelection();
			if (caseHome.isManaged())
				referTBUnit.setTbunit(getTbCase().getReferToTBUnit());
		}
		return referTBUnit;
	}

	public void setReferTBUnit(TBUnitSelection referTBUnit) {
		this.referTBUnit = referTBUnit;
	}
	
	public TbCaseAZ getTbCase() {
		return (TbCaseAZ)caseHome.getInstance();
	}
}
