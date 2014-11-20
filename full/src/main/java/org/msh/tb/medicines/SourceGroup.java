package org.msh.tb.medicines;

import org.msh.tb.entities.Source;

import java.util.ArrayList;
import java.util.List;

public class SourceGroup {

	private Source source;
	private List<MedicineGroup> medicines = new ArrayList<MedicineGroup>();

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
	 * @return the medicines
	 */
	public List<MedicineGroup> getMedicines() {
		return medicines;
	}
	/**
	 * @param medicines the medicines to set
	 */
	public void setMedicines(List<MedicineGroup> medicines) {
		this.medicines = medicines;
	}
}
