package org.msh.tb.ua.medicines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.App;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Transfer;
import org.msh.tb.entities.TransferBatch;
import org.msh.tb.entities.TransferItem;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.entities.enums.TransferStatus;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.medicines.BatchSelection.BatchItem;
import org.msh.tb.medicines.movs.BatchMovementsQuery;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.movs.TransferHome;
import org.msh.tb.medicines.movs.TransferHome.SourceItem;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.tb.ua.utils.MedicineCalculator;
import org.msh.utils.ItemSelect;
import org.msh.utils.date.DateUtils;

@Name("transferUAHome")
@Scope(ScopeType.CONVERSATION)
public class TransferUAHome {
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
	
	/**
	 * Remember some data from base transfer.
	 * Recalculate batchSelection for edit
	 * */
	public void initialize(){
		if (transferHome.getInstance().getId()!=null){
			if (sources == null){
				sources = transferHome.getSources();
				sourcesOld = new ArrayList<TransferHome.SourceItem>();
				for (SourceItem si:sources){
					sourcesOld.add(si.copyBatches());
					for (TransferItem ti:si.getItems()){
						movementsOld.add(ti.getMovementOut());
						transferHome.initBatchSelection(ti);
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
		
		sources = transferHome.getSources(); //refresh sources
		for (SourceItem si:sources)
			for (TransferItem ti:si.getItems()){
				transferHome.initBatchSelection(ti);
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
		
		Transfer transfer = transferHome.getInstance();
		transfer.setUnitTo(transferHome.getTbunitSelection().getSelected());
		Tbunit unit = userSession.getTbunit();
		transfer.setUnitFrom(unit);
		transfer.setStatus(TransferStatus.WAITING_RECEIVING);
		transfer.setUserFrom(transferHome.getUser());
		
		Date dt = transfer.getShippingDate();

		// creates out movements
		movementHome.initMovementRecording();
		boolean bCanTransfer = true;
		for (TransferItem it: transfer.getItems()) 
			if (!it.getBatches().isEmpty())	{
				String comment = transfer.getUnitTo().getName().getDefaultName();
				
				Movement movOut = movementHome.prepareNewMovement(dt, unit, it.getSource(), 
						it.getMedicine(), MovementType.TRANSFEROUT, 
						transferHome.getBatchesMap(it, true), comment);
	
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
		TransactionLogService log = transferHome.getLogService();
		log.addTableRow(".unitFrom", transfer.getUnitFrom());
		log.addTableRow(".unitTo", transfer.getUnitTo());
		log.addTableRow(".shippingDate", transfer.getShippingDate());
		log.save("NEW_TRANSFER", RoleAction.EXEC, transfer);
		
		Events.instance().raiseEvent("medicine-new-transfer");
		
		//post-deleting empty transfer item.  because of pre-deleting call the exception
		Iterator<TransferItem> it2 = transferHome.getInstance().getItems().iterator();
		while (it2.hasNext()){
			TransferItem ti = it2.next();
			if (ti.getBatches().isEmpty())
				App.getEntityManager().createQuery("delete from TransferItem ti where ti.id="+ti.getId()).executeUpdate();
		}
		
		return transferHome.persist();

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
		boolean wasAllStock = true;
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
		}
/*		for (TransferBatch tb:ti.getBatches()){
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
				bq.setQuantity(ti.getQuantity());
				bq.setSource(ti.getSource());	
				bi.setBatchQuantity(bq);
				bq.setTbunit((Tbunit)App.getComponent("selectedUnit"));
				App.getEntityManager().persist(bq);
				bi.setBatch(tb.getBatch());
				ItemSelect<BatchItem> item = new ItemSelect<BatchSelection.BatchItem>();
				item.setItem(bi);
				//item.getItem().getBatchQuantity().setQuantity(ti.getQuantity());
				item.getItem().setQuantity(ti.getQuantity());
				item.setSelected(true);
				batchSelection.getItems().add(item);
			}
		}*/
	}
	
	/**
	 * Return ItemSelect from BatchSelection, which contains necessary Batch 
	 * */
/*	private ItemSelect<BatchItem> findBatch(BatchSelection bs, Batch b) {
		for (ItemSelect<BatchItem> is: bs.getItems())
			if (is.getItem().getBatch().getId().intValue() == b.getId().intValue()){
				return is;
			}
		return null;
	}
*/
	
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
			TransferBatch tb = transferHome.getTransferItem().findByBatch(b);
			if (tb == null){
				tb = new TransferBatch();
				tb.setBatch(b);
				tb.setQuantity(sels.get(b));
				tb.setTransferItem(transferHome.getTransferItem());
				transferHome.getTransferItem().getBatches().add(tb);
			}
			else{
				tb.setQuantity(sels.get(b));
			}
		}
		
		Iterator<TransferBatch> it = transferHome.getTransferItem().getBatches().iterator();
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
		transferHome.setTransferItem(null);
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
		for (SourceItem s:transferHome.getSources())
			for (TransferItem ti:s.getItems())
					res += MedicineCalculator.calculateTotalPrice(ti.getBatches(),5);
		return res;
	}
	
	public double getGlobalTotalReceived() {
		double res = 0;
		for (SourceItem s:transferHome.getSources())
			for (TransferItem ti:s.getItems())
					res += MedicineCalculator.calculateTotalPriceReceived(ti.getBatches(),5);
		return res;
	}
	
	private boolean validateData(){
		boolean res = true;
		if(!userSession.isCanGenerateMovements(transferHome.getTransfer().getShippingDate())){
			FacesMessages.instance().addToControlFromResourceBundle("edtdate", "meds.movs.errorlimitdate", DateUtils.formatAsLocale(userSession.getTbunit().getLimitDateMedicineMovement(), false));
			res = false;
		}
		
		// checks if any medicine were selected for transfer
		if (transferHome.getTransfer().getItems().size() == 0) {
			FacesMessages.instance().addFromResourceBundle("edtrec.nomedicine");
			res = false;
		}

		// checks if any batch were selected for transfer
		int totSize = 0;
		for (TransferItem it: transferHome.getTransfer().getItems()) {
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
}
