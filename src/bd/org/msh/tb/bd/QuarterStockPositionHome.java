package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.bd.QSPEditingMedicine.QSPEditingBatchDetails;
import org.msh.tb.bd.entities.QuarterlyReportDetailsBD;
import org.msh.tb.bd.entities.enums.QuarterMonths;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.dispensing.DispensingHome;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.tbunits.TbUnitHome;
import org.msh.utils.date.DateUtils;

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
	@In(create=true) QuarterBatchExpiringReport quarterBatchExpiringReport;
	@In(required=true) UserSession userSession;
	@In(required=true) FacesMessages facesMessages;
	@In(create=true) MovementHome movementHome;
	@In(create=true) DispensingHome dispensingHome;
	@In(create=true) TbUnitHome tbunitHome;
	Map<String, String> messages = Messages.instance();
	private boolean initialized;
	private HashMap<Batch, String> batchErrorMessages;
	private ArrayList<String> errorMessages;
	
	private QSPEditingMedicine editingMedicine;
	private Integer medicineId;
	
	private Quarter selectedQuarter;
	private Medicine medicine;
	
	public void initialize(){
		updateQuarterAsTbunit();
		setParametersOnReports();		
		refresh();
		this.initialized = true;
	}
	
	public void setParametersOnReports(){
		//Set the parameters of the selected unit to generate the report
		quarterStockPositionReport.getTbunitselection().setTbunit(userSession.getTbunit());
		quarterStockPositionReport.setSource(null);
		quarterStockPositionReport.setSelectedQuarter(selectedQuarter);
		
		//Set the parameters of the selected unit to generate the report
		quarterBatchExpiringReport.getTbunitselection().setTbunit(userSession.getTbunit());
		quarterBatchExpiringReport.setSource(null);
		quarterBatchExpiringReport.setSelectedQuarter(selectedQuarter);
	}
	
	public void refresh(){
		setParametersOnReports();		
		quarterStockPositionReport.refresh();
		quarterBatchExpiringReport.refresh();
	}
	
	/**
	 * Updates the selected quarter according to the last opened quarter of the
	 * selected tbunit
	 */
	public void updateQuarterAsTbunit(){
		if(userSession.getTbunit().getLimitDateMedicineMovement() != null){
			Date dt = userSession.getTbunit().getLimitDateMedicineMovement();
			
			selectedQuarter = Quarter.getQuarterByMonth(DateUtils.monthOf(dt), DateUtils.yearOf(dt));
		}else{
			//for units that had the control of medicines started before the quarterly report implementation;
			Date dt = (Date) getEntityManager().createQuery("select max(date) from Movement m where m.tbunit.id = :unitId")
								.setParameter("unitId", userSession.getTbunit().getId())
								.getSingleResult();
				
			if(dt != null){
				selectedQuarter = Quarter.getQuarterByDate(dt);
			}else{
				selectedQuarter = new Quarter(QuarterMonths.FIRST, 2013);
			}
			
			if(selectedQuarter == null || selectedQuarter.getYear() == 0)
				return;
				
			tbunitHome.setInstance(userSession.getTbunit());
			tbunitHome.getInstance().setLimitDateMedicineMovement(selectedQuarter.getIniDate());
			tbunitHome.persist();
			
			tbunitHome.clearInstance();				
			facesMessages.clear();
		}
	}
	
	/**
	 * Loads the QuarterlyReportDetailsBD according to the selected parameters (quarter, year, unit and medicine).
	 */
	public void initializeEditing(){
		if(!checkMainParameters())
			return;
		
		this.clearInstance();
		
		QuarterlyReportDetailsBD details = null;
		try{
			details = (QuarterlyReportDetailsBD) getEntityManager().createQuery("from QuarterlyReportDetailsBD q " +
																					"where q.quarterMonth = :quarterMonth and q.tbunit.id = :unitId " +
																					"and q.year = :year and q.medicine.id = :medicineId")
																					.setParameter("quarterMonth", selectedQuarter.getQuarter())
																					.setParameter("unitId", userSession.getTbunit().getId())
																					.setParameter("year", selectedQuarter.getYear())
																					.setParameter("medicineId", medicine.getId())
																					.getSingleResult();
			setInstance(details);
		}catch(NoResultException e){
			this.clearInstance();
		}
		
		loadMedicineinformation();
	}
	
	/**
	 * Load the batches and the quantities that will be available for editing for the user.
	 */
	public void loadMedicineinformation(){
		if(!checkMainParameters())
			return;
		
		//Creates editingMedicine using the Medicine that comes from the page
		editingMedicine = new QSPEditingMedicine(medicine);
		
		//Loads the quantity for each type of movement - batch
		String queryString = "select b, s, " +
		
								"(select sum (bm2.quantity * m2.oper) " +
									"from BatchMovement bm2 join bm2.movement m2 left join m2.adjustmentType " +
									"where bm2.batch.id = b.id and m2.source.id = s.id and m2.tbunit.id = :unitId and m2.date >= :iniDate and m2.date <= :endDate " +
									"and m2.type = 4 and (bm2.quantity * m2.oper) > 0 and (m2.adjustmentType.id <> :workspaceExpiredAdjust or m2.adjustmentType is null))," +
								
								"(select sum (bm2.quantity * m2.oper) " +
									"from BatchMovement bm2 join bm2.movement m2 left join m2.adjustmentType " +
									"where bm2.batch.id = b.id and m2.source.id = s.id and m2.tbunit.id = :unitId and m2.date >= :iniDate and m2.date <= :endDate " +
									"and m2.type = 4 and (bm2.quantity * m2.oper) < 0 and (m2.adjustmentType.id <> :workspaceExpiredAdjust or m2.adjustmentType is null))," +
								
								"(select sum (bm2.quantity * m2.oper) " +
									"from BatchMovement bm2 join bm2.movement m2 left join m2.adjustmentType " +
									"where bm2.batch.id = b.id and m2.source.id = s.id and m2.tbunit.id = :unitId and m2.date >= :iniDate and m2.date <= :endDate " +
									"and m2.type = 4 and (bm2.quantity * m2.oper) < 0 and (m2.adjustmentType.id = :workspaceExpiredAdjust))" +	
								
								"from BatchMovement bm join bm.batch b join bm.movement m join m.source s " +
								"where m.tbunit.id = :unitId and m.medicine.id = :medicineId and ((m.date < :iniDate) or (m.date = :iniDate and m.type = 7)) " +
								"group by b.id, s.id " +
								"having sum(bm.quantity * m.oper) > 0";
		
		List<Object[]> result = getEntityManager().createQuery(queryString)
									.setParameter("medicineId", medicine.getId())
									.setParameter("unitId", userSession.getTbunit().getId())
									.setParameter("iniDate", selectedQuarter.getIniDate())
									.setParameter("endDate", selectedQuarter.getEndDate())
									.setParameter("workspaceExpiredAdjust", UserSession.getWorkspace().getExpiredMedicineAdjustmentType().getId())
									.getResultList();
		
		for(Object[] o : result){
			QSPEditingBatchDetails item = editingMedicine.createQSPEditingBatch((Batch)o[0], (Source)o[1]);
			item.setPosAdjust( (o[2] == null ? null : ((Long)o[2]).intValue()) );
			item.setNegAdjust( (o[3] == null ? null : (((Long)o[3]).intValue())*-1) );
			item.setExpired( (o[4] == null ? null : (((Long)o[4]).intValue())*-1) );
			
			editingMedicine.getBatchList().add(item);
		}
		
		//Loads the out of stock in days for the selected medicine
		if(getInstance() != null)
			editingMedicine.setOutOfStock(getInstance().getOutOfStock());
		else
			editingMedicine.setOutOfStock(0);
		
		//Loads the total consumption for the medicine in question
		try{
			Long qtd = (Long) getEntityManager().createQuery("select sum (mov.quantity * mov.oper) from Movement mov where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
																	"and mov.type in (3) and mov.medicine.id = :medicineId ")
																	.setParameter("unitId", userSession.getTbunit().getId())
																	.setParameter("iniDate", selectedQuarter.getIniDate())
																	.setParameter("endDate", selectedQuarter.getEndDate())
																	.setParameter("medicineId", medicine.getId())
																	.getSingleResult();
			
			editingMedicine.setConsumption(qtd == null ? 0 : (qtd.intValue()*-1));
		}catch(NoResultException e){
			editingMedicine.setConsumption(0);
		}
		
		//Loads the movements consolidated in negative adjustments column that are not negative adjustments (movement type equals 1 or 6)
		try{
			Long qtd = (Long) getEntityManager().createQuery("select sum (mov.quantity * mov.oper) from Movement mov " +
																	"where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
																	"and mov.type in (1,6) and mov.medicine.id = :medicineId ")
																	.setParameter("unitId", userSession.getTbunit().getId())
																	.setParameter("iniDate", selectedQuarter.getIniDate())
																	.setParameter("endDate", selectedQuarter.getEndDate())
																	.setParameter("medicineId", medicine.getId())
																	.getSingleResult();
			
			editingMedicine.setNonNegativeAdjustments(qtd == null ? 0 : (qtd.intValue()*-1));
		}catch(NoResultException e){
			editingMedicine.setNonNegativeAdjustments(0);
		}
		
		//Loads the opening balance of the selected medicine.
		try{
			Long qtd = (Long) getEntityManager().createQuery("select sum (mov.quantity * mov.oper) from Movement mov " +
																	"where mov.tbunit.id = :unitId and mov.medicine.id = :medicineId " +
																	"and ( (mov.date < :iniDate) or (mov.date >= :iniDate and mov.date <= :endDate and mov.type in (7)) ) ")
																	.setParameter("unitId", userSession.getTbunit().getId())
																	.setParameter("iniDate", selectedQuarter.getIniDate())
																	.setParameter("endDate", selectedQuarter.getEndDate())
																	.setParameter("medicineId", medicine.getId())
																	.getSingleResult();
			
			editingMedicine.setOpeningBalance(qtd == null ? 0 : (qtd.intValue()));
		}catch(NoResultException e){
			editingMedicine.setOpeningBalance(0);
		}
		
		//Loads the received of the selected medicine.
		try{
			Long qtd = (Long) getEntityManager().createQuery("select sum (mov.quantity * mov.oper) from Movement mov " +
																"where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
																"and mov.type in (2,5) and mov.medicine.id = :medicineId ")
																.setParameter("unitId", userSession.getTbunit().getId())
																	.setParameter("iniDate", selectedQuarter.getIniDate())
																	.setParameter("endDate", selectedQuarter.getEndDate())
																.setParameter("medicineId", medicine.getId())
																.getSingleResult();
			
			editingMedicine.setReceived(qtd == null ? 0 : (qtd.intValue()));
		}catch(NoResultException e){
			editingMedicine.setReceived(0);
		}
	}
	
	public boolean checkMainParameters(){
		medicine = (Medicine) getEntityManager().createQuery("from Medicine where id = :id").setParameter("id", medicineId).getSingleResult();
		
		if(userSession.getTbunit() == null || selectedQuarter == null || selectedQuarter.getYear() == 0 || medicine == null)
			return false;
		
		return true;
	}
	
	/**
	 * Closes the selected quarter
	 * @return
	 */
	public String closeQuarter(){		
		if(selectedQuarter == null || selectedQuarter.getYear() == 0)
			return "error";
		
		if(selectedQuarter.getEndDate().compareTo(DateUtils.getDate()) >= 0){
			facesMessages.addFromResourceBundle("validator.assertFalse");
			facesMessages.addToControlFromResourceBundle("page:main:quarterdiv:quarter","quarter.closequarter.msg2", DateUtils.formatAsLocale(selectedQuarter.getEndDate(), false));
			return "validation-error";
		}
		
		Quarter nextQuarter = selectedQuarter.getNextQuarter();
				
		tbunitHome.setInstance(userSession.getTbunit());
		tbunitHome.getInstance().setLimitDateMedicineMovement(nextQuarter.getIniDate());
		String s = tbunitHome.persist();
		
		if(!s.equalsIgnoreCase("persisted"))
			return "error";
			
		quarterStockPositionReport.refresh();
		
		tbunitHome.clearInstance();
		facesMessages.clear();
		facesMessages.addFromResourceBundle("default.entity_updated");
		return "success-close";
	}
	
	public String save(){
		movementHome.initMovementRecording();
		
		if(!deletePreviousMovements()){
			addValidErrorToClient(null);
			return "error";
		}
		
		if(!savePositiveAdjustment()){
			addValidErrorToClient(messages.get("quarter.posadjust"));
			return "error";
		}
		
		if(!saveNegativeAdjustment()){
			addValidErrorToClient(messages.get("quarter.negadjust"));
			return "error";
		}
		
		if(!saveExpired()){
			addValidErrorToClient(messages.get("quarter.expired"));
			return "error";
		}

		movementHome.savePreparedMovements();
		
		if(!saveConsumption()){
			addValidErrorToClient(messages.get("manag.forecast.tabres2"));
			return "error";
		}
		
		if(!saveOutOfStockDays()){
			addValidErrorToClient(null);
			return "error";
		}		
		
		quarterStockPositionReport.refresh();
		
		facesMessages.clear();
		facesMessages.addFromResourceBundle("default.entity_updated");
		return "success";
	}

	/**
	 * Adds a message to be after added to facesMessages
	 */
	private void addValidError(Batch errorBatch, String errorMessage){
		if(batchErrorMessages == null)
			batchErrorMessages = new HashMap<Batch, String>();
		
		if(errorMessages == null)
			errorMessages = new ArrayList<String>();
		
		if(movementHome.getErrorBatch() != null)
			batchErrorMessages.put(errorBatch, errorMessage);
		else
			errorMessages.add(errorMessage);
	}
	
	/**
	 * Adds to facesMessages the existing errorMessages 
	 */
	private void addValidErrorToClient(String typeOfmovement){
		facesMessages.clear();
		facesMessages.addFromResourceBundle("validator.assertFalse");
		
		if(errorMessages != null){
			for(String message : errorMessages){
				String s = typeOfmovement + " - " + message;
				facesMessages.add(s);
			}
		}
		
		if(batchErrorMessages != null){
			for(Batch b : batchErrorMessages.keySet()){
				String s = typeOfmovement + " - " + b.getBatchNumber() + ": " + batchErrorMessages.get(b);
				facesMessages.add(s);
			}
		}
		
		errorMessages = null;
		batchErrorMessages = null;
	}
	
	/**
	 * Prepare all movements that will be deleted before creating the new ones.
	 */
	public boolean deletePreviousMovements(){
		if(!checkMainParameters())
			return false;
		
		//delete all positive, negative, expired adjustments and consumptions in the quarter period
		List<Movement> movs = getEntityManager().createQuery("from Movement mov where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
												"and mov.type in (4,3) and mov.medicine.id = :medicineId ")
												.setParameter("unitId", userSession.getTbunit().getId())
												.setParameter("iniDate", selectedQuarter.getIniDate())
												.setParameter("endDate", selectedQuarter.getEndDate())
												.setParameter("medicineId", medicine.getId())
												.getResultList();
		
		for(Movement m : movs){
			movementHome.prepareMovementsToRemove(m);
		}
		
		return true;
	}
	
	/**
	 * Save positive adjustment movements
	 */
	public boolean savePositiveAdjustment(){
		boolean allOk = true;
		
		if(!checkMainParameters())
			return false;
		
		for(QSPEditingBatchDetails b : editingMedicine.getBatchList()){
			  
			if(b.getPosAdjust() > 0){
				try{
					HashMap<Batch, Integer> batches = new HashMap<Batch, Integer>();
					
					batches.put(b.getBatch(), b.getPosAdjust());
					Movement m = movementHome.prepareNewAdjustment(selectedQuarter.getEndDate(), userSession.getTbunit(), b.getSource(), 
															medicine, batches, null);
					if(m == null){
						allOk = false;
						addValidError(movementHome.getErrorBatch(), movementHome.getErrorMessage());
					}
					
				}catch(Exception e){
					e.printStackTrace();
					facesMessages.addFromResourceBundle(e.getMessage());
					return false;
				}
			}
		}
		return allOk;
	}
	
	/**
	 * Save negative adjustment movements
	 */
	public boolean saveNegativeAdjustment(){
		boolean allOk = true;
		
		if(!checkMainParameters())
			return false;
		
		//create adjustment movement
		for(QSPEditingBatchDetails b : editingMedicine.getBatchList()){
			  
			if(b.getNegAdjust() > 0){
				try{
					HashMap<Batch, Integer> batches = new HashMap<Batch, Integer>();
					
					batches.put(b.getBatch(), (b.getNegAdjust()>0 ? b.getNegAdjust()*-1 : b.getNegAdjust()));
					
					Movement m = movementHome.prepareNewAdjustment(selectedQuarter.getEndDate(), userSession.getTbunit(), b.getSource(), 
																		medicine, batches, null);
					if(m == null){
						allOk = false;
						addValidError(movementHome.getErrorBatch(), movementHome.getErrorMessage());
					}
				}catch(Exception e){
					e.printStackTrace();
					facesMessages.addFromResourceBundle(e.getMessage());
					return false;
				}
			}
		}
		return allOk;
	}
	
	/**
	 * Save negative adjustment movements
	 */
	public boolean saveConsumption(){
		if(!checkMainParameters())
			return false;
		
		Long totalConsumption = editingMedicine.getConsumption().longValue();
		
		if(totalConsumption > 0){
			//validates if the available quantity is enough
			Long availableQtd = (Long) getEntityManager().createQuery("select sum(m.quantity * m.oper) from Movement m " +
																	"where m.tbunit.id = :unitId and m.medicine.id = :medicineId " +
																	"and m.date <= :endQuarterDate ")
																	.setParameter("unitId", userSession.getTbunit().getId())
																	.setParameter("medicineId", medicine.getId())
																	.setParameter("endQuarterDate", selectedQuarter.getEndDate())
																	.getSingleResult();
			
			if(availableQtd < totalConsumption){
				addValidError(null, messages.get("meds.movs.invalidqtd"));
				return false;
			}
			
			//Check available quantity to each batch in stock to apply FEFO rule.
			List<Object[]> lst = getEntityManager().createQuery("select m.source, b, sum(bm.quantity * m.oper) from BatchMovement bm join bm.movement m join bm.batch b " +
																		"where m.tbunit.id = :unitId and m.medicine.id = :medicineId " +
																		"and m.date <= :endQuarterDate " +
																		"group by m.source.id, b.id " +
																		"order by b.expiryDate")
																		.setParameter("unitId", userSession.getTbunit().getId())
																		.setParameter("medicineId", medicine.getId())
																		.setParameter("endQuarterDate", selectedQuarter.getEndDate())
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
			
			dispensingHome.getInstance().setDispensingDate(selectedQuarter.getEndDate());
			dispensingHome.saveDispensing();
		}
		dispensingHome.clearInstance();
		return true;
	}
	
	/**
	 * Save expired movements
	 */
	public boolean saveExpired(){
		boolean allOk = true;
		
		if(!checkMainParameters())
			return false;
		
		for(QSPEditingBatchDetails b : editingMedicine.getBatchList()){
			  
			if(b.getExpired() > 0){
				try{
					HashMap<Batch, Integer> batches = new HashMap<Batch, Integer>();
					FieldValueComponent expiredFieldValue = new FieldValueComponent();
					expiredFieldValue.setValue(UserSession.getWorkspace().getExpiredMedicineAdjustmentType());
					
					batches.put(b.getBatch(), (b.getExpired()>0 ? b.getExpired()*-1 : b.getExpired()));
				
					Movement m = movementHome.prepareNewAdjustment(selectedQuarter.getEndDate(), userSession.getTbunit(), b.getSource(), 
							medicine, batches, expiredFieldValue);
					
					if(m == null){
						allOk = false;
						addValidError(movementHome.getErrorBatch(), movementHome.getErrorMessage());
					}
					
				}catch(Exception e){
					e.printStackTrace();
					facesMessages.addFromResourceBundle(e.getMessage());
					return false;
				}
			}
		}
		return allOk;
	}
	
	public boolean saveOutOfStockDays(){
		getInstance().setMedicine(medicine);
		getInstance().setQuarterMonth(selectedQuarter.getQuarter());
		getInstance().setTbunit(userSession.getTbunit());
		getInstance().setYear(selectedQuarter.getYear());
		getInstance().setOutOfStock(editingMedicine.getOutOfStock());
		if(persist().equalsIgnoreCase("persisted"))
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if the quarter and year selected is editable.
	 */
	public boolean canEdit(){
		if(Identity.instance().hasRole("MED_MOV_EDIT_OUT_PERIOD")  && userSession.getTbunit().getLimitDateMedicineMovement().compareTo(selectedQuarter.getIniDate()) >= 0)
			return true;
			
		if(!Identity.instance().hasRole("QUARTERLY_EDIT"))
			return false;
			
		if(userSession.getTbunit().getLimitDateMedicineMovement() == null)
			return true;
		
		return isOpenedQuarter();
	}
	
	/**
	 * Returns true if can close the selected quarter and year.
	 */
	public boolean canCloseQuarter(){
		if(!Identity.instance().hasRole("QUARTERLY_EDIT"))
			return false;
			
		if(userSession.getTbunit().getLimitDateMedicineMovement() == null)
			return true;
		
		return isOpenedQuarter();
	}
	
	/**
	 * Returns true if the selected quarter is the opened one for the selected unit.
	 */
	public boolean isOpenedQuarter(){		
		Date dt = userSession.getTbunit().getLimitDateMedicineMovement();
		Quarter editableQuarter = Quarter.getQuarterByMonth(DateUtils.monthOf(dt), DateUtils.yearOf(dt));
				
		return selectedQuarter.isTheSame(editableQuarter);
	}
	
	public Quarter getOpenedQuarter(){
		return Quarter.getQuarterByDate(userSession.getTbunit().getLimitDateMedicineMovement());
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
	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}
	/**
	 * @param initialized the initialized to set
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
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
}
