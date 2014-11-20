package org.msh.tb.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BATCH_ID")
	private ForecastingBatch batch;

	

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

	/**
	 * @return the batch
	 */
	public ForecastingBatch getBatch() {
		return batch;
	}

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(ForecastingBatch batch) {
		this.batch = batch;
	}
}
