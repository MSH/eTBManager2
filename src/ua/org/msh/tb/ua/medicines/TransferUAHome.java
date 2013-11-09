package org.msh.tb.ua.medicines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.application.App;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Transfer;
import org.msh.tb.entities.TransferBatch;
import org.msh.tb.entities.TransferItem;
import org.msh.tb.entities.User;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.entities.enums.TransferStatus;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.medicines.MedicineStockSelection;
import org.msh.tb.medicines.BatchSelection.BatchItem;
import org.msh.tb.medicines.movs.BatchMovementsQuery;
import org.msh.tb.medicines.movs.MedicineTransferMsgDispatcher;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.movs.TransferHome;
import org.msh.tb.medicines.movs.TransferHome.SourceItem;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.tb.tbunits.TBUnitType;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.tb.ua.entities.TransferUA;
import org.msh.tb.ua.utils.MedicineCalculator;
import org.msh.utils.ItemSelect;
import org.msh.utils.date.DateUtils;

@Name("transferUAHome")
@Scope(ScopeType.CONVERSATION)
public class TransferUAHome extends EntityHomeEx<TransferUA>{
	private static final long serialVersionUID = -7100704369626250194L;
	
	@In(create=true) TransferHome transferHome;
	@In(create=true) BatchSelection batchSelection;
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	@In(create=true) BatchMovementsQuery batchMovements;
	
	private List<SourceItem> sourcesOld;
	private List<SourceItem> sources;
	private List<Movement> movementsOld = new ArrayList<Movement>(); 
	private List<TransferBatch> tbToRemove = new ArrayList<TransferBatch>();
	private List<TransferItem> tiToRemove = new ArrayList<TransferItem>();
	//private List<BatchSelection> batchSelections = new ArrayList<BatchSelection>();
	

	@In(create=true) MedicineStockSelection medicineStockSelection;
	@In(create=true) FacesMessages facesMessages;
	
	private TransferItem transferItem;
	
	private TBUnitSelection tbunitSelection = new TBUnitSelection("unitid", false, TBUnitType.MEDICINE_WAREHOUSES);
	
	@Factory("transferUA")
	public TransferUA getTransfer() {
		return getInstance();
	}
	
	/**
	 * Remember some data from base transfer.
	 * Recalculate batchSelection for edit
	 * */
	public void initialize(){
		if (getInstance().getId()!=null){
			if (sources == null){
				sources = getSources();
				sourcesOld = new ArrayList<TransferHome.SourceItem>();
				for (SourceItem si:sources){
					sourcesOld.add(si.copyBatches());
					for (TransferItem ti:si.getItems()){
						movementsOld.add(ti.getMovementOut());
						initBatchSelection(ti);
						reCalcFromCurrentTransfer(ti);
					}
				}
				
			}
		}
	}
	
	/**
	 * Remove a item from the transfer
	 * @param ti
	 */
	public void removeItem(TransferItem ti) {
		tbToRemove.addAll(ti.getBatches());
		tiToRemove.add(ti);
		ti.getBatches().clear();
	}

	/*
	private void printTransferItem(List<SourceItem> s){
		String res="";
		for (SourceItem si:s){
			res = si.getSource().getId()+"|";
			for (TransferItem ti:si.getItems()){
				String rm = res + ti.getMedicine().getId()+"|";
				for (TransferBatch tb:ti.getBatches()){
					String r = rm+tb.getId() +"|"+tb.getQuantity();
					System.out.println(r);
				}
			}
		}
	}*/
	
