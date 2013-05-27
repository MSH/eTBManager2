package org.msh.tb.az;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseResultItem;
import org.msh.tb.cases.PatientsQuery.Item;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.cases.treatment.StartTreatmentIndivHome;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.SequenceInfo;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.tbunits.TBUnitSelection;

@Name("casesControlAZ")
@Scope(ScopeType.PAGE)
public class CasesControlAZ {
	
	@In(create=true) CaseAZHome caseAZHome;
	@In(required=false) Workspace defaultWorkspace;
	@In(create=true) FacesMessages facesMessages;
	
	private Set<Integer> deleteList = new HashSet<Integer>();
	private Integer markToDel;
	private Boolean canDeleteEIDSS;
	
	private Integer numDefault;
	private SequenceInfo sequenceInfo;
	
	//======================DELETE EIDSS NOT BINDED CASES==========================
	/**
	 * Return true if this list of cases is the list of not binded EIDSS-cases and if user have enough permissions 
	 * */
	public Boolean getCanDeleteEIDSS(){
		if (canDeleteEIDSS==null){
			/*canDeleteEIDSS = false;
			CaseFilters caseFilters = (CaseFilters)App.getComponent("caseFilters");
			if (caseFilters != null) 
				if (caseFilters.getStateIndex()!=null)
					if (caseFilters.getStateIndex()==1515)
						canDeleteEIDSS = true;
			canDeleteEIDSS = canDeleteEIDSS&&Identity.instance().hasRole("TB_DELETE_EIDSS_NOT_BINDED");*/
			setCanDeleteEIDSSInTrue();
		}
		return canDeleteEIDSS;
	}
	
	/**
	 * Add case to list for delete OR remove it from it
	 * */
	public void setMarkToDel(Integer markToDel) {
		if (deleteList.contains(markToDel))
			deleteList.remove(markToDel);
		else
			deleteList.add(markToDel);
	}
	
	/**
	 * Add all cases from list of result of search to list for delete OR clear it
	 * */
	public void fillDeleteListFromCases(){
		CasesQueryAZ cq = (CasesQueryAZ)App.getComponent("casesAZ");
		List<CaseResultItem> lst = cq.getResultList();
		if (deleteList.size()>=getCountEIDSSCases())
			deleteList.clear();
		else{
			for (CaseResultItem it:lst)
				if (caseAZHome.reGetTbCase(it.getTbcase()).getLegacyId()!=null)
					deleteList.add(it.getTbcase().getId());
		}
	}

	/**
	 * Add all EIDSS-cases from list of patients to list for delete OR clear it
	 * */
	public void fillDeleteListFromPatients(){
		PatientsQueryAZ cq = (PatientsQueryAZ)App.getComponent("patientsAZ");
		List<Item> lst = cq.getPatientList();
		if (deleteList.size()>=getCountEIDSSPatients())
			deleteList.clear();
		else{
			for (Item it:lst)
				if (it.getTbcase().getLegacyId()!=null)
					deleteList.add(it.getTbcase().getId());
		}
	}

	/**
	 * Return count of EIDSS-cases from patientList 
	 * @return
	 */
	public int getCountEIDSSPatients() {
		PatientsQueryAZ cq = (PatientsQueryAZ)App.getComponent("patientsAZ");
		int eidss_cases = 0;
		for (Item it:cq.getPatientList()){
			if (it.getTbcase().getLegacyId()!=null)
				eidss_cases++;
		}
		return eidss_cases;
	}
	
	/**
	 * Return count of EIDSS-cases from  casesList
	 * @return
	 */
	public int getCountEIDSSCases() {
		CasesQueryAZ cq = (CasesQueryAZ)App.getComponent("casesAZ");
		int eidss_cases = 0;
		for (CaseResultItem it:cq.getResultList()){
			if (caseAZHome.reGetTbCase(it.getTbcase()).getLegacyId()!=null)
				eidss_cases++;
		}
		return eidss_cases;
	}
	
