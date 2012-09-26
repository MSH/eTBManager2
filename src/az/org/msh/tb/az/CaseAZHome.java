package org.msh.tb.az;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.az.entities.CaseSeverityMark;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseState;
import org.msh.utils.date.DateUtils;

@Name("caseAZHome")
public class CaseAZHome {
	private String notifEIDSS="";
	private String addrEIDSS="";
	private boolean to3category;
	private static final CaseState[] outcomes3cat = {
		CaseState.CURED, 
		CaseState.TREATMENT_COMPLETED};
	private Map<String, String> messages;
	
	@In CaseHome caseHome;
	@In(create=true) CaseEditingAZHome caseEditingAZHome;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(create=true) CaseSeverityMarksHome caseSeverityMarksHome;
	@In(create=true) CaseCloseHome caseCloseHome;
	
	/**
	 * Save case data
	 * @return
	 */
	public String persist() {
		String res;
		if (caseHome.isManaged())
			 res = caseEditingAZHome.saveEditing();
		else res = caseEditingAZHome.saveNew();
		
		if (!res.equals("persisted"))
			return res;
		
		caseSeverityMarksHome.save();
		
		return res;
	}
	

	public List<CaseSeverityMark> getCaseSeverityMarks() {
		if (!caseSeverityMarksHome.isEditing())
			caseSeverityMarksHome.setEditing(true);
		
		return caseSeverityMarksHome.getSeverityMarks();
	}
	
	public TbCaseAZ getTbCase() {
		return (TbCaseAZ)caseHome.getInstance();
	}
	public String getNotifEIDSS(){
		TbCaseAZ c=getTbCase();
		String s=c.getEIDSSComment();
		if (s!=null){
			String [] parts=s.split("/", 5);
			notifEIDSS= parts[0];	
		}
			return notifEIDSS;
	}
	public String getAddrEIDSS(){
		TbCaseAZ c=getTbCase();
		String s=c.getEIDSSComment();
		if (s!=null){
			if	(s.contains("/")){
				String [] parts=s.split("/", 5);
				addrEIDSS= parts[1];	
			}
		}
		return addrEIDSS;
	}
	
	/**
	 * Outcames in case of third category
	 * */
	public CaseState[] getOutcomes() {
		if (!isTo3category())
			return caseCloseHome.getOutcomes();
		return outcomes3cat;
	}


	public boolean isTo3category() {
		if (!to3category && getTbCase().isToThirdCategory()) 
			to3category = true;
		return to3category;
	}


	public void setTo3category(boolean to3category) {
		this.to3category = to3category;
	}
	
	public String closeCase() {
		getTbCase().setToThirdCategory(to3category);
		if (isTo3category()){
			getTbCase().getThirdCatPeriod().setIniDate(caseCloseHome.getDate());
		}
		return caseCloseHome.closeCase();
	}
	/**
	 * Cases, which we can refer to third category
	 * */
	public boolean isCanEditCloseCaseData() {
		return (!getTbCase().isOpen()) && Arrays.asList(outcomes3cat).contains(getTbCase().getState()) && caseHome.checkRoleBySuffix("CASE_DATA_EDT") && (caseHome.isWorkingUnit());
	}
	public boolean isCanEditExams() {
		return ((caseHome.isCanEditExams()) || (getTbCase().isToThirdCategory() && caseHome.checkRoleBySuffix("CASE_EXAMS_EDT") && caseHome.isWorkingUnit()));
	}
	public boolean isCanTransferReturn() {
		TbCase tbcase = caseHome.getInstance();
		return (tbcase.isOpen()) && (tbcase.getState() == CaseState.TRANSFERRING) && (caseHome.checkRoleBySuffix("CASE_TRANSFER")) && (isOwnerOrNotifUnit());
	}
	
	public boolean isOwnerOrNotifUnit() {
		UserWorkspace ws = (UserWorkspace)Component.getInstance("userWorkspace");
		if (ws.isPlayOtherUnits())
			return true;

		TbCase tbcase = caseHome.getInstance();
		Tbunit treatmentUnit = tbcase.getOwnerUnit();
		boolean owornot = false;
		if (treatmentUnit != null)
			owornot = (treatmentUnit.getId().equals(ws.getTbunit().getId()));

		Tbunit unit = tbcase.getNotificationUnit();
		if (unit != null)
			owornot = owornot|((unit != null) && (unit.getId().equals(ws.getTbunit().getId())));

		return owornot;
	}
	
	public String getYearOfBirth(TbCase tbcase){
		String res = "-";
		EntityManager em = (EntityManager)Component.getInstance("entityManager", true);
		TbCase tc = (TbCase) em.find(TbCase.class, tbcase.getId());
		if (tc.getPatient().getBirthDate()!=null)
			res = Integer.toString(DateUtils.yearOf(tc.getPatient().getBirthDate()));
		else{
			if ((tc.getAge() != null) && (tc.getRegistrationDate() != null)){
				res = Integer.toString(DateUtils.yearOf(tc.getRegistrationDate()) - tc.getPatientAge());
			}
		}
		return res;
	}
	
	public String deleteComas(String name){
		String res = name.replaceAll(",","");
		return res;
	}
	
	public String getRightPatientName(TbCase tbcase){
		EntityManager em = (EntityManager)Component.getInstance("entityManager", true);
		TbCase tc = (TbCase) em.find(TbCase.class, tbcase.getId());
		return tc.getPatient().getFullName();
	}
	
	public String getRightCaseDetailsTitle(){
		TbCaseAZ tc = getTbCase();
		if (tc.getNotificationUnit()==null && tc.getLegacyId()!=null)
			return getMessages().get("cases.detailseidsstitle");
		return getMessages().get("cases.detailstitle");
	}
	
	/**
	 * Return the current resource message file
	 * @return
	 */
	protected Map<String, String> getMessages() {
		if (messages == null)
			messages = Messages.instance();
		return messages;
	}
}

