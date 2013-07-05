package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;

/**
 * Stores information about the medicine that is being edited.
 * @author MSANTOS
 */
public class QSPEditingMedicine{
	private Medicine medicine;
	private List<QSPEditingBatchDetails> batchList;
	private Integer consumption;
	private Integer outOfStock;
	
	public QSPEditingMedicine(Medicine medicine){
		this.medicine = medicine;
		batchList = new ArrayList<QSPEditingBatchDetails>();
		consumption = new Integer(0);
		outOfStock = new Integer(0);
	}
	
	/**
	 * @return the batchList
	 */
	public List<QSPEditingBatchDetails> getBatchList() {
		return batchList;
	}

	/**
	 * @param batchList the batchList to set
	 */
	public void setBatchList(List<QSPEditingBatchDetails> batchList) {
		this.batchList = batchList;
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
	 * @return the consumption
	 */
	public Integer getConsumption() {
		return consumption;
	}

	/**
	 * @param consumption the consumption to set
	 */
	public void setConsumption(Integer consumption) {
		this.consumption = consumption;
	}

	/**
	 * @return the outOfStock
	 */
	public Integer getOutOfStock() {
		return outOfStock;
	}

	/**
	 * @param outOfStock the outOfStock to set
	 */
	public void setOutOfStock(Integer outOfStock) {
		this.outOfStock = outOfStock;
	}

	/**
	 * @return a new instance of QSPSourceEditingBatch
	 */
	public QSPEditingBatchDetails createQSPEditingBatch(Batch batch, Source source){
		return new QSPEditingBatchDetails(batch, source);
	}
	
	/**
	 * Stores information about the batches of the medicine that is being edited.
	 * @author MSANTOS
	 */
	public class QSPEditingBatchDetails{
		private Batch batch;
		private Source source;
		private Integer posAdjust;
		private Integer negAdjust;
		private Integer expired;
		
		public QSPEditingBatchDetails(Batch batch, Source source){
			this.batch = batch;
			this.source = source;
		}
		
		/**
		 * @return the posAdjust
		 */
		public Integer getPosAdjust() {
			return (posAdjust == null ? 0 : posAdjust);
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
		 * @param posAdjust the posAdjust to set
		 */
		public void setPosAdjust(Integer posAdjust) {
			this.posAdjust = posAdjust;
		}
		/**
		 * @return the negAdjust
		 */
		public Integer getNegAdjust() {
			return (negAdjust == null ? 0 : negAdjust);
		}
		/**
		 * @param negAdjust the negAdjust to set
		 */
		public void setNegAdjust(Integer negAdjust) {
			this.negAdjust = negAdjust;
		}
		/**
		 * @return the expired
		 */
		public Integer getExpired() {
			return (expired == null ? 0 : expired);
		}
		/**
		 * @param expired the expired to set
		 */
		public void setExpired(Integer expired) {
			this.expired = expired;
		}
	}
}
