package org.msh.tb.cases.dispensing;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.CaseDispensing;
import org.msh.tb.cases.CaseHome;


/**
 * Handle registering dispensing days of a TB case
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingHome")
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

		CaseDispensing caseDisp = caseDispensingInfo.getCaseDispensing();
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
	public List<WeekInfo> getWeeks() {
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
		
		List<CaseDispensing> lst = entityManager.createQuery("from CaseDispensing d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where c.id = #{caseHome.id} " +
				"and d.month = " + (month + 1) + " and d.year = " + year)
				.getResultList();

		if (lst.size() > 0)
			caseDispensingInfo = new CaseDispensingInfo(lst.get(0));
		else {
			CaseDispensing caseDispensing = new CaseDispensing();
			caseDispensing.setTbcase(caseHome.getInstance());
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
