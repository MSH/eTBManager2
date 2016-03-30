package org.msh.tb.bd;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.bd.entities.QuarterlyReportDetailsBD;
import org.msh.tb.bd.entities.enums.QuarterMonths;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.dispensing.DispensingHome;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.tbunits.TbUnitHome;
import org.msh.utils.date.DateUtils;

import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import java.util.*;

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
		if(this.initialized == true)
			return;
			
		updateQuarterAsTbunit();
		setParametersOnReports();		
		refresh();
		this.initialized = true;
	}
	
	public void setParametersOnReports(){
		//Set the parameters of the selected unit to generate the report
        quarterStockPositionReport.getTbunitselection().setTbunit(userSession.getTbunit());
        quarterStockPositionReport.getTbunitselection().getAuselection().setSelectedUnit(userSession.getTbunit().getAdminUnit());
		quarterStockPositionReport.setSource(null);
		quarterStockPositionReport.setSelectedQuarter(selectedQuarter);

		//Set the parameters of the selected unit to generate the report
		quarterBatchExpiringReport.getTbunitselection().setTbunit(userSession.getTbunit());
        quarterBatchExpiringReport.getTbunitselection().getAuselection().setSelectedUnit(userSession.getTbunit().getAdminUnit());
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
		String selQuarter = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selQuarter");
		String selYear = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selYear");
		
		if(selQuarter != null && !selQuarter.isEmpty() && selYear != null && !selYear.isEmpty()){
			selectedQuarter = new Quarter(QuarterMonths.valueOf(selQuarter), Integer.parseInt(selYear));
		}else if(userSession.getTbunit().getLimitDateMedicineMovement() != null){
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
		
		String queryString = "select m, " +
				
								"(select sum(mov.quantity * mov.oper) from Movement mov where mov.tbunit.id = :unitId " +
									" and ( (mov.date < :iniDate) or (mov.date >= :iniDate and mov.date <= :endDate and mov.type in (7)) ) " +
									"and mov.medicine.id = m.id) as openingBalance, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov where mov.tbunit.id = :unitId " + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (2,5) " +
									"and mov.medicine.id = m.id) as received, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov where mov.tbunit.id = :unitId " +  
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and (mov.quantity * mov.oper > 0) " +
									" and mov.medicine.id = m.id) as posAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov left join mov.adjustmentType where mov.tbunit.id = :unitId " +  
									" and mov.date >= :iniDate and mov.date <= :endDate and (mov.quantity * mov.oper) < 0" +
									" and mov.type in (4)" +
									" and (mov.adjustmentType.id <> :workspaceExpiredAdjust or mov.adjustmentType is null)" +
									" and mov.medicine.id = m.id) as negAdjust, " +
									
								"(select sum(mov.quantity * mov.oper) from Movement mov where mov.tbunit.id = :unitId " + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (3) and mov.medicine.id = m.id) as dispensed, " +
								
								"(select sum(mov.quantity * mov.oper) from Movement mov where mov.tbunit.id = :unitId " + 
									" and mov.date >= :iniDate and mov.date <= :endDate and mov.type in (4) and mov.adjustmentType.id = :workspaceExpiredAdjust " +
									" and mov.medicine.id = m.id and (mov.quantity * mov.oper) < 0) as expired " +

							  "from Medicine m " +
							  "where m.workspace.id = :workspaceId and m.id = :medicineId " +
							  "group by m";
		
		List<Object[]> result = getEntityManager().createQuery(queryString)
									.setParameter("iniDate", selectedQuarter.getIniDate())
									.setParameter("endDate", selectedQuarter.getEndDate())
									.setParameter("workspaceId", UserSession.getWorkspace().getId())
									.setParameter("workspaceExpiredAdjust", UserSession.getWorkspace().getExpiredMedicineAdjustmentType().getId())
									.setParameter("unitId", userSession.getTbunit().getId())
									.setParameter("medicineId", medicine.getId())
									.getResultList();
		
		if(result.size() > 0){
			editingMedicine.setOpeningBalance((result.get(0)[1] == null ? 0 : ((Long)result.get(0)[1]).intValue()));
			editingMedicine.setReceived((result.get(0)[2] == null ? 0 : ((Long)result.get(0)[2]).intValue()));
			editingMedicine.setPositiveAdjustment((result.get(0)[3] == null ? 0 : ((Long)result.get(0)[3]).intValue()));
			editingMedicine.setNegativeAdjustment((result.get(0)[4] == null ? 0 : ((Long)result.get(0)[4]).intValue()*-1));
			editingMedicine.setConsumption((result.get(0)[5] == null ? 0 : ((Long)result.get(0)[5]).intValue()*-1));
			editingMedicine.setExpired((result.get(0)[6] == null ? 0 : ((Long)result.get(0)[6]).intValue()*-1));
		}
		
		//Loads the out of stock in days for the selected medicine
		if(getInstance() != null)
			editingMedicine.setOutOfStock(getInstance().getOutOfStock());
		else
			editingMedicine.setOutOfStock(0);
		
		//Loads the transfered out quantity for the selected medicine
		try{
			Long qtd = (Long) getEntityManager().createQuery("select sum (mov.quantity * mov.oper) from Movement mov " +
																	"where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
																	"and mov.type in (6) and mov.medicine.id = :medicineId ")
																	.setParameter("unitId", userSession.getTbunit().getId())
																	.setParameter("iniDate", selectedQuarter.getIniDate())
																	.setParameter("endDate", selectedQuarter.getEndDate())
																	.setParameter("medicineId", medicine.getId())
																	.getSingleResult();
			
			editingMedicine.setTransferedOutQtd(qtd == null ? 0 : (qtd.intValue()*-1));
		}catch(NoResultException e){
			editingMedicine.setTransferedOutQtd(0);
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
		
		//Can edit dispensing only if the unit is not configured for patient dispensing
		if(!deletePreviousMovements(!userSession.getTbunit().isPatientDispensing())){
			addValidErrorToClient(null);
			return "error";
		}

		movementHome.savePreparedMovements();
		//Can edit dispensing only if the unit is not configured for patient dispensing
		if(!userSession.getTbunit().isPatientDispensing()){
			if(!saveConsumption()){
				addValidErrorToClient(messages.get("manag.forecast.tabres2"));
				return "error";
			}
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
	public boolean deletePreviousMovements(boolean deleteDispensings){
		if(!checkMainParameters())
			return false;
		
		String query = "";
		
		if(deleteDispensings){
			query = "from Movement mov where mov.tbunit.id = :unitId and mov.date >= :iniDate and mov.date <= :endDate " +
					"and mov.type in (3) and mov.medicine.id = :medicineId ";
		}
		
		//delete all consumptions in the quarter period
		List<Movement> movs = getEntityManager().createQuery(query)
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
	 * Save negative dispensing movements
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
			
			dispensingHome.getInstance().setDispensingDate(getDispensingDate());
			dispensingHome.saveDispensing();
		}
		dispensingHome.clearInstance();
		return true;
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
		
		return isEditableQuarter();
	}
	
	/**
	 * Returns true if can close the selected quarter and year.
	 */
	public boolean canCloseQuarter(){
		if(!Identity.instance().hasRole("QUARTERLY_EDIT"))
			return false;
			
		if(userSession.getTbunit().getLimitDateMedicineMovement() == null)
			return true;
		
		return isEditableQuarter();
	}
	
	/**
	 * Returns true if the selected quarter is the editable one for the selected unit.
	 */
	public boolean isEditableQuarter(){		
		Date dt = userSession.getTbunit().getLimitDateMedicineMovement();
		Quarter editableQuarter = Quarter.getQuarterByMonth(DateUtils.monthOf(dt), DateUtils.yearOf(dt));
				
		return selectedQuarter.isTheSame(editableQuarter);
	}
	
	/**
	 * Returns true if the selected quarter is opened.
	 */
	public boolean isOpenedQuarter(){		
		Date limitDate = userSession.getTbunit().getLimitDateMedicineMovement();
		return limitDate.compareTo(selectedQuarter.getEndDate()) <= 0;
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

	public Date getDispensingDate() {
		Date dt = DateUtils.getDate();

		if(dt.after(selectedQuarter.getEndDate())){
			dt = selectedQuarter.getEndDate();
		}

		return dt;
	}
}
