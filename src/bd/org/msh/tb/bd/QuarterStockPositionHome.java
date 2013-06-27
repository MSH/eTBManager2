package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.bd.QSPEditingMedicine.QSPEditingBatchQuantity;
import org.msh.tb.bd.entities.QuarterlyReportDetailsBD;
import org.msh.tb.bd.entities.enums.Quarter;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.dispensing.DispensingHome;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.tbunits.TbUnitHome;
import org.msh.tb.transactionlog.TransactionLogService;

/**
 * Manage all actions needed to edit a medicine row in Quarterly Stock position From Upazilla
 * in Bangladesh
 * @author MSANTOS
 */
@Scope(ScopeType.CONVERSATION)
@Name("quarterStockPositionHome")
public class QuarterStockPositionHome extends EntityHomeEx<QuarterlyReportDetailsBD>{

	private static final long serialVersionUID = 3382653446118482945L;

	@In(create=true) QuarterStockPositionReport quarterStockPositionReport;
	@In(required=true) UserSession userSession;
	@In(required=true) FacesMessages facesMessages;
	@In(create=true) MovementHome movementHome;
	@In(create=true) DispensingHome dispensingHome;
	@In(create=true) TbUnitHome tbunitHome;
	
	private QSPEditingMedicine editingMedicine;
	private Integer medicineId;
	
	private Quarter quarter;
	private Integer year;
	private Medicine medicine;
	private Date iniQuarterDate;
	private Date endQuarterDate;
	
	public void initialize(){
		quarterStockPositionReport.getTbunitselection().setTbunit(userSession.getTbunit());
		quarterStockPositionReport.setSource(null);
		quarterStockPositionReport.initialize();
	}
	
	public void initializeEditing(){
		checkMainParameters();
		QuarterlyReportDetailsBD details;
		try{
			details = (QuarterlyReportDetailsBD) getEntityManager().createQuery("from QuarterlyReportDetailsBD q " +
																					"where q.quarter = :quarter and q.tbunit.id = :unitId " +
																					"and q.year = :year and q.medicine.id = :medicineId")
																					.setParameter("quarter", quarter)
																					.setParameter("unitId", userSession.getTbunit().getId())
																					.setParameter("year", year)
																					.setParameter("medicineId", medicine.getId())
																					.getSingleResult();
			setInstance(details);
		}catch(NoResultException e){
			setInstance(null);
		}
		
		loadMedicineinformation();
	}
	
	public void loadMedicineinformation(){
		if(!checkMainParameters())
			return;
		
		//Creates editingMedicine using the Medicine that comes from the page
		editingMedicine = new QSPEditingMedicine(medicine);
		
		//Load batch quantity for each Source in List
		List<BatchQuantity> lst = getEntityManager().createQuery("from BatchQuantity bq where bq.batch.medicine.id = :medicineId and bq.tbunit.id = :unitId")
										.setParameter("unitId", userSession.getTbunit().getId())
										.setParameter("medicineId", editingMedicine.getMedicine().getId())
										.getResultList();

		for(BatchQuantity bq : lst){
			editingMedicine.getBatchList().add(editingMedicine.createQSPEditingBatch(bq));
		}
	}
	
	public boolean checkMainParameters(){
		quarter = quarterStockPositionReport.getQuarter();
		year = quarterStockPositionReport.getYear();
		medicine = (Medicine) getEntityManager().createQuery("from Medicine where id = :id").setParameter("id", medicineId).getSingleResult();
		iniQuarterDate = quarterStockPositionReport.getIniQuarterDate();
		endQuarterDate = quarterStockPositionReport.getEndQuarterDate();
		
		if(userSession.getTbunit() == null || quarter == null || year == null || medicine == null || iniQuarterDate == null || endQuarterDate == null)
			return false;
		
		return true;
	}
	
	public String save(){
		if(!savePositiveAdjustment()){
			//facesMessages.addMessage
			return "error";
		}
		
		if(!saveNegativeAdjustment()){
			//facesMessages.addMessage
			return "error";
		}
		
		if(!saveConsumption()){
			//facesMessages.addMessage
			return "error";
		}
		
		if(!saveExpired()){
			//facesMessages.addMessage
			return "error";
		}
		
		if(!saveOutOfStockDays()){
			//facesMessages.addMessage
			return "error";
		}		
		
		facesMessages.clear();
		facesMessages.addFromResourceBundle("default.entity_updated");
		return "success";
	}
	
	/**
	 * Closes the selected quarter
	 * @return
	 */
	public String closeQuarter(){
		quarter = quarterStockPositionReport.getQuarter();
		year = quarterStockPositionReport.getYear();
		
		if(quarter == null || year == null)
			return "error";
		
		Quarter nextQuarter = Quarter.getNextQuarter(quarter);
		if(nextQuarter.equals(Quarter.FIRST))
			year++;
		
		GregorianCalendar date = new GregorianCalendar();
		date.set(GregorianCalendar.YEAR, year);
		date.set(GregorianCalendar.MONTH, nextQuarter.getIniMonth());
		date.set(GregorianCalendar.DAY_OF_MONTH, nextQuarter.getIniDay());
		
		tbunitHome.setInstance(userSession.getTbunit());
		tbunitHome.getInstance().setLimitDateMedicineMovement(date.getTime());
		tbunitHome.persist();
		
		facesMessages.clear();
		facesMessages.addFromResourceBundle("default.entity_updated");
		return "success";
	}
	
