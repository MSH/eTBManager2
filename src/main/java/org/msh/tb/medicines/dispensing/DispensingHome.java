package org.msh.tb.medicines.dispensing;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.medicines.MedicineGroup;
import org.msh.tb.medicines.movs.MovementHome;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * Standard API to register medicine dispensing (by patient or consolidated by unit).
 * <p/>
 *  It's a statefull component, but executed in a single request.
 * @author Ricardo Memoria
 *
 */
@Name("dispensingHome")
public class DispensingHome extends EntityHomeEx<MedicineDispensing> {
	private static final long serialVersionUID = 1820669849645381520L;

	private List<MedicineItem> medicines;
	private List<MedicineDispensingCase> patients;
	private Map<MedicineItem, String> errorMessages;
	private Map<BatchItem, String> batchErrorMessages;
	private List<TbCase> cases;
	
	@In EntityManager entityManager;

	// interface to traverse errors during dispensing saving
	public interface ErrorTraverser {
		void traverse(Source source, Medicine medicine, Batch batch, String errorMessage);
	}


	/**
	 * Initialize medicine dispensing for a specific unit
	 * @param unit
	 */
	public void initialize(Tbunit unit) {
		clearInstance();
		getInstance().setTbunit(unit);
		medicines = null;
		patients = null;
	}


	/**
	 * Initialize medicine dispensing for editing of an existing {@link MedicineDispensing} instance by its id
	 * @param id
	 */
	public void initEditing(Integer id) {
		setId(id);
		medicines = null;
		patients = null;
	}



	/**
	 * Return the instance of {@link MedicineDispensing} class being edited
	 * @return managed instance of {@link MedicineDispensing} being edited, or null if it's a new dispensing
	 */
	public MedicineDispensing getMedicineDispensing() {
		return getInstance();
	}

	/**
	 * Include a new dispensing done for a batch and source with the given quantity.
	 * The dispensing is saved when the method 'saveDispensing()' is called
	 * @param batch
	 * @param source
	 * @param quantity
	 */
	public void addDispensing(Batch batch, Source source, int quantity) {
		if (getInstance().getTbunit().isPatientDispensing())
			throw new RuntimeException("Method addDispensing cannot be directly called when dispensing is done by patient. Call addPatientDispensing instead");

		addBatchQuantity(batch, source, quantity);
	}


