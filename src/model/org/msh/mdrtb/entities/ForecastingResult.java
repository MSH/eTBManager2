package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Hold the result by month of the forecasting 
 * @author Ricardo Memoria
 *
 */
@Entity
public class ForecastingResult implements Serializable {
	private static final long serialVersionUID = -3157222657470999356L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@ManyToOne
	@JoinColumn(name="FORECASTING_ID")
	private Forecasting forecasting;

	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	private Medicine medicine; 
	
	/**
	 * Month of result, starting by 1 as the first month of forecasting
	 */
	private int monthIndex;

	/**
	 * Number of cases on treatment in for the month and medicine 
	 */
	private int numCasesOnTreatment;


	/**
	 * Number of new cases for the month and medicine
	 */
	private float numNewCases;


	/**
	 * Stock on hand at the specific month
	 */
	private int stockOnHand;
	
	/**
	 * Estimated quantity of medicine for consumption of cases on treatment in the month 
	 */
	private int quantityCasesOnTreatment;

	/**
	 * Estimated quantity of medicine for consumption of new cases in the month
	 */
	private float quantityNewCases;
	
	/**
	 * Quantity of medicine that is going to expire in the month
	 */
	private int quantityToExpire;
	
	/**
	 * Quantity of medicine lost because expired
	 */
	private int quantityExpired;
	
	/**
	 * Quantity on order to be arrived in the month for the medicine
	 */
	private int quantityOnOrder;

	@Transient
	private List<ForecastingBatch> batchesToExpire;


	/**
	 * Return list of batches to expire for the medicine in the month (month index)
	 * @return List of instances of {@link ForecastingBatch} class
	 */
	public List<ForecastingBatch> getBatchesToExpire() {
		if (batchesToExpire == null) {
			batchesToExpire = new ArrayList<ForecastingBatch>();
			for (ForecastingMedicine med: forecasting.getMedicines()) {
				for (ForecastingBatch batch: med.getBatchesToExpire()) {
					if (forecasting.getMonthIndex(batch.getExpiryDate()) == monthIndex)
						batchesToExpire.add(batch);
				}
			}
		}
		return batchesToExpire;
	}


	/**
	 * Return the total number of cases for the month
	 * @return number of cases, i.e., new cases + previous cases on treatment
	 */
	public int getTotalCases() {
		return numCasesOnTreatment + Math.round(numNewCases);
	}

	
	/**
	 * Return the total quantity of medicine necessary for the total number of cases on treatment
	 * @return
	 */
	public int getTotalQuantity() {
		return Math.round(quantityNewCases) + quantityCasesOnTreatment;
	}

	/**
	 * Increase the number of cases and quantity of medicines for cases on treatment
	 * @param value
	 * @return
	 */
	public void increaseCasesOnTreatment(int numCases, int quantity) {
		numCasesOnTreatment += numCases;
		quantityCasesOnTreatment += quantity;
	}

	
	/**
	 * Increase the number of new cases and quantity of medicines for new cases
	 * @param numNewCases
	 * @param quantity
	 */
	public void increaseNewCases(float numNewCases, float quantity) {
		this.numNewCases += numNewCases;
		quantityNewCases += quantity;
	}
	
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public Forecasting getForecasting() {
		return forecasting;
	}


	public void setForecasting(Forecasting forecasting) {
		this.forecasting = forecasting;
	}


	public Medicine getMedicine() {
		return medicine;
	}


	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}


	public int getMonthIndex() {
		return monthIndex;
	}


	public void setMonthIndex(int monthIndex) {
		this.monthIndex = monthIndex;
	}



	public float getNumNewCases() {
		return numNewCases;
	}


	public void setNumNewCases(float numNewCases) {
		this.numNewCases = numNewCases;
	}


	public int getNumCasesOnTreatment() {
		return numCasesOnTreatment;
	}


	public void setNumCasesOnTreatment(int numCasesOnTreatment) {
		this.numCasesOnTreatment = numCasesOnTreatment;
	}


	public int getQuantityCasesOnTreatment() {
		return quantityCasesOnTreatment;
	}


	public void setQuantityCasesOnTreatment(int quantityCasesOnTreatment) {
		this.quantityCasesOnTreatment = quantityCasesOnTreatment;
	}


	public float getQuantityNewCases() {
		return quantityNewCases;
	}


	public void setQuantityNewCases(float quantityNewCases) {
		this.quantityNewCases = quantityNewCases;
	}


	public int getStockOnHand() {
		return stockOnHand;
	}


	public void setStockOnHand(int stockOnHand) {
		this.stockOnHand = stockOnHand;
	}


	public int getQuantityToExpire() {
		return quantityToExpire;
	}


	public void setQuantityToExpire(int quantityToExpire) {
		this.quantityToExpire = quantityToExpire;
	}


	public int getQuantityExpired() {
		return quantityExpired;
	}


	public void setQuantityExpired(int quantityExpired) {
		this.quantityExpired = quantityExpired;
	}


	public int getQuantityOnOrder() {
		return quantityOnOrder;
	}


	public void setQuantityOnOrder(int quantityOnOrder) {
		this.quantityOnOrder = quantityOnOrder;
	}
}
