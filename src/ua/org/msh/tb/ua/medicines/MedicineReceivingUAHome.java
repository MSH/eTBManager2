package org.msh.tb.ua.medicines;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.MedicineSelection;
import org.msh.tb.medicines.SourceMedicineTree;
import org.msh.tb.medicines.SourceMedicineTree.MedicineNode;
import org.msh.tb.medicines.SourceMedicineTree.SourceNode;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.ua.entities.MedicineReceivingUA;
import org.msh.tb.ua.medicines.RegistrationNumbers.RegistrationCard;
import org.msh.tb.ua.utils.MedicineCalculator;



@Name("medicineReceivingUAHome")
@Scope(ScopeType.CONVERSATION)
@LogInfo(roleName="RECEIV", entityClass=MedicineReceivingUA.class)
public class MedicineReceivingUAHome extends EntityHomeEx<MedicineReceivingUA> {
	private static final long serialVersionUID = 28538032481597899L;

	@In(create=true) MovementHome movementHome;
	@In(create=true) MedicineSelection medicineSelection;
	@In(required=false) UserSession userSession;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) RegistrationNumbers registrationNumbers;
		
	private SourceMedicineTree<Batch> sourceTree;
	
	private Batch batch;
	private List<Movement> remMovs = new ArrayList<Movement>();
	private List<Batch> remBatches = new ArrayList<Batch>();
	private Medicine medicine;
	
	private int numContainers;
	private double containerPrice;
	private double totalPrice;
	
	private RegistrationCard registCard;
	private boolean newRegCard;

	@Factory("medicineReceivingUA")
	public MedicineReceivingUA getMedicineReceivingUA() {
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
		
		//delete empty medicine nodes
		Iterator<Object> it = getRoot().getMedicines().iterator();
		while (it.hasNext()){
			Object o = it.next();
			MedicineNode mn = (MedicineNode)o;
			if (mn.getBatches().size()==0)
				it.remove();
		}
		
		MedicineReceivingUA rec = getInstance();

		if (rec.getTbunit() == null)
			rec.setTbunit(userSession.getTbunit());

		// if movements can't be saved (because it may be an editing and the quantity was changed to a 
		// quantity that makes the stock negative) the system will just return an error message
		movementHome.initMovementRecording();
		if (!prepareMovements())
			return "error";
		
		double totalPrice = getGlobalTotal();
		rec.setTotalPrice(totalPrice);

		// register log
		getLogDetailWriter().addTableRow("Source", rec.getSource());
		getLogDetailWriter().addTableRow(".receivingDate", rec.getReceivingDate());
		getLogDetailWriter().addTableRow("global.totalPrice", totalPrice);

		if (isManaged())
			getLogService().recordEntityState(getInstance(), Operation.EDIT);

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
	public boolean validateDrugReceiving() {
		Tbunit unit = userSession.getTbunit();

		if (getInstance().getReceivingDate().before( unit.getMedManStartDate()) ) {
			facesMessages.addFromResourceBundle("meds.movs.datebefore", unit.getMedManStartDate());
			return false;
		}

		SourceNode sourceNode = getRoot();
		
		if (sourceNode.getMedicines().size() == 0) {
			facesMessages.addFromResourceBundle("meds.receiving.nobatch");
			return false;
		}
		
		boolean res = false;
		for (Object o: sourceNode.getMedicines()){
			MedicineNode mn = (MedicineNode)o;
			if (mn.getBatches().size()>0)
				res = true;
		}
		
		if (!res)
			facesMessages.addFromResourceBundle("meds.receiving.nobatch");
		
		return res;
	}


	/**
	 * Prepara o novo recebimento para ser salvo. Grava novos movimentos 
	 */
	public boolean prepareMovements() {
		MedicineReceivingUA rec = getInstance();
		
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
		newRegCard = true;
	}


	/**
	 * Finish the selection of the medicines to be included in the order
	 */
	public void finishMedicineSelection() {
		MedicineSelection medSel = (MedicineSelection)Component.getInstance("medicineSelection", true);
		
		List<Medicine> meds = medSel.getSelectedMedicines();

		for (Medicine med: meds) {
			// this method automatically includes a new medicine if the medicine is not already in the list
			sourceTree.addMedicine(getRoot().getSource(), med);
		}
		
	}
	
	/**
	 * Finishing editing of a batch (new or existing one)
	 */
	public void finishBatchEditing() {
		if (!isNewRegCard()){
			if (getRegistCard()==null) registCard = new RegistrationCard();
			batch.setRegistCardNumber(registCard.getNumber());
			batch.setRegistCardBeginDate(registCard.getIniDate());
			batch.setRegistCardEndDate(registCard.getEndDate());
		}
		batch.setUnitPrice(MedicineCalculator.calculateUnitPrice(containerPrice, batch.getQuantityContainer()));
		
		MedicineNode node = sourceTree.addMedicine(getRoot().getSource(), batch.getMedicine());
		
		if (node.getItem() == null)
			node.setItem(new MedicineInfo(node, null));

		// is new ?
		if (!node.getBatches().contains(batch)) {
			node.getBatches().add(batch);
		}
		// clean all components for batch
		cleanAll();
	}


	/**
	 * 
	 */
	public void cleanAll() {
		batch = null;
		numContainers = 0;
		containerPrice = 0;
		totalPrice = 0;
		newRegCard = false;
		registCard = new RegistrationCard();
	}

	public void verifyBatch(){
		if(batch != null && batch.getBatchNumber() != null && 
					!batch.getBatchNumber().equals("") && batch.getMedicine() != null){	
		
			ArrayList<Batch> b = (ArrayList<Batch>) getEntityManager().createQuery("from Batch b " +
																			  "where b.batchNumber = :batchNumber and " +
																			  "b.manufacturer = :manufacturer and " +
																			  "b.medicine.id = :medicineId")
																				.setParameter("batchNumber", batch.getBatchNumber())
																				.setParameter("manufacturer", batch.getManufacturer())
																				.setParameter("medicineId", batch.getMedicine().getId())
																				.getResultList();

			if(b!=null && b.size() > 0){
				batch = b.get(0);
				batch.setQuantityReceived(0);
			}else{
				Batch ba = new Batch();
				ba.setMedicine(batch.getMedicine());
				ba.setManufacturer(batch.getManufacturer());
				ba.setBatchNumber(batch.getBatchNumber());
				batch = ba;
			}
		}
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
		String hql = "select count(*) from BatchQuantity where batch.id in (select bm.batch.id from MedicineReceiving mr " +
				"join mr.movements m join m.batches bm " +
				"where mr.id = :id) and quantity < batch.quantityReceived";

		Long num = (Long)getEntityManager().createQuery(hql)
				.setParameter("id", getInstance().getId())
				.getSingleResult();

		if ((num != null) &&(num > 0)) {
			facesMessages.addFromResourceBundle("meds.receiving.removeerror");
			return "error";
		}
		
		// remove os movimentos do recebimento
		movementHome.initMovementRecording();
		for (Movement mov: getInstance().getMovements()) {
			movementHome.prepareMovementsToRemove(mov);
		}
		movementHome.savePreparedMovements();
		
		return super.remove();
	}
	

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(Batch batch) {
		this.batch = batch;
	}

	public int getNumContainers() {
		if (numContainers == 0)
			numContainers = MedicineCalculator.calculateNumContainers(batch.getQuantityContainer(), batch.getQuantityReceived());
		return numContainers;
	}
	
	public double getContainerPrice() {
		if (containerPrice == 0)
			containerPrice = MedicineCalculator.calculateContPrice(batch.getQuantityContainer(), batch.getUnitPrice());
		return containerPrice;
	}
	
	public double getTotalPrice() {
		if (totalPrice == 0)
			totalPrice = MedicineCalculator.calculateTotalPrice(batch.getQuantityReceived(),batch.getQuantityContainer(), batch.getUnitPrice(),3);
		return totalPrice;
	}
	
	public void setNumContainers(int numContainers) {
		this.numContainers = numContainers;
	}
	
	public void setContainerPrice(double containerPrice) {
		this.containerPrice = containerPrice;
	}
	
	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}
	

	
	public void setId(Object id) {
		if ((id != null) && (getId() != null) && (getId().equals(id)))
			return;
		
		super.setId(id);

		if (isManaged())
			sourceTree = null;
	}
	
	public void setRegistCard(RegistrationCard registCard) {
		this.registCard = registCard;
		registrationNumbers.setSelected(registCard);
		if (batch!=null)
		if (registCard!=null){
				batch.setRegistCardNumber(registCard.getNumber());
				batch.setRegistCardBeginDate(registCard.getIniDate());
				batch.setRegistCardEndDate(registCard.getEndDate());
			}
		else{
			batch.setRegistCardNumber(null);
			batch.setRegistCardBeginDate(null);
			batch.setRegistCardEndDate(null);
		}	
	}

	/*public void setRegistCardInd(Integer registCardind) {
		if(registCardind!=null)
		if (!registrationNumbers.getResultList().isEmpty())
			setRegistCard((RegistrationCard)registrationNumbers.getResultList().get(registCardind).getValue());
	}
	
	public Integer getRegistCardInd(){
		if (registCard!=null){
			for (SelectItem it:registrationNumbers.getResultList()){
				RegistrationCard rc = (RegistrationCard)it.getValue();
				if (rc.getNumber().equals(registCard.getNumber())){
					return registrationNumbers.getResultList().indexOf(it);
				}
			}
		}
		return 0;
	}
	
	public RegistrationCard getRegistCardExist() {
		if(!newRegCard)
			if (registCard==null || registCard.getNumber()==null){
				for (SelectItem it:registrationNumbers.getResultList()){
					RegistrationCard rc = (RegistrationCard)it.getValue();
					if (rc.getNumber().equals(batch.getRegistCardNumber())){
						setRegistCard(rc);
						break;
					}
				}
		}
		return registCard;
	}*/
	
	
	
	public RegistrationCard getRegistCard() {
		if ((registCard==null || registCard.getNumber()==null) && batch.getRegistCardNumber()!=null){
			registCard = new RegistrationCard(batch.getRegistCardNumber(), batch.getRegistCardBeginDate(), batch.getRegistCardEndDate());
			registrationNumbers.setSelected(registCard);
		}
		
		if(!newRegCard)
			registCard = registrationNumbers.getSelected();
		return registCard;
	}
	
	public void setNewRegCard(boolean newRegCard) {
		this.newRegCard = newRegCard;
		if (newRegCard){
			registCard = new RegistrationCard();
		}
	}

	public boolean isNewRegCard() {
		return newRegCard;
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
		public double getTotalPrice() {
			return MedicineCalculator.calculateTotalPrice(node.getBatches(),3);
		}
		
		public double getUnitPrice() {
			return MedicineCalculator.calculateUnitPriceAvg(getTotalPrice(), getQuantity());
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


	/**
	 * @return the sourceTree
	 */
	public SourceMedicineTree<Batch> getSourceTree() {
		return sourceTree;
	}


	/**
	 * @return the remBatches
	 */
	public List<Batch> getRemBatches() {
		return remBatches;
	}


	/**
	 * @return the remMovs
	 */
	public List<Movement> getRemMovs() {
		return remMovs;
	}

	public double getGlobalTotal() {
		BigDecimal res = new BigDecimal(0);
		for (MedicineNode mn:getMedicines()){
			BigDecimal d = new BigDecimal(MedicineCalculator.calculateTotalPrice(mn.getBatches(),3));
			d = d.setScale(2,RoundingMode.HALF_UP);
			res = res.add(d);
		}
		return res.doubleValue();
	}
}
