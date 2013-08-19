package org.msh.tb.az.cases;

import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.application.App;
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
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.tbunits.TBUnitSelection;


@Name("caseEditingAZHome")
public class CaseEditingAZHome extends CaseEditingHome{

	private TBUnitSelection referTBUnit;
	@In(create=true) FacesMessages facesMessages;

	@Override
	@Transactional
	public String saveNew() {
		if (!validateData())
			return "error";
		if (!validateMedExam())
			return "error";
		
		TbCaseAZ tbcase = getTbCase();
		// save the patient's data
		getPatientHome().persist();

		tbcase.setNotificationUnit(getTbunitselection().getTbunit());
		tbcase.getNotifAddress().setAdminUnit(getNotifAdminUnit().getSelectedUnit());
		tbcase.getCurrentAddress().setAdminUnit(getCurrentAdminUnit().getSelectedUnit());

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
		
		//log info for management
		tbcase.setSystemDate(new Date());
		UserWorkspace uw = (UserWorkspace) App.getComponent("userWorkspace");
		tbcase.setCreateUser(uw.getUser());
		
		tbcase.setEditingDate(new Date());
		tbcase.setEditingUser(uw.getUser());
		
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
				//medExa.setAppointmentType(MedAppointmentType.SCHEDULLED);
				//medExa.setUsingPrescMedicines(YesNoType.YES);
				getMedicalExaminationHome().persist();			
			}
		}

		// save additional information
		if (!tbcase.isColPrevTreatUnknown()){
			if (getPrevTBTreatmentHome() != null)
				getPrevTBTreatmentHome().persist();
		}
		
		caseHome.updateCaseTags();

		return (regimenType == 2? "individualized": "persisted");
	}
	/**
	 * Return true, if select tb-unit not where user from
	 */
	public boolean notOwnTBUnit(){
		Tbunit nTbU = getTbunitselection().getTbunit();
		Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		if (nTbU==null || nTbUUser==null) return false;
		if (nTbU.getId().intValue() != nTbUUser.getId().intValue())
			return true;
		return false;
	}
	/**
	 * Set tb-unit where user from
	 */
	public void setUserTBUnitDefault(){
		//Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		getTbunitselection().setTbunitWithOptions(caseHome.getTbCase().getNotificationUnit());
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
				if (tbCase.getOutcomeDate() == null) //AK 11/12/2012 may be closed case from EIDSS
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

		return initSuperNotif();
	}

	@Override
	public String selectPatientData() {
		initialized = false;
		if (initializeNewNotification().equals("initialized"))
			return "/custom/az/cases/casenew.xhtml";
		return "error";
	}

	private String initSuperNotif(){
		if (initialized)
			return "initialized";

		if (caseHome.getInstance().getClassification() == null)
			return "/cases/index.xhtml";

		caseHome.getInstance().setPatient(patientHome.getInstance());

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

		// initialize items with previous TB treatments
		//		prevTBTreatmentHome.getTreatments();

		Events.instance().raiseEvent("new-notification");

		initialized = true;
		return "initialized";
	}

	/**
	 * Initialize a new notification
	 */
	public String initWithoutNotifications() {
		if (initialized)
			return "initialized";

		if (caseHome.getInstance().getClassification() == null)
			return "/cases/index.xhtml";

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
	public boolean validateData() {
		TbCaseAZ tc = getTbCase();
		if (tc.isToThirdCategory())
			if (tc.getOutcomeDate() != null){
				if (tc.getThirdCatPeriod().getIniDate().before(tc.getOutcomeDate())){
					facesMessages.addToControlFromResourceBundle("datefieldini", "TbCase.toThirdCategory.error1");
					return false;
				}
			}
		
		return super.validateData();
	}
	
	public boolean validateMedExam(){
		MedicalExamination me = getMedicalExaminationHome().getMedicalExamination();
		try{
			if (!(me.getDate()==null && me.getResponsible().isEmpty() && me.getHeight()==null && me.getWeight()==null && me.getComments().isEmpty())){
				if (!(me.getDate()!=null && me.getResponsible()!=null && me.getHeight()!=null && me.getWeight()!=null)){
					facesMessages.addToControlFromResourceBundle("medexamerror", App.getMessage("MedicalExamination.errorValidate"));
					return false;
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return true;
		}
		return true;
	}
	
	@Override
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
		
		updatePatientAge();

		initialized = true;
	}
	
	@Override
	public String saveEditing() {
		if (!validateData())
			return "error";
		
		TbCaseAZ tbcase = getTbCase();
		if (tbcase.isFirstEIDSSBind())
			if (!validateMedExam())
				return "error";
		
		tbcase.setReferToTBUnit(getReferTBUnit().getTbunit());

		UserWorkspace uw = (UserWorkspace) App.getComponent("userWorkspace");
		if (tbcase.isFirstEIDSSBind()){
			tbcase.setSystemDate(new Date());
			tbcase.setCreateUser(uw.getUser());
		}

		tbcase.setEditingDate(new Date());
		tbcase.setEditingUser(uw.getUser());

		if (getMedicalExaminationHome() != null) {
			MedicalExamination medExa = getMedicalExaminationHome().getInstance();
			if (medExa.getDate() != null) {
				//medExa.setAppointmentType(MedAppointmentType.SCHEDULLED);
				//medExa.setUsingPrescMedicines(YesNoType.YES);
				getMedicalExaminationHome().persist();			
			}
		}
		
		/*Tbunit nTbU = getTbunitselection().getTbunit();
		Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		if (nTbU.getId().intValue() != nTbUUser.getId().intValue())
			if (confirmTbUnit() == JOptionPane.NO_OPTION)
				getTbunitselection().setTbunit(nTbUUser);
		*/
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
	
	/**
	 * Case, when user hasn't permissions to select notificationUnit 
	 * and there is set userUnit as notification unit for TB-case from EIDSS  
	 * */
	public Tbunit getNotificationUnitDefault(){
		Tbunit tbu = getTbunitselection().getTbunit();
		if (tbu == null){
			UserWorkspace uw = (UserWorkspace)App.getComponent("userWorkspace");
			tbu = uw.getTbunit();
			getTbunitselection().setTbunit(tbu);
		}
		return tbu;
	}

	@Override
	public TBUnitSelection getTbunitselection() {
		TbCase tbcase = caseHome.getInstance();
		if (tbcase.getNotificationUnit()!=null && super.getTbunitselection().getTbunit() == null && super.getTbunitselection().getAdminUnit() == null) 
			super.getTbunitselection().setTbunit(tbcase.getNotificationUnit());
		return super.getTbunitselection();
	}
	
	@Override
	public AdminUnitSelection getNotifAdminUnit() {
		TbCase tbcase = caseHome.getInstance();
		if (super.getNotifAdminUnit().getSelectedUnit() == null)
			super.getNotifAdminUnit().setSelectedUnit(tbcase.getNotifAddress().getAdminUnit());
		return super.getNotifAdminUnit();
	}
	
	@Override
	public AdminUnitSelection getCurrentAdminUnit() {
		TbCase tbcase = caseHome.getInstance();
		if (super.getCurrentAdminUnit().getSelectedUnit() == null)
			super.getCurrentAdminUnit().setSelectedUnit(tbcase.getCurrentAddress().getAdminUnit());
		return super.getCurrentAdminUnit();
	}
}
