package org.msh.tb.ke;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.ke.entities.CaseDispensing_Ke;
import org.msh.tb.ke.entities.TbCaseKE;



/**
 * Handle registering dispensing days of a TB case
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingHome_Ke")
@Scope(ScopeType.CONVERSATION)
public class CaseDispensingHome {

	@In(required=true) CaseHome caseHome;
	@In EntityManager entityManager;
	
	private Integer month;
	private Integer year;

	private CaseDispensingInfo caseDispensingInfo;

	
	/**
	 * Save dispensing for the month/year of the selected case
	 * @return "dispensing-saved" if it was successfully saved
	 */
	@Transactional
	public String saveDispensing() {
		if (caseDispensingInfo == null)
			return "error";
		
		caseDispensingInfo.updateDispensingDays();

		CaseDispensing_Ke caseDisp = caseDispensingInfo.getCaseDispensing();
		entityManager.persist(caseDisp);
			
		caseDisp.getDispensingDays().setId(caseDisp.getId());
		entityManager.persist(caseDisp.getDispensingDays());
		entityManager.flush();

		return "dispensing-saved";
	}


	/**
	 * Return list of weeks
	 * @return
	 */
	public List<org.msh.tb.ke.WeekInfo> getWeeks() {
		if (caseDispensingInfo == null)
			createDispensingInfo();
	
		return (caseDispensingInfo == null? null: caseDispensingInfo.getWeeks());
	}


	
	/**
	 * Load dispensing data and create week structure
	 */
	protected void createDispensingInfo() {
		if ((month == null) || (year == null))
			return;
		
		List<CaseDispensing_Ke> lst = entityManager.createQuery("from CaseDispensing_Ke d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where c.id = #{caseHome.id} " +
				"and d.month = " + (month + 1) + " and d.year = " + year)
				.getResultList();

		if (lst.size() > 0)
			caseDispensingInfo = new CaseDispensingInfo(lst.get(0));
		else {
			CaseDispensing_Ke caseDispensing = new CaseDispensing_Ke();
			caseDispensing.setTbcase((TbCaseKE) caseHome.getInstance());
			caseDispensing.setMonth(month + 1);
			caseDispensing.setYear(year);
			
			caseDispensingInfo = new CaseDispensingInfo(caseDispensing);
		}
	}


	/**
	 * @return the month
	 */
	public Integer getMonth() {
		return month;
	}
	/**
	 * @param month the month to set
	 */
	public void setMonth(Integer month) {
		this.month = month;
	}
	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}
	
}
