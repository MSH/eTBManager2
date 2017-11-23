package org.msh.tb.entities;

public class ForecastingItem {

	public ForecastingItem(int monthIndex) {
		super();
		this.monthIndex = monthIndex;
	}

	private Integer quantity;
	private int monthIndex;


	public void addQuantity(int qtd) {
		if (quantity == null)
			 quantity = qtd;
		else quantity += qtd;
	}
	
	/**
	 * @return the quantity
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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
}
