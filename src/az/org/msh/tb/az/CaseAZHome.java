package org.msh.tb.az;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.CaseSeverityMark;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.PrevTBTreatmentHome;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.SystemParam;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseState;
import org.msh.utils.date.DateUtils;

@Name("caseAZHome")
public class CaseAZHome {
//	private String notifEIDSS="";
//	private String addrEIDSS="";
	private boolean to3category;
	private String systemMessage;
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
/*	public String getNotifEIDSS(){
		TbCaseAZ c=getTbCase();
		String s=c.getEIDSSComment();
		if (s!=null){
			String [] parts=s.split(" / ", 6);
			notifEIDSS= parts[0];	
		}
			return notifEIDSS;
	}
	public String getAddrEIDSS(){
		TbCaseAZ c=getTbCase();
		String s=c.getEIDSSComment();
		if (s!=null){
			if	(s.contains(" / ")){
				String [] parts=s.split(" / ", 6);
				addrEIDSS= parts[1];	
			}
		}
		return addrEIDSS;
	}*/
	
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
		TbCaseAZ tc = reGetTbCase(tbcase);
		if (tc.getPatient().getBirthDate()!=null)
			res = Integer.toString(DateUtils.yearOf(tc.getPatient().getBirthDate()));
		else{
			if ((tc.getAge() != null) && (tc.getRegistrationDate() != null)){
				res = Integer.toString(DateUtils.yearOf(tc.getRegistrationDate()) - tc.getPatientAge());
			}
		}
		return res;
	}
	
	public String getRightPatientName(TbCase tbcase){
		TbCase tc = (TbCase) App.getEntityManager().find(TbCase.class, tbcase.getId());
		return tc.getPatient().getFullName();
	}
	
	public String getRightCaseDetailsTitle(){
		TbCaseAZ tc = getTbCase();
		if (tc.getNotificationUnit()==null && tc.getLegacyId()!=null)
			return App.getMessage("cases.detailseidsstitle");
	//return getMessages().get("cases.detailstitle");
		return App.getMessage("CaseState." + tc.getState());
	}
	
	
	
	/**
	 * Return the correct instance of tbcase
	 * */
	public TbCaseAZ reGetTbCase(TbCase tbcase){
		if (tbcase==null) return null;
		if (tbcase.getId() == null) return (TbCaseAZ) tbcase;
		EntityManager em = (EntityManager)Component.getInstance("entityManager", true);
		TbCaseAZ tc = (TbCaseAZ) em.find(TbCaseAZ.class, tbcase.getId());
		return tc;
	}
	
	public String getEIDSSBlock(TbCase tbcase){
		TbCaseAZ az = reGetTbCase(tbcase);
		String res = "";
		if (az.getEIDSSComment()!= null){
			String [] ec = az.getEIDSSComment().split(" / ");
			//====FULL NAME FROM EIDSS====
			res+="<b>"+(existInImport(ec,3)?ec[3]:"---")+"</b><br/>";

			//====DATE OF BIRTH====
			if (existInImport(ec,5)) {
				Date eidssDBirth;
				try {
					eidssDBirth = new SimpleDateFormat("dd-MM-yyyy").parse(ec[5]);
					res += DateUtils.yearOf(eidssDBirth)+ " " 
						+ App.getMessage("az_EIDSS_Year_Of_Birth") + " "
						+ "("+DateUtils.formatDate(eidssDBirth, App.getMessage("locale.datePattern"))+"), ";
				} catch (ParseException e) {
					res += "("+ec[5]+"), ";
				}
			}
			else
				/*if (existInImport(ec,4)){
					int y = DateUtils.yearOf(new Date())-Integer.parseInt(ec[4]);
					res += "\u2248"+y+" "+ getMessages().get("az_EIDSS_Year_Of_Birth.short")+ " (XX.XX."+y+"), ";
				}
				else*/
					res += "XXXX " + App.getMessage("az_EIDSS_Year_Of_Birth.short") + " (XX.XX.XXXX), ";
			
			//====AGE====
			res += (existInImport(ec,4)?ec[4]:"XX")+ " " + App.getMessage("az_EIDSS_years")+"</br>";
			
			//====NOTIFICATION ADRESS====
			res += App.getMessage("Address.address")+" "+App.getMessage("excel.fromEIDSS")+": "+(existInImport(ec,1) ? ec[1] : "-")+"<br/>";

			//====NOTIFICATION DATE AND INNER DATE====
			Date dInEIDSS;
			res += "<sub>"+App.getMessage("az_EIDSS_InNotifDate")+": ";
			if (existInImport(ec,6)){
				try {
					dInEIDSS = new SimpleDateFormat("dd-MM-yyyy").parse(ec[6]);
					res += DateUtils.formatDate(dInEIDSS, App.getMessage("locale.datePattern"));
				} catch (ParseException e) {
					res += ec[6];
				}
			}
			else res+="XX.XX.XXXX";
			res += "/";
			if (existInImport(ec,2)){
				try {
					dInEIDSS = new SimpleDateFormat("dd-MM-yyyy").parse(ec[2]);
					res += DateUtils.formatDate(dInEIDSS, App.getMessage("locale.datePattern"));
				} catch (ParseException e) {
					res += ec[2];
				}
			}
			else res+="XX.XX.XXXX";
			res+="</sub><br/>";
			
			//====NOTIFICATION UNIT====
			res += "<b>"+App.getMessage("az_EIDSS_Notify_LPU")+": "+(existInImport(ec,0)?ec[0]:"-")+"</b><br/>";
			//====EIDSS ID====
			res += "<b>"+App.getMessage("TbCase.eidssid")+": "+(az.getLegacyId()!=null ? az.getLegacyId() : "-")+"</b><br/>";
			//====UNICAL ID====
			res += "<b>"+App.getMessage("az_AZ.case.unicalID")+": </b>"+(az.getUnicalID()!=null ? az.getUnicalID() : "XXXXXXX")+"<br/>";
		}
		else
			res += "<center>"+App.getMessage("manag.ind.interim.unknown")+"</center>";
		return res;
	}
	
	private boolean existInImport(String [] ec, int ind){
		if (ec.length<ind+1)
			return false;
		if ("".equals(ec[ind]))
			return false;
		return true;
	}
	
	public String verifyDateIniTreat(TbCase tbcase){
		TbCaseAZ az = reGetTbCase(tbcase);
		if (az.getTreatmentPeriod()==null) return "XX.XX.XXXX";
		if (az.getTreatmentPeriod().getIniDate()==null) return "XX.XX.XXXX";
		boolean exams = false;
		if (az.getExamsDST()!=null)
			for (ExamDST ex:az.getExamsDST()){
				Date dt = ex.getDateRelease()!=null?ex.getDateRelease():ex.getDateCollected();
				if (!dt.after(az.getTreatmentPeriod().getIniDate()))
					exams = true;
			}
		String res = "";
		if (!exams)
			res+= "<span class=\"underline\" style=\"border-bottom: 2px solid red;\">";
		res += DateUtils.formatDate(az.getTreatmentPeriod().getIniDate(), App.getMessage("locale.datePattern"));
		if (!exams)
			res+= "</span>";
		return res;
	}
	
	public String getSystemMessage(){
		if (systemMessage==null){
			String s = null;
			try {
				SystemParam sysparam = (SystemParam)App.getEntityManager()
				.createQuery("from SystemParam sp where sp.workspace.id = :id and sp.key = :param")
				.setParameter("id", ((Workspace)App.getComponent("defaultWorkspace")).getId())
				.setParameter("param", "admin.system_message")
				.getSingleResult();
				s = sysparam.getValue();
			} catch (Exception e) {
				s = null;
			}
			systemMessage = s;
		}
		return systemMessage;
	}
	
	public void setSystemMessage(String value){
		this.systemMessage = value;
	}
	
	public void saveSystemMessage(){
		SystemParam p;
		try {
			p = App.getEntityManager().find(SystemParam.class, "admin.system_message");
		} catch (Exception e) {
			p = null;
		}

		if (p == null) {
			p = new SystemParam();
			p.setKey("admin.system_message");
			//defaultWorkspace = entityManager.merge(defaultWorkspace);
			p.setWorkspace((Workspace)App.getComponent("defaultWorkspace"));
		}
		p.setValue(systemMessage.toString());
		App.getEntityManager().persist(p);
	}
	
	public String convertToHTML(String s){
		String res = s;
		if (res!=null)
			res = res.replaceAll("\n", "<br/>");
		return res;
	}
	
	public String convertFromHTML(String s){
		String res = s;
		if (res!=null)
			res = res.replaceAll("<br/>", "\n");
		return res;
	}
	
	/**
	 * @return the numTreatments
	 */
	public Integer getNumTreatments() {
		TbCaseAZ tc = getTbCase();
		PrevTBTreatmentHome prev = (PrevTBTreatmentHome)App.getComponent("prevTBTreatmentHome");
		if (tc.isColPrevTreatUnknown())
			return null;
		else
			return prev.getTreatments().size();
	}


	/**
	 * @param numTreatments the numTreatments to set
	 */
	public void setNumTreatments(Integer numTreatments) {
		if (numTreatments==null){
				TbCaseAZ tc = getTbCase();
				tc.setColPrevTreatUnknown(true);
			}else{
				PrevTBTreatmentHome prev = (PrevTBTreatmentHome)App.getComponent("prevTBTreatmentHome");
				prev.setNumTreatments(numTreatments);
			}
		}
}

