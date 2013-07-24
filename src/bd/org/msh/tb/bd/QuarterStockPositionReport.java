package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

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
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection2;
import org.msh.utils.date.DateUtils;

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
	private Quarter selectedQuarter;
	private Source source;
	
	private List<QSPMedicineRow> rows;
	private Map<Batch, Long> batchDetails;
	private List<Integer> years;
	private List<Tbunit> pendCloseQuarterUnits;
		
	/**
	 * Refreshes the rows values according to the filters.
	 */
	public void refresh(){
		if(getLocationWhereClause() == null)
			facesMessages.addToControlFromResourceBundle("cbselau1", "javax.faces.component.UIInput.REQUIRED");
		else{
			updateQuarterReport();
			updateBatchList();
			updatePendCloseQuarterUnits();
		}
	}
	
	/**
	 * Load the list of years that is shown in the selection list
	 */
	public void loadYears(){
		if(years == null || years.size() == 0){
			Integer currYear = DateUtils.yearOf(DateUtils.getDate());
			years = new ArrayList<Integer>();
			for(int i = currYear ; i >= 2010 ; i--){
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
		loadMedicineList(medicines.getResultList());
		
		if(!isFiltersFilledIn()){
			return;
		}
		
		//calc parameters for selecting the number dispensed to use on amc calc
		Quarter quarterAmcCalc = selectedQuarter.copy();
		//If the selected quarter is opened it has to consider the last consumption of the last closed quarter.
		if(tbunitselection.getTbunit()!=null && tbunitselection.getTbunit().getLimitDateMedicineMovement() != null
				&& ( tbunitselection.getTbunit().getLimitDateMedicineMovement().equals(quarterAmcCalc.getIniDate())
						|| tbunitselection.getTbunit().getLimitDateMedicineMovement().before(quarterAmcCalc.getIniDate()) )){
			
			Date baseDate = tbunitselection.getTbunit().getLimitDateMedicineMovement();
			quarterAmcCalc = Quarter.getPreviousQuarter(Quarter.getQuarterByMonth(DateUtils.monthOf(baseDate), DateUtils.yearOf(baseDate)));
		}
		
		String queryString = "select m, " +
		
								"(select sum(mov.quantity * mov.oper) from Movement mov " + getLocationWhereClause() + 
									" and ( (mov.date < :iniDate) or (mov.date >= :iniDate and mov.date <= :endDate and mov.type in (7)) ) " +
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
									.setParameter("iniDate", selectedQuarter.getIniDate())
									.setParameter("endDate", selectedQuarter.getEndDate())
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("quarter", selectedQuarter)
									.setParameter("year", selectedQuarter.getYear())
									.setParameter("iniDateAmcCalc", quarterAmcCalc.getIniDate())
									.setParameter("endDateAmcCalc", quarterAmcCalc.getEndDate())									
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
		Date endDatePlus90 = DateUtils.incMonths(selectedQuarter.getEndDate(), 3);

		String queryString = "select b, sum(bm.quantity * mov.oper) " +
							"from BatchMovement bm join bm.batch b join bm.movement mov " +
							getLocationWhereClause() + " and mov.date <= :endDate " +
							"and b.expiryDate <= :endDatePlus90 " +
							"group by b.id " +
							"having sum(bm.quantity * mov.oper) > 0 " +
							"order by b.medicine.genericName.name1 ";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("endDate", selectedQuarter.getEndDate())
									.setParameter("endDatePlus90", endDatePlus90)
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
	 * Update the list of tbunits that haven't closed the selected quarter.
	 */
	private void updatePendCloseQuarterUnits(){
		if(getLocationWhereClause() == null || getTbunitselection().getTbunit() != null){
			pendCloseQuarterUnits = null;
			return;
		}
		
		String queryString = "from Tbunit u where u.adminUnit.code like :code and u.workspace.id = :workspaceId " +
									"and (u.limitDateMedicineMovement is null or u.limitDateMedicineMovement <= :iniQuarterDate) " +
									"and u.treatmentHealthUnit = :true " +
									"order by u.adminUnit.code, u.name.name1";
		
		pendCloseQuarterUnits = entityManager.createQuery(queryString)
									.setParameter("code", tbunitselection.getAuselection().getSelectedUnit().getCode()+'%')
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("iniQuarterDate", selectedQuarter.getIniDate())
									.setParameter("true", true)
									.getResultList();
		
	}
	
	/**
	 * Checks if all the requirements to load the table is attended
	 */
	public boolean isFiltersFilledIn(){
		if(rows == null || rows.size() == 0){
			loadMedicineList(medicines.getResultList());
		}
		
		return !(selectedQuarter == null || selectedQuarter.getYear() == 0 || getLocationWhereClause() == null);
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
			return "where mov.tbunit.adminUnit.code like '" + tbunitselection.getAuselection().getSelectedUnit().getCode() + "%' " +
					"and mov.tbunit.workspace.id = " + UserSession.getWorkspace().getId() + " and mov.tbunit.treatmentHealthUnit = true";
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
		return Quarter.getCurrentQuarter().isTheSame(selectedQuarter);
	}
	
	/**
	 * @return the selectedQuarter
	 */
	public Quarter getSelectedQuarter() {
		//Solve JSF problem when trying to set year before setting the quarter, the year depends on the quarter.
		if(this.selectedQuarter == null)
			return Quarter.FIRST;
		
		return selectedQuarter;
	}

	/**
	 * @param selectedQuarter the selectedQuarter to set
	 */
	public void setSelectedQuarter(Quarter selectedQuarter) {
		this.selectedQuarter = selectedQuarter;
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
	/**
	 * @return the pendCloseQuarterUnits
	 */
	public List<Tbunit> getPendCloseQuarterUnits() {
		return pendCloseQuarterUnits;
	}
	/**
	 * @param pendCloseQuarterUnits the pendCloseQuarterUnits to set
	 */
	public void setPendCloseQuarterUnits(List<Tbunit> pendCloseQuarterUnits) {
		this.pendCloseQuarterUnits = pendCloseQuarterUnits;
	}
}
