package org.msh.tb.medicines.movs;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.tb.application.App;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.medicines.InventoryReport;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.utils.date.DateUtils;

import java.util.*;


/**
 * Handle funcionalities for adjustment in the quantity of medicine available for a specific source
 * @author Ricardo Memoria
 *
 */
@Name("stockAdjustmentHome")
@Scope(ScopeType.CONVERSATION)
public class StockAdjustmentHome extends Controller {
	private static final long serialVersionUID = 5670008276682104291L;

	private Source source;
	private List<StockPositionItem> items;
	private AdjustmentItem item;
	private StockPosition stockPosition;
	private boolean actionExecuted;
	private boolean existingBatchInThisUnit;
	private BatchQuantity batchQuantity;
	private Date movementDate;
	private FieldValueComponent adjustmentInfo = new FieldValueComponent();
	private BatchSelection batchSelection;
	
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	@In(create=true) InventoryReport inventoryReport;
	@In(create=true) FacesMessages facesMessages;

	/**
	 * Initialize the items for adjustment (used when source or tbunit chage)
	 */
	public void initialize() {
		items = null;
	}

	public boolean isExistingBatchInThisUnit() {
		return existingBatchInThisUnit;
	}

	public void setExistingBatchInThisUnit(boolean existingBatchInThisUnit) {
		this.existingBatchInThisUnit = existingBatchInThisUnit;
	}
	
	public void sourceChanged(){
		items = null;
	}

	/**
	 * Cria os itens para ajuste
	 */
	private void createItems() {
		items = new ArrayList<StockPositionItem>();
		Tbunit tbunit = userSession.getTbunit();
		
		if ((source == null) || (tbunit == null)) {
			return;
		}
		
		// get information about stock position
		StockPositionList srv = (StockPositionList)Component.getInstance("stockPositionList", true);

		List<StockPosition> lst = srv.generate(tbunit, source); 
		
		for (StockPosition sp: lst) {
			StockPositionItem it = new StockPositionItem();
			it.setStockPosition(sp);
			items.add(it);
		}
		
		// get information about batches available
		List<BatchQuantity> batches = srv.getBatchAvailable(tbunit, source); 

		for (BatchQuantity b: batches) {
			StockPositionItem item = findStockPosition(b.getBatch().getMedicine());
			if (item != null)
				item.getBatches().add(b);
			else {
				// in this situation the batch has expired
				// so it'll create a monk StockPosition record
				StockPosition sp = new StockPosition();
				sp.setSource(b.getSource());
				sp.setTbunit(b.getTbunit());
				sp.setMedicine(b.getBatch().getMedicine());
				item = new StockPositionItem();
				item.setStockPosition(sp);
				items.add(item);
				item.getBatches().add(b);
			}
		}
	}
	
	
	/**
	 * Search for instance of {@link StockPosition} class in the list of medicine quantities 
	 * from its medicine parameter
	 * @param med
	 * @return
	 */
	public StockPositionItem findStockPosition(Medicine med) {
		for (StockPositionItem item: getItems()) {
			if (item.getStockPosition().getMedicine().equals(med))
				return item;
		}
		return null;
	}

	
	/**
	 * Execute the adjustment
	 * @return
	 */
	@Transactional
	public String executeBatchAdjustment() {
		if ((adjustmentInfo == null))
			return "error";
		
		if (stockPosition == null)
			return "error";
		
		//Check limit date
		if(!userSession.isCanGenerateMovements(DateUtils.getDate())){
			facesMessages.addToControlFromResourceBundle("adjInfofldoptions", "meds.movs.errorlimitdate", DateUtils.formatAsLocale(userSession.getTbunit().getLimitDateMedicineMovement(), false));
			return "error";
		}
		
		facesMessages.clear();
	
//		MovementType type = MovementType.ADJUSTMENT;
		
		Map<BatchQuantity, Integer> sels = getBatchSelection().getSelectedBatchesQtds();

		// create batch map to be saved with the movement
		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		StockPositionItem item = findStockPosition(stockPosition.getMedicine());

		for (BatchQuantity bq: sels.keySet()) {
			Integer qtd = sels.get(bq);
			if ((qtd != null) && (qtd != bq.getQuantity())) {
				int qtdAdjust = qtd - bq.getQuantity();
				batches.put(bq.getBatch(), qtdAdjust);
			}
		}
		
		//Validates if the user is doing an expired adjustment with a positive difference
		if(UserSession.getWorkspace().getExpiredMedicineAdjustmentType() != null && 
				UserSession.getWorkspace().getExpiredMedicineAdjustmentType().getId() == adjustmentInfo.getValue().getId()){
			for (Batch b: batches.keySet()) {
				Integer qtd = batches.get(b);
				if(qtd!=null && qtd > 0){
					facesMessages.addToControlFromResourceBundle("adjInfofldoptions", "meds.movs.expiredposdjust");
					return "error-expiredposdjust";
				}
			}
		}
		
		if(batches.size() == 0){
			facesMessages.addFromResourceBundle("Batch.cantAdjustBatch");
			actionExecuted = true;
			return "batches-adjusted";
		}

		try{
			movementHome.initMovementRecording();

			Movement m = movementHome.prepareNewAdjustment(DateUtils.getDate(), userSession.getTbunit(), source, item.getStockPosition().getMedicine(), batches, adjustmentInfo);
			
			if(m == null){
				facesMessages.clear();
				facesMessages.addToControlFromResourceBundle("adjInfofldoptions", movementHome.getErrorMessage());
				facesMessages.addFromResourceBundle(movementHome.getErrorMessage());
				return "error";
			}
			
			movementHome.savePreparedMovements();
			
			saveLog(RoleAction.EDIT, batches);

			actionExecuted = true;
			
		}catch(Exception e){
			e.printStackTrace();
			facesMessages.addFromResourceBundle("Batch.cantAdjustBatch");
			return "batches-adjusted";
		}
		
		facesMessages.addFromResourceBundle("Batch.adjustBatchSuccess");
		
		return "batches-adjusted";
	}

	
	/**
	 * Initialize the batch selection 
	 */
	public void initializeBatchAdjustment() {
		if (stockPosition == null)
			return;

		getBatchSelection().clear();
		getBatchSelection().setTbunit(userSession.getTbunit());
		getBatchSelection().setMedicine(stockPosition.getMedicine());
		getBatchSelection().setSource(source);
		getBatchSelection().setAllowQtdOverStock(true);

		StockPositionItem item = findStockPosition(stockPosition.getMedicine());
		getBatchSelection().setSelectedBatches(getBatchesMap(item, true));
		actionExecuted = false;
	}


