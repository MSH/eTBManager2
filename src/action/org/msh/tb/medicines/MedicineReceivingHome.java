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
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.MedicineReceiving;
import org.msh.mdrtb.entities.MedicineReceivingItem;
import org.msh.mdrtb.entities.Movement;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.tb.EntityHomeEx;
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
	
	@Override
	public String persist() {		
		if (!validateDrugReceiving())
			return "error";
		
		MedicineReceiving rec = getInstance();
		if (rec.getTbunit() == null)
			rec.setTbunit(userSession.getTbunit());
		
		movs.clear();
		
		for (MedicineReceivingItem it: rec.getMedicines()) {
			// remove movement, if exists
			Movement mov = it.getMovement();
			movs.put(it, mov);
			it.setMovement(null);
		}
		
		// exclui os movimentos (melhor opção custo x benefício, pois exigiria
		// uma grande quantidade de código para saber se mudou data ou drug storage ou
		// source ou quantidade
		for (MedicineReceivingItem it: movs.keySet()) {
			Movement mov = movs.get(it);
			if (mov.getId() != null) {
				movementHome.removeMovement(mov);
			}
		}

		// remove movimentos excluídos
		for (MedicineReceivingItem it: remMovs) {
			Movement mov = it.getMovement();
			it.setMovement(null);
			movementHome.removeMovement(mov);
		}
		
		getLogService().addValue("Source", getInstance().getSource());
		getLogService().addValue(".receivingDate", getInstance().getReceivingDate());
		getLogService().addValue("global.totalPrice", getInstance().getTotalPrice());

		if (isManaged()) {
			getLogService().startEntityMonitoring(getInstance(), false);
		}
		
		getEntityManager().persist(getInstance());
		getEntityManager().flush();
		saveMovements();
		
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
	private void saveMovements() {
		MedicineReceiving rec = getInstance();
		MovementType type = MovementType.DRUGRECEIVING;
		
		Source source = rec.getSource();
		Tbunit unit = rec.getTbunit();
		Date date = rec.getReceivingDate();
		
		movementHome.initMovementRecording();
		for (MedicineReceivingItem it: movs.keySet()) {
			Movement aux = movs.get(it);
			Medicine medicine = aux.getMedicine();

			// mount batch list
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
			for (Batch b: it.getBatches()) {
				batches.put(b, b.getQuantityReceived());
			}
			Movement mov = movementHome.prepareNewMovement(date, unit, source, medicine, type, batches, null); 
			it.setMovement(mov);
		}
		movementHome.savePreparedMovements();
	}
	
	/**
	 * @return Item do recebimento de medicamento a ser editado ou incluído 
	 */
	public MedicineReceivingItem getItem() {
		return item;
	}
	
	public void addItem() {
		// verifica se medicamento já não foi informado 
		MedicineReceiving rec = getInstance();
		for (MedicineReceivingItem it: rec.getMedicines()) {
			if (it.getMovement().getMedicine() == movement.getMedicine()) {
				facesMessages.addFromResourceBundle("edtrec.uniquedrug", movement.getMedicine().getGenericName());
				return;
			}
		}
		
		item = new MedicineReceivingItem();
		item.setMovement(movement);
		getInstance().getMedicines().add(item);
		item.setMedicineReceiving(getInstance());
		// limpa referência do movimento para próximo produto
		movement = null;
	}

	public void removeItem(MedicineReceivingItem item) {
		getInstance().getMedicines().remove(item);

		// verifica se o movimento deve ser excluído
		Movement mov = item.getMovement();
		if (getEntityManager().contains(mov)) {
			remMovs.add(item);
		}
	}
	
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
	
	public void removeBatch(Batch batch) {
		item = batch.getMedicineReceivingItem();
		item.getBatches().remove(batch);
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
	 * Includes selected medicines
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
