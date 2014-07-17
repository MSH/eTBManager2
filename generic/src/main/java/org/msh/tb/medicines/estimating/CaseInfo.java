package org.msh.tb.medicines.estimating;

import org.msh.tb.entities.Patient;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.TbCase;

import java.util.ArrayList;
import java.util.List;

public class CaseInfo {

	private TbCase tbcase;
	private List<PrescribedMedicine> prescriptions = new ArrayList<PrescribedMedicine>();
	private List<MedicineQuantity> quantities = new ArrayList<MedicineQuantity>();

	public CaseInfo() {
		super();
		tbcase = new TbCase();
		tbcase.setPatient(new Patient());
	}
	
	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}
	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}
	/**
	 * @return the prescriptions
	 */
	public List<PrescribedMedicine> getPrescriptions() {
		return prescriptions;
	}
	/**
	 * @param prescriptions the prescriptions to set
	 */
	public void setPrescriptions(List<PrescribedMedicine> prescriptions) {
		this.prescriptions = prescriptions;
	}
	/**
	 * @return the quantities
	 */
	public List<MedicineQuantity> getQuantities() {
		return quantities;
	}
	/**
	 * @param quantities the quantities to set
	 */
	public void setQuantities(List<MedicineQuantity> quantities) {
		this.quantities = quantities;
	}

	
}
