package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class ForecastingBatch implements Serializable {
	private static final long serialVersionUID = -1478650018567664180L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="FORECASTINGMEDICINE_ID")
	private ForecastingMedicine forecastingMedicine;

	private Date expiryDate;
	
	private int quantity;

	/**
	 * Calculated by the forecasting tool. Quantity of medicine that will expire based on the
	 * consumption of medicine
	 */
	private int quantityToExpire;

	
	/**
	 * Calculated by the forecasting tool. Consumption of medicine of cases on treatment
	 */
	private int consumptionInMonth;

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

	public int getQuantityToExpire() {
		return quantityToExpire;
	}

	public void setQuantityToExpire(int quantityToExpire) {
		this.quantityToExpire = quantityToExpire;
	}

	public int getConsumptionInMonth() {
		return consumptionInMonth;
	}

	public void setConsumptionInMonth(int consumptionInMonth) {
		this.consumptionInMonth = consumptionInMonth;
	}
}
