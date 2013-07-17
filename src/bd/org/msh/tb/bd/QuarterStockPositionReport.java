package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.bd.entities.enums.Quarter;
import org.msh.tb.entities.Batch;
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
	@In(required=true) FacesMessages facesMessages;
	
	private TBUnitSelection2 tbunitselection;
	private Quarter quarter;
	private Integer year;
	private Source source;
	
	private List<QSPMedicineRow> rows;
	private Map<Batch, Long> batchDetails;
	private Date iniQuarterDate;
	private Date endQuarterDate;
	private List<Integer> years;
		
	/**
	 * Refreshes the rows values according to the filters.
	 */
	public void refresh(){
		if(getLocationWhereClause() == null)
			facesMessages.addToControlFromResourceBundle("cbselau1", "javax.faces.component.UIInput.REQUIRED");
		else{
			updateQuarterReport();
			updateBatchList();
		}
	}
	
	/**
	 * Load the list of years that is shown in the selection list
	 */
	private void loadYears(){
		if(years == null || years.size() == 0){
			Integer currYear = (new GregorianCalendar()).get(GregorianCalendar.YEAR);
			years = new ArrayList<Integer>();
			for(int i = currYear ; i >= 2012 ; i--){
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
	private void updateQuarterReport(){
		updateQuarterDates();
		loadMedicineList(medicines.getResultList());
		
		if(!isFiltersFilledIn()){
			loadMedicineList(medicines.getResultList());
			return;
		}
		
		//calc parameters for selecting the number dispensed to use on amc calc
		Date iniDateAmcCalc = iniQuarterDate;
		Date endDateAmcCalc = endQuarterDate;
		//If the selected quarter is opened it has to consider the last consumption of a closed quarter.
		if(tbunitselection.getTbunit()!=null && tbunitselection.getTbunit().getLimitDateMedicineMovement() != null
				&& ( tbunitselection.getTbunit().getLimitDateMedicineMovement().equals(iniQuarterDate)
						|| tbunitselection.getTbunit().getLimitDateMedicineMovement().before(iniQuarterDate) )){
			
			GregorianCalendar baseDate = new GregorianCalendar();
			baseDate.setTime(tbunitselection.getTbunit().getLimitDateMedicineMovement());
			
			Integer year = baseDate.get(GregorianCalendar.YEAR);
			Quarter quarter = Quarter.getPreviousQuarter(Quarter.getQuarterByMonth(baseDate.get(GregorianCalendar.MONTH)));
			
			if(quarter.equals(Quarter.FOURTH))
				year = year -1;
						
			GregorianCalendar iniDate = new GregorianCalendar();
			GregorianCalendar endDate = new GregorianCalendar();
			
			iniDate.set(year, quarter.getIniMonth(), quarter.getIniDay());
			endDate.set(year, quarter.getEndMonth(), quarter.getEndDay());
			
			iniDateAmcCalc = iniDate.getTime();
			endDateAmcCalc = endDate.getTime();
			
		}
		
		String queryString = "select m, " +
		
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and ( (mov.date < :iniDate) or " +
									"(mov.date >= :iniDate and mov.date <= :endDate and mov.type in (7)) ) " +
									"and mov.medicine.id = m.id " + getSourceClause() + ") as openingBalance, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (2,5) " +
									"and mov.medicine.id = m.id " + getSourceClause() + ") as received, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and (mov.quantity * mov.oper > 0) " +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as posAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov left join mov.adjustmentType " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and (mov.quantity * mov.oper) < 0" +
									" and mov.type in (1,4,6)" +
									" and (mov.adjustmentType.id <> :workspaceExpiredAdjust or mov.adjustmentType is null)" +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as negAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (3) and mov.medicine.id = m.id " + getSourceClause() + ") as dispensed, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and mov.adjustmentType.id = :workspaceExpiredAdjust " +
									" and mov.medicine.id = m.id and (mov.quantity * mov.oper) < 0 " + getSourceClause() + ") as expired, " +
								
								"(select sum(mov.outOfStock) from QuarterlyReportDetailsBD mov " + getLocationWhereClause() + 
									" and mov.quarter = :quarter and mov.year = :year and mov.medicine.id = m.id), " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and mov.date >= :iniDateAmcCalc and mov.date <= :endDateAmcCalc and mov.type in (3) and mov.medicine.id = m.id " + getSourceClause() + ") as dispensed " +

							  "from Medicine m " +
							  "where m.workspace.id = :workspaceId " +
							  "group by m";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("iniDate", iniQuarterDate)
									.setParameter("endDate", endQuarterDate)
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("quarter", quarter)
									.setParameter("year", year)
									.setParameter("iniDateAmcCalc", iniDateAmcCalc)
									.setParameter("endDateAmcCalc", endDateAmcCalc)									
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
					row.setDispensedForAmcCalc((Long) o[8]);
									
				}
			}
		}
	}
	
	/**
	 * Update the list of batches that will expired with in three months
	 */
	private void updateBatchList(){
		GregorianCalendar endDatePlus90 = new GregorianCalendar();
		endDatePlus90.setTime(endQuarterDate);
		endDatePlus90.add(GregorianCalendar.MONTH, 3);

		String queryString = "select b, sum(bm.quantity * mov.oper) " +
							"from BatchMovement bm join bm.batch b join bm.movement mov " +
							getLocationWhereClause() + " and mov.date <= :endDate " +
							"and b.expiryDate <= :endDatePlus90 " +
							"group by b.id " +
							"having sum(bm.quantity * mov.oper) > 0 " +
							"order by b.medicine.genericName.name1 ";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("endDate", endQuarterDate)
									.setParameter("endDatePlus90", endDatePlus90.getTime())
									.getResultList();

		if(batchDetails == null)
			batchDetails = new HashMap<Batch, Long>();
		else
			batchDetails.clear();	
		
		for(Object[] o : result){
			batchDetails.put((Batch) o[0], (Long) o[1]);
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
		if(tbunitselection == null)
			return null;
		
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

	public boolean isCurrQuarter(){
		GregorianCalendar currDate = new GregorianCalendar();
		Integer currYear = currDate.get(GregorianCalendar.YEAR);
		Integer currMonth = currDate.get(GregorianCalendar.MONTH);
		
		return quarter.equals(Quarter.getQuarterByMonth(currMonth)) && year.equals(currYear);
		
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
		if(rows == null || rows.size() == 0)
			loadMedicineList(medicines.getResultList());
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
		if(years == null || years.size() == 0)
			loadYears();
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
	/**
	 * @return the batchDetails
	 */
	public Map<Batch, Long> getBatchDetails() {
		return batchDetails;
	}
	/**
	 * @param batchDetails the batchDetails to set
	 */
	public void setBatchDetails(Map<Batch, Long> batchDetails) {
		this.batchDetails = batchDetails;
	}
	/**
	 * @return the batchDetails keyset in a list to use with JSF data table
	 */
	public ArrayList<Batch> getBatchDetailsKeySet() {
		if(batchDetails == null)
			return null;
		
		ArrayList<Batch> ret = new ArrayList<Batch>();
        for (Batch b : batchDetails.keySet())
            ret.add(b);
        return ret;
	}
}
