package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.bd.entities.enums.Quarter;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection2;

/**
 * @author MSANTOS
 * 
 * Class responsible to retrieve and organize all necessary 
 * information to display the Quarterly Stock Position
 */
@Name("quarterStockPositionReport")
@Scope(ScopeType.CONVERSATION)
public class QuarterStockPositionReport {

	@In(create=true) MedicinesQuery medicines;
	@In(required=true) EntityManager entityManager;
	
	private TBUnitSelection2 tbunitselection;
	private Quarter quarter;
	private Integer year;
	private Source source;
	
	private List<QSPMedicineRow> rows;
	private Date iniQuarterDate;
	private Date endQuarterDate;
	private List<Integer> years;
	
	/**
	 * Initializes the list of medicines and its stock information.
	 */
	public void initialize(){
		loadYears();
		loadMedicineList(medicines.getResultList());
		updateValues();
	}
	
	/**
	 * Refreshes the rows values according to the filters.
	 */
	public void refresh(){
		updateValues();
	}
	
	/**
	 * Load the list of years that is shown in the selection list
	 */
	private void loadYears(){
		if(years == null || years.size() == 0){
			Integer currYear = (new GregorianCalendar()).get(GregorianCalendar.YEAR);
			years = new ArrayList<Integer>();
			for(int i = currYear ; i > 1999 ; i--){
				years.add(i);
			}
		}
	}
	
	/**
	 * Insert each row in the quarterly stock table information
	 * @param medicines
	 */
	private void loadMedicineList(List<Medicine> medicines){
		if(rows == null)
			this.rows = new ArrayList<QSPMedicineRow>();
		else 
			this.rows.clear();
			
		for(Medicine medicine : medicines){
			rows.add(new QSPMedicineRow(medicine));
		}
	}
	
