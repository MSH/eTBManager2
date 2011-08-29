package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineDispensing;
import org.msh.tb.entities.MedicineDispensingCase;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.medicines.MedicineGroup;
import org.msh.tb.medicines.movs.MovementHome;

/**
 * Standard API to register medicine dispensing (by patient or consolidated by unit) 
 * @author Ricardo Memoria
 *
 */
@Name("dispensingHome")
public class DispensingHome extends EntityHomeEx<MedicineDispensing> {
	private static final long serialVersionUID = 1820669849645381520L;

	private List<MedicineItem> medicines;
	private List<MedicineDispensingCase> patients;
	private Map<MedicineItem, String> errorMessages;
	private List<TbCase> cases;
	
	@In EntityManager entityManager;

	// interface to traverse errors during dispensing saving
	public interface ErrorTraverser {
		void traverse(Source source, Medicine medicine, String errorMessage);
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
		
		BatchItem it = new BatchItem();
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
		MedicineDispensing medicineDispensing = getInstance();
		if (medicineDispensing.getDispensingDate() == null)
			return false;

		// prepare movements to be recorded
		MovementHome movHome = (MovementHome)Component.getInstance("movementHome");
		movHome.initMovementRecording();

		// is this a new dispensing for the date and unit ?
/*		if (!restoreDispensing()) {
			medicineDispensing = new MedicineDispensing();
			UserSession userSession = (UserSession)Component.getInstance("userSession");
			Tbunit unit = entityManager.find(Tbunit.class, userSession.getTbunit().getId());
			medicineDispensing.setTbunit(unit);
		}
		else {
*/			
		if (restoreDispensing()) {
			includePastCaseDispensing();
//			removePastCaseDispensing();
			// prepare previous movements to be removed
			while (medicineDispensing.getMovements().size() > 0) {
				Movement mov = medicineDispensing.getMovements().get(0);
				movHome.prepareMovementsToRemove(mov);
				medicineDispensing.getMovements().remove(0);
			}
		}

		// prepare new movements to be saved
		List<Movement> movs = new ArrayList<Movement>();
		for (MedicineItem it: medicines) {
			Movement mov = movHome.prepareNewMovement(medicineDispensing.getDispensingDate(), 
					medicineDispensing.getTbunit(), 
					it.getSource(),
					it.getMedicine(),
					MovementType.DISPENSING, 
					it.getBatches(),
					null);

			if (mov == null)
				addErrorMessage(it, movHome.getErrorMessage());
			movs.add(mov);
		}
		
		// has errors?
		if ((errorMessages != null) && (!errorMessages.isEmpty()))
			return false;
		
		movHome.savePreparedMovements();
		
		removePastCaseDispensing();
		
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

		persist();
		
		return true;
	}

	
	private void includePastCaseDispensing() {
		List<MedicineDispensingCase> lst = getInstance().getPatients();

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
	private boolean restoreDispensing() {
		if (isManaged())
			return true;

		List<MedicineDispensing> lst = entityManager.createQuery("from MedicineDispensing " +
				"where dispensingDate = :date and tbunit.id = #{userSession.tbunit.id}")
				.setParameter("date", getInstance().getDispensingDate())
				.getResultList();
		
		if (lst.size() == 0)
			return false;

		clearInstance();
		setId(lst.get(0).getId());
		return true;
	}
	
	/**
	 * Add error message
	 * @param medItem
	 * @param message
	 */
	protected void addErrorMessage(MedicineItem medItem, String message) {
		if (errorMessages == null)
			errorMessages = new HashMap<MedicineItem, String>();
		errorMessages.put(medItem, message);
	}


	/**
	 * Traverse the list of error messages rose during dispensing saving
	 * @param intf
	 */
	public void traverseErrors(ErrorTraverser intf) {
		if (errorMessages == null)
			return;
		for (MedicineItem item: errorMessages.keySet()) {
			intf.traverse(item.getSource(), item.getMedicine(), errorMessages.get(item));
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
	}
}