	/**
	 * Save in the transaction log table the adjustments made in the stock position 
	 * @param roleAction
	 * @param batches
	 */
	private void saveLog(RoleAction roleAction, Map<Batch, Integer> batches) {
		TransactionLogService logSrv = new TransactionLogService();
		for (Batch batch: batches.keySet()) {
			Integer qtd = batches.get(batch);
			logSrv.addTableRow("Medicine", batch.getMedicine().toString());
			logSrv.addTableRow("Batch", batch.getBatchNumber());
			logSrv.addTableRow("Movement.quantity", qtd);
		}
		logSrv.save("STOCKPOS", roleAction, null, null, null, null);
	}
	
	/**
	 * Save positive adjustment movements
	 */
	public boolean savePositiveAdjustment(){
		if(!checkMainParameters())
			return false;
		
		//delete all positive adjustment in the quarter period
		List<Movement> movs = getEntityManager().createQuery("from Movement mov where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
												"and mov.type in (4) and (mov.quantity * mov.oper > 0) and mov.medicine.id = :medicineId ")
												.setParameter("unitId", userSession.getTbunit().getId())
												.setParameter("iniDate", iniQuarterDate)
												.setParameter("endDate", endQuarterDate)
												.setParameter("medicineId", medicine.getId())
												.getResultList();
		
		for(Movement m : movs){
			movementHome.prepareMovementsToRemove(m);
		}
		movementHome.savePreparedMovements();
		
		//create adjustment movement
		for(QSPEditingBatchQuantity b : editingMedicine.getBatchList()){
			  
			if(b.getPosAdjust() > 0){
				try{
					HashMap<Batch, Integer> batches = new HashMap<Batch, Integer>();
					
					batches.put(b.getBatchQtd().getBatch(), b.getPosAdjust());
					movementHome.initMovementRecording();
					movementHome.prepareNewAdjustment(endQuarterDate, userSession.getTbunit(), b.getBatchQtd().getSource(), 
															medicine, batches, null);
					movementHome.savePreparedMovements();
					saveLog(RoleAction.EDIT, batches);
				}catch(Exception e){
					e.printStackTrace();
					facesMessages.addFromResourceBundle(e.getMessage());
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Save negative adjustment movements
	 */
	public boolean saveNegativeAdjustment(){
		if(!checkMainParameters())
			return false;
		
		//delete all negative adjustment in the quarter period
		List<Movement> movs = getEntityManager().createQuery("from Movement mov where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
												"and mov.type in (4) and (mov.quantity * mov.oper < 0) and mov.medicine.id = :medicineId ")
												.setParameter("unitId", userSession.getTbunit().getId())
												.setParameter("iniDate", iniQuarterDate)
												.setParameter("endDate", endQuarterDate)
												.setParameter("medicineId", medicine.getId())
												.getResultList();
		
		for(Movement m : movs){
			movementHome.prepareMovementsToRemove(m);
		}
		movementHome.savePreparedMovements();
		
		//create adjustment movement
		for(QSPEditingBatchQuantity b : editingMedicine.getBatchList()){
			  
			if(b.getNegAdjust() > 0){
				try{
					HashMap<Batch, Integer> batches = new HashMap<Batch, Integer>();
					
					batches.put(b.getBatchQtd().getBatch(), (b.getNegAdjust()>0 ? b.getNegAdjust()*-1 : b.getNegAdjust()));
					movementHome.initMovementRecording();
					movementHome.prepareNewAdjustment(endQuarterDate, userSession.getTbunit(), b.getBatchQtd().getSource(), 
															medicine, batches, null);
					movementHome.savePreparedMovements();
					saveLog(RoleAction.EDIT, batches);
					
				}catch(Exception e){
					e.printStackTrace();
					facesMessages.addFromResourceBundle(e.getMessage());
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Save negative adjustment movements
	 */
	public boolean saveConsumption(){
		if(!checkMainParameters())
			return false;
		
		//delete all positive adjustment in the quarter period
		List<Movement> movs = getEntityManager().createQuery("from Movement mov where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
												"and mov.type in (3) and mov.medicine.id = :medicineId ")
												.setParameter("unitId", userSession.getTbunit().getId())
												.setParameter("iniDate", iniQuarterDate)
												.setParameter("endDate", endQuarterDate)
												.setParameter("medicineId", medicine.getId())
												.getResultList();
		
		for(Movement m : movs){
			movementHome.prepareMovementsToRemove(m);
		}
		movementHome.savePreparedMovements();
		
		if(editingMedicine.getConsumption() > 0){
			
		}
		
		Long totalConsumption = editingMedicine.getConsumption().longValue();
		
		if(totalConsumption > 0){
			//Check if the available quantity to each batch in stock to apply FEFO rule.
			List<Object[]> lst = getEntityManager().createQuery("select m.source, b, sum(bm.quantity * m.oper) from BatchMovement bm join bm.movement m join bm.batch b " +
																		"where m.tbunit.id = :unitId and m.medicine.id = :medicineId " +
																		"and m.date <= :endQuarterDate " +
																		"group by m.source.id, b.id " +
																		"order by b.expiryDate")
																		.setParameter("unitId", userSession.getTbunit().getId())
																		.setParameter("medicineId", medicine.getId())
																		.setParameter("endQuarterDate", endQuarterDate)
																		.getResultList();
			
			//Clear zero quantities, can't do it in the query because of mysql error 'invalid use of group function'.
			List<Object[]> batchStqPos = new ArrayList<Object[]>();
			for(Object[] o : lst){
				if((Long) o[2] > 0){
					batchStqPos.add(o);
				}
			}
			
			dispensingHome.initialize(userSession.getTbunit());
			
			//FEFO Rule, considering the order by in the query
			for(Object[] o : batchStqPos){
				if(totalConsumption > 0){
					Long batchQuantity = (Long) o[2];
					
					if(totalConsumption-batchQuantity < 0)
						batchQuantity = totalConsumption;
					
					totalConsumption = totalConsumption - batchQuantity;
					
					dispensingHome.addDispensing((Batch)o[1], (Source)o[0], (batchQuantity != null ? batchQuantity.intValue() : null));
				}
			}
			
			dispensingHome.getInstance().setDispensingDate(endQuarterDate);
			dispensingHome.saveDispensing();
		}
		return true;
	}
	
	/**
	 * Save expired movements
	 */
	public boolean saveExpired(){
		if(!checkMainParameters())
			return false;
		
		//delete all expired adjustment in the quarter period
		List<Movement> movs = getEntityManager().createQuery("from Movement mov where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
												"and mov.type in (4) and mov.adjustmentType.id = :workspaceExpiredAdjust and mov.medicine.id = :medicineId ")
												.setParameter("unitId", userSession.getTbunit().getId())
												.setParameter("iniDate", iniQuarterDate)
												.setParameter("endDate", endQuarterDate)
												.setParameter("medicineId", medicine.getId())
												.setParameter("workspaceExpiredAdjust", UserSession.getWorkspace().getExpiredMedicineAdjustmentType().getId())
												.getResultList();
		
		for(Movement m : movs){
			movementHome.prepareMovementsToRemove(m);
		}
		movementHome.savePreparedMovements();
		
		//create adjustment movement
		for(QSPEditingBatchQuantity b : editingMedicine.getBatchList()){
			  
			if(b.getExpired() > 0){
				try{
					HashMap<Batch, Integer> batches = new HashMap<Batch, Integer>();
					FieldValueComponent expiredFieldValue = new FieldValueComponent();
					expiredFieldValue.setValue(UserSession.getWorkspace().getExpiredMedicineAdjustmentType());
					
					batches.put(b.getBatchQtd().getBatch(), (b.getExpired()>0 ? b.getExpired()*-1 : b.getExpired()));
					movementHome.initMovementRecording();
					movementHome.prepareNewAdjustment(endQuarterDate, userSession.getTbunit(), b.getBatchQtd().getSource(), 
															medicine, batches, expiredFieldValue);
					movementHome.savePreparedMovements();
					saveLog(RoleAction.EDIT, batches);
					
				}catch(Exception e){
					e.printStackTrace();
					facesMessages.addFromResourceBundle(e.getMessage());
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean saveOutOfStockDays(){
		getInstance().setMedicine(medicine);
		getInstance().setQuarter(quarter);
		getInstance().setTbunit(userSession.getTbunit());
		getInstance().setYear(year);
		getInstance().setOutOfStock(editingMedicine.getOutOfStock());
		if(persist().equalsIgnoreCase("persisted"))
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if the quarter and year selected is editable.
	 */
	public boolean isEditableQuarter(){
		quarter = quarterStockPositionReport.getQuarter();
		year = quarterStockPositionReport.getYear();
		
		GregorianCalendar date = new GregorianCalendar();
		date.setTime(userSession.getTbunit().getLimitDateMedicineMovement());
		
		Quarter editableQuarter = Quarter.getQuarterByMonth(date.get(GregorianCalendar.MONTH));
		Integer editableYear = date.get(GregorianCalendar.YEAR);
				
		return (quarter.equals(editableQuarter) && year.equals(editableYear));
	}
	
	/**
	 * @return the editingMedicine
	 */
	public QSPEditingMedicine getEditingMedicine() {
		return editingMedicine;
	}

	/**
	 * @param editingMedicine the editingMedicine to set
	 */
	public void setEditingMedicine(QSPEditingMedicine editingMedicine) {
		this.editingMedicine = editingMedicine;
	}

	
	/**
	 * @return the medicineId
	 */
	public Integer getMedicineId() {
		return medicineId;
	}

	/**
	 * @param medicineId the medicineId to set
	 */
	public void setMedicineId(Integer medicineId) {
		this.medicineId = medicineId;
	}	
}
