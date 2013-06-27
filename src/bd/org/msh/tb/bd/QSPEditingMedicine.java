package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;

/**
 * Stores information about the medicine that is being edited.
 * @author MSANTOS
 */
public class QSPEditingMedicine{
	private Medicine medicine;
	private List<QSPEditingBatchQuantity> batchList;
	private Integer consumption;
	private Integer outOfStock;
	
	public QSPEditingMedicine(Medicine medicine){
		this.medicine = medicine;
		batchList = new ArrayList<QSPEditingBatchQuantity>();
		consumption = new Integer(0);
		outOfStock = new Integer(0);
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
	 * @return the batchList
	 */
	public List<QSPEditingBatchQuantity> getBatchList() {
		return batchList;
	}

	/**
	 * @param batchList the batchList to set
	 */
	public void setBatchList(List<QSPEditingBatchQuantity> batchList) {
		this.batchList = batchList;
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
	public QSPEditingBatchQuantity createQSPEditingBatch(BatchQuantity bq){
		return new QSPEditingBatchQuantity(bq);
	}
	
	/**
	 * Stores information about the batches of the medicine that is being edited.
	 * @author MSANTOS
	 */
	public class QSPEditingBatchQuantity{
		private BatchQuantity batchQtd;
		private Integer posAdjust;
		private Integer negAdjust;
		private Integer expired;
		
		public QSPEditingBatchQuantity(BatchQuantity batchQtd){
			this.batchQtd = batchQtd;
			this.posAdjust = new Integer(0);
			this.negAdjust = new Integer(0);
			this.expired = new Integer(0);
		}
		
		/**
		 * @return the batchQtd
		 */
		public BatchQuantity getBatchQtd() {
			return batchQtd;
		}
		/**
		 * @param batchQtd the batchQtd to set
		 */
		public void setBatchQtd(BatchQuantity batchQtd) {
			this.batchQtd = batchQtd;
		}
		/**
		 * @return the posAdjust
		 */
		public Integer getPosAdjust() {
			return posAdjust;
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
			return negAdjust;
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
			return expired;
		}
		/**
		 * @param expired the expired to set
		 */
		public void setExpired(Integer expired) {
			this.expired = expired;
		}
	}
}
