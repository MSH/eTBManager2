package org.msh.tb.bd;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitSelection2;
import org.msh.tb.tbunits.TBUnitType;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * @author MSANTOS
 * 
 * Class responsible to retrieve and organize all necessary 
 * information to display the Quarterly Stock Position
 */
@Name("quarterBatchExpiringReport")
@Scope(ScopeType.CONVERSATION)
public class QuarterBatchExpiringReport {
 
	@In(required=true) EntityManager entityManager;
	@In(required=true) FacesMessages facesMessages;
	@In(create=true) QSPExcelUtils qspExcelUtils;
	
	private TBUnitSelection2 tbunitselection;
	private Quarter selectedQuarter;
	private Source source;
	
	private Map<Batch, Long> batchDetailsConsolidated;
	private List<ExpiringBatchDetails> unitBatchDetails;
	private List<Tbunit> pendCloseQuarterUnits;
	private List<Tbunit> unitsNotInitialized;
	
	private static final int PERIOD_ON_MONTHS = 6;
		
	/**
	 * Refreshes the rows values according to the filters.
	 */
	public void refresh(){
		//Checks if the user has selected an admin unit or a tbunit.
		if(QSPUtils.getLocationWhereClause(tbunitselection) == null)
			return;
		
		if(!isFiltersFilledIn())
			return;
		
		//clear information on memory
		if(unitBatchDetails != null)
			unitBatchDetails.clear();
		else
			unitBatchDetails = new ArrayList<ExpiringBatchDetails>();
			
		if(batchDetailsConsolidated != null)
			batchDetailsConsolidated.clear();
		else
			batchDetailsConsolidated = new HashMap<Batch, Long>();
		
		//loads informations according to the selected (admin unit or tbunit)
		if(tbunitselection.getTbunit() != null){
			unitBatchDetails.add(createBatchDetailedUnit(tbunitselection.getTbunit()));
			batchDetailsConsolidated = null;
		}else if(tbunitselection.getAuselection().getSelectedUnit() != null){
			List<Tbunit> units = entityManager.createQuery(" from Tbunit u where u.adminUnit.code like :code and u.treatmentHealthUnit = :true " +
					"and u.workspace.id = :workspaceId " +
					"order by u.adminUnit.code, u.name.name1")
					.setParameter("true", true)
					.setParameter("code", tbunitselection.getAuselection().getSelectedUnit().getCode()+"%")
					.setParameter("workspaceId", UserSession.getWorkspace().getId())
					.getResultList();
					for(Tbunit u : units){
					unitBatchDetails.add(createBatchDetailedUnit(u));
					}			
					updateConsolidatedBatchList();
		}else{
			List<Tbunit> units = entityManager.createQuery(" from Tbunit u where u.treatmentHealthUnit = :true " +
							"and u.workspace.id = :workspaceId " +
							"order by u.adminUnit.code, u.name.name1")
				.setParameter("true", true)
				.setParameter("workspaceId", UserSession.getWorkspace().getId())
				.getResultList();
			for(Tbunit u : units){
				unitBatchDetails.add(createBatchDetailedUnit(u));
			}			
			updateConsolidatedBatchList();
		}
		
		pendCloseQuarterUnits = QSPUtils.getPendCloseQuarterUnits(selectedQuarter, tbunitselection);
		unitsNotInitialized = QSPUtils.getNotInitializedUnits(selectedQuarter, tbunitselection);
	}
	
