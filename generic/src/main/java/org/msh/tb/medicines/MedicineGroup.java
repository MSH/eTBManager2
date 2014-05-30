package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.Medicine;

public class MedicineGroup<E> {
	
	public MedicineGroup() { super(); }

	private Medicine medicine;
	private List<E> items = new ArrayList<E>();
	
	public List<E> getItems() {
		return items;
	}
	public void setItems(List<E> items) {
		this.items = items;
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

}