	/**
	 * Include a new dispensing done for a specific patient. This information is kept in memory and saved 
	 * when the method 'saveDispensing()' is called 
	 * @param tbcase
	 * @param batch
	 * @param source
	 * @param quantity
	 */
	public void addPatientDispensing(TbCase tbcase, Batch batch, Source source, int quantity) {
		if (!getInstance().getTbunit().isPatientDispensing())
			throw new RuntimeException("Method addDispensing cannot be directly called when dispensing is done by patient. Call addPatientDispensing instead");

		addBatchQuantity(batch, source, quantity);

		if (patients == null) {
			patients = new ArrayList<MedicineDispensingCase>();
			cases = new ArrayList<TbCase>();
		}
		
		MedicineDispensingCase pat = findPatientDispensing(tbcase, batch, source);
		pat.setQuantity( pat.getQuantity() + quantity );

		// create a list of patients being dispensed
		if (!cases.contains(tbcase))
			cases.add(tbcase);
	}

	
	/**
	 * Delete the dispensing of a specific case
	 * @param medDisp
	 * @param tbcase
	 */
	public void deleteCaseDispensing(MedicineDispensing medDisp, TbCase tbcase) {
		List<MedicineDispensingCase> cases = new ArrayList<MedicineDispensingCase>();
		List<Movement> remMovs = new ArrayList<Movement>();

		for (MedicineDispensingCase dispcase: medDisp.getPatients()) {
			if (dispcase.getTbcase().equals(tbcase)) {
				cases.add(dispcase);
				Movement mov = findMovementDispensingByBatch(medDisp, dispcase.getBatch());
				if (mov != null)
					remMovs.add(mov);
			}
		}

		// prepare movements to be changed
		MovementHome movHome = (MovementHome)Component.getInstance("movementHome");
		movHome.initMovementRecording();

		for (Movement mov: remMovs) {
			movHome.prepareMovementsToRemove(mov);
			
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
			for (BatchMovement bm: mov.getBatches()) {
				// search for batch dispensed to this patient
				int quantity = bm.getQuantity();
				for (MedicineDispensingCase mdc: cases) {
					if (mdc.getBatch().equals(bm.getBatch())) {
						quantity -= mdc.getQuantity();
					}
				}
				
				if (quantity < 0)
					throw new RuntimeException("Error deleting medicine dispensing: " + tbcase.toString() + "... " + medDisp.getId() + "... Negative quantity");
				
				if (quantity != 0)
					batches.put(bm.getBatch(), quantity);
			}
			
			movHome.prepareNewMovement(mov.getDate(), mov.getTbunit(), mov.getSource(), mov.getMedicine(), mov.getType(), batches, mov.getComment());
		}
		movHome.savePreparedMovements();

		for (MedicineDispensingCase mdc: cases) {
			entityManager.remove(mdc);
			medDisp.getPatients().remove(mdc);
		}
		entityManager.persist(medDisp);
		entityManager.flush();
	}

	
	/**
	 * Search for dispensing movement where batch is being dispensed  
	 * @param medDisp
	 * @param batch
	 * @return
	 */
	protected Movement findMovementDispensingByBatch(MedicineDispensing medDisp, Batch batch) {
		for (Movement mov: medDisp.getMovements()) {
			if (mov.getMedicine().equals(batch.getMedicine())) {
				for (BatchMovement bm: mov.getBatches()) {
					if (bm.getBatch().equals(batch))
						return mov;
				}
			}
		}

		return null;
	}

	/**
	 * Include medicine dispensing quantity to a specific batch and source
	 * @param batch
	 * @param source
	 * @param quantity
	 */
	protected void addBatchQuantity(Batch batch, Source source, int quantity) {
		if (medicines == null)
			medicines = new ArrayList<MedicineItem>();
		
		BatchItem it = findBatchDispensing(batch, source);
		
		it.setQuantity( it.getQuantity() + quantity );
	}


	/**
	 * Find information about a batch dispensing by its batch and source information
	 * @param batch instance of {@link Batch} class to search for dispensing
	 * @param source instance of {@link Source} class to search for dispensing
	 * @return instance of the {@link BatchDispensing} class containing information about the quantity dispensed
	 */
	protected BatchItem findBatchDispensing(Batch batch, Source source) {
		MedicineItem meddisp = findMedicineItem(batch.getMedicine(), source);
		
		for (BatchItem it: meddisp.getItems())
			if (it.getBatch().equals(batch))
				return it;
		
		BatchItem it = new BatchItem(meddisp);
		it.setBatch(batch);
		meddisp.getItems().add(it);

		return it;
	}


	/**
	 * Find medicine dispensing
	 * @param med
	 * @return
	 */
	private MedicineItem findMedicineItem(Medicine med, Source source) {
		for (MedicineItem disp: medicines) {
			if ((disp.getMedicine().equals(med)) && (disp.getSource().equals(source))) {
				return disp;
			}
		}
		MedicineItem item = new MedicineItem();
		item.setMedicine(med);
		item.setSource(source);
		medicines.add(item);

		return item;
	}


	/**
	 * Find patient dispensing information by its TB case, batch and medicine source
	 * @param tbcase
	 * @param batch
	 * @param source
	 * @return
	 */
	protected MedicineDispensingCase findPatientDispensing(TbCase tbcase, Batch batch, Source source) {
		for (MedicineDispensingCase it: patients) {
			if ((it.getTbcase().equals(tbcase)) && (it.getBatch().equals(batch)) && (it.getSource().equals(source))) {
				return it;
			}
		}
		
		MedicineDispensingCase it = new MedicineDispensingCase();
		it.setTbcase(tbcase);
		it.setSource(source);
		it.setBatch(batch);
		patients.add(it);

		return it;
	}


