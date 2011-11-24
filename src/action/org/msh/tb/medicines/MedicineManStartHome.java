package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.SourceGroup;
import org.msh.tb.SourcesQuery;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.log.LogService;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.movs.MovementHome;

/**
 * Home class to handle a TB Unit that start medicine management in e-TB Manager
 * @author Ricardo Memoria
 *
 */
/**
 * Handle actions to include and remove a TB unit from the medicine module control
 * @author Ricardo Memoria
 *
 */
@Name("medicineManStartHome")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class MedicineManStartHome {

	@In(create=true) SourcesQuery sources;
	@In(create=true) MedicinesQuery medicines;
	@In(create=true) MovementHome movementHome;
	@In(required=true) UserSession userSession;
	@In EntityManager entityManager;
	
	private Date startDate;
	private BatchInfo batchInfo;
	private MedicineInfo medicineInfo;
	private List<SourceInfo> sourcesInfo;
	private boolean newBatch;
	private Tbunit unit;
	private boolean validated;


	/**
	 * Initialize medicine module control for UI processing
	 */
	public void initialize() {
		if (userSession.isMedicineManagementStarted())
			return;

		if (sourcesInfo != null)
			return;

		sourcesInfo = new ArrayList<SourceInfo>();
		
		for (Source source: sources.getResultList()) {
			SourceInfo sourceInfo = new SourceInfo();
			sourceInfo.setSource(source);
			
			for (Medicine med: medicines.getResultList()) {
				sourceInfo.getItems().add( new MedicineInfo(med, source) );
			}
			sourcesInfo.add(sourceInfo);
		}
		
		unit = userSession.getTbunit();
	}


	/**
	 * Start new batch form for UI processing
	 * @param medicine
	 */
	public void startNewBatch(MedicineInfo medicineInfo) {
		batchInfo = new BatchInfo();
		batchInfo.setBatch(new Batch());
		this.medicineInfo = medicineInfo;
		newBatch = true;
		validated = false;
	}
	

	/**
	 * Start editing of an existing batch for UI processing
	 * @param medInfo
	 * @param batch
	 */
	public void startBatchEdit(MedicineInfo medInfo, BatchInfo batchInfo) {
		this.batchInfo = batchInfo;
		medicineInfo = medInfo;
		newBatch = false;
		validated = false;
	}

	
	/**
	 * Delete a batch from one specific medicine entered by the user
	 * @param medInfo
	 * @param batch
	 */
	public void deleteBatch(MedicineInfo medInfo, BatchInfo batchInfo) {
		medInfo.getBatches().remove(batchInfo);
	}

	
	/**
	 * Finish editing of a batch done by the user
	 */
	public void finishBatchEditing() {
		if (batchInfo == null)
			return;

		Batch batch = batchInfo.getBatch();
		
		if (newBatch) {
			batch.setMedicine(medicineInfo.getMedicine());
			medicineInfo.getBatches().add(batchInfo);
		}
	
//		batchQuantity.setQuantity(0);
//		batch.setQuantityReceived(batchQuantity.getQuantity());
		validated = true;
	}

	
	/**
	 * Start the medicine management control, specifying the starting date of the control
	 * and initializing the stock in unit based on the medicines and batches selected by the user in the UI   
	 * @return
	 */
	@Transactional
	public String startMedicineManagement() {
		if (startDate == null)
			return "error";

		Tbunit unit = userSession.getTbunit();
		
		if ((unit == null) || (unit.isMedicineManagementStarted()))
			return "error";

		// remove movements of the unit
		removeMovements(unit);
		
		// fill start date for medicine management
		unit = entityManager.merge(unit);
		unit.setMedManStartDate(startDate);
		entityManager.persist(unit);
		
		// create movements
		movementHome.initMovementRecording();
		for (SourceInfo sourceInfo: sourcesInfo) {
			for (MedicineInfo medInfo: sourceInfo.getItems()) {

				// save batches before creating movements
				for (BatchInfo batchInfo: medInfo.getBatches()) {
					BatchQuantity batchQuantity = new BatchQuantity();
					batchQuantity.setSource(medInfo.getSource());
					batchQuantity.setTbunit(unit);
					batchQuantity.setBatch(batchInfo.getBatch());
					batchQuantity.setQuantity(0);
					
					entityManager.persist(batchQuantity.getBatch());
					entityManager.persist(batchQuantity);
					entityManager.flush();
				}
				
				if (medInfo.getBatches().size() > 0)
					movementHome.prepareNewMovement(startDate, unit, sourceInfo.getSource(), medInfo.getMedicine(), MovementType.INITIALIZE, medInfo.getBatchesMap(), null);
			}
		}
		movementHome.savePreparedMovements();
		
		userSession.setTbunit(unit);
		
		entityManager.flush();

		registerStartManLog();
		
		return "medman-started";
	}


	/**
	 * Cancel  medicine management control in a TB unit. All medicine information of the TB Unit will be deleted after this operation
	 * and the TB unit will be out of the medicine module control 
	 * @return
	 */
	@Transactional
	public String cancelMedicineManagement() {
		Tbunit unit = userSession.getTbunit();
		unit = entityManager.merge(unit);

		removeMovements(unit);
		
		unit.setMedManStartDate(null);
		entityManager.persist(unit);
		entityManager.flush();
		
		userSession.setTbunit(unit);
		
		registerRemoveManLog();
		
		return "medman-cancelled";
	}


	/**
	 * Remove movements and other information of medicine management from a TB unit
	 * @param unit
	 */
	protected void removeMovements(Tbunit unit) {
		execQueryUnit(unit, "delete from Movement m where m.tbunit.id = :id");
		execQueryUnit(unit, "delete from StockPosition sp where sp.tbunit.id = :id");
		execQueryUnit(unit, "delete from Order aux where (aux.unitFrom.id = :id or aux.unitTo.id = :id)");
		execQueryUnit(unit, "delete from Transfer aux where (aux.unitFrom.id = :id or aux.unitTo.id = :id)");
		execQueryUnit(unit, "delete from MedicineReceiving aux where aux.tbunit.id = :id");
		execQueryUnit(unit, "delete from MedicineDispensing aux where aux.tbunit.id = :id");
		execQueryUnit(unit, "delete from BatchQuantity sp where sp.tbunit.id = :id");

		// delete unused batches
		entityManager.createQuery("delete from Batch where id not in (select bm.batch.id from BatchMovement bm)").executeUpdate();
	}
	
	
	/**
	 * Execute a HQL query to a TB unit with an ID parameter
	 * @param unit
	 * @param hql
	 */
	private void execQueryUnit(Tbunit unit, String hql) {
		entityManager.createQuery(hql).setParameter("id", unit.getId()).executeUpdate();
	}

	
	/**
	 * Register in the logging system the starting of the unit in the medicine management control
	 */
	public void registerStartManLog() {
		LogService logService = new LogService();
		Tbunit unit = userSession.getTbunit();
		
		logService.addValue("Tbunit.medManStartDate", unit.getMedManStartDate());
		for (SourceInfo si: sourcesInfo) {
			String meds = "";
			for (MedicineInfo medInfo: si.getItems()) {
				if (medInfo.getBatches().size() > 0) {
					if (!meds.isEmpty())
						meds += ", ";
					meds += medInfo.getMedicine().getFullAbbrevName();
				}
			}
			if (!meds.isEmpty()) {
				meds = si.getSource().getAbbrevName() + " [" + meds + "]";
				logService.addValue("form.selectedmeds", meds);
			}
		}
		logService.saveExecuteTransaction(unit, "MED_INIT");
	}


	/**
	 * Register in the logging system the removing of the unit from the medicine management control 
	 */
	public void registerRemoveManLog() {
		LogService logService = new LogService();
		Tbunit unit = userSession.getTbunit();
		
		unit = entityManager.merge(unit);

		logService.saveExecuteTransaction(unit, "MED_INIT_REM");
	}

/*	*//**
	 * @param med
	 * @param source
	 * @return
	 *//*
	protected MedicineInfo findMedicineInfo(Medicine med, Source source) {
		for (SourceInfo sourceInfo: sourcesInfo) {
			if (sourceInfo.getSource().equals(source)) {
				for (MedicineInfo medInfo: sourceInfo.getItems()) {
					if (medInfo.getMedicine().equals(med))
						return medInfo;
				}
			}
		}
		return null;
	}
*/

	/**
	 * Group information by source
	 * @author Ricardo Memoria
	 *
	 */
	public class SourceInfo extends SourceGroup<MedicineInfo> {}


	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class BatchInfo {
		private Batch batch;
		private int quantity;

		public float getTotalPrice() {
			return quantity * batch.getUnitPrice();
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
	}

	

	/**
	 * Information about the medicines by source
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineInfo {
		private Medicine medicine;
		private Source source;
		private List<BatchInfo> batches = new ArrayList<BatchInfo>();
		
		public MedicineInfo(Medicine medicine, Source source) {
			super();
			this.medicine = medicine;
			this.source = source;
		}

		
		/**
		 * Return the quantity available
		 * @return
		 */
		public int getQuantity() {
			int qtd = 0;
			for (BatchInfo batchInfo: batches) {
				qtd += batchInfo.getQuantity();
			}
			return qtd;
		}
		
		
		/**
		 * Return the total price
		 * @return
		 */
		public float getTotalPrice() {
			float tot = 0;
			for (BatchInfo batchInfo: batches)
				tot += batchInfo.getTotalPrice();
			return tot;
		}
		
		
		/**
		 * Return the average unit price
		 * @return
		 */
		public Float getUnitPrice() {
			float tot = getTotalPrice();
			float qtd = getQuantity();
			if (qtd == 0)
				 return null;
			else return qtd/tot;
		}
		
		public Map<Batch, Integer> getBatchesMap() {
			Map<Batch, Integer> btmap = new HashMap<Batch, Integer>();
			for (BatchInfo batchInfo: batches) {
				btmap.put(batchInfo.getBatch(), batchInfo.getBatch().getQuantityReceived());
			}
			return btmap;
		}
		
		public Medicine getMedicine() {
			return medicine;
		}
		public void setMedicine(Medicine medicine) {
			this.medicine = medicine;
		}
		public Source getSource() {
			return source;
		}
		public void setSource(Source source) {
			this.source = source;
		}
		public List<BatchInfo> getBatches() {
			return batches;
		}
	}

	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}



	public MedicineInfo getMedicineInfo() {
		return medicineInfo;
	}


	public List<SourceInfo> getSourcesInfo() {
		return sourcesInfo;
	}


	public boolean isNewBatch() {
		return newBatch;
	}


	public BatchInfo getBatchInfo() {
		return batchInfo;
	}


	public Tbunit getUnit() {
		return unit;
	}


	public boolean isValidated() {
		return validated;
	};
}
