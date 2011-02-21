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
	 * Estimated consumption of medicine for cases on treatment
	 */
	private int consumptionCases;
	
	/**
	 * Estimated consumption of medicine for new cases 
	 */
	private int consumptionNewCases;
	
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
	private int stockOnOrder;

	@Transient
	private List<ForecastingBatch> batchesToExpire;

	@Transient
	private List<BatchConsumption> batchesConsumed = new ArrayList<BatchConsumption>();


	/**
	 * Increment quantity on order
	 * @param qtd
	 */
	public void addStockOnOrder(int qtd) {
		stockOnOrder += qtd;
	}
	
	
	public void addBatchConsumption(ForecastingBatch batch, int quantity) {
		batchesConsumed.add(new BatchConsumption(batch, quantity));
	}
	
	/**
	 * Return list of batches to expire for the medicine in the month (month index)
	 * @return List of instances of {@link ForecastingBatch} class
	 */
	public List<ForecastingBatch> getBatchesToExpire() {
		if (batchesToExpire == null) {
			batchesToExpire = new ArrayList<ForecastingBatch>();
			ForecastingMedicine fm = forecasting.findMedicineById(medicine.getId());
			for (ForecastingBatch batch: fm.getBatchesToExpire()) {
				if (forecasting.getMonthIndex(batch.getExpiryDate()) == monthIndex)
					batchesToExpire.add(batch);
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
	public int getTotalConsumption() {
		return Math.round(consumptionNewCases) + consumptionCases;
	}

	/**
	 * Increase the number of cases and quantity of medicines for cases on treatment
	 * @param value
	 * @return
	 */
	public void increaseCasesOnTreatment(int numCases, int quantity) {
		numCasesOnTreatment += numCases;
		consumptionCases += quantity;
	}

	
	/**
	 * Increase the number of new cases and quantity of medicines for new cases
	 * @param numNewCases
	 * @param quantity
	 */
	public void increaseNewCases(float numNewCases, float quantity) {
		this.numNewCases += numNewCases;
		consumptionNewCases += quantity;
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


	public int getStockOnOrder() {
		return stockOnOrder;
	}


	public void setStockOnOrder(int quantityOnOrder) {
		this.stockOnOrder = quantityOnOrder;
	}
	
	
	/**
	 * Store the quantity of each batch consumed in the month
	 * @author Ricardo Memoria
	 *
	 */
	public class BatchConsumption {
		private ForecastingBatch batch;
		private int quantity;
		private int quantityAvailable;
		private int quantityExpired;

		public BatchConsumption(ForecastingBatch batch, int quantity) {
			super();
			this.batch = batch;
			this.quantity = quantity;
			this.quantityAvailable = batch.getQuantityAvailable();
			this.quantityExpired = batch.getQuantityExpired();
		}

		/**
		 * @return the batch
		 */
		public ForecastingBatch getBatch() {
			return batch;
		}
		/**
		 * @return the quantity
		 */
		public int getQuantity() {
			return quantity;
		}

		/**
		 * @return the quantityAvailable
		 */
		public int getQuantityAvailable() {
			return quantityAvailable;
		}

		/**
		 * @return the quantityExpired
		 */
		public int getQuantityExpired() {
			return quantityExpired;
		}
	}


	/**
	 * @return the batchesConsumed
	 */
	public List<BatchConsumption> getBatchesConsumed() {
		return batchesConsumed;
	}


	/**
	 * @return the consumptionCases
	 */
	public int getConsumptionCases() {
		return consumptionCases;
	}


	/**
	 * @param consumptionCases the consumptionCases to set
	 */
	public void setConsumptionCases(int consumptionCases) {
		this.consumptionCases = consumptionCases;
	}


	/**
	 * @return the consumptionNewCases
	 */
	public int getConsumptionNewCases() {
		return consumptionNewCases;
	}


	/**
	 * @param consumptionNewCases the consumptionNewCases to set
	 */
	public void setConsumptionNewCases(int consumptionNewCases) {
		this.consumptionNewCases = consumptionNewCases;
	}
}
