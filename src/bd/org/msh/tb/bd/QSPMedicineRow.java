/**
 * @author MSANTOS
 * Class responsible to allocate the informations of a respective row on the 
 * Quarterly Stock Position. 
 */
package org.msh.tb.bd;

import org.msh.tb.entities.Medicine;

public class QSPMedicineRow {

	private Medicine medicine;
	private Long openingBalance;
	private Long receivedFromCS;
	private Long positiveAdjust;
	private Long negativeAdjust;
	private Long dispensed;
	private Long expired;
	private Long outOfStockDays;
	
	public Long getClosingBalance(){
		return getOpeningBalance() + getReceivedFromCS()+ getPositiveAdjust() - getNegativeAdjust() - getDispensed() - getExpired();
	}
	
	public QSPMedicineRow(Medicine medicine){
		this.medicine = medicine;
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
	 * @return the openingBalance
	 */
	public Long getOpeningBalance() {
		if(openingBalance == null) openingBalance = new Long (0);
		return openingBalance;
	}

	/**
	 * @param openingBalance the openingBalance to set
	 */
	public void setOpeningBalance(Long openingBalance) {
		this.openingBalance = openingBalance;
	}

	/**
	 * @return the receivedFromCS
	 */
	public Long getReceivedFromCS() {
		if(receivedFromCS == null) receivedFromCS = new Long (0);
		return receivedFromCS;
	}

	/**
	 * @param receivedFromCS the receivedFromCS to set
	 */
	public void setReceivedFromCS(Long receivedFromCS) {
		this.receivedFromCS = receivedFromCS;
	}

	/**
	 * @return the positiveAdjust
	 */
	public Long getPositiveAdjust() {
		if(positiveAdjust == null) positiveAdjust = new Long (0);
		return positiveAdjust;
	}

	/**
	 * @param positiveAdjust the positiveAdjust to set
	 */
	public void setPositiveAdjust(Long positiveAdjust) {
		this.positiveAdjust = positiveAdjust;
	}

	/**
	 * @return the negativeAdjust
	 */
	public Long getNegativeAdjust() {
		if(negativeAdjust == null) negativeAdjust = new Long (0);
		if(negativeAdjust < 0) negativeAdjust = negativeAdjust * -1;
		return negativeAdjust;
	}

	/**
	 * @param negativeAdjust the negativeAdjust to set
	 */
	public void setNegativeAdjust(Long negativeAdjust) {
		this.negativeAdjust = negativeAdjust;
	}

	/**
	 * @return the dispensed
	 */
	public Long getDispensed() {
		if(dispensed == null) dispensed = new Long (0);
		if(dispensed < 0) dispensed = dispensed * -1;
		return dispensed;
	}

	/**
	 * @param dispensed the dispensed to set
	 */
	public void setDispensed(Long dispensed) {
		this.dispensed = dispensed;
	}

	/**
	 * @return the expired
	 */
	public Long getExpired() {
		if(expired == null) expired = new Long (0);
		if(expired < 0) expired = expired * -1;
		return expired;
	}

	/**
	 * @param expired the expired to set
	 */
	public void setExpired(Long expired) {
		this.expired = expired;
	}

	/**
	 * @return the outOfStockDays
	 */
	public Long getOutOfStockDays() {
		if(outOfStockDays == null) outOfStockDays = new Long (0);
		return outOfStockDays;
	}

	/**
	 * @param outOfStockDays the outOfStockDays to set
	 */
	public void setOutOfStockDays(Long outOfStockDays) {
		this.outOfStockDays = outOfStockDays;
	}
}
