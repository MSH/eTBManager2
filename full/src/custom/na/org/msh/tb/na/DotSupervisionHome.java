package org.msh.tb.na;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.na.entities.CaseDispensingNA;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.*;


/**
 * List all cases on treatment in a specific health unit
 * @author Utkarsh Srivastava
 *
 */
@Name("dotSupervisionHome")
@Scope(ScopeType.CONVERSATION)
public class DotSupervisionHome {

	@In(create=true) EntityManager entityManager;
	
	private Tbunit tbunit;
	private Integer month;
	private Integer year;
	private List<CaseDispensingNA> dispensingList;

	/**
	 * Return the list of cases and its dispensing information of a specific month/year
	 * @return List of {@link CaseDispensingNA} objects
	 */
	public List<CaseDispensingNA> getDispensingList() {
		if (dispensingList == null)
			createCasesList();
		return dispensingList;
	}

	/**
	 * Create the list of {@link CaseDispensingNA} objects
	 */
	protected void createCasesList() {
		if ((month == null) || (year == null) || (tbunit == null))
			return;
		
		dispensingList = new ArrayList<CaseDispensingNA>();
		
		// load the dispensing days information recorded in the database
		dispensingList = entityManager.createQuery("from CaseDispensingNA d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where exists(select hu.id from TreatmentHealthUnit hu where hu.tbcase.id = c.id and hu.period.endDate = c.treatmentPeriod.endDate " +
				"and hu.tbunit.id = " + tbunit.getId() + ") " +
				"and d.month = " + (month + 1) + " and d.year = " + year +
				" and c.state = " + CaseState.ONTREATMENT.ordinal())
				.getResultList();
		
		// load the remaining cases on treatment
		List<TbCase> lstcases = entityManager.createQuery("from TbCase c join fetch c.patient p " +
				"where exists(select hu.id from TreatmentHealthUnit hu where hu.tbcase.id = c.id and hu.period.endDate = c.treatmentPeriod.endDate " +
				"and hu.tbunit.id = " + tbunit.getId() + ") " + 
				" and c.state = " + CaseState.ONTREATMENT.ordinal())
				.getResultList();
		for (TbCase tbcase: lstcases) {
			CaseDispensingNA info = findCaseDispensing(tbcase);
			if (info == null) {
				CaseDispensingNA caseDispensing = new CaseDispensingNA();
				caseDispensing.setTbcase(tbcase);
				caseDispensing.setMonth(month + 1);
				caseDispensing.setYear(year);				
				dispensingList.add(info);
			}
		}

		if (dispensingList.size() > 0) {
			// sort the list of cases by patient name
			Collections.sort(dispensingList, new Comparator<CaseDispensingNA>() {
				public int compare(CaseDispensingNA o1, CaseDispensingNA o2) {
					String name1 = o1.getTbcase().getPatient().getFullName();
					String name2 = o2.getTbcase().getPatient().getFullName();
					return name1.compareToIgnoreCase(name2);
				}				
			});
		}
	}

	
	protected CaseDispensingNA findCaseDispensing(TbCase tbcase) {
		for (CaseDispensingNA info: dispensingList) {
			if (info.getTbcase().getId().equals(tbcase.getId())) {
				return info;
			}
		}
		
		return null;
	}

	public Integer getTbunitId() {
		return (tbunit == null? null: tbunit.getId());
	}
	
	public void setTbunitId(Integer id) {
		if (id == null)
			 tbunit = null;
		else tbunit = entityManager.find(Tbunit.class, id);
	}
	
	/**
	 * @return the tbunit
	 */
	public Tbunit getTbunit() {
		return tbunit;
	}
	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}
	/**
	 * @return the month
	 */
	public Integer getMonth() {
		if (month == null)
			month = DateUtils.monthOf(new Date());
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
		if (year == null)
			year = DateUtils.yearOf(new Date());
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}
}

