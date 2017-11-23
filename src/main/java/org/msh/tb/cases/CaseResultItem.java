package org.msh.tb.cases;

import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.utils.date.DateUtils;


public class CaseResultItem {
	private TbCase tbcase = new TbCase();
	private String adminUnitDisplay;
	
	public String getyearOfBirth(){
		String res = "";
		if ((tbcase.getAge() != null) && (tbcase.getRegistrationDate() != null)){
			Integer resI = DateUtils.yearOf(tbcase.getRegistrationDate()) - tbcase.getPatientAge();
			res = resI.toString();
		}
		return res;
	}

	/**
	 * Return the patient number formatted to be displayed  
	 * @return
	 */
	public String getDisplayCaseNumber() {
		return tbcase.getDisplayCaseNumber();
/*		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace");
		
		String displayNumber;
		
		if (ws.getDisplayCaseNumber() == DisplayCaseNumber.REGISTRATION_CODE)
			 displayNumber = registrationCode;
		else {
			if ((patientRecordNumber == null) || (caseNumber == null))
				 displayNumber = null;
			else displayNumber = TbCase.formatCaseNumber(patientRecordNumber, caseNumber);
		}

		if (displayNumber == null)
			displayNumber = Messages.instance().get("cases.new");
		
		return displayNumber;
*/		
	}

	/**
	 * @return the adminUnitDisplay
	 */
	public String getAdminUnitDisplay() {
		return adminUnitDisplay;
	}

	/**
	 * @param adminUnitDisplay the adminUnitDisplay to set
	 */
	public void setAdminUnitDisplay(String adminUnitDisplay) {
		this.adminUnitDisplay = adminUnitDisplay;
	}

	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}


	public Patient getPatient() {
		return tbcase.getPatient();
	}
	
	public void setTbcase(TbCase tbcase){
		this.tbcase = tbcase;
	}
}
