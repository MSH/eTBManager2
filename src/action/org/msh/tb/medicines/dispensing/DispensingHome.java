package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchDispensing;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineDispensing;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.MedicineDispensingCase;
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
public class DispensingHome {
	
	private Date dispensingDate;
	private Tbunit unit;
	private List<MedicineItem> medicines;
	private List<MedicineDispensingCase> patients;
	private Map<MedicineItem, String> errorMessages;
	
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
		this.unit = unit;
		medicines = null;
		patients = null;
	}


	/**
	 * Include a new dispensing done for a batch and source with the given quantity.
	 * The dispensing is saved when the method 'saveDispensing()' is called
	 * @param batch
	 * @param source
	 * @param quantity
	 */
	public void addDispensing(Batch batch, Source source, int quantity) {
		if (unit.isPatientDispensing())
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
		if (unit.isPatientDispensing())
			throw new RuntimeException("Method addDispensing cannot be directly called when dispensing is done by patient. Call addPatientDispensing instead");

		addBatchQuantity(batch, source, quantity);

		if (patients == null)
			patients = new ArrayList<MedicineDispensingCase>();
		
		MedicineDispensingCase pat = findPatientDispensing(tbcase, batch, source);
		pat.setQuantity( pat.getQuantity() + quantity );
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
		
		BatchDispensing it = findBatchDispensing(batch, source);
		
		it.setQuantity( it.getQuantity() + quantity );
	}


	/**
	 * Find information about a batch dispensing by its batch and source information
	 * @param batch instance of {@link Batch} class to search for dispensing
	 * @param source instance of {@link Source} class to search for dispensing
	 * @return instance of the {@link BatchDispensing} class containing information about the quantity dispensed
	 */
	protected BatchDispensing findBatchDispensing(Batch batch, Source source) {
		MedicineItem meddisp = findMedicineItem(batch.getMedicine(), source);
		
		for (BatchDispensing it: meddisp.getItems())
			if ((it.getBatch().equals(batch)) && (it.getSource().equals(source)))
				return it;
		
		BatchDispensing it = new BatchDispensing();
		it.setBatch(batch);
		it.setSource(source);
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
		it.setSource(it.getSource());
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
		if (dispensingDate == null)
			return false;

		MedicineDispensing medDispensing = new MedicineDispensing();
		medDispensing.setDispensingDate(dispensingDate);

		// test movement generation
		MovementHome movHome = (MovementHome)Component.getInstance("movementHome");
		movHome.initMovementRecording();
		for (MedicineItem it: medicines) {
			Movement mov = movHome.prepareNewMovement(dispensingDate, 
					unit, 
					it.getSource(),
					it.getMedicine(),
					MovementType.DISPENSING, 
					it.getBatches(), null);

			if (mov == null)
				addErrorMessage(it, movHome.getErrorMessage());

			medDispensing.getMovements().add(mov);
		}
		
		// has errors?
		if ((errorMessages != null) && (!errorMessages.isEmpty()))
			return false;
		
		movHome.savePreparedMovements();

		// save dispensing records
		for (MedicineItem it: medicines) {
			for (BatchDispensing disp: it.getItems()) {
				medDispensing.getBatches().add(disp);
				disp.setDispensing(medDispensing);
			}
		}
		
		// save patient records
		if (patients != null) {
			for (MedicineDispensingCase pat: patients) {
				medDispensing.getPatients().add(pat);
				pat.setDispensing(medDispensing);
			}
		}
		
		entityManager.persist(medDispensing);
		entityManager.flush();
		
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
	 * @return the dispensingDate
	 */
	public Date getDispensingDate() {
		return dispensingDate;
	}

	/**
	 * @param dispensingDate the dispensingDate to set
	 */
	public void setDispensingDate(Date dispensingDate) {
		this.dispensingDate = dispensingDate;
	}

	/**
	 * @return the unit
	 */
	public Tbunit getUnit() {
		return unit;
	}

	/**
	 * Wrapper class to group batches by medicine
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineItem extends MedicineGroup<BatchDispensing> {

		private Source source;
		
		/**
		 * @return
		 */
		public Map<Batch, Integer> getBatches() {
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
			for (BatchDispensing it: getItems()) {
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

}
