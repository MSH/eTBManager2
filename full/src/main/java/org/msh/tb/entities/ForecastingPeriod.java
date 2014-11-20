package org.msh.tb.entities;

import org.msh.utils.date.Period;

public class ForecastingPeriod {

	private Period period;
	
	private int estConsumptionCases;
	
	private int estConsumptionNewCases;
	
	private int quantityMissing;
	
	private int stockOnHand;
	
	private int quantityExpired;
	
	private int quantityToExpire;
	
	private int quantityInOrder;


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
	 * Return the sum of the estimated consumption for previous cases and new cases
	 * @return the total consumption of the period
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

	/**
	 * @return the quantityToExpire
	 */
	public int getQuantityToExpire() {
		return quantityToExpire;
	}

	/**
	 * @param quantityToExpire the quantityToExpire to set
	 */
	public void setQuantityToExpire(int quantityToExpire) {
		this.quantityToExpire = quantityToExpire;
	}

	/**
	 * @return the quantityInOrder
	 */
	public int getQuantityInOrder() {
		return quantityInOrder;
	}

	/**
	 * @param quantityInOrder the quantityInOrder to set
	 */
	public void setQuantityInOrder(int quantityInOrder) {
		this.quantityInOrder = quantityInOrder;
	}
}
