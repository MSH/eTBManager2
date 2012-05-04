package org.msh.tb.az;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.MedAppointmentType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.YesNoType;

@Name("caseEditingAZHome")
public class CaseEditingAZHome extends CaseEditingHome{
	
	@Override
	public String saveNew() {
		if (!validateData())
			return "error";
		
		TbCase tbcase = caseHome.getInstance();

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
}
