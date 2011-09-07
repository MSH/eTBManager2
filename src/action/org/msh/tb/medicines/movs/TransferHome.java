package org.msh.tb.medicines.movs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.SourceGroup;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Transfer;
import org.msh.tb.entities.TransferBatch;
import org.msh.tb.entities.TransferItem;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.entities.enums.TransferStatus;
import org.msh.tb.log.LogService;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.medicines.MedicineStockSelection;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;


@Name("transferHome")
public class TransferHome extends EntityHomeEx<Transfer> {
	private static final long serialVersionUID = 5951453459621134668L;

	@In(create=true) UserSession userSession;
	@In(create=true) MedicineStockSelection medicineStockSelection;
	@In(create=true) BatchSelection batchSelection;
	@In(create=true) MovementHome movementHome;
	@In(create=true) FacesMessages facesMessages;

	// structures the items by source
	public class SourceItem extends SourceGroup<TransferItem> {};

	private List<SourceItem> sources;
	private TransferItem transferItem;
	
	private TBUnitSelection tbunitSelection = new TBUnitSelection(false, TBUnitFilter.MEDICINE_WAREHOUSES);


	@Factory("transfer")
	public Transfer getTransfer() {
		return getInstance();
	}

	@Factory("transferStatus")
	public TransferStatus[] getTransferStatus() {
		return TransferStatus.values();
	}

	
	/**
	 * Saves a new medicine transfer. When the transfer is created, just the stock of the "unitFrom" is decreased.
	 * The stock of the "unitTo" is decreased when it's confirmed the receiving of the medicines
	 * @return
	 */
	@Transactional
	public String saveNewTransfer() {
		Transfer transfer = getInstance();

		// checks if any medicine were selected for transfer
		if (transfer.getItems().size() == 0) {
			facesMessages.addFromResourceBundle("edtrec.nomedicine");
			return "error";
		}

		for (TransferItem it: transfer.getItems()) {
			if (it.getBatches().size() == 0) {
				facesMessages.addFromResourceBundle("edtrec.nobatch", it.getMedicine().toString());
				return "error";
			}
		}
		
		transfer.setUnitTo(tbunitSelection.getTbunit());
		
		Tbunit unit = userSession.getTbunit();
		
		transfer.setUnitFrom(unit);
		transfer.setStatus(TransferStatus.WAITING_RECEIVING);
		transfer.setUserFrom(getUser());
		
		Date dt = transfer.getShippingDate();

		// creates out movements
		movementHome.initMovementRecording();
		boolean bCanTransfer = true;
		for (TransferItem it: transfer.getItems()) {
			String comment = transfer.getUnitTo().getName().getDefaultName();
			
			Movement movOut = movementHome.prepareNewMovement(dt, unit, it.getSource(), 
					it.getMedicine(), MovementType.TRANSFEROUT, 
					getBatchesMap(it, true), comment);

			if (movOut == null) {
				bCanTransfer = false;
				it.setData(movementHome.getErrorMessage());
			}
			
			it.setMovementOut(movOut);
		}
		
		if (!bCanTransfer)
			return "error";
		movementHome.savePreparedMovements();

		// register transfer in the log system
		LogService log = getLogService();
		log.addValue(".unitFrom", transfer.getUnitFrom());
		log.addValue(".unitTo", transfer.getUnitTo());
		log.addValue(".shippingDate", transfer.getShippingDate());
		log.saveExecuteTransaction(transfer, "NEW_TRANSFER");
		
		return persist();
	}

	
	/**
	 * Register the transfer receiving. When the transfer is received, the quantity received is included in the unit stock.
	 * @return
	 */
	@Transactional
	public String receiveTransfer() {
		if (!isCanReceive())
			return "denied";
		
		Transfer transfer = getInstance();

		if (!validateReceiving())
			return "error";
		
		Date dt = transfer.getReceivingDate();
		Tbunit unit = transfer.getUnitTo();
		
		movementHome.initMovementRecording();
		for (TransferItem it: transfer.getItems()) {
			String comment = transfer.getUnitFrom().getName().getDefaultName();
			
			// create batch list
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>(); 
			for (TransferBatch tb: it.getBatches()) {
				batches.put(tb.getBatch(), tb.getQuantityReceived());
			}

			// create receiving movement
			Movement movIn = movementHome.prepareNewMovement(dt, unit, it.getSource(), it.getMedicine(), MovementType.TRANSFERIN, 
					batches, comment);
			
			it.setMovementIn(movIn);
		}
		movementHome.savePreparedMovements();
		
		transfer.setStatus(TransferStatus.DONE);
		transfer.setUserTo(getUser());
		
		getEntityManager().persist(transfer);
		getEntityManager().flush();

		// register transfer in the log system
		LogService log = getLogService();
		log.addValue(".unitFrom", transfer.getUnitFrom());
		log.addValue(".unitTo", transfer.getUnitTo());
		log.addValue(".receivingDate", transfer.getReceivingDate());
		log.saveExecuteTransaction(transfer, "TRANSF_REC");

		return "received";
	}

	
	/**
	 * Check if the received data is correct
	 * @return
	 */
	protected boolean validateReceiving() {
		Transfer transfer = getInstance();
		
		// check if the receiving date is not before the sending date
		Date dt = transfer.getReceivingDate();
		if (transfer.getShippingDate().after(dt)) {
			facesMessages.addFromResourceBundle("medicines.transfer.datebefore");
			return false;
		}
		
		// check if the comment is required
		
		// comment is blank
		if ((transfer.getCommentsTo() == null) || (transfer.getCommentsTo().isEmpty())) {
			for (TransferItem t: transfer.getItems()) {
				for (TransferBatch b: t.getBatches()) {
					if (!b.getQuantityReceived().equals(b.getQuantity())) {
						facesMessages.addFromResourceBundle("medicines.transfer.commentreq");
						return false;
					}
				}
			}
		}
		
		return true;
	}
	

	
	/**
	 * Initialize the quantity received to be equals the quantity shipped. It's intended to be called before
	 * editing and make easier for the user to just confirm the total quantity 
	 */
	public void initializeReceiving() {
		Transfer transfer = getInstance();
		
		if (sources == null) {
			for (TransferItem it: transfer.getItems()) {
				for (TransferBatch bt: it.getBatches()) {
					if (bt.getQuantityReceived() == null)
						bt.setQuantityReceived(bt.getQuantity());
				}
			}			
		}

//		if (transfer.getReceivingDate() == null)
//			transfer.setReceivingDate(DateUtils.getDate());
	}
	
	
	/**
	 * Cancel the receiving
	 * @return
	 */
	@Transactional
	public String cancel() {
		Transfer transfer = getInstance();
		
		if (transfer.getStatus() != TransferStatus.WAITING_RECEIVING)
			return "denied";
		
		transfer.setStatus(TransferStatus.CANCELLED);
		
		movementHome.initMovementRecording();
		// remove the movements
		for (TransferItem it: transfer.getItems()) {
			Movement mov = it.getMovementIn();
			if (mov != null) {
				it.setMovementIn(null);
				movementHome.prepareMovementsToRemove(mov);
			}
			
			mov = it.getMovementOut();
			if (mov != null) {
				it.setMovementOut(null);
				movementHome.prepareMovementsToRemove(mov);
			}
		}
		movementHome.savePreparedMovements();

		getEntityManager().persist(transfer);
		

		// register transfer in the log system
		LogService log = getLogService();
		log.saveExecuteTransaction(transfer, "TRANSF_CANCEL");

		return "canceled";
	}
	

