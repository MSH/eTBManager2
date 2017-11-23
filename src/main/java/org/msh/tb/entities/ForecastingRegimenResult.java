package org.msh.tb.entities;

public class ForecastingRegimenResult {
	
	private int numCasesOnTreatment;
	
	private float numNewCases;
	
	private int monthIndex;


	public int getTotalCases() {
		return numCasesOnTreatment + Math.round(numNewCases);
	}


	/**
	 * @return the numNewCases
	 */
	public float getNumNewCases() {
		return numNewCases;
	}

	/**
	 * @param numNewCases the numNewCases to set
	 */
	public void setNumNewCases(float numNewCases) {
		this.numNewCases = numNewCases;
	}

	/**
	 * @return the monthIndex
	 */
	public int getMonthIndex() {
		return monthIndex;
	}

	/**
	 * @param monthIndex the monthIndex to set
	 */
	public void setMonthIndex(int monthIndex) {
		this.monthIndex = monthIndex;
	}

	/**
	 * @return the numCasesOnTreatment
	 */
	public int getNumCasesOnTreatment() {
		return numCasesOnTreatment;
	}

	/**
	 * @param numCasesOnTreatment the numCasesOnTreatment to set
	 */
	public void setNumCasesOnTreatment(int numCasesOnTreatment) {
		this.numCasesOnTreatment = numCasesOnTreatment;
	}
}
