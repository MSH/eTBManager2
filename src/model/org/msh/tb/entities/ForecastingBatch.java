package org.msh.tb.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="forecastingbatch")
public class ForecastingBatch implements Serializable {
	private static final long serialVersionUID = -1478650018567664180L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="FORECASTINGMEDICINE_ID")
	private ForecastingMedicine forecastingMedicine;

	private Date expiryDate;
	
	/**
	 * Initial available quantity set by the user
	 */
	private int quantity;

	/**
	 * Calculated by the forecasting tool. Quantity of medicine that will expire based on the
	 * consumption of medicine
	 */
	private int quantityExpired;

	/**
	 * Calculated by the forecasting tool. Quantity available of the medicine, decreased month by month until the expiration date
	 */
	private int quantityAvailable;
	
	/**
	 * Calculated by the forecasting tool. Consumption of medicine of cases on treatment in the month medicine will expire
	 */
	private int consumptionInMonth;

	
	/**
	 * Initialize variables for a new calculation
	 */
	public void initialize() {
		quantityExpired = 0;
		quantityAvailable = quantity;
		consumptionInMonth = 0;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ForecastingMedicine getForecastingMedicine() {
		return forecastingMedicine;
	}

	public void setForecastingMedicine(ForecastingMedicine forecastingMedicine) {
		this.forecastingMedicine = forecastingMedicine;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getConsumptionInMonth() {
		return consumptionInMonth;
	}

	public void setConsumptionInMonth(int consumptionInMonth) {
		this.consumptionInMonth = consumptionInMonth;
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
	 * @return the quantityAvailable
	 */
	public int getQuantityAvailable() {
		return quantityAvailable;
	}

	/**
	 * @param quantityAvailable the quantityAvailable to set
	 */
	public void setQuantityAvailable(int quantityAvailable) {
		this.quantityAvailable = quantityAvailable;
	}
}
