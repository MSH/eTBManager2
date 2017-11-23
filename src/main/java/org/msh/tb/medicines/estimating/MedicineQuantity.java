package org.msh.tb.medicines.estimating;

import org.msh.tb.entities.Medicine;

public class MedicineQuantity {

	private Medicine medicine;
	private int qtyEstimated;
	private int qtyDispensed;
	
	
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
	 * @return the qtyEstimated
	 */
	public int getQtyEstimated() {
		return qtyEstimated;
	}
	/**
	 * @param qtyEstimated the qtyEstimated to set
	 */
	public void setQtyEstimated(int qtyEstimated) {
		this.qtyEstimated = qtyEstimated;
	}
	/**
	 * @return the qtyDispensed
	 */
	public int getQtyDispensed() {
		return qtyDispensed;
	}
	/**
	 * @param qtyDispensed the qtyDispensed to set
	 */
	public void setQtyDispensed(int qtyDispensed) {
		this.qtyDispensed = qtyDispensed;
	}
}
