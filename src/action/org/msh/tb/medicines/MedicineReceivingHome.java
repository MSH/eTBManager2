package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineReceiving;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.log.LogInfo;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.SourceMedicineTree.MedicineNode;
import org.msh.tb.medicines.SourceMedicineTree.SourceNode;
import org.msh.tb.medicines.movs.MovementHome;


@Name("medicineReceivingHome")
@LogInfo(roleName="RECEIV")
public class MedicineReceivingHome extends EntityHomeEx<MedicineReceiving> {
	private static final long serialVersionUID = -3792901040950537438L;

	@In(create=true) MovementHome movementHome;
	@In(create=true) MedicineSelection medicineSelection;
	@In(required=false) UserSession userSession;
	@In(create=true) FacesMessages facesMessages;

	private SourceMedicineTree<Batch> sourceTree;
	
	private Batch batch;
	private List<Movement> remMovs = new ArrayList<Movement>();
	private List<Batch> remBatches = new ArrayList<Batch>();
	private Medicine medicine;
	

	@Factory("medicineReceiving")
	public MedicineReceiving getMedicineReceiving() {
		return getInstance();
	}

	
	/**
	 * Return list of medicine nodes
	 * @return
	 */
	public SourceNode getRoot() {
		if (sourceTree == null) {
			createSourceTree();
		}
		return sourceTree.getSources().get(0);
	}

	
	/**
	 * Create table of medicines and batches
	 */
	private void createSourceTree() {
		sourceTree = new SourceMedicineTree<Batch>();
		// add an empty source just to make it the root of the tree
		Source source = new Source();
		sourceTree.addSource(source);

		List<Movement> movs = getInstance().getMovements();
		for (Movement mov: movs) {
			MedicineNode node = sourceTree.addMedicine(source, mov.getMedicine());
			node.setItem(new MedicineInfo(node, mov));
			for (BatchMovement bm: mov.getBatches()) {
				Batch b = bm.getBatch();
				sourceTree.addItem(source, mov.getMedicine(), b);
			}
		}
	}

	/**
	 * Return the list of medicines
	 * @return
	 */
	public List<MedicineNode> getMedicines() {
		return (List<MedicineNode>)getRoot().getMedicines();
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
		movementHome.initMovementRecording();
		if (!prepareMovements())
			return "error";
		
		float totalPrice = 0;
		for (Batch b: sourceTree.getItems())
				totalPrice += b.getTotalPrice();
		rec.setTotalPrice(totalPrice);

		// register log
		getLogService().addValue("Source", rec.getSource());
		getLogService().addValue(".receivingDate", rec.getReceivingDate());
		getLogService().addValue("global.totalPrice", rec.getTotalPrice());

		if (isManaged()) 
			getLogService().startEntityMonitoring(getInstance(), false);

		// save all batches
		sourceTree.traverse(new SourceMedicineTree.ItemTraversing<Batch>() {
			public void traverse(Source source, Medicine medicine, Batch item) {
				getEntityManager().persist(item);
			}
		});

		// save all movements and update stock position
		movementHome.savePreparedMovements();
		
		for (Batch batch: remBatches) {
			getEntityManager().remove(batch);
		}

		// save receiving
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

		SourceNode sourceNode = getRoot();
		
		if (sourceNode.getMedicines().size() == 0) {
			facesMessages.addFromResourceBundle("meds.receiving.nobatch");
			return false;
		}
		
		return true;
	}


