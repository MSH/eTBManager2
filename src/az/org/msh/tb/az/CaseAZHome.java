package org.msh.tb.az;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.CaseSeverityMark;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;

@Name("caseAZHome")
public class CaseAZHome {
	private String notifEIDSS="";
	private String addrEIDSS="";
	@In CaseHome caseHome;
	@In(create=true) CaseEditingAZHome caseEditingAZHome;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(create=true) CaseSeverityMarksHome caseSeverityMarksHome;

	
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
}