	/**
	 * Save a new batch
	 * @return
	 */
	public String saveNewBatch() {
		if (batchQuantity == null)
			return "error";

		if(existingBatchInThisUnit)
			return "error";
		
		//check limit movement date
		if(!userSession.isCanGenerateMovements(movementDate)){
			facesMessages.addToControlFromResourceBundle("edtdate", "meds.movs.errorlimitdate", DateUtils.formatAsLocale(userSession.getTbunit().getLimitDateMedicineMovement(), false));
			return "error";
		}
			
		Tbunit unit = userSession.getTbunit();
		
		Batch batch = batchQuantity.getBatch();
		batchQuantity.setSource(source);
		batchQuantity.setTbunit(unit);
		int qtd = batchQuantity.getQuantity();
		batchQuantity.setQuantity(0);
		App.getEntityManager().persist(batch);
		App.getEntityManager().persist(batchQuantity);
		App.getEntityManager().flush();
		
		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		batches.put(batch, qtd);
		
		// generate the new movement
		movementHome.initMovementRecording();
		movementHome.prepareNewAdjustment(movementDate, unit, source, batch.getMedicine(), batches, adjustmentInfo);
		movementHome.savePreparedMovements();
		saveLog(RoleAction.NEW, batches);
		
		actionExecuted = true;
		
		this.items = null;
		
		return "new-batch-saved";
	}
	
	/**
	 * Save a new batch without nulling batchQuantity.quantity
	 * @author A.M.
	 */
	public String saveNewBatchWithQtd() {
		if (batchQuantity == null)
			return "error";

		if(existingBatchInThisUnit)
			return "error";
		
		//check limit movement date
		if(!userSession.isCanGenerateMovements(movementDate)){
			facesMessages.addToControlFromResourceBundle("edtdate", "meds.movs.errorlimitdate", DateUtils.formatAsLocale(userSession.getTbunit().getLimitDateMedicineMovement(), false));
			return "error";
		}
			
		Tbunit unit = userSession.getTbunit();
		
		Batch batch = batchQuantity.getBatch();
		batchQuantity.setSource(source);
		batchQuantity.setTbunit(unit);
		int qtd = batch.getQuantityReceived();
		batchQuantity.setQuantity(qtd);
		App.getEntityManager().persist(batch);
		App.getEntityManager().persist(batchQuantity);
		App.getEntityManager().flush();
		
		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		batches.put(batch, qtd);
		
		// generate the new movement
		movementHome.initMovementRecording();
		movementHome.prepareNewAdjustment(movementDate, unit, source, batch.getMedicine(), batches, adjustmentInfo);
		movementHome.savePreparedMovements();
		saveLog(RoleAction.NEW, batches);
		
		batchQuantity.setQuantity(batchQuantity.getQuantity()-qtd);
		
		actionExecuted = true;
		
		this.items = null;
		
		return "new-batch-saved";
	}
	