	/**
	 * Update the values of each medicine in the quarterly stock table information
	 */
	private void updateValues(){
		updateQuarterDates();
		
		if(!isFiltersFilledIn()){
			loadMedicineList(medicines.getResultList());
			return;
		}
		
		String queryString = "select m, " +
		
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and ((mov.date < :iniDate) or (mov.date >= :iniDate and mov.date <= :endDate " +
									"and mov.type in (7) )) and mov.medicine.id = m.id " + getSourceClause() + ") as openingBalance, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (2,5) and mov.medicine.id = m.id " + getSourceClause() + ") as received, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and (mov.quantity * mov.oper > 0) " +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as posAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov left join mov.adjustmentType " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and (mov.quantity * mov.oper) < 0" +
									" and mov.type in (4)" +
									" and (mov.adjustmentType.id <> :workspaceExpiredAdjust or mov.adjustmentType is null)" +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as negAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (3) and mov.medicine.id = m.id " + getSourceClause() + ") as dispensed, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and mov.adjustmentType.id = :workspaceExpiredAdjust " +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as expired, " +
								
								"(select sum(mov.outOfStock) from QuarterlyReportDetailsBD mov " + getLocationWhereClause() + 
									" and mov.quarter = :quarter and mov.year = :year and mov.medicine.id = m.id) " +
									
							  "from Medicine m " +
							  "where m.workspace.id = :workspaceId " +
							  "group by m";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("iniDate", iniQuarterDate)
									.setParameter("endDate", endQuarterDate)
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("quarter", quarter)
									.setParameter("year", year)
									.setParameter("workspaceExpiredAdjust", UserSession.getWorkspace().getExpiredMedicineAdjustmentType().getId())
									.getResultList();
		
		for(Object[] o : result){
			for(QSPMedicineRow row : rows){
				if(row.getMedicine().getId() == ((Medicine) o[0]).getId()){
					row.setOpeningBalance((Long) o[1]);
					row.setReceivedFromCS((Long) o[2]);
					row.setPositiveAdjust((Long) o[3]);
					row.setNegativeAdjust((Long) o[4]);
					row.setDispensed((Long) o[5]);
					row.setExpired((Long) o[6]);
					row.setOutOfStockDays((Long) o[7]);
				}
			}
		}
		
	}
	
	/**
	 * Updates the iniQuarterDate and endQuarterDate according to the quarter and year in memory.
	 */
	private void updateQuarterDates(){
		if(year == null || quarter == null){
			this.iniQuarterDate = null;
			this.endQuarterDate = null;
			return;
		}
		
		GregorianCalendar iniDate = new GregorianCalendar();
		GregorianCalendar endDate = new GregorianCalendar();
		
		iniDate.set(year, quarter.getIniMonth(), quarter.getIniDay());
		endDate.set(year, quarter.getEndMonth(), quarter.getEndDay());
		
		this.iniQuarterDate = iniDate.getTime();
		this.endQuarterDate = endDate.getTime();
	}
	
	/**
	 * Checks if all the requirements to load the table is attended
	 */
	public boolean isFiltersFilledIn(){
		return !(rows == null || rows.size() == 0 || quarter == null || year == null || getLocationWhereClause() == null 
					|| iniQuarterDate == null || endQuarterDate == null);
	}
	
	/**
	 * Returns where clause depending on the tbunitselection filter
	 */
	public String getLocationWhereClause(){
		if(tbunitselection.getTbunit()!=null){
			return "where mov.tbunit.id = " + tbunitselection.getTbunit().getId() + " and mov.tbunit.workspace.id = " + UserSession.getWorkspace().getId();
		}else if(tbunitselection.getAuselection().getSelectedUnit()!=null){
			return "where mov.tbunit.adminUnit.code like '" + tbunitselection.getAuselection().getSelectedUnit().getCode() + "%' and mov.tbunit.workspace.id = " + UserSession.getWorkspace().getId();
		}
		return null;
	}
	
	/**
	 * Returns where clause depending on the selected source
	 */
	public String getSourceClause(){
		if(source == null)
			return "";
		
		return " and mov.source.id = " + source.getId() + " ";
	}
	
	/**
	 * @return the tbunitselection2
	 */
	public TBUnitSelection2 getTbunitselection() {
		if (tbunitselection == null)
			tbunitselection = new TBUnitSelection2(false, TBUnitFilter.HEALTH_UNITS);
		return tbunitselection;
	}

	/**
	 * @param tbunitselection the tbunitselection2 to set
	 */
	public void setTbunitselection(TBUnitSelection2 tbunitselection) {
		this.tbunitselection = tbunitselection;
	}

	/**
	 * @return the quarter
	 */
	public Quarter getQuarter() {
		return quarter;
	}

	/**
	 * @param quarter the quarter to set
	 */
	public void setQuarter(Quarter quarter) {
		this.quarter = quarter;
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

	/**
	 * @return the rows
	 */
	public List<QSPMedicineRow> getRows() {
		return rows;
	}

	/**
	 * @param row the rows to set
	 */
	public void setRows(List<QSPMedicineRow> rows) {
		this.rows = rows;
	}

	/**
	 * @return the iniQuarterDate
	 */
	public Date getIniQuarterDate() {
		return iniQuarterDate;
	}

	/**
	 * @param iniQuarterDate the iniQuarterDate to set
	 */
	public void setIniQuarterDate(Date iniQuarterDate) {
		this.iniQuarterDate = iniQuarterDate;
		updateQuarterDates();
	}

	/**
	 * @return the endQuarterDate
	 */
	public Date getEndQuarterDate() {
		return endQuarterDate;
	}

	/**
	 * @param endQuarterDate the endQuarterDate to set
	 */
	public void setEndQuarterDate(Date endQuarterDate) {
		this.endQuarterDate = endQuarterDate;
		updateQuarterDates();
	}
	
	public List<Integer> getYears(){
		return years;
	}
	
	/**
	 * @param years the years to set
	 */
	public void setYears(List<Integer> years) {
		this.years = years;
	}
	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}
}
