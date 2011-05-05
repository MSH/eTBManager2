package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineReceiving;
import org.msh.tb.entities.MedicineReceivingItem;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.log.LogInfo;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.movs.MovementHome;


@Name("medicineReceivingHome")
@LogInfo(roleName="RECEIV")
public class MedicineReceivingHome extends EntityHomeEx<MedicineReceiving> {
	private static final long serialVersionUID = -3792901040950537438L;

	@In(create=true) MovementHome movementHome;
	@In(create=true) MedicineSelection medicineSelection;
	@In(required=false) UserSession userSession;
	@In(create=true) FacesMessages facesMessages;
	
	private MedicineReceivingItem item;
	private Movement movement;
	private Batch batch;
	private Map<MedicineReceivingItem, Movement> movs = new HashMap<MedicineReceivingItem, Movement>();
	private List<MedicineReceivingItem> remMovs = new ArrayList<MedicineReceivingItem>();
	
	@Factory("medicineReceiving")
	public MedicineReceiving getMedicineReceiving() {
		return getInstance();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#persist()
	 */
	@Override
	public String persist() {		
		if (!validateDrugReceiving())
			return "error";
		
		MedicineReceiving rec = getInstance();

		if (rec.getTbunit() == null)
			rec.setTbunit(userSession.getTbunit());

		// if movements can't be saved (because it may be an editing and the quantity was changed to a 
		// quantity that makes the stock negative) the system will just return an error message
		movs.clear();
		movementHome.initMovementRecording();
		if (!prepareMovements())
			return "error";

		// register log
		getLogService().addValue("Source", getInstance().getSource());
		getLogService().addValue(".receivingDate", getInstance().getReceivingDate());
		getLogService().addValue("global.totalPrice", getInstance().getTotalPrice());

		if (isManaged()) 
			getLogService().startEntityMonitoring(getInstance(), false);

		// clear all movements, because the batch must be saved before movement is created,
		// and to save the batch, the MedicineReceiving must be saved 
		for (MedicineReceivingItem it: getInstance().getMedicines())
			it.setMovement(null);

		// save instance
		getEntityManager().persist(getInstance());
		getEntityManager().flush();

		// now the movements will be saved and stock position will be updated
		movementHome.savePreparedMovements();
		
		for (MedicineReceivingItem it: movs.keySet()) {
			it.setMovement( movs.get(it) );
			getEntityManager().persist( it );
		}
		
		return super.persist();
	}


	/**
	 * Valida os dados do recebimento de medicamento
	 * @return
	 */
	private boolean validateDrugReceiving() {
		Tbunit unit = userSession.getTbunit();
		
		if (getInstance().getReceivingDate().before( unit.getMedManStartDate()) ) {
			facesMessages.addFromResourceBundle("medicines.movs.datebefore", unit.getMedManStartDate());
			return false;
		}
		
		boolean res = true;
		
		MedicineReceiving rec = getInstance();
		for (MedicineReceivingItem it: rec.getMedicines()) {
			if (it.getBatches().size() == 0) {
				facesMessages.addFromResourceBundle("edtrec.nobatch", it.getMovement().getMedicine().getGenericName());
				res = false;
			}
		}
		return res;
	}


	/**
	 * Prepara o novo recebimento para ser salvo. Grava novos movimentos 
	 */
	private boolean prepareMovements() {
		MedicineReceiving rec = getInstance();
		MovementType type = MovementType.DRUGRECEIVING;
		
		Source source = rec.getSource();
		Tbunit unit = rec.getTbunit();
		Date date = rec.getReceivingDate();
		

		//  prepare to remove movements of medicines removed by the user
		for (MedicineReceivingItem it: remMovs) {
			if (it.getMovement() != null)
				movementHome.prepareMovementsToRemove(it.getMovement());
		}

		// update current movements
		for (MedicineReceivingItem it: getInstance().getMedicines()) {
			Movement mov = it.getMovement();
			Medicine medicine = mov.getMedicine();

			// remove movements previously saved (if it's an editing of an existing receiving) 
			if (mov.getId() != null)
				movementHome.prepareMovementsToRemove(mov);

			// mount batch list
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
			for (Batch b: it.getBatches()) {
				batches.put(b, b.getQuantityReceived());
			}
			
			// prepare movement to be saved
			mov = movementHome.prepareNewMovement(date, unit, source, medicine, type, batches, null);
			if (mov == null) {
				facesMessages.add(movementHome.getErrorMessage());
				return false;
			}
			// link the new movement to the item
			movs.put(it, mov);
		}

		return true;
	}


	/**
	 * @return Item do recebimento de medicamento a ser editado ou incluído 
	 */
	public MedicineReceivingItem getItem() {
		return item;
	}


	/**
	 * Remove a medicine already included in the receiving
	 * @param item
	 */
	public void removeItem(MedicineReceivingItem item) {
		getInstance().getMedicines().remove(item);

		// verifica se o movimento deve ser excluído
		Movement mov = item.getMovement();
		if (getEntityManager().contains(mov)) {
			remMovs.add(item);
		}
	}


	/**
	 * Start editing of a new batch for a specific medicine in the receiving
	 * @param item
	 */
	public void newBatch(MedicineReceivingItem item) {
		this.item = item;
		batch = new Batch();
		batch.setMedicineReceivingItem(item);
		batch.setMedicine(item.getMovement().getMedicine());
	}


	public void addBatch() {
		if (!item.getBatches().contains(batch))
			item.getBatches().add(batch);

		batch.setMedicineReceivingItem(item);
	}


	public void editBatch(Batch batch) {
		this.batch = batch;
		item = batch.getMedicineReceivingItem();
	}


	/**
	 * Remove a batch from the receiving
	 * @param batch
	 */
	public void removeBatch(Batch batch) {
		item = batch.getMedicineReceivingItem();
		item.getBatches().remove(batch);
		
		// is managed ?
		if (batch.getId() != null)
			getEntityManager().remove(batch);
	}


	public Movement getMovement() {
		if (movement == null)
			movement = new Movement();
		return movement;
	}


	public Batch getBatch() {
		if (batch == null)
			batch = new Batch();
	
		return batch;
	}
	
	public int getItensCount() {
		return getMedicineReceiving().getMedicines().size();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#remove()
	 */
	@Override
	public String remove() {
		// check if there are batches already in use
		Long num = (Long)getEntityManager().createQuery("select count(*) from BatchQuantity b " + 
				"where b.batch.medicineReceivingItem.medicineReceiving.id = :id " +
				"and b.quantity < b.batch.quantityReceived")
				.setParameter("id", getInstance().getId())
				.getSingleResult();
		if ((num != null) &&(num > 0)) {
			facesMessages.addFromResourceBundle("medicines.medicinerecs.removeerror");
			return "error";
		}
		
		// remove os movimentos do recebimento
		for (MedicineReceivingItem it: getInstance().getMedicines()) {
			Movement mov = it.getMovement();
			it.setMovement(null);
			if (mov != null)
				movementHome.removeMovement(mov);
		}
		
		return super.remove();
	}
	
	/**
	 * avoid displaying medicines already selected 
	 */
	public void filterMedicines() {
		List<Medicine> lst = new ArrayList<Medicine>();

		for (MedicineReceivingItem it: getInstance().getMedicines()) 
			lst.add(it.getMovement().getMedicine());
		medicineSelection.applyFilter(lst);
	}


	/**
	 * Include in the receiving the medicines selected by the user
	 */
	public void addMedicines() {
		List<Medicine> lst =  medicineSelection.getSelectedMedicines();
		for (Medicine m: lst) {
			item = new MedicineReceivingItem();
			Movement mov = new Movement();
			mov.setMedicine(m);
			item.setMovement(mov);
			getInstance().getMedicines().add(item);
			item.setMedicineReceiving(getInstance());
		}
	}
}
