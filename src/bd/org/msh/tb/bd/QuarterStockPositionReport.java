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
import org.msh.tb.tbunits.TBUnitType;
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
	private List<Tbunit> pendCloseQuarterUnits;
		
	/**
	 * Refreshes the rows values according to the filters.
	 */
	public void refresh(){
		if(QSPUtils.getLocationWhereClause(tbunitselection) == null)
			return;
		else{
			updateQuarterReport();
			pendCloseQuarterUnits = QSPUtils.getPendCloseQuarterUnits(selectedQuarter, tbunitselection);
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
		
		//Select witch medicines enters in the list according to medicine line
		if(tbunitselection.getTbunit() != null){
			for(Medicine medicine : medicines){
				if(medicine.getLine().equals(MedicineLine.FIRST_LINE) && tbunitselection.getTbunit().getFirstLineSupplier() != null)
					rows.add(new QSPMedicineRow(medicine));
				else if(medicine.getLine().equals(MedicineLine.SECOND_LINE) && tbunitselection.getTbunit().getSecondLineSupplier() != null)
					rows.add(new QSPMedicineRow(medicine));
				else if(medicine.getLine().equals(MedicineLine.OTHER))
					rows.add(new QSPMedicineRow(medicine));
			}
		}else{
			for(Medicine medicine : medicines){
				rows.add(new QSPMedicineRow(medicine));
			}
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
		Quarter quarterAmcCalc = selectedQuarter.clone();
		//If the selected quarter is opened it has to consider the last consumption of the last closed quarter.
		if(tbunitselection.getTbunit()!=null && tbunitselection.getTbunit().getLimitDateMedicineMovement() != null
				&& ( tbunitselection.getTbunit().getLimitDateMedicineMovement().equals(quarterAmcCalc.getIniDate())
						|| tbunitselection.getTbunit().getLimitDateMedicineMovement().before(quarterAmcCalc.getIniDate()) )){
			
			Date baseDate = tbunitselection.getTbunit().getLimitDateMedicineMovement();
			quarterAmcCalc = Quarter.getQuarterByMonth(DateUtils.monthOf(baseDate), DateUtils.yearOf(baseDate)).getPreviousQuarter();
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
									" and mov.quarterMonth = :quarterMonth and mov.year = :year and mov.medicine.id = m.id), " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDateAmcCalc and mov.date <= :endDateAmcCalc and mov.type in (3) and mov.medicine.id = m.id " + getSourceClause() + ") as dispensed, " +
								
								"(select sum(mov.outOfStock) from QuarterlyReportDetailsBD mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.quarterMonth = :quarterMonthAmcCalc and mov.year = :yearAmcCalc and mov.medicine.id = m.id) " +

							  "from Medicine m " +
							  "where m.workspace.id = :workspaceId " +
							  "group by m";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("iniDate", selectedQuarter.getIniDate())
									.setParameter("endDate", selectedQuarter.getEndDate())
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("quarterMonth", selectedQuarter.getQuarter())
									.setParameter("year", selectedQuarter.getYear())
									.setParameter("iniDateAmcCalc", quarterAmcCalc.getIniDate())
									.setParameter("endDateAmcCalc", quarterAmcCalc.getEndDate())
									.setParameter("quarterMonthAmcCalc", quarterAmcCalc.getQuarter())
									.setParameter("yearAmcCalc", quarterAmcCalc.getYear())
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
					row.setOutOfStockForAmcCalc((Long) o[9]);
				}
			}
		}
	}
		
	/**
	 * Checks if all the requirements to load the table is attended
	 */
	public boolean isFiltersFilledIn(){
		if(rows == null || rows.size() == 0){
			loadMedicineList(medicines.getResultList());
		}
		
		return !(selectedQuarter == null || selectedQuarter.getYear() == 0 || QSPUtils.getLocationWhereClause(tbunitselection) == null);
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
}
