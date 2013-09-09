package org.msh.tb.ua.medicines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.TransferBatch;
import org.msh.tb.entities.TransferItem;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.medicines.BatchSelection.BatchItem;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.movs.TransferHome;
import org.msh.tb.medicines.movs.TransferHome.SourceItem;
import org.msh.tb.ua.utils.MedicineCalculator;
import org.msh.utils.ItemSelect;

@Name("transferUAHome")
@Scope(ScopeType.CONVERSATION)
public class TransferUAHome {
	@In(create=true) TransferHome transferHome;
	@In(create=true) BatchSelection batchSelection;
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	
	private List<SourceItem> sourcesOld;
	private List<SourceItem> sources;
	private List<Movement> movementsOld = new ArrayList<Movement>(); 
	
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
		//List<TransferBatch> ltb = compareEditions();
		sources = transferHome.getSources(); //refresh sources
		for (SourceItem si:sources)
			for (TransferItem ti:si.getItems()){
				transferHome.initBatchSelection(ti);
				reCalcFromCurrentTransferBack(ti);
			}

		movementHome.initMovementRecording();
		//List<TransferItem> newTransItems = getNewTransferItems();
		//String ret = createNewMovements(newTransItems);
		//if ("error".equals(ret))
		//	return ret;
		
		//List<TransferItem> transItemsToremove = getTransferItemsToRemove();
		removeMovements(movementsOld);
		
		movementHome.savePreparedMovements();
		return transferHome.saveNewTransfer();
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



	/*private void removeMovements(List<TransferItem> transItemsToremove) {
		for (TransferItem it: transItemsToremove) {
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
	}*/



	/*private List<TransferItem> getTransferItemsToRemove() {
		List<TransferItem> lst = new ArrayList<TransferItem>();
		for (SourceItem si:sources){
			for (TransferItem ti:si.getItems())
				for (TransferBatch tb:ti.getBatches()){
					if (!findBatch(sourcesOld,tb.getBatch()))
						lst.add(ti);
				}
		}
		return lst;
	}*/


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



	/*private String createNewMovements(List<TransferItem> newTransItems) {
		if (newTransItems.isEmpty())
			return "";
		movementHome.initMovementRecording();
		boolean bCanTransfer = true;
		for (TransferItem it: newTransItems) {
			String comment = it.getTransfer().getUnitTo().getName().getDefaultName();
			
			Movement movOut = movementHome.prepareNewMovement(it.getTransfer().getShippingDate(), userSession.getTbunit(), it.getSource(), 
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
		
		return "";
	}
*/


	/*private List<TransferItem> getNewTransferItems() {
		List<TransferItem> lst = new ArrayList<TransferItem>();
		for (SourceItem si:sources){
			for (TransferItem ti:si.getItems()){
				 If ID of transfer batch is NULL that means, that there is new record. 
				 * If all IDs of transfer batches are NULLs that means, that is almost new transfer item
				 * 		and we must CREATE new movementOut for this items.
				 * If even one transfer batch have already existed before, that means, that 
				 * 		we must EDIT existing movementOut and create batch movements only for new transfer batch
				  
				boolean allNull = true;
				for (TransferBatch tb:ti.getBatches())
					if (tb.getId()!=null){
						allNull = false;
						break;
					}
				if (allNull)
					lst.add(ti);
			}
		}
		return lst;
	}
*/


	/*private List<TransferBatch> compareEditions() {
		List<TransferBatch> lst = new ArrayList<TransferBatch>();
		for (SourceItem sb: sources){
			for (TransferItem ti: sb.getItems())
				for (TransferBatch tb: ti.getBatches()){
					
					for (SourceItem sbo: sourcesOld)
						for (TransferItem tio: sb.getItems(){
							tio.get
						}
					
				}
			for (TransferBatch sbo: sourcesOld)
				if (tb.getId().equals(tbo.getId())){
					
				}
		}
		return null;
	}*/
	
	
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
		/*
		Iterator<TransferBatch> it = transferItem.getBatches().iterator();
		while (it.hasNext()){
			TransferBatch tb = it.next();
			Batch b = tb.getBatch();
			if (!sels.keySet().contains(b)){
				getEntityManager().remove(tb);
				it.remove();
			}
		}
		 */
		
		
		/*List<TransferBatch> lst = new ArrayList<TransferBatch>();
		for (TransferBatch tb: transferItem.getBatches()) {
			Batch b = tb.getBatch();
			if (sels.keySet().contains(b)){
				lst.add(tb);
			}
			else
			{
				for (BatchMovement bm: transferItem.getMovementOut().getBatches()){
					if (bm.getBatch().getId() == b.getId())
						getEntityManager().remove(bm);
				}
			}
		}*/
		
		
		/*for (TransferBatch tb: transferItem.getBatches())
			if (getEntityManager().contains(tb))
				getEntityManager().remove(tb);
		transferItem.getBatches().clear();*/
		//transferItem.setBatches(lst);
		
		// free memory space
		batchSelection.clear();
		transferHome.setTransferItem(null);
	}
	
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
}
