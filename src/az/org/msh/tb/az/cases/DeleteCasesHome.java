package org.msh.tb.az.cases;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseResultItem;
import org.msh.tb.cases.PatientsQuery.Item;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;

@Name("deleteCasesHome")
@Scope(ScopeType.PAGE)
public class DeleteCasesHome {
	@In(create=true) CaseAZHome caseAZHome;

	@In(create=true) CaseHome caseHome;
	
	private Set<Integer> deleteList = new HashSet<Integer>();
	private Integer markToDel;
	private Boolean canDeleteEIDSS;
	
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
}
