package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitSelection2;
import org.msh.tb.tbunits.TBUnitType;
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
	@In(create=true) QSPExcelUtils qspExcelUtils;
	
	private TBUnitSelection2 tbunitselection;
	private Quarter selectedQuarter;
	private Source source;
	
	private List<QSPMedicineRow> rows;
	private List<Tbunit> pendCloseQuarterUnits;
	private List<Tbunit> unitsNotInitialized;
	
	/**
	 * Refreshes the rows values according to the filters.
	 */
	public void refresh(){
		if(QSPUtils.getLocationWhereClause(tbunitselection) == null){
			return;
		}else{
			updateQuarterReport();
			pendCloseQuarterUnits = QSPUtils.getPendCloseQuarterUnits(selectedQuarter, tbunitselection);
			unitsNotInitialized = QSPUtils.getNotInitializedUnits(selectedQuarter, tbunitselection);
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
		
		String queryString = "select m, " +
		
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and ( (mov.date < :iniDate) or (mov.date >= :iniDate and mov.date <= :endDate and mov.type in (7)) ) " +
									"and mov.medicine.id = m.id " + getSourceClause() + ") as openingBalance, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (2,5) " +
									"and mov.medicine.id = m.id " + getSourceClause() + ") as received, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and (mov.quantity * mov.oper > 0) " +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as posAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov left join mov.adjustmentType " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and (mov.quantity * mov.oper) < 0" +
									" and mov.type in (1,4,6)" +
									" and (mov.adjustmentType.id <> :workspaceExpiredAdjust or mov.adjustmentType is null)" +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as negAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (3) and mov.medicine.id = m.id " + getSourceClause() + ") as dispensed, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and mov.adjustmentType.id = :workspaceExpiredAdjust " +
									" and mov.medicine.id = m.id and (mov.quantity * mov.oper) < 0 " + getSourceClause() + ") as expired, " +
								
								"(select sum(mov.outOfStock) from QuarterlyReportDetailsBD mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.quarterMonth = :quarterMonth and mov.year = :year and mov.medicine.id = m.id) " +

							  "from Medicine m " +
							  "where m.workspace.id = :workspaceId " +
							  "group by m";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("iniDate", selectedQuarter.getIniDate())
									.setParameter("endDate", selectedQuarter.getEndDate())
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("quarterMonth", selectedQuarter.getQuarter())
									.setParameter("year", selectedQuarter.getYear())
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
		
		searchAmcCalcNums();
		
		//If it is being selected the results for a specific tbunit filter the medicines according to the specified rule.
		if(tbunitselection.getTbunit() != null)
			selectDisplayableMedicines();
	}
	
	private void searchAmcCalcNums(){
		String queryString = "";
		Quarter quarterAmcCalc = selectedQuarter.clone();
		
		if(tbunitselection.getSelected() != null){
			//A certain tbunit is selected
			
			//use the last closed quarter if the selected quarter is not closed
			if(tbunitselection.getTbunit().getLimitDateMedicineMovement() != null
					&& ( tbunitselection.getTbunit().getLimitDateMedicineMovement().compareTo(quarterAmcCalc.getIniDate()) <= 0 )){
				Date baseDate = tbunitselection.getTbunit().getLimitDateMedicineMovement();
				quarterAmcCalc = Quarter.getQuarterByMonth(DateUtils.monthOf(baseDate), DateUtils.yearOf(baseDate)).getPreviousQuarter();
			}
			
			queryString = "select m, " +
					
							"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
								" and mov.date >= :iniDateAmcCalc and mov.date <= :endDateAmcCalc and mov.type in (3) and mov.medicine.id = m.id " + getSourceClause() + ") as dispensed, " +
							
							"(select sum(mov.outOfStock) from QuarterlyReportDetailsBD mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
								" and mov.quarterMonth = :quarterMonthAmcCalc and mov.year = :yearAmcCalc and mov.medicine.id = m.id) " +
							
							"from Medicine m " +
							"where m.workspace.id = :workspaceId " +
							"group by m";

		}else{			
			String locationWhereClause = "where u.workspace.id = " + UserSession.getWorkspace().getId() + " and u.treatmentHealthUnit = true " +
							"and u.medManStartDate is not null and u.active = true ";
			
			if(tbunitselection.getAuselection().getSelectedUnit()!=null){
				locationWhereClause = locationWhereClause + "and u.adminUnit.code like '" + tbunitselection.getAuselection().getSelectedUnit().getCode() + "%' ";
			}			
			
			queryString = "select m, " +
					
			   				"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
								" and mov.date >= :iniDateAmcCalc and mov.date <= :endDateAmcCalc and mov.type in (3) and mov.medicine.id = m.id " + 
								" and mov.tbunit.limitDateMedicineMovement is not null and mov.tbunit.limitDateMedicineMovement > :endDateAmcCalc " + 
								getSourceClause() + ") as dispensedAMC, " +
								
							"(select sum(mov.outOfStock) from QuarterlyReportDetailsBD mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
								" and mov.tbunit.limitDateMedicineMovement is not null and mov.tbunit.limitDateMedicineMovement > :endDateAmcCalc " +
								" and mov.quarterMonth = :quarterMonthAmcCalc and mov.year = :yearAmcCalc and mov.medicine.id = m.id) as setOutOfStockForAmcCalc, " +

							"(select count(*) from Tbunit u " + locationWhereClause + 
								" and u.limitDateMedicineMovement is not null and u.limitDateMedicineMovement > :endDateAmcCalc ) as qtdUnit " +
							 
							"from Medicine m " +
							"where m.workspace.id = :workspaceId " +
							"group by m";
		}
		
		List<Object[]> result = entityManager.createQuery(queryString)
				.setParameter("workspaceId", UserSession.getWorkspace().getId())
				.setParameter("iniDateAmcCalc", quarterAmcCalc.getIniDate())
				.setParameter("endDateAmcCalc", quarterAmcCalc.getEndDate())
				.setParameter("quarterMonthAmcCalc", quarterAmcCalc.getQuarter())
				.setParameter("yearAmcCalc", quarterAmcCalc.getYear())
				.getResultList();
		
		for(Object[] o : result){
			for(QSPMedicineRow row : rows){
				if(row.getMedicine().getId() == ((Medicine) o[0]).getId()){
					row.setDispensedForAmcCalc((Long) o[1]);
					row.setOutOfStockForAmcCalc((Long) o[2]);
					
					if(tbunitselection.getSelected() != null)
						row.setUnitQtdForAmcCalc(new Long(1));
					else
						row.setUnitQtdForAmcCalc((Long) o[3]);
				}
			}
		}
	}
	
	private void selectDisplayableMedicines(){
		List<QSPMedicineRow> newRows = new ArrayList<QSPMedicineRow>(); 
		
		for(QSPMedicineRow row : rows){
			row.setHighlight(false);
			
			if(row.getMedicine().getLine().equals(MedicineLine.OTHER) && hasStockMovement(row))
				newRows.add(row);
			else if(row.getMedicine().getLine().equals(MedicineLine.FIRST_LINE) && hasStockMovement(row)){
				if(tbunitselection.getTbunit().getFirstLineSupplier() == null)
					row.setHighlight(true);
				newRows.add(row);
			}else if(row.getMedicine().getLine().equals(MedicineLine.SECOND_LINE) && hasStockMovement(row)){
				if(tbunitselection.getTbunit().getSecondLineSupplier() == null)
					row.setHighlight(true);
				newRows.add(row);
			}
		}
		
		rows = newRows;
	}
	
	private boolean hasStockMovement(QSPMedicineRow row){
		if(row.getOpeningBalance() !=0 || row.getClosingBalance() != 0 || row.getDispensed() != 0
				|| row.getExpired() != 0 || row.getNegativeAdjust() != 0
				|| row.getPositiveAdjust() != 0 || row.getReceivedFromCS() != 0){
			return true;
		}
		return false;
	}
		
	/**
	 * Checks if all the requirements to load the table is attended
	 */
	public boolean isFiltersFilledIn(){
		/*if(rows == null || rows.size() == 0){
			loadMedicineList(medicines.getResultList());
		}*/
		
		return !(selectedQuarter == null || selectedQuarter.getYear() == 0 || QSPUtils.getLocationWhereClause(tbunitselection) == null);
	}
	
	/**
	 * Downloads an excel file containing the information of the current state of this class.
	 */
	public void downloadExcel(){
		if(rows == null || rows.size()<1){
			facesMessages.addFromResourceBundle("cases.details.noresultfound");
			return;
		}
		qspExcelUtils.downloadQuarterlyConsolidatedSP(source, selectedQuarter, tbunitselection.getAuselection().getSelectedUnit(), tbunitselection.getTbunit(), rows);
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
			tbunitselection = new TBUnitSelection2("unitid", false, TBUnitType.HEALTH_UNITS);
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
		if(selectedQuarter == null)
			this.selectedQuarter = new Quarter();
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
		return rows;
	}

	/**
	 * @param row the rows to set
	 */
	public void setRows(List<QSPMedicineRow> rows) {
		this.rows = rows;
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

	/**
	 * @return the unitsNotInitialized
	 */
	public List<Tbunit> getUnitsNotInitialized() {
		return unitsNotInitialized;
	}

	/**
	 * @param unitsNotInitialized the unitsNotInitialized to set
	 */
	public void setUnitsNotInitialized(List<Tbunit> unitsNotInitialized) {
		this.unitsNotInitialized = unitsNotInitialized;
	}
}