	/**
	 * Save dispensing data included in memory
	 * @return
	 */
	@Transactional
	public boolean saveDispensing() {
		if (medicines == null) {
			FacesMessages.instance().add("No medicines selected for dispensing");
			return false;
		}

		MedicineDispensing medicineDispensing = getInstance();
		if (medicineDispensing.getDispensingDate() == null)
			return false;

		Date dispensingDate = medicineDispensing.getDispensingDate();
		
		// prepare movements to be recorded
		MovementHome movHome = (MovementHome)Component.getInstance("movementHome");
		movHome.initMovementRecording();

		// check if there is previous dispensing for that date and unit or if it's an editing
		MedicineDispensing recDisp = restoreDispensing();
		if (recDisp != null) {
			includePastCaseDispensing(recDisp);
			// prepare previous movements to be removed
			for (Movement mov: recDisp.getMovements())
				if (isMedicineBeingDispensed(mov.getMedicine()))
					movHome.prepareMovementsToRemove(mov);
		}

		// prepare new movements to be saved
		List<Movement> movs = new ArrayList<Movement>();
		for (MedicineItem it: medicines) {
			Map<Batch, Integer> batches = it.getBatches();
			// check if there is any positive quantity
			if (hasQuantity(batches)) {
				// prepare movement to be saved
				Movement mov = movHome.prepareNewMovement(medicineDispensing.getDispensingDate(), 
						medicineDispensing.getTbunit(), 
						it.getSource(),
						it.getMedicine(),
						MovementType.DISPENSING, 
						it.getBatches(),
						null);

				if (mov == null)
					addErrorMessage(it, movHome.getErrorMessage(), movHome.getErrorBatch());
				movs.add(mov);
			}
		}
		
		// has errors?
		if (((errorMessages != null) && (!errorMessages.isEmpty())) || ((batchErrorMessages != null) && (!batchErrorMessages.isEmpty())))
			return false;
		
		movHome.savePreparedMovements();
		
		if (recDisp != null) {
			setInstance(recDisp);
			medicineDispensing = recDisp;
			medicineDispensing.setDispensingDate(dispensingDate);
		}
		
		removePastCaseDispensing();
		
		// remove previous movements
		medicineDispensing.getMovements().clear();
		
		// add movements to dispensing
		for (Movement mov: movs)
			medicineDispensing.getMovements().add(mov);
		
		// save patient records
		if (patients != null) {
			for (MedicineDispensingCase pat: patients) {
				medicineDispensing.getPatients().add(pat);
				pat.setDispensing(medicineDispensing);
			}
		}

		setDisplayMessage(false);
		persist();
		
		return true;
	}

	
	/**
	 * Check if there is any positive quantity to register
	 * @param batches
	 * @return
	 */
	private boolean hasQuantity(Map<Batch, Integer> batches) {
		for (Batch batch: batches.keySet()) {
			Integer qtd = batches.get(batch);
			if ((qtd != null) && (qtd > 0)) 
				return true;
		}
		return false;
	}

	/**
	 * Return true if medicine is being dispensed
	 * @param med
	 * @return
	 */
	private boolean isMedicineBeingDispensed(Medicine med) {
		if (medicines != null) {
			for (MedicineItem item: medicines) {
				if (item.getMedicine().equals(med))
					return true;
			}
		}
		
		return false;
	}
	
	
	private void includePastCaseDispensing(MedicineDispensing medDispensing) {
		List<MedicineDispensingCase> lst = medDispensing.getPatients();

		if ((lst == null) || (lst.size() == 0))
			return;
		
		for (MedicineDispensingCase mdc: lst) {
			if (!cases.contains(mdc.getTbcase()))
				addBatchQuantity(mdc.getBatch(), mdc.getSource(), mdc.getQuantity());
		}
	}