	/**
	 * Prepara o novo recebimento para ser salvo. Grava novos movimentos 
	 */
	private boolean prepareMovements() {
		MedicineReceiving rec = getInstance();
		
		//  prepare to remove movements of medicines removed by the user
		for (Movement mov: remMovs) {
			movementHome.prepareMovementsToRemove(mov);
		}
		
		SourceNode node = getRoot();
		rec.getMovements().clear();

		// update current movements
		for (Object obj: node.getMedicines()) {
			MedicineNode medNode = (MedicineNode)obj;
			MedicineInfo medInfo = (MedicineInfo)medNode.getItem();

			Movement mov = medInfo.getMovement();

			// remove movements previously saved (if it's an editing of an existing receiving) 
			if (mov != null)
				movementHome.prepareMovementsToRemove(mov);

			// mount batch list
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
			for (Object aux: medNode.getBatches()) {
				Batch b = (Batch)aux;
				batches.put(b, b.getQuantityReceived());
			}
			
			// prepare movement to be saved
			mov = movementHome.prepareNewMovement(rec.getReceivingDate(),
					rec.getTbunit(), 
					rec.getSource(), 
					medNode.getMedicine(), 
					MovementType.DRUGRECEIVING, 
					batches, null);

			if (mov == null) {
				facesMessages.add(movementHome.getErrorMessage());
				return false;
			}

			// link the new movement to the item
			rec.getMovements().add(mov);
		}

		return true;
	}



	/**
	 * Start editing of a new batch for a specific medicine in the receiving
	 * @param item
	 */
	public void startNewBatch(Medicine med) {
		batch = new Batch();
		
		if (med != null)
			batch.setMedicine(med);
	}


	/**
	 * Start the editing of a batch 
	 * @param batch
	 */
	public void startBatchEditing(Batch batch) {
		this.batch = batch;
	}


	/**
	 * Finishing editing of a batch (new or existing one)
	 */
	public void finishBatchEditing() {
		MedicineNode node = sourceTree.addMedicine(getRoot().getSource(), batch.getMedicine());
		
		if (node.getItem() == null)
			node.setItem(new MedicineInfo(node, null));

		// is new ?
		if (!node.getBatches().contains(batch)) {
			node.getBatches().add(batch);
		}
		
		batch = null;
	}


	/**
	 * Check if batch is being edited
	 * @return true if is being edited, or false if not editing or if editing is over
	 */
	public boolean isEditingBatch() {
		return (batch != null);
	}


	/**
	 * Remove a batch from the receiving
	 * @param batch
	 */
	public void removeBatch(Batch batch) {
		MedicineNode node = getRoot().nodeByMedicine(batch.getMedicine());
		node.getBatches().remove(batch);

		if (node.getBatches().size() == 0)
			getRoot().getMedicines().remove(node);
		
		if (getEntityManager().contains(batch))
			remBatches.add(batch);
	}



	/**
	 * Return the batch being edited 
	 * @return instance of the {@link Batch} class
	 */
	public Batch getBatch() {
		if (batch == null)
			batch = new Batch();
	
		return batch;
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
		for (Movement mov: getInstance().getMovements()) {
			movementHome.removeMovement(mov);
		}
		
		return super.remove();
	}
	

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(Batch batch) {
		this.batch = batch;
	}

	
	public void setId(Object id) {
		if ((id != null) && (getId() != null) && (getId().equals(id)))
			return;
		
		super.setId(id);

		if (isManaged())
			sourceTree = null;
	}

	/**
	 * Display information about the medicine and its receiving data
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineInfo {
		private MedicineNode node;
		private Movement movement;

		public MedicineInfo(MedicineNode node, Movement mov) {
			this.node = node;
			this.movement = mov;
		}

		/**
		 * Return the quantity
		 * @return
		 */
		public int getQuantity() {
			int tot = 0;
			for (Object item: node.getBatches()) {
				tot += ((Batch)item).getQuantityReceived();
			}
			return tot;
		}

		/**
		 * Return the total price
		 * @return
		 */
		public float getTotalPrice() {
			float tot = 0;
			for (Object item: node.getBatches()) {
				tot += ((Batch)item).getTotalPrice();
			}
			return tot;
		}
		
		public float getUnitPrice() {
			float tot = getQuantity();
			return (tot != 0? getTotalPrice()/tot : 0);
		}
		
		/**
		 * @return the movement
		 */
		public Movement getMovement() {
			return movement;
		}

		/**
		 * @param movement the movement to set
		 */
		public void setMovement(Movement movement) {
			this.movement = movement;
		}
	}

	/**
	 * @return the medicine
	 */
	public Medicine getMedicine() {
		return medicine;
	}


	/**
	 * @param medicine the medicine to set
	 */
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}
}
