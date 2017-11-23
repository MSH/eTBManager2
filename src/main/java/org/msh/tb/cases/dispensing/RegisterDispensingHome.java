package org.msh.tb.cases.dispensing;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.entities.enums.CaseState;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.*;


/**
 * Register the dispensing of all cases on treatment in a specific health unit
 * @author Ricardo Memoria
 *
 */
@Name("registerDispensingHome")
@Scope(ScopeType.CONVERSATION)
public class RegisterDispensingHome {

	@In(create=true) EntityManager entityManager;
	
	private Tbunit tbunit;
	private Integer month;
	private Integer year;
	private List<CaseDispensingInfo> cases;
	private List<WeekInfo> weeks;
	private int weekIndex;

	/**
	 * Return the list of cases and its dispensing information of a specific month/year
	 * @return List of {@link CaseDispensingInfo} objects
	 */
	public List<CaseDispensingInfo> getCases() {
		if (cases == null)
			createCasesList();
		return cases;
	}


	/**
	 * Save all dispensing information to the database
	 * @return
	 */
	@Transactional
	public String save() {
		if (cases == null)
			return "error";
		
		for (CaseDispensingInfo dispInfo: cases) {
			dispInfo.updateDispensingDays();

			TreatmentMonitoring treatMon = dispInfo.getTreatmentMonitoring();
			entityManager.persist(treatMon);
		}
		entityManager.flush();

		return "dispensing-saved";
	}


	/**
	 * Create the list of {@link CaseDispensingInfo} objects read for editing
	 */
	protected void createCasesList() {
		if ((month == null) || (year == null) || (tbunit == null))
			return;
		
		cases = new ArrayList<CaseDispensingInfo>();
		
		// load the dispensing days information recorded in the database
		List<TreatmentMonitoring> lst = entityManager.createQuery("from TreatmentMonitoring d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where c.ownerUnit.id = " + tbunit.getId() + 
				"and d.month = " + (month + 1) + " and d.year = " + year +
				" and c.state = " + CaseState.ONTREATMENT.ordinal())
				.getResultList();
		
		for (TreatmentMonitoring disp: lst) {
			CaseDispensingInfo info = new CaseDispensingInfo(disp);
			cases.add(info);
		}
		
		// load the remaining cases on treatment
		List<TbCase> lstcases = entityManager.createQuery("from TbCase c join fetch c.patient p " +
				"where c.ownerUnit.id = " + tbunit.getId() + ") " + 
				" and c.state = " + CaseState.ONTREATMENT.ordinal())
				.getResultList();
		for (TbCase tbcase: lstcases) {
			CaseDispensingInfo info = findCaseDispensingInfo(tbcase);
			if (info == null) {
				TreatmentMonitoring tm = new TreatmentMonitoring();
				tm.setTbcase(tbcase);
				tm.setMonth(month + 1);
				tm.setYear(year);
				
				info = new CaseDispensingInfo(tm);
				cases.add(info);
			}
		}

		if (cases.size() > 0) {
			weeks = cases.get(0).getWeeks();
			// sort the list of cases by patient name
			Collections.sort(cases, new Comparator<CaseDispensingInfo>() {
				public int compare(CaseDispensingInfo o1, CaseDispensingInfo o2) {
					String name1 = o1.getTreatmentMonitoring().getTbcase().getPatient().getFullName();
					String name2 = o2.getTreatmentMonitoring().getTbcase().getPatient().getFullName();
					return name1.compareToIgnoreCase(name2);
				}				
			});
			
			if ((weekIndex == -1) || (weekIndex >= weeks.size()))
				weekIndex = 0;
			selectWeek();
		}
		else {
			weeks = null;
			weekIndex = -1;
			selectWeek();
		}
	}

	
	protected CaseDispensingInfo findCaseDispensingInfo(TbCase tbcase) {
		for (CaseDispensingInfo info: cases) {
			if (info.getTreatmentMonitoring().getTbcase().getId().equals(tbcase.getId())) {
				return info;
			}
		}
		
		return null;
	}


	private void selectWeek() {
		if (cases == null)
			return;
		for (CaseDispensingInfo info: cases) {
			if (weekIndex == -1)
				 info.setSelectedWeek(null);
			else info.setSelectedWeek(info.getWeeks().get(weekIndex));
		}
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

	/**
	 * @return the weeks
	 */
	public List<WeekInfo> getWeeks() {
		if (weeks == null)
			createCasesList();
		return weeks;
	}


	/**
	 * @return the weekIndex
	 */
	public int getWeekIndex() {
		return weekIndex;
	}


	/**
	 * @param weekIndex the weekIndex to set
	 */
	public void setWeekIndex(int week) {
		this.weekIndex = week;
		selectWeek();
	}
	

	public WeekInfo getSelectedWeek() {
		if (weeks == null)
			createCasesList();
		if (weeks != null) {
			if ((weekIndex >= weeks.size()) || (weekIndex < 0))
				return null;
			return weeks.get(weekIndex);			
		}
		else return null;
	}

}
