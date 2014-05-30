package org.msh.tb.medicines.movs;

import org.msh.tb.entities.Batch;

public class BatchTransferItem {

	private Batch batch;
	private int quantity;

	public Batch getBatch() {
		return batch;
	}
	public void setBatch(Batch batch) {
		this.batch = batch;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