	public void createSources() {
		sources = new ArrayList<SourceItem>();
		
		for (TransferItem item: getInstance().getItems()) {
			Source s = item.getSource();
			SourceItem si = null;
			for (SourceItem it: sources)
				if (it.getSource().getId().equals(s.getId())) {
					si = it;
					break;
				}
			
			if (si == null) {
				si = new SourceItem();
				si.setSource(s);
				sources.add(si);
			}
	
			si.getItems().add(item);
		}
	}
	
	public List<SourceItem> getSources() {
		if (sources == null)
			createSources();
		return sources;
	}
	
	public void filterMedicines() {
		medicineStockSelection.setTbunit(userSession.getTbunit());
		
		for (TransferItem it: getInstance().getItems()) {
			medicineStockSelection.removeItem(it.getSource(), it.getMedicine());
		}
	}
	
	public void selectMedicines() {
		List<StockPosition> lst = medicineStockSelection.getSelectedMedicines();

		for (StockPosition sp: lst) {
			TransferItem it = new TransferItem();
			it.setMedicine(sp.getMedicine());
			it.setSource(sp.getSource());
			it.setTransfer(getInstance());
			getInstance().getItems().add(it);
		}
		sources = null;
	}


	/**
	 * Remove a item from the transfer
	 * @param ti
	 */
	public void removeItem(TransferItem ti) {
		getInstance().getItems().remove(ti);
		if (getEntityManager().contains(ti))
			getEntityManager().remove(ti);
		sources = null;
	}

