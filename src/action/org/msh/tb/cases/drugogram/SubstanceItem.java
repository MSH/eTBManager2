package org.msh.tb.cases.drugogram;

import org.msh.mdrtb.entities.enums.SusceptibilityResultTest;

public class SubstanceItem {

	private String medicine;
	private SusceptibilityResultTest dstResult;
	private boolean prescribed;


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
	public SusceptibilityResultTest getDstResult() {
		return dstResult;
	}
	/**
	 * @param dstResult the dstResult to set
	 */
	public void setDstResult(SusceptibilityResultTest dstResult) {
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
}