	/**
	 * Remove selected cases
	 * */
	public void removeCasesFromDeleteList(){
		for (Integer tc_id:deleteList){
			try{
				TbCase tc = (TbCase) App.getEntityManager().find(TbCase.class, tc_id);
				Patient p = tc.getPatient();
				int count = App.getEntityManager()
										.createQuery("select count(*) from TbCase c where c.patient.id = :id")
										.setParameter("id", p.getId())
										.getResultList().size();
				if (count==1)
					App.getEntityManager().remove(p);
				else
					App.getEntityManager().remove(tc);
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		App.getEntityManager().flush();
		deleteList.clear();
	}
	
	public Integer getMarkToDel() {
		return markToDel;
	}
	
	public Set<Integer> getDeleteList() {
		return deleteList;
	}
	
	/**
	 * Set canDeleteEIDSS in true if user have enough permissions 
	 */
	public void setCanDeleteEIDSSInTrue() {
		canDeleteEIDSS = Identity.instance().hasRole("TB_DELETE_EIDSS_NOT_BINDED");;
	}
	
	//========================CHANGE PATIENT RECORD NUMBER==========================
	
	/**
	 * Change recordNumber of patient in current instance in numDefault
	 */
	public void changeNumber(){
		String res="";
		try{
			if (!validateChange()){
				return;
				/*SequenceGenerator sequenceGenerator = (SequenceGenerator) App.getComponent("sequenceGenerator");
				sequenceGenerator.generateNewNumber("CASE_NUMBER");*/
			}
			CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
			Patient p = caseHome.getTbCase().getPatient();
			p.setRecordNumber(numDefault);
			App.getEntityManager().persist(p);
			App.getEntityManager().flush();
			res=App.getMessage("default.entity_updated");
			sequenceInfo = null;
		}
		catch (Exception e) {
			res="ERROR. In train of correcting number arised a problem: "+e.getLocalizedMessage();
		}
		facesMessages.add(res);
	}
	/**
	 * Return true if we can change number without problems
	 * */
	private boolean validateChange() {
		CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
		Patient p = caseHome.getTbCase().getPatient();
		int num = p.getRecordNumber().intValue();
		if (num == numDefault)
			return false;
		long duplicate = (Long)App.getEntityManager().createQuery("select count(*) from Patient p " +
											"where p.id != " +p.getId() +
											  "and p.recordNumber="+numDefault +
											  "and p.workspace.id=#{defaultWorkspace.id}")
							  .getResultList().get(0);
		if (duplicate!=0){
			facesMessages.add(App.getMessage("az_AZ.changeNumber.alreadyExist"));
			return false;
		}
		if (numDefault>getSequenceInfo().getNumber()){
			facesMessages.add(App.getMessage("az_AZ.changeNumber.maxOverflow"));
			return false;
		}
		
		return true;
	}

	/**
	 * Return SequenceInfo with max caseNumber for Azerbaijan
	 */
	private SequenceInfo getSequenceInfo() {
		if (sequenceInfo==null){
			String seq = "CASE_NUMBER";
			try {
				sequenceInfo = (SequenceInfo) App.getEntityManager().createQuery("from SequenceInfo s where s.sequence = :seq and s.workspace.id = :id")
				.setParameter("seq", seq)
				.setParameter("id", defaultWorkspace.getId())
				.getSingleResult();			
			} catch (Exception e) {
				sequenceInfo = null;
				e.printStackTrace();
			}
			
			if (sequenceInfo == null) {
				sequenceInfo = new SequenceInfo();
				sequenceInfo.setSequence(seq);
				sequenceInfo.setWorkspace(defaultWorkspace);
			}
		}
		return sequenceInfo;
	}
	
	public Integer getNumDefault(){
		//numDefault = getSequenceInfo().getNumber()+1;
		CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
		Patient p = caseHome.getTbCase().getPatient();
		numDefault = p.getRecordNumber();
		return numDefault;
	}

	public void setNumDefault(Integer numDefault) {
		this.numDefault = numDefault;
	}

	
	//==========================CONFIRM FOR START TREATMENT==================================
	/**
	 * Return true, if select tb-unit not where user from
	 */
	public boolean notOwnTBUnit(){
		TBUnitSelection nTbUSel;
		StartTreatmentHome th = (StartTreatmentHome)App.getComponent("startTreatmentHome",false);
		if (th!=null)
			nTbUSel = th.getTbunitselection();
		else{
			StartTreatmentIndivHome tih = (StartTreatmentIndivHome)App.getComponent("startTreatmentIndivHome",false);
			nTbUSel = tih.getTbunitselection();
		}
		Tbunit nTbU = nTbUSel.getTbunit();
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
		Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		TBUnitSelection nTbUSel;
		StartTreatmentHome th = (StartTreatmentHome)App.getComponent("startTreatmentHome",false);
		if (th!=null)
			nTbUSel = th.getTbunitselection();
		else{
			StartTreatmentIndivHome tih = (StartTreatmentIndivHome)App.getComponent("startTreatmentIndivHome",false);
			nTbUSel = tih.getTbunitselection();
		}
		nTbUSel.setTbunitWithOptions(nTbUUser);
	}
	
}