	public String saveEditTransfer(){
		if (!validateData())
			return "error";
		
		sources = getSources(); //refresh sources
		for (SourceItem si:sources)
			for (TransferItem ti:si.getItems()){
				initBatchSelection(ti);
				reCalcFromCurrentTransferBack(ti);
			}

		Iterator<TransferBatch> iter = tbToRemove.iterator();
		while (iter.hasNext()){
			TransferBatch tb = iter.next();
			App.getEntityManager().createQuery("delete from TransferBatch tb where tb.id="+tb.getId()).executeUpdate();
			//App.getEntityManager().remove(tb);
		}

		//App.getEntityManager().flush();
		
		movementHome.initMovementRecording();
		removeMovements(movementsOld);
		movementHome.savePreparedMovements();
		
		TransferUA transfer = getInstance();
		transfer.setUnitTo(getTbunitSelection().getSelected());
		Tbunit unit = userSession.getTbunit();
		transfer.setUnitFrom(unit);
		transfer.setStatus(TransferStatus.WAITING_RECEIVING);
		transfer.setUserFrom(getUser());
		
		Date dt = transfer.getShippingDate();

		// creates out movements
		movementHome.initMovementRecording();
		boolean bCanTransfer = true;
		for (TransferItem it: transfer.getItems()) 
			if (!it.getBatches().isEmpty())	{
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
		TransactionLogService log = getLogService();
		log.addTableRow(".unitFrom", transfer.getUnitFrom());
		log.addTableRow(".unitTo", transfer.getUnitTo());
		log.addTableRow(".shippingDate", transfer.getShippingDate());
		log.save("NEW_TRANSFER", RoleAction.EXEC, transfer);
		
		notifyTransference(); //instead of Events.instance().raiseEvent("medicine-new-transfer");
		
		//post-deleting empty transfer item.  because of pre-deleting call the exception
		Iterator<TransferItem> it2 = getInstance().getItems().iterator();
		while (it2.hasNext()){
			TransferItem ti = it2.next();
			if (ti.getBatches().isEmpty())
				App.getEntityManager().createQuery("delete from TransferItem ti where ti.id="+ti.getId()).executeUpdate();
		}
		
		return persist();

		/*if (!"error".equals(res)){
			Iterator<TransferItem> it2 = transferHome.getInstance().getItems().iterator();
			while (it2.hasNext()){
				TransferItem ti = it2.next();
				if (ti.getBatches().isEmpty())
					App.getEntityManager().createQuery("delete from TransferItem ti where ti.id="+ti.getId()).executeUpdate();
			}
		}
		return res;*/
		/*printTransferItem(sourcesOld);
		System.out.println("--------");
		printTransferItem(sources);
		System.out.println("--------");
		return "persist";*/
	}

	/**
	 * Check all movements in list to remove
	 * */
	private void removeMovements(List<Movement> lst) {
		for (Movement mov:lst){
			movementHome.prepareMovementsToRemove(mov);
			for (SourceItem si:sources)
				for (TransferItem ti:si.getItems())
					if (ti.getMovementOut()!=null)
						if (ti.getMovementOut().getId() == mov.getId()){
							ti.setMovementOut(null);
							break;
					}
		}
	}

	/**
	 * Return TransferBatch, which contains necessary Batch 
	 * */
	private TransferBatch findBatch(List<SourceItem> s, Batch b) {
		for (SourceItem si:s)
			for (TransferItem ti:si.getItems()){
				TransferBatch tb = ti.findByBatch(b);
				if (tb!=null)
					return tb;
				}
		return null;
	}

	/**
	 * Add quantity from transferItem to batch selection
	 * */
	private void reCalcFromCurrentTransfer(TransferItem ti) {
		/*boolean wasAllStock = true;
		for (ItemSelect<BatchItem> is: batchSelection.getItems()){
			//if(ti.getBatches().contains((Batch)is.getItem().getBatch()))
				if (is.isSelected()){
					wasAllStock = false;
					int qtd = is.getItem().getQuantity();
					is.getItem().getBatchQuantity().setQuantity(is.getItem().getBatchQuantity().getQuantity()+qtd);
			}
		}
		if (wasAllStock)
		for (TransferBatch tb:ti.getBatches()){
			BatchItem bi = batchSelection.new BatchItem();
			BatchQuantity bq = new BatchQuantity();
			bq.setBatch(tb.getBatch());
			bq.setQuantity(ti.getQuantity());
			bq.setSource(ti.getSource());	
			bi.setBatchQuantity(bq);
			bi.setBatch(tb.getBatch());
			ItemSelect<BatchItem> item = new ItemSelect<BatchSelection.BatchItem>();
			item.setItem(bi);
			//item.getItem().getBatchQuantity().setQuantity(ti.getQuantity());
			item.getItem().setQuantity(ti.getQuantity());
			item.setSelected(true);
			batchSelection.getItems().add(item);
		}*/
		for (TransferBatch tb:ti.getBatches()){
			ItemSelect<BatchItem> is = findBatch(batchSelection, tb.getBatch());
			if (is!=null){
				if (is.isSelected()){
					//wasAllStock = false;
					int qtd = is.getItem().getQuantity();
					is.getItem().getBatchQuantity().setQuantity(is.getItem().getBatchQuantity().getQuantity()+qtd);
				}
			}
			else{
				BatchItem bi = batchSelection.new BatchItem();
				BatchQuantity bq = new BatchQuantity();
				bq.setBatch(tb.getBatch());
				bq.setQuantity(tb.getQuantity());
				bq.setSource(ti.getSource());	
				bq.setTbunit((Tbunit)App.getComponent("selectedUnit"));
				bi.setBatchQuantity(bq);
				//App.getEntityManager().persist(bq);
				bi.setBatch(tb.getBatch());
				ItemSelect<BatchItem> item = new ItemSelect<BatchSelection.BatchItem>();
				item.setItem(bi);
				//item.getItem().getBatchQuantity().setQuantity(ti.getQuantity());
				item.getItem().setQuantity(tb.getQuantity());
				item.setSelected(true);
				batchSelection.getItems().add(item);
			}
			//batchSelections.add(batchSelection);
		}
	}
	
	/**
	 * Return ItemSelect from BatchSelection, which contains necessary Batch 
	 * */
	private ItemSelect<BatchItem> findBatch(BatchSelection bs, Batch b) {
		for (ItemSelect<BatchItem> is: bs.getItems())
			if (is.getItem().getBatch().getId().intValue() == b.getId().intValue()){
				return is;
			}
		return null;
	}

	
	/**
	 * Subtract quantity, which added in process initialize
	 * */
	private void reCalcFromCurrentTransferBack(TransferItem ti) {
		for (ItemSelect<BatchItem> is: batchSelection.getItems()){
			if (is.isSelected()){
				TransferBatch tb = findBatch(sourcesOld, is.getItem().getBatch());
				if (tb!=null){
					int qtd = tb.getQuantity();
					is.getItem().getBatchQuantity().setQuantity(is.getItem().getBatchQuantity().getQuantity()-qtd);
				}
			}
		}
	}
	
	/**
	 * Edit existing selected batches
	 * */
	public void selectExistBatches() {
		Map<Batch, Integer> sels = batchSelection.getSelectedBatches();
		
		for (Batch b: sels.keySet()) {
			TransferBatch tb = getTransferItem().findByBatch(b);
			if (tb == null){
				tb = new TransferBatch();
				tb.setBatch(b);
				tb.setQuantity(sels.get(b));
				tb.setTransferItem(getTransferItem());
				getTransferItem().getBatches().add(tb);
			}
			else{
				tb.setQuantity(sels.get(b));
			}
		}
		
		Iterator<TransferBatch> it = getTransferItem().getBatches().iterator();
		while (it.hasNext()){
			TransferBatch tb = it.next();
			if (!sels.containsKey(tb.getBatch())){
				//movementsOld.remove(tb.getTransferItem().getMovementOut());
				tbToRemove.add(tb);
				//removeTransfBatchFromSources(tb.getBatch());
				it.remove();
			}
		}
		
		// free memory space
		batchSelection.clear();
		setTransferItem(null);
	}
	
/*	private void removeTransfBatchFromSources(Batch batch) {
		List<List<SourceItem>> ss = new ArrayList<List<SourceItem>>();
		ss.add(sources);
		ss.add(sourcesOld);
		for (List<SourceItem> s:ss)
			for (SourceItem si:s)
				for (TransferItem ti:si.getItems())	{
					Iterator<TransferBatch> it = ti.getBatches().iterator();
					while (it.hasNext()){
						TransferBatch tb = it.next();
						if (batch.equals(tb.getBatch()))
							it.remove();
					}
				}
	}
	
	private void removeTransfItemFromSources(TransferItem item) {
		List<List<SourceItem>> ss = new ArrayList<List<SourceItem>>();
		ss.add(sources);
		ss.add(sourcesOld);
		for (List<SourceItem> s:ss)
			for (SourceItem si:s){
				Iterator<TransferItem> it = si.getItems().iterator();
				while (it.hasNext()){
					TransferItem ti = it.next();
					if (ti.getId().intValue() == item.getId().intValue()){
						it.remove();
					}
				}
			}
	}*/

	public double getGlobalTotal() {
		double res = 0;
		for (SourceItem s:getSources())
			for (TransferItem ti:s.getItems())
					res += MedicineCalculator.calculateTotalPrice(ti.getBatches(),5);
		return res;
	}
	
	public double getGlobalTotalReceived() {
		double res = 0;
		for (SourceItem s:getSources())
			for (TransferItem ti:s.getItems())
					res += MedicineCalculator.calculateTotalPriceReceived(ti.getBatches(),5);
		return res;
	}
	
	private boolean validateData(){
		boolean res = true;
		if(!userSession.isCanGenerateMovements(getTransfer().getShippingDate())){
			FacesMessages.instance().addToControlFromResourceBundle("edtdate", "meds.movs.errorlimitdate", DateUtils.formatAsLocale(userSession.getTbunit().getLimitDateMedicineMovement(), false));
			res = false;
		}
		
		// checks if any medicine were selected for transfer
		if (getTransfer().getItems().size() == 0) {
			FacesMessages.instance().addFromResourceBundle("edtrec.nomedicine");
			res = false;
		}

		// checks if any batch were selected for transfer
		int totSize = 0;
		for (TransferItem it: getTransfer().getItems()) {
			totSize += it.getBatches().size();
		}
		if (totSize == 0) {
			FacesMessages.instance().addFromResourceBundle("edtrec.nobatch");
			res = false;
		}
		return res;
	}
	
	public List<TransferItem> getTiToRemove() {
		return tiToRemove;
	}
	
	/**
	 * Saves a new medicine transfer. When the transfer is created, just the stock of the "unitFrom" is decreased.
	 * The stock of the "unitTo" is decreased when it's confirmed the receiving of the medicines
	 * @return
	 */
	@Transactional
	public String saveNewTransfer() {
		TransferUA transfer = getInstance();

		if(!userSession.isCanGenerateMovements(transfer.getShippingDate())){
			facesMessages.addToControlFromResourceBundle("edtdate", "meds.movs.errorlimitdate", DateUtils.formatAsLocale(userSession.getTbunit().getLimitDateMedicineMovement(), false));
			return "error";
		}
		
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
		
		transfer.setUnitTo(tbunitSelection.getSelected());
		
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
		TransactionLogService log = getLogService();
		log.addTableRow(".unitFrom", transfer.getUnitFrom());
		log.addTableRow(".unitTo", transfer.getUnitTo());
		log.addTableRow(".shippingDate", transfer.getShippingDate());
		log.save("NEW_TRANSFER", RoleAction.EXEC, transfer);
		
		notifyTransference(); //instead of Events.instance().raiseEvent("medicine-new-transfer");
		
		return persist();
	}

	public void notifyTransference(){		
		TransferUA transfer = getInstance();
		
		List<User> users = MedicineTransferMsgDispatcher.instance().getUsersByRoleAndUnit("TRANSF_REC", transfer.getUnitTo(), true);
				
		MedicineTransferMsgDispatcher.instance().addComponent("transfer", transfer);
		MedicineTransferMsgDispatcher.instance().addComponent("list", getSources());
		
		MedicineTransferMsgDispatcher.instance().sendMessage(users, "/mail/newmedtransfer.xhtml");
	}
	
	/**
	 * Register the transfer receiving. When the transfer is received, the quantity received is included in the unit stock.
	 * @return
	 */
	@Transactional
	public String receiveTransfer() {
		if (!isCanReceive())
			return "denied";
		
		TransferUA transfer = getInstance();

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
		TransactionLogService log = getLogService();
		log.addTableRow(".unitFrom", transfer.getUnitFrom());
		log.addTableRow(".unitTo", transfer.getUnitTo());
		log.addTableRow(".receivingDate", transfer.getReceivingDate());
		log.save("TRANSF_REC", RoleAction.EXEC, transfer);

		return "received";
	}

	
	/**
	 * Check if the received data is correct
	 * @return
	 */
	protected boolean validateReceiving() {
		TransferUA transfer = getInstance();
		
		// check if the receiving date is not before the sending date
		Date dt = transfer.getReceivingDate();
		if (transfer.getShippingDate().after(dt)) {
			facesMessages.addFromResourceBundle("medicines.transfer.datebefore");
			return false;
		}
		
		//check limit date to receive
		if(!userSession.isCanGenerateMovements(transfer.getReceivingDate())){
			facesMessages.addToControlFromResourceBundle("edtdate", "meds.movs.errorlimitdate", DateUtils.formatAsLocale(userSession.getTbunit().getLimitDateMedicineMovement(), false));
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
		TransferUA transfer = getInstance();
		
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
		TransferUA transfer = getInstance();
		
		if (transfer.getStatus() != TransferStatus.WAITING_RECEIVING){
			facesMessages.addFromResourceBundle("meds.orders.cannotcancel");
			return "denied";
		}
		
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
		TransactionLogService log = getLogService();
		log.save("TRANSF_CANCEL", RoleAction.EXEC, transfer);
		
		facesMessages.addFromResourceBundle("meds.orders.cancelsuccess");
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
				si = transferHome.new SourceItem();
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
	

	public Map<Batch, Integer> getBatchesMap(TransferItem it, boolean shippedQtd) {
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
		TransferUA transfer = getInstance();
		
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
	
	public TBUnitSelection getUnitToSelection() {
		if (tbunitSelection.getSelected() == null){
			TransferUA transfer = getInstance();
			tbunitSelection.getAuselection().setSelectedUnit(transfer.getUnitTo().getAdminUnit());
			tbunitSelection.setSelected(transfer.getUnitTo());
		}
		return tbunitSelection;
	}
	
	/**
	 * Return total price by all medicines in movements by all sources
	 * */
	public double getTotal(){
		double res = 0;
		for (SourceItem si:sources){
			res += si.getTotalBySource();
		}
		return res;
	}

	/**
	 * @return the transferItem
	 */
	public TransferItem getTransferItem() {
		return transferItem;
	}

	/**
	 * @param transferItem the transferItem to set
	 */
	public void setTransferItem(TransferItem transferItem) {
		this.transferItem = transferItem;
	}
	
}
