package org.msh.tb.az;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseResultItem;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;

@Name("casesControlAZ")
@Scope(ScopeType.PAGE)
public class CasesControlAZ {
	@In(create=true) CaseHome caseHome;
	
	private Set<Integer> deleteList = new HashSet<Integer>();
	private Integer markToDel;
	private Boolean canDeleteEIDSS;
	
	/**
	 * Return true if this list of cases is the list of not binded EIDSS-cases and if user have enough permissions 
	 * */
	public Boolean getCanDeleteEIDSS(){
		if (canDeleteEIDSS==null){
			canDeleteEIDSS = false;
			CaseFilters caseFilters = (CaseFilters)App.getComponent("caseFilters");
			if (caseFilters != null) 
				if (caseFilters.getStateIndex()!=null)
					if (caseFilters.getStateIndex()==1515)
						canDeleteEIDSS = true;
			canDeleteEIDSS = canDeleteEIDSS&&Identity.instance().hasRole("TB_DELETE_EIDSS_NOT_BINDED");
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
	public void fillDeleteList(){
		CasesQueryAZ cq = (CasesQueryAZ)App.getComponent("casesAZ");
		List<CaseResultItem> lst = cq.getResultList();
		if (deleteList.size()>=cq.getMaxResults())
			deleteList.clear();
		else{
			for (CaseResultItem it:lst)
				deleteList.add(it.getTbcase().getId());
		}
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
		deleteList.clear();
	}
	
	public Integer getMarkToDel() {
		return markToDel;
	}
	
	public Set<Integer> getDeleteList() {
		return deleteList;
	}

}
