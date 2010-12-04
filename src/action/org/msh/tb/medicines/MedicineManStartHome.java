package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.SourceGroup;
import org.msh.tb.SourcesQuery;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.movs.MovementHome;

/**
 * Home class to handle a TB Unit that start medicine management in e-TB Manager
 * @author Ricardo Memoria
 *
 */
@Name("medicineManStartHome")
@Scope(ScopeType.CONVERSATION)
public class MedicineManStartHome {

	@In(create=true) SourcesQuery sources;
	@In(create=true) MedicinesQuery medicines;
	@In(create=true) MovementHome movementHome;
	@In(required=true) UserSession userSession;
	@In EntityManager entityManager;
	
	private Date startDate;
	private BatchQuantity batchQuantity;
	private MedicineInfo medicineInfo;
	private List<SourceInfo> sourcesInfo;
	private boolean newBatch;
	private Tbunit unit;
	private boolean validated;


	/**
	 * Initialize medicine management starting
	 */
	public void initialize() {
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
	 * Start new batch editing
	 * @param medicine
	 */
	public void startNewBatch(MedicineInfo medicineInfo) {
		batchQuantity = new BatchQuantity();
		batchQuantity.setBatch(new Batch());
		this.medicineInfo = medicineInfo;
		newBatch = true;
		validated = false;
	}
	

	/**
	 * Start editing of a batch
	 * @param medInfo
	 * @param batch
	 */
	public void startBatchEdit(MedicineInfo medInfo, BatchQuantity batchInfo) {
		this.batchQuantity = batchInfo;
		medicineInfo = medInfo;
		newBatch = false;
		validated = false;
	}

	
	/**
	 * Delete a batch from the medicines
	 * @param medInfo
	 * @param batch
	 */
	public void deleteBatch(MedicineInfo medInfo, BatchQuantity batchQuantity) {
		medicineInfo.getBatches().remove(batchQuantity);
	}

	
	/**
	 * Finish editing of a batch
	 */
	public void finishBatchEditing() {
		if (batchQuantity == null)
			return;

		Batch batch = batchQuantity.getBatch();
		
		if (newBatch) {
			batchQuantity.setSource(medicineInfo.getSource());
			batchQuantity.setTbunit(unit);
			batch.setMedicine(medicineInfo.getMedicine());
			medicineInfo.getBatches().add(batchQuantity);
		}
		
		batch.setQuantityReceived(batchQuantity.getQuantity());
		validated = true;
	}

	
	/**
	 * Start the medicine management control
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
		for (SourceInfo sourceInfo: sourcesInfo) {
			for (MedicineInfo medInfo: sourceInfo.getItems()) {

				// save batches before creating movements
				for (BatchQuantity batchQuantity: medInfo.getBatches()) {
					entityManager.persist(batchQuantity.getBatch());
					entityManager.persist(batchQuantity);
					entityManager.flush();
				}
				
				movementHome.newMovement(startDate, unit, sourceInfo.getSource(), medInfo.getMedicine(), MovementType.INITIALIZE, medInfo.getBatchesMap(), null);
			}
		}
		
		userSession.setTbunit(unit);

		return "medman-started";
	}


	/**
	 * Cancel  medicine management control in a TB unit. All stock information will be deleted after this operation 
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
		
		return "medman-cancelled";
	}


	/**
	 * Remove movements and other information of medicine management from a TB unit
	 * @param unit
	 */
	protected void removeMovements(Tbunit unit) {
		execQueryUnit(unit, "delete from Movement m where m.tbunit.id = :id");
		execQueryUnit(unit, "delete from StockPosition sp where sp.tbunit.id = :id");
		execQueryUnit(unit, "delete from Order aux where (aux.tbunitFrom.id = :id or aux.tbunitTo.id = :id)");
		execQueryUnit(unit, "delete from Transfer aux where (aux.unitFrom.id = :id or aux.unitTo.id = :id)");
		execQueryUnit(unit, "delete from MedicineReceiving aux where aux.tbunit.id = :id");
		execQueryUnit(unit, "delete from MedicineDispensing aux where aux.tbunit.id = :id");
		execQueryUnit(unit, "delete from BatchQuantity sp where sp.tbunit.id = :id");
	}
	
	
	/**
	 * Execute a HQL query to a TB unit with an ID parameter
	 * @param unit
	 * @param hql
	 */
	private void execQueryUnit(Tbunit unit, String hql) {
		entityManager.createQuery(hql).setParameter("id", unit.getId()).executeUpdate();
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
	 * Information about the medicines by source
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineInfo {
		private Medicine medicine;
		private Source source;
		private List<BatchQuantity> batches = new ArrayList<BatchQuantity>();
		
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
			for (BatchQuantity batchInfo: batches) {
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
			for (BatchQuantity batchInfo: batches)
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
			for (BatchQuantity batchInfo: batches) {
				btmap.put(batchInfo.getBatch(), batchInfo.getQuantity());
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
		public List<BatchQuantity> getBatches() {
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


	public BatchQuantity getBatchQuantity() {
		return batchQuantity;
	}


	public Tbunit getUnit() {
		return unit;
	}


	public boolean isValidated() {
		return validated;
	};
}
