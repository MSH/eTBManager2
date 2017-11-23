package org.msh.tb.medicines.movs;

import org.msh.tb.entities.BatchQuantity;

/**
 * Store information about batches quantity to be adjusted
 * @author Ricardo Memoria
 *
 */
public class BatchAdjustmentItem {

	private BatchQuantity batchQtd;
	private int quantity;


	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	/**
	 * @param batchQtd the batchQtd to set
	 */
	public void setBatchQtd(BatchQuantity batchQtd) {
		this.batchQtd = batchQtd;
	}
	/**
	 * @return the batchQtd
	 */
	public BatchQuantity getBatchQtd() {
		return batchQtd;
	}

}
