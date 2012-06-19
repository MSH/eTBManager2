package org.msh.tb.kh;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.WsEntityHome;
import org.msh.tb.cases.treatment.TreatmentHome;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.TbCase;
import org.msh.tb.kh.MedicalExaminationKHHome;
import org.msh.tb.kh.entities.TbCaseKH;
import org.msh.tb.transactionlog.LogInfo;



/**
 * Handle basic operations with a TB/MDR case. Check {@link CaseEditingHome} for notification and editing of case data
 * Specific operations concerning exams, case regimes, heath units and medical consultations are handled by other classes.
 * @author Ricardo Mem�ria 
 * Cambodia - Vani Rao
 */
@Name("caseKHHome")
public class CaseKHHome{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6745610319523948621L;
	@In(create = true)
	CaseHome caseHome;
	@In(create = true)
	CaseEditingHome caseEditingHome;
	@In(create = true)
	TreatmentHome treatmentHome;
	@In(required = false)
	MedicalExaminationKHHome medicalExaminationKHHome;
	
	private String houseNum = "";
	private String streetNum = "";
	private String groupNum = "";
	private String villageName = "";
	
	/**
	 * Return an instance of a {@link TbCaseKH} class
	 * 
	 * @return
	 */
	@Factory("tbcasekh")
	public TbCaseKH getTbCaseKH() {
		return  (TbCaseKH)caseHome.getTbCase();
	}

	/**
	 * Save a new case for the Cambodian workspace. Don't use the class
	 * {@link CaseEditingHome}, because this class already saves it using the
	 * {@link CaseEditingHome} component
	 * 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveNew() {
		TbCase tbcase = caseHome.getInstance();
		String completeAddress = houseNum+";".concat(streetNum)+";".concat(groupNum)+";".concat(villageName)+";";
		Address address = new Address();
		address.setAddress(completeAddress);
		tbcase.setNotifAddress(address);
		String ret = caseEditingHome.saveNew();
		saveMedicalExamination();
		return ret;
	}

	/**
	 * Save changes made to an already existing case in the Cambodian workspace
	 * 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveEditing() {
		return caseEditingHome.saveEditing();
	}

	/**
	 * Save changes made to an already existing case in the Cambodian workspace
	 * 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveTreatment() {
		return treatmentHome.saveChanges();
	}

	@Transactional
	public String saveTBCase() {
		return "error";
	}

	/**
	 * Save medical examination
	 * 
	 * @return
	 */
	public String saveMedicalExamination() {
		if (medicalExaminationKHHome == null)
			return "error";

		medicalExaminationKHHome.setDisplayMessage(false);
		return medicalExaminationKHHome.persist();
	}

	public String gethouseNum() {
		String addr = deConcatAddr();
		if(addr == null)
			return houseNum;
		else{
		String delimiter = "\\;";
		String temp[] = addr.split(delimiter);
		sethouseNum(temp[0]);
		return houseNum;
		}
	}

	public void sethouseNum(String houseNum) {
		this.houseNum = houseNum;
	}
	
	public String getStreetNum() {
		String addr = deConcatAddr();
		if(addr == null)
			return streetNum;
		else{
		String delimiter = "\\;";
		String temp[] = addr.split(delimiter);
		setStreetNum(temp[1]);
		return streetNum;
		}
	}
	
	public void setStreetNum(String streetNum) {
		this.streetNum = streetNum;
	}

	public String getGroupNum() {
		String addr = deConcatAddr();
		if(addr == null)
			return "";
		else{
		String delimiter = "\\;";
		String temp[] = addr.split(delimiter);
		setGroupNum(temp[2]);
		return groupNum;
		}
	}
	
	public void setGroupNum(String groupNum) {
		this.groupNum = groupNum;
	}

	public String getVillageName() {
		String addr = deConcatAddr();
		if(addr == null)
			return "";
		else{
		String delimiter = "\\;";
		String temp[] = addr.split(delimiter);
		setVillageName(temp[3]);
		return villageName;
		}
	}

	public void setVillageName(String villageName) {
		this.villageName = villageName;
	}

	public String deConcatAddr(){
		TbCase tbcase = caseHome.getInstance();
		String addr = tbcase.getNotifAddress().getAddress();
		return addr;
	}
	
}

