package org.msh.tb.bd;

import freemarker.template.utility.DateUtil;
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

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author MSANTOS
 * 
 * Class responsible to retrieve and organize all necessary 
 * information to display the Quarterly Stock Position
 */
@Name("stockPositionReportBD")
@Scope(ScopeType.CONVERSATION)
public class StockPositionReportBD {
 
	@In(create=true) MedicinesQuery medicines;
	@In(required=true) EntityManager entityManager;
	@In(required=true) FacesMessages facesMessages;
	@In(create=true) QSPExcelUtils qspExcelUtils;
	
	private TBUnitSelection2 tbunitselection;
	private Date iniDate;
	private Date endDate;
	private Source source;
	
	private List<QSPMedicineRow> rows;
	
	/**
	 * Refreshes the rows values according to the filters.
	 */
	public void refresh(){
		if(QSPUtils.getLocationWhereClause(tbunitselection) == null || iniDate == null || endDate == null)
			return;

		updateQuarterReport();
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
									" and mov.type in (4)" +
									" and (mov.adjustmentType.id <> :workspaceExpiredAdjust or mov.adjustmentType is null)" +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as negAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (3) and mov.medicine.id = m.id " + getSourceClause() + ") as dispensed, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and mov.adjustmentType.id = :workspaceExpiredAdjust " +
									" and mov.medicine.id = m.id and (mov.quantity * mov.oper) < 0 " + getSourceClause() + ") as expired, " +

								"(select sum(mov.quantity * mov.oper) from Movement mov " + QSPUtils.getLocationWhereClause(tbunitselection) + 
									" and mov.date >= :iniDate and mov.date <= :endDate and (mov.quantity * mov.oper) < 0" +
									" and mov.type in (6)" +
									" and mov.medicine.id = m.id " + getSourceClause() + ") as transferedOutQtd " +

							  "from Medicine m " +
							  "where m.workspace.id = :workspaceId " +
							  "group by m";
		
		List<Object[]> result = entityManager.createQuery(queryString)
				.setParameter("iniDate", getIniDate())
				.setParameter("endDate", getEndDate())
				.setParameter("workspaceId", UserSession.getWorkspace().getId())
				.setParameter("workspaceExpiredAdjust", UserSession.getWorkspace().getExpiredMedicineAdjustmentType().getId())
									.getResultList();

		int daysInPeriod = DateUtils.daysBetween(iniDate, endDate);

		for(Object[] o : result){
			for(QSPMedicineRow row : rows){
				if(row.getMedicine().getId() == ((Medicine) o[0]).getId()){
					row.setOpeningBalance((Long) o[1]);
					row.setReceivedFromCS((Long) o[2]);
					row.setPositiveAdjust((Long) o[3]);
					row.setNegativeAdjust((Long) o[4]);
					row.setDispensed((Long) o[5]);
					row.setExpired((Long) o[6]);
					row.setTransferedOutQtd((Long) o[7]);
					row.setDaysQtdInPeriod(new Long(daysInPeriod));
				}
			}
		}

		//If it is being selected the results for a specific tbunit filter the medicines according to the specified rule.
		if(tbunitselection.getTbunit() != null)
			selectDisplayableMedicines();
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
		return !(getIniDate() == null || getEndDate() == null || QSPUtils.getLocationWhereClause(tbunitselection) == null);
	}
	
	/**
	 * Downloads an excel file containing the information of the current state of this class.
	 */
	public void downloadExcel(){
		if(rows == null || rows.size()<1){
			facesMessages.addFromResourceBundle("cases.details.noresultfound");
			return;
		}

		qspExcelUtils.downloadQuarterlyConsolidatedSP(source, iniDate, endDate, tbunitselection.getAuselection().getSelectedUnit(), tbunitselection.getTbunit(), rows);
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

	/**
	 * @return the rows
	 */
	public List<QSPMedicineRow> getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
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

	public Date getIniDate() {
		return iniDate;
	}

	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
