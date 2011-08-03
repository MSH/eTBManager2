package org.msh.tb.medicines.dispensing;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.MedicineGroup;
import org.msh.tb.medicines.SourceGroup;
import org.msh.tb.medicines.SourceMedicineGroup;
import org.msh.tb.medicines.movs.StockPositionList;

@Name("batchDispensingUIHome")
public class BatchDispensingUIHome {

	@In EntityManager entityManager;
	@In(create=true) StockPositionList stockPositionList;
	@In(create=true) UserSession userSession;

	private Date dispensingDate;
	private SourceMedicineGroup sources;


	/**
	 * Return list of sources
	 * @return
	 */
	public List<SourceGroup> getSources() {
		if (sources == null)
			createSources();
		
		return sources.getSources();
	}


	/**
	 * Save dispensing
	 * @return
	 */
	public String saveDispensing() {
		DispensingHome dispHome = (DispensingHome)Component.getInstance("dispensingHome");
		
		dispHome.initialize(userSession.getTbunit());
		dispHome.setDispensingDate(dispensingDate);
		return "error";
	}


	/**
	 * Create list of sources and its medicines and batches, ready for editing
	 */
	private void createSources() {
		Tbunit unit = userSession.getTbunit();
		SourceMedicineGroup<BatchQuantity> items = stockPositionList.getBatchAvailable(unit, null);

		// create list of batches
		sources = new SourceMedicineGroup<BatchItem>(MedicineDispensingItem.class);

		for (BatchQuantity item: items.getItems()) {
			BatchItem b = new BatchItem();
			b.setBatchQuantity(item);
			sources.addItem(item.getSource(), item.getBatch().getMedicine(), b);
		}
	}

	
	public class BatchItem {
		private BatchQuantity batchQuantity;
		private Integer dispensingQuantity;
		/**
		 * @return the batchQuantity
		 */
		public BatchQuantity getBatchQuantity() {
			return batchQuantity;
		}
		/**
		 * @param batchQuantity the batchQuantity to set
		 */
		public void setBatchQuantity(BatchQuantity batchQuantity) {
			this.batchQuantity = batchQuantity;
		}
		/**
		 * @return the dispensingQuantity
		 */
		public Integer getDispensingQuantity() {
			return dispensingQuantity;
		}
		/**
		 * @param dispensingQuantity the dispensingQuantity to set
		 */
		public void setDispensingQuantity(Integer dispensingQuantity) {
			this.dispensingQuantity = dispensingQuantity;
		}
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
}