	/**
	 * Initialize the batch selection
	 * @param it
	 */
	public void initBatchSelection(TransferItem it) {
		transferItem = it;
		batchSelection.clear();
		batchSelection.setTbunit(userSession.getTbunit());
		batchSelection.setMedicine(it.getMedicine());
		batchSelection.setSource(it.getSource());

		batchSelection.setSelectedBatches(getBatchesMap(it, true));
	}
	

	private Map<Batch, Integer> getBatchesMap(TransferItem it, boolean shippedQtd) {
		Map<Batch, Integer> sels = new HashMap<Batch, Integer>();
		for (TransferBatch b: it.getBatches()) {
			if (shippedQtd)
			 	 sels.put(b.getBatch(), b.getQuantity());
			else sels.put(b.getBatch(), b.getQuantityReceived());
		}
		return sels;
	}
	
	
	/**
	 * Include the selected batches
	 */
	public void selectBatches() {
		Map<Batch, Integer> sels = batchSelection.getSelectedBatches();
		
		// check if there is a batch with zero quantity or quantity over the remaining quantity
/*		for (Batch b: sels.keySet()) {
			Integer val = sels.get(b);
			if ((val == null) || (val == 0) || (val > b.getRemainingQuantity())) 
				return;
		}
*/
		for (TransferBatch tb: transferItem.getBatches())
			if (getEntityManager().contains(tb))
				getEntityManager().remove(tb);
		transferItem.getBatches().clear();
		
		for (Batch b: sels.keySet()) {
			TransferBatch tb = new TransferBatch();
			tb.setBatch(b);
			tb.setQuantity(sels.get(b));
			tb.setTransferItem(transferItem);
			transferItem.getBatches().add(tb);
		}

		// free memory space
		batchSelection.clear();
		transferItem = null;
	}

	
	/**
	 * Check if the order can be received by the user
	 * @return
	 */
	public boolean isCanReceive() {
		if (getInstance().getStatus() != TransferStatus.WAITING_RECEIVING)
			return false;
		
		Tbunit userunit = userSession.getWorkingTbunit();
		Tbunit transfunit = getInstance().getUnitTo();
		
		return ((transfunit != null) && (transfunit.getId().equals(userunit.getId())));
	}

	
	/**
	 * Check if transfer can be canceled
	 * @return
	 */
	public boolean isCanCancel() {
		Transfer transfer = getInstance();
		
		Tbunit userunit = userSession.getWorkingTbunit();

		// is transfer sent and is unit that shipped the medicines ?
		if ((transfer.getStatus() == TransferStatus.WAITING_RECEIVING) && (userunit.equals(transfer.getUnitFrom()))) {
			return Identity.instance().hasRole("NEW_TRANSFER");
		}
		
		// is transfer received and is unit that received the medicines ?
		if ((transfer.getStatus() == TransferStatus.DONE) && (userunit.equals(transfer.getUnitTo()))) {
			return Identity.instance().hasRole("TRANSF_REC");
		}
		
		return false;
	}

	/**
	 * @return the tbunitSelection
	 */
	public TBUnitSelection getTbunitSelection() {
		return tbunitSelection;
	}
}
