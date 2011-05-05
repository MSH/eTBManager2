package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.Medicine;

/**
 * @author Ricardo
 *
 * Stores information about a medicine for dispensing
 */
public class MonthDispensingInfo {

	private Medicine medicine;

	private List<Integer> quantities = new ArrayList<Integer>();
	
	public void setNumberOfDates(int num) {
		quantities.clear();
		for (int i = 0; i < num; i++) {
			quantities.add(0);
		}
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
	 * @return the total
	 */
	public int getTotal() {
		int total = 0;
		for (Integer val: quantities)
			total += val;
		return total;
	}

	/**
	 * @return the quantities
	 */
	public List<Integer> getQuantities() {
		return quantities;
	}

	/**
	 * @param quantities the quantities to set
	 */
	public void setQuantities(List<Integer> quantities) {
		this.quantities = quantities;
	}


}