	/**
	 * Persist changes
	 * @author A.M.
	 */
	public String saveEditBatch() {
		if (batchQuantity == null)
			return "error";

		if(existingBatchInThisUnit)
			return "error";
			
		Tbunit unit = userSession.getTbunit();
		
		Batch batch = batchQuantity.getBatch();
		batchQuantity.setSource(source);
		batchQuantity.setTbunit(unit);
		int qtd = batchQuantity.getBatch().getQuantityReceived();
		
		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		batches.put(batch, qtd-batchQuantity.getQuantity());
		//batchQuantity.setQuantity(qtd);
		App.getEntityManager().persist(batch);
		//App.getEntityManager().persist(batchQuantity);
		App.getEntityManager().flush();
		
		
		// generate the new movement
		movementHome.initMovementRecording();
		movementHome.prepareNewAdjustment(movementDate, userSession.getTbunit(), source, batchQuantity.getBatch().getMedicine(), batches, adjustmentInfo);
		movementHome.savePreparedMovements();

		saveLog(RoleAction.EDIT, batches);
		
		actionExecuted = true;
		
		this.items = null;
		
		return "edit-batch-finished";
	}
	
	/**
	 * Save new batch or persist changes
	 * @author A.M.
	 */
	public String saveBatch() {
		if (batchQuantity.getId()==null)
			return saveNewBatchWithQtd();
		return saveEditBatch();
	}
	
	/**
	 * Remove a batch from a unit
	 * @return
	 */
	public String deleteBatch() {
		if ((batchQuantity == null) || (adjustmentInfo == null) || (movementDate == null))
			return "error";

		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		batches.put(batchQuantity.getBatch(), -batchQuantity.getQuantity());

		// generate movement to remove the batch quantity
		movementHome.initMovementRecording();
		movementHome.prepareNewAdjustment(movementDate, userSession.getTbunit(), source, batchQuantity.getBatch().getMedicine(), batches, adjustmentInfo);
		movementHome.savePreparedMovements();
		saveLog(RoleAction.DELETE, batches);
		
		actionExecuted = true;
		
		return "batch-deleted";
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
	
	private Map<Batch, Integer> getBatchesMap(StockPositionItem it, boolean shpippedQtd) {
		Map<Batch, Integer> sels = new HashMap<Batch, Integer>();
		for (BatchQuantity b: it.getBatches()) {
		 	 sels.put(b.getBatch(), b.getQuantity());
		}
		return sels;
	}
	
	public void verifyBatch(){
		if(batchQuantity != null && batchQuantity.getBatch() != null && batchQuantity.getBatch().getBatchNumber() != null && 
					!batchQuantity.getBatch().getBatchNumber().equals("") && batchQuantity.getBatch().getMedicine() != null){	
			
			ArrayList<BatchQuantity> bq = (ArrayList<BatchQuantity>) App.getEntityManager().createQuery("from BatchQuantity bq " +
																			  "join bq.batch b " +
																			  "where bq.tbunit.id = :unitId and " +
																			  "b.batchNumber = :batchNumber and " +
																			  "b.manufacturer = :manufacturer and " +
																			  "b.medicine.id = :medicineId")
																				.setParameter("unitId", userSession.getTbunit().getId())
																				.setParameter("batchNumber", batchQuantity.getBatch().getBatchNumber())
																				.setParameter("manufacturer", batchQuantity.getBatch().getManufacturer())
																				.setParameter("medicineId", batchQuantity.getBatch().getMedicine().getId())
																				.getResultList();
			
			if(bq != null && bq.size() > 0){
				existingBatchInThisUnit = true;
			}else{
				existingBatchInThisUnit = false;
				ArrayList<Batch> b = (ArrayList<Batch>) App.getEntityManager().createQuery("from Batch b " +
									  "where b.batchNumber = :batchNumber and " +
									  "b.manufacturer = :manufacturer and " +
									  "b.medicine.id = :medicineId")
									  .setParameter("batchNumber", batchQuantity.getBatch().getBatchNumber())
									  .setParameter("manufacturer", batchQuantity.getBatch().getManufacturer())
									  .setParameter("medicineId", batchQuantity.getBatch().getMedicine().getId())
									  .getResultList();
	
				if(b!=null && b.size() > 0){
					batchQuantity.setBatch(b.get(0));
				}
			}
		}
	}
	
	public void clearBatch(){
		existingBatchInThisUnit = false;
		batchQuantity = new BatchQuantity();
		batchQuantity.setBatch(new Batch());
	}
	
	/**
	 * Return the list of medicines and its available quantity to be displayed
	 * and selected by the user  
	 * @return List of {@link StockPositionItem} objects
	 */
	public List<StockPositionItem> getItems() {
		if (items == null)
			createItems();
		return items;
	}

	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}

