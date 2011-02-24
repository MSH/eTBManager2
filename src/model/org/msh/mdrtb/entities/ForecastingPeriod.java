package org.msh.mdrtb.entities;

import org.msh.utils.date.Period;

public class ForecastingPeriod {

	private Period period;
	
	private int quantity;

	private int estConsumptionCases;
	
	private int estConsumptionNewCases;
	
	private int quantityMissing;
	
	private int stockOnHand;
	
	private int quantityExpired;


	/**
	 * @return the period
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(Period period) {
		this.period = period;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the estimatedConsumption
	 */
	public int getEstimatedConsumption() {
		return estConsumptionCases + estConsumptionNewCases;
	}


	/**
	 * @return the quantityMissing
	 */
	public int getQuantityMissing() {
		return quantityMissing;
	}

	/**
	 * @param quantityMissing the quantityMissing to set
	 */
	public void setQuantityMissing(int quantityMissing) {
		this.quantityMissing = quantityMissing;
	}

	/**
	 * @return the stockOnHand
	 */
	public int getStockOnHand() {
		return stockOnHand;
	}

	/**
	 * @param stockOnHand the stockOnHand to set
	 */
	public void setStockOnHand(int stockOnHand) {
		this.stockOnHand = stockOnHand;
	}

	/**
	 * @return the quantityExpired
	 */
	public int getQuantityExpired() {
		return quantityExpired;
	}

	/**
	 * @param quantityExpired the quantityExpired to set
	 */
	public void setQuantityExpired(int quantityExpired) {
		this.quantityExpired = quantityExpired;
	}

	/**
	 * @return the estConsumptionCases
	 */
	public int getEstConsumptionCases() {
		return estConsumptionCases;
	}

	/**
	 * @param estConsumptionCases the estConsumptionCases to set
	 */
	public void setEstConsumptionCases(int estConsumptionCases) {
		this.estConsumptionCases = estConsumptionCases;
	}

	/**
	 * @return the estConsumptionNewCases
	 */
	public int getEstConsumptionNewCases() {
		return estConsumptionNewCases;
	}

	/**
	 * @param estConsumptionNewCases the estConsumptionNewCases to set
	 */
	public void setEstConsumptionNewCases(int estConsumptionNewCases) {
		this.estConsumptionNewCases = estConsumptionNewCases;
	}
}
