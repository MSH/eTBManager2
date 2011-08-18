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
@Table(name="forecastingorder")
public class ForecastingOrder implements Serializable{
	private static final long serialVersionUID = -5754517154721852537L;


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;


	@ManyToOne
	@JoinColumn(name="FORECASTINGMEDICINE_ID")
	private ForecastingMedicine forecastingMedicine;
	

	/**
	 * Date when medicine will arrive
	 */
	private Date arrivalDate;

	
	/**
	 * Quantity to arrive
	 */
	private int quantity;

	

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

	public Date getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(Date arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