	public AdjustmentItem getItem() {
		return item;
	}
	
	public void setSourceId(Integer id) {
		if (id == null)
			 source = null;
		else source = App.getEntityManager().find(Source.class, id); 
	}
	
	public Integer getSourceId() {
		if (source == null)
			 return null;
		else return source.getId();
	}
	public FieldValueComponent getAdjustmentInfo() {
		if (adjustmentInfo == null)
			adjustmentInfo = new FieldValueComponent();
		return adjustmentInfo;
	}

	public void setAdjustmentInfo(FieldValueComponent adjustmentInfo) {
		this.adjustmentInfo = adjustmentInfo;
	}



	/**
	 * Item in the list of medicines to be displayed, carrying information about batches and quantity available
	 * @author Ricardo Memoria
	 *
	 */
	public class StockPositionItem {
		private StockPosition stockPosition;
		private List<BatchQuantity> batches = new ArrayList<BatchQuantity>();

		/**
		 * @return the stockPosition
		 */
		public StockPosition getStockPosition() {
			return stockPosition;
		}
		/**
		 * @param stockPosition the stockPosition to set
		 */
		public void setStockPosition(StockPosition stockPosition) {
			this.stockPosition = stockPosition;
		}
		/**
		 * @return the batches
		 */
		public List<BatchQuantity> getBatches() {
			return batches;
		}
		/**
		 * @param batches the batches to set
		 */
		public void setBatches(List<BatchQuantity> batches) {
			this.batches = batches;
		}
	}



	/**
	 * @return the stockPosition
	 */
	public StockPosition getStockPosition() {
		return stockPosition;
	}

	/**
	 * @param stockPosition the stockPosition to set
	 */
	public void setStockPosition(StockPosition stockPosition) {
		this.stockPosition = stockPosition;
	}
	
	public Integer getStockPositionId() {
		return (stockPosition != null? stockPosition.getId(): null);
	}
	
	public void setStockPositionId(Integer val) {
		if (val == null)
			 stockPosition = null;
		else stockPosition = App.getEntityManager().find(StockPosition.class, val);
	}

	/**
	 * @return the actionExecuted
	 */
	public boolean isActionExecuted() {
		return actionExecuted;
	}

	/**
	 * @return the batchQuantity
	 */
	public BatchQuantity getBatchQuantity() {
		if (batchQuantity == null) {
			batchQuantity = new BatchQuantity();
			batchQuantity.setBatch(new Batch());
		}
		return batchQuantity;
	}

	/**
	 * @return the movementDate
	 */
	public Date getMovementDate() {
		if (movementDate == null)
			movementDate = new Date();
		return movementDate;
	}

	/**
	 * @param movementDate the movementDate to set
	 */
	public void setMovementDate(Date movementDate) {
		this.movementDate = movementDate;
	}

	public Integer getBatchQuantityId() {
		return (batchQuantity == null? null: batchQuantity.getId());
	}

	public void setBatchQuantityId(Integer id) {
		if (id == null)
			 batchQuantity = null;
		else batchQuantity = App.getEntityManager().find(BatchQuantity.class, id);
	}
	
	public boolean checkExpiringBatch(Object o){
		StockPositionItem item = (StockPositionItem) o;
		if(item!=null){
			for(BatchQuantity bq : item.getBatches()){
				if(inventoryReport.isExpiringBatch(bq))
					return true;
			}
		}
		return false;
	}

	public boolean checkExpiredBatch(Object o){
		StockPositionItem item = (StockPositionItem) o;
		if(item!=null){
			for(BatchQuantity bq : item.getBatches()){
				if(bq.getBatch().isExpired())
					return true;
			}
		}
		return false;
	}

	/**
	 * Get instance of batchSelection
	 * @return the batchSelection
	 */
	public BatchSelection getBatchSelection() {
		if (batchSelection == null)
			batchSelection = (BatchSelection) App.getComponent("batchSelection");
		return batchSelection;
	}
	
}
