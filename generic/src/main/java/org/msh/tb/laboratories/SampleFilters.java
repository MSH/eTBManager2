package org.msh.tb.laboratories;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.enums.ExamStatus;

@Name("sampleFilters")
@Scope(ScopeType.SESSION)
public class SampleFilters {

	public enum SearchCriteria {
		PATIENT, TABLE;
	}
	
	private String patient;
	private ExamType examType;
	private Integer month;
	private Integer year;
	private ExamStatus examStatus;
	private Integer newOrder;
	private SearchCriteria searchCriteria;
	private boolean inverseOrder;
	


	/**
	 * @return the patient
	 */
	public String getPatient() {
		return patient;
	}
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(String patient) {
		this.patient = patient;
	}
	/**
	 * @return the examType
	 */
	public ExamType getExamType() {
		return examType;
	}
	/**
	 * @param examType the examType to set
	 */
	public void setExamType(ExamType examType) {
		this.examType = examType;
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
	
	public static SampleFilters instance() {
		return (SampleFilters)Component.getInstance("sampleFilters");
	}
	/**
	 * @return the newOrder
	 */
	public Integer getNewOrder() {
		return newOrder;
	}
	/**
	 * @param newOrder the newOrder to set
	 */
	public void setNewOrder(Integer newOrder) {
		if (this.newOrder == newOrder)
			inverseOrder = !inverseOrder;
		else {
			this.newOrder = newOrder;
			inverseOrder = false;
		}
	}
	/**
	 * @return the searchCriteria
	 */
	public SearchCriteria getSearchCriteria() {
		return searchCriteria;
	}
	/**
	 * @param searchCriteria the searchCriteria to set
	 */
	public void setSearchCriteria(SearchCriteria searchCriteria) {
		if (this.searchCriteria == searchCriteria)
			return;
		
		this.searchCriteria = searchCriteria;
		searchCriteriaChanged();
	}
	/**
	 * Called when the search criteria changes
	 */
	protected void searchCriteriaChanged() {
		if (searchCriteria == null)
			searchCriteria = SearchCriteria.PATIENT;
		
		switch (searchCriteria) {
			case TABLE:
				Integer m = month;
				Integer y = year;
				ExamStatus es = examStatus;
				ExamType et = examType;
				clearFilters();
				month = m;
				year = y;
				examStatus = es;
				examType = et;
				break;

			case PATIENT:
				String s = patient;
				clearFilters();
				patient = s;
				break;
		}
	}
	
	
	/**
	 * Clear filters restoring it to its original state
	 */
	public void clearFilters() {
		patient = null;
		examType = null; 
		month = null;
		year = null;
		examStatus = null;
		newOrder = 2;
	}
	/**
	 * @return the inverseOrder
	 */
	public boolean isInverseOrder() {
		return inverseOrder;
	}
	/**
	 * @param inverseOrder the inverseOrder to set
	 */
	public void setInverseOrder(boolean inverseOrder) {
		this.inverseOrder = inverseOrder;
	}
	/**
	 * @return the examStatus
	 */
	public ExamStatus getExamStatus() {
		return examStatus;
	}
	/**
	 * @param examStatus the examStatus to set
	 */
	public void setExamStatus(ExamStatus examStatus) {
		this.examStatus = examStatus;
	}
}