	/**
	 * If there is a dispensing already registered in the date, check if the patients being dispensed
	 * were already dispensed. If so, remove past information about patient dispensing that are dispensed again
	 */
	private void removePastCaseDispensing() {
		MedicineDispensing medicineDispensing = getInstance();
		// is dispensing by patient ?
		if (patients == null) {
			// if it's not by patient, so remove any information of dispensing by patient
			for (MedicineDispensingCase mdc: medicineDispensing.getPatients())
				entityManager.remove(mdc);
			medicineDispensing.getPatients().clear();
			return;
		}

		// check if cases being dispensed were already dispensed in the date
		// if so, remove past registration
		int index = 0;
		while (index < medicineDispensing.getPatients().size()) {
			MedicineDispensingCase it = medicineDispensing.getPatients().get(index);
			if (cases.contains(it.getTbcase())) {
				entityManager.remove(it);
				medicineDispensing.getPatients().remove(index);
			}
			else index++;
		}
	}


	/**
	 * Return instance of {@link MedicineDispensing} being edited, or try to load the dispensing
	 * based on the dispensing date. If there is no dispensing in the date, the method returns null
	 * @return
	 */
	private MedicineDispensing restoreDispensing() {
		if (isManaged())
			return getInstance();

		List<MedicineDispensing> lst = entityManager.createQuery("from MedicineDispensing " +
				"where dispensingDate = :date and tbunit.id = #{userSession.tbunit.id}")
				.setParameter("date", getInstance().getDispensingDate())
				.getResultList();
		
		if (lst.size() == 0)
			return null;

		return lst.get(0);
	}


	/**
	 * Add error message
	 * @param medItem
	 * @param message
	 */
	protected void addErrorMessage(MedicineItem medItem, String message, Batch batch) {
		if (batch != null) {
			batchErrorMessages = new HashMap<DispensingHome.BatchItem, String>();
			BatchItem batchItem = findBatchDispensing(batch, medItem.getSource());
			batchErrorMessages.put(batchItem, message);
		}
		else {
			if (errorMessages == null)
				errorMessages = new HashMap<MedicineItem, String>();
			errorMessages.put(medItem, message);
		}
	}


	/**
	 * Traverse the list of error messages rose during dispensing saving
	 * @param intf
	 */
	public void traverseErrors(ErrorTraverser intf) {
		if (batchErrorMessages != null) {
			for (BatchItem item: batchErrorMessages.keySet()) {
				intf.traverse(item.getMedicineItem().getSource(), 
						item.getMedicineItem().getMedicine(), 
						item.getBatch(), batchErrorMessages.get(item));
			}
		}
		
		if (errorMessages == null)
			return;
		for (MedicineItem item: errorMessages.keySet()) {
			intf.traverse(item.getSource(), item.getMedicine(), null, errorMessages.get(item));
		}
	}
	

	/**
	 * Check if there are error messages during saving of data
	 * @return
	 */
	public boolean isErrorMessages() {
		return errorMessages != null;
	}


	/**
	 * Wrapper class to group batches by medicine
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineItem extends MedicineGroup<BatchItem> {

		private Source source;
		
		/**
		 * @return
		 */
		public Map<Batch, Integer> getBatches() {
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
			for (BatchItem it: getItems()) {
				batches.put(it.getBatch(), it.getQuantity());
			}
			return batches;
		}

		/**
		 * @return the source
		 */
		public Source getSource() {
			return source;
		}

		/**
		 * @param source the source to set
		 */
		public void setSource(Source source) {
			this.source = source;
		}
	}

	public class BatchItem {
		private Batch batch;
		private int quantity;
		private MedicineItem medicineItem;
		
		public BatchItem(MedicineItem meditem) {
			this.medicineItem = meditem; 
		}
		
		/**
		 * @return the batch
		 */
		public Batch getBatch() {
			return batch;
		}
		/**
		 * @param batch the batch to set
		 */
		public void setBatch(Batch batch) {
			this.batch = batch;
		}
		/**
		 * @return the quantity
		 */
		public int getQuantity() {
			return quantity;
		}
		/**
		 * @param quantity the quantity to set
		 */
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		/**
		 * @return the medicineItem
		 */
		public MedicineItem getMedicineItem() {
			return medicineItem;
		}
	}

}
