package org.msh.tb.medicines.movs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.utils.date.DateUtils;


/**
 * Handle funcionalities for adjustment in the quantity of medicine available for a specific source
 * @author Ricardo Memoria
 *
 */
@Name("stockAdjustmentHome")
public class StockAdjustmentHome extends Controller {
	private static final long serialVersionUID = 5670008276682104291L;

	private Source source;
	private List<StockPositionItem> items;
	private AdjustmentItem item;
	private StockPosition stockPosition;
	private String reasonAdjustment;
	private boolean actionExecuted;
	private BatchQuantity batchQuantity;
	private Date movementDate;
	
	@In(create=true) EntityManager entityManager;
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	@In(create=true) BatchSelection batchSelection;
	@In(create=true) FacesMessages facesMessages;

	/**
	 * Initialize the items for adjustment (used when source or tbunit chage)
	 */
	public void initialize() {
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
			item.getBatches().add(b);
		}
	}
	
	
	/**
	 * Search for instance of {@link StockPosition} class in the list of medicine quantities 
	 * from its medicine parameter
	 * @param med
	 * @return
	 */
	protected StockPositionItem findStockPosition(Medicine med) {
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
		if ((reasonAdjustment == null) || (reasonAdjustment.isEmpty()))
			return "error";
		
		if (stockPosition == null)
			return "error";
		
		MovementType type = MovementType.ADJUSTMENT;
		
		Map<BatchQuantity, Integer> sels = batchSelection.getSelectedBatchesQtds();

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
		
		movementHome.initMovementRecording();
		movementHome.prepareNewMovement(DateUtils.getDate(), userSession.getTbunit(), source, item.getStockPosition().getMedicine(),
				type, batches, reasonAdjustment);
		movementHome.savePreparedMovements();
		saveLog(RoleAction.EDIT, batches);

		actionExecuted = true;
		
		return "batches-adjusted";
	}

	
	/**
	 * Initialize the batch selection 
	 */
	public void initializeBatchAdjustment() {
		if (stockPosition == null)
			return;

		batchSelection.clear();
		batchSelection.setTbunit(userSession.getTbunit());
		batchSelection.setMedicine(stockPosition.getMedicine());
		batchSelection.setSource(source);
		batchSelection.setAllowQtdOverStock(true);

		StockPositionItem item = findStockPosition(stockPosition.getMedicine());
		batchSelection.setSelectedBatches(getBatchesMap(item, true));
		actionExecuted = false;
	}


	/**
	 * Save a new batch
	 * @return
	 */
	public String saveNewBatch() {
		if (batchQuantity == null)
			return "error";

		Tbunit unit = userSession.getTbunit();
		
		Batch batch = batchQuantity.getBatch();
		batchQuantity.setSource(source);
		batchQuantity.setTbunit(unit);
		int qtd = batchQuantity.getQuantity();
		batchQuantity.setQuantity(0);
		entityManager.persist(batch);
		entityManager.persist(batchQuantity);
		entityManager.flush();
		
		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		batches.put(batch, qtd);

		// generate the new movement
		movementHome.initMovementRecording();
		movementHome.prepareNewMovement(movementDate, unit, source, batch.getMedicine(), MovementType.ADJUSTMENT, batches, reasonAdjustment);
		movementHome.savePreparedMovements();
		saveLog(RoleAction.NEW, batches);
		
		actionExecuted = true;
		
		return "new-batch-saved";
	}
	
	
	/**
	 * Remove a batch from a unit
	 * @return
	 */
	public String deleteBatch() {
		if ((batchQuantity == null) || (reasonAdjustment == null) || (reasonAdjustment.isEmpty()) || (movementDate == null))
			return "error";

		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		batches.put(batchQuantity.getBatch(), -batchQuantity.getQuantity());

		// generate movement to remove the batch quantity
		movementHome.initMovementRecording();
		movementHome.prepareNewMovement(movementDate, 
				userSession.getTbunit(),
				source,
				batchQuantity.getBatch().getMedicine(),
				MovementType.ADJUSTMENT,
				batches,
				reasonAdjustment);
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
		logSrv.save("STOCKPOS", roleAction, null, null);
	}
	
	private Map<Batch, Integer> getBatchesMap(StockPositionItem it, boolean shpippedQtd) {
		Map<Batch, Integer> sels = new HashMap<Batch, Integer>();
		for (BatchQuantity b: it.getBatches()) {
		 	 sels.put(b.getBatch(), b.getQuantity());
		}
		return sels;
	}
	
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
		else source = entityManager.find(Source.class, id); 
	}
	
	public Integer getSourceId() {
		if (source == null)
			 return null;
		else return source.getId();
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
		else stockPosition = entityManager.find(StockPosition.class, val);
	}

	/**
	 * @return the reasonAdjustment
	 */
	public String getReasonAdjustment() {
		return reasonAdjustment;
	}

	/**
	 * @param reasonAdjustment the reasonAdjustment to set
	 */
	public void setReasonAdjustment(String reasonAdjustment) {
		this.reasonAdjustment = reasonAdjustment;
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
		else batchQuantity = entityManager.find(BatchQuantity.class, id);
	}
}
