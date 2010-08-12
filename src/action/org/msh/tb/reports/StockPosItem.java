package org.msh.tb.reports;

import java.util.Date;

import org.msh.mdrtb.entities.Medicine;



public class StockPosItem {

	private Integer id;
	private Medicine medicine;
	private int quantity;
	private Date lastMovement;
	private Date nextExpirationBatch;
	private Integer minBufferStock;
	private boolean underBufferStock;
	private float totalPrice;

	/**
	 * @return the totalPrice
	 */
	public float getTotalPrice() {
		return totalPrice;
	}
	/**
	 * @param totalPrice the totalPrice to set
	 */
	public void setTotalPrice(float totalPrice) {
		this.totalPrice = totalPrice;
	}
	/**
	 * @return the unitPrice
	 */
	public float getUnitPrice() {
		return (quantity > 0? totalPrice / quantity: quantity);
	}
	
	public boolean isUnderBufferStock() {
		return underBufferStock;
	}
	public void setUnderBufferStock(boolean underBufferStock) {
		this.underBufferStock = underBufferStock;
	}
	public Integer getMinBufferStock() {
		return minBufferStock;
	}
	public void setMinBufferStock(Integer minBufferStock) {
		this.minBufferStock = minBufferStock;
	}
	public Date getLastMovement() {
		return lastMovement;
	}
	public void setLastMovement(Date lastMovement) {
		this.lastMovement = lastMovement;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Date getNextExpirationBatch() {
		return nextExpirationBatch;
	}
	public void setNextExpirationBatch(Date nextExpirationBatch) {
		this.nextExpirationBatch = nextExpirationBatch;
	}
	public Medicine getMedicine() {
		return medicine;
	}
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
}
