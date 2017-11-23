package org.msh.tb.cases.drugogram;

import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.DstResult;

public class SubstanceItem {

	private String medicine;
	private DstResult dstResult;
	private boolean prescribed;
	private Substance substance;


	/**
	 * @return the medicine
	 */
	public String getMedicine() {
		return medicine;
	}
	/**
	 * @param medicine the medicine to set
	 */
	public void setMedicine(String medicine) {
		this.medicine = medicine;
	}
	/**
	 * @return the dstResult
	 */
	public DstResult getDstResult() {
		return dstResult;
	}
	/**
	 * @param dstResult the dstResult to set
	 */
	public void setDstResult(DstResult dstResult) {
		this.dstResult = dstResult;
	}
	/**
	 * @param prescribed the prescribed to set
	 */
	public void setPrescribed(boolean prescribed) {
		this.prescribed = prescribed;
	}
	/**
	 * @return the prescribed
	 */
	public boolean isPrescribed() {
		return prescribed;
	}
	/**
	 * @return the substance
	 */
	public Substance getSubstance() {
		return substance;
	}
	/**
	 * @param substance the substance to set
	 */
	public void setSubstance(Substance substance) {
		this.substance = substance;
	}
}