	/**
	 * Checks if all the requirements to load the table is attended
	 */
	public boolean isFiltersFilledIn(){
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
	 * Returns the expiring limit date considered on the query.
	 */
	public Date getExiringLimitDate(){
		Date exiringLimitDate = DateUtils.incMonths(selectedQuarter.getEndDate(), PERIOD_ON_MONTHS);
		return exiringLimitDate;
	}
	
	/**
	 * Update the list of batches that will expired with in three months
	 */
	private ExpiringBatchDetails createBatchDetailedUnit(Tbunit unit){
		ExpiringBatchDetails ret = new ExpiringBatchDetails(unit);
		HashMap<Batch, Long> batchInfo = new HashMap<Batch, Long>();
		
		String queryString = "select b, sum(bm.quantity * mov.oper) " +
								"from BatchMovement bm join bm.batch b join bm.movement mov " +
								"where mov.tbunit.id = :unitId and mov.tbunit.workspace.id = :workspaceId " +
									"and mov.date <= :endQuarterDate and b.expiryDate > :endQuarterDate and b.expiryDate < :expiringLimitDate " + getSourceClause() +
								"group by b.id " +
								"having sum(bm.quantity * mov.oper) > 0 " +
								"order by b.medicine.genericName.name1 ";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("endQuarterDate", selectedQuarter.getEndDate())
									.setParameter("expiringLimitDate", getExiringLimitDate())
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("unitId", ret.getUnit().getId())
									.getResultList();

		
		for(Object[] o : result){
			batchInfo.put((Batch) o[0], (Long) o[1]);
		}
		
		ret.setBatchInfo(batchInfo);
		
		return ret;
	}
	
	/**
	 * Update the list of batches that will expired with in three months
	 */
	private void updateConsolidatedBatchList(){
		String queryString = "select b, sum(bm.quantity * mov.oper) " +
							"from BatchMovement bm join bm.batch b join bm.movement mov " +
							QSPUtils.getLocationWhereClause(tbunitselection) + " and mov.date <= :endQuarterDate " +
							"and b.expiryDate > :endQuarterDate and b.expiryDate < :exiringLimitDate " + getSourceClause() +
							"group by b.id " +
							"having sum(bm.quantity * mov.oper) > 0 " +
							"order by b.medicine.genericName.name1 ";
		
		List<Object[]> result = entityManager.createQuery(queryString)
									.setParameter("endQuarterDate", selectedQuarter.getEndDate())
									.setParameter("exiringLimitDate", getExiringLimitDate())
									.getResultList();

		if(batchDetailsConsolidated == null)
			batchDetailsConsolidated = new HashMap<Batch, Long>();
		else
			batchDetailsConsolidated.clear();	
		
		for(Object[] o : result){
			batchDetailsConsolidated.put((Batch) o[0], (Long) o[1]);
		}
	}
	
	public void downloadExcel(){
		if(unitBatchDetails == null || unitBatchDetails.size()<1){
			facesMessages.addFromResourceBundle("cases.details.noresultfound");
			return;
		}
		
		qspExcelUtils.downloadQuarterlyBatchExpiringReport(source, selectedQuarter, tbunitselection.getAuselection().getSelectedUnit(), tbunitselection.getTbunit(), unitBatchDetails, batchDetailsConsolidated, pendCloseQuarterUnits, unitsNotInitialized);
	}
	
	/**
	 * @return the tbunitselection
	 */
	public TBUnitSelection2 getTbunitselection() {
		if (tbunitselection == null)
			tbunitselection = new TBUnitSelection2("unitid", false, TBUnitType.HEALTH_UNITS);
		return tbunitselection;
	}

	/**
	 * @param tbunitselection the tbunitselection to set
	 */
	public void setTbunitselection(TBUnitSelection2 tbunitselection) {
		this.tbunitselection = tbunitselection;
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
	 * @return the batchDetailsConsolidated
	 */
	public Map<Batch, Long> getBatchDetailsConsolidated() {
		return batchDetailsConsolidated;
	}
	
	/**
	 * @return the batchDetails keyset in a list to use with JSF data table
	 */
	public ArrayList<Batch> getBatchDetailsConsolidatedKeySet() {
		if(batchDetailsConsolidated == null)
			return null;
		
		ArrayList<Batch> ret = new ArrayList<Batch>();
        for (Batch b : batchDetailsConsolidated.keySet())
            ret.add(b);
        return ret;
	}

	/**
	 * @param batchDetailsConsolidated the batchDetailsConsolidated to set
	 */
	public void setBatchDetailsConsolidated(
			Map<Batch, Long> batchDetailsConsolidated) {
		this.batchDetailsConsolidated = batchDetailsConsolidated;
	}
	
	/**
	 * @return the unitBatchDetails
	 */
	public List<ExpiringBatchDetails> getUnitBatchDetails() {
		return unitBatchDetails;
	}

	/**
	 * @param unitBatchDetails the unitBatchDetails to set
	 */
	public void setUnitBatchDetails(List<ExpiringBatchDetails> unitBatchDetails) {
		this.unitBatchDetails = unitBatchDetails;
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
	 * @return the periodOnMonths
	 */
	public static int getPeriodOnMonths() {
		return PERIOD_ON_MONTHS;
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



	/**
	 * Class used to storage the expiring batch details by unit
	 * @author MSANTOS
	 */
	public class ExpiringBatchDetails{
		private Tbunit unit;
		private Map<Batch, Long> batchInfo;
		
		public ExpiringBatchDetails(Tbunit unit){
			this.unit = unit;
		}
		
		/**
		 * @return the unit
		 */
		public Tbunit getUnit() {
			return unit;
		}
		/**
		 * @param unit the unit to set
		 */
		public void setUnit(Tbunit unit) {
			this.unit = unit;
		}
		/**
		 * @return the batchInfo
		 */
		public Map<Batch, Long> getBatchInfo() {
			return batchInfo;
		}
		/**
		 * @param batchInfo the batchInfo to set
		 */
		public void setBatchInfo(Map<Batch, Long> batchInfo) {
			this.batchInfo = batchInfo;
		}
		/**
		 * @return the batchDetails keyset in a list to use with JSF data table
		 */
		public ArrayList<Batch> getBatchDetailsKeySet() {
			if(batchInfo == null)
				return null;
			
			ArrayList<Batch> ret = new ArrayList<Batch>();
	        for (Batch b : batchInfo.keySet())
	            ret.add(b);
	        return ret;
		}
	}
}
