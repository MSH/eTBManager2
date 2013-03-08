package org.msh.tb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;
import org.msh.utils.date.DateUtils;

@Entity
@Table(name="forecastingmedicine")
public class ForecastingMedicine implements Serializable {
	private static final long serialVersionUID = 4483825849526469919L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@ManyToOne
	@JoinColumn(name="FORECASTING_ID")
	private Forecasting forecasting;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;

	/**
	 * Stock on order during lead time
	 */
	private int stockOnOrderLT;
	
	/**
	 * Consumption during lead time
	 */
	private int consumptionLT;
	
	/**
	 * Quantity missing for the cases during lead time due to shortage of medicines or expired batches
	 */
	private int quantityMissingLT;
	
	/**
	 * Quantity expired during lead time
	 */
	private int quantityExpiredLT;

	/**
	 * Stock on order during review period
	 */
	private int stockOnOrder;

	/**
	 * Quantity expired during the review period
	 */
	private int quantityExpired;
	
	/**
	 * Consumption of cases on treatment during the review period
	 */
	private int consumptionCases;

	/**
	 * Consumption of new cases during review period
	 */
	private int consumptionNewCases;

	/**
	 * Unit price of the medicine
	 */
	private float unitPrice;

	/**
	 * Stock on hand
	 */
	private int stockOnHand;
	
	/**
	 * The estimated quantity to procure
	 */
	private int quantityToProcure;

	/**
	 * Batches to expire during the forecasting period
	 */
	@OneToMany(mappedBy="forecastingMedicine", cascade={CascadeType.ALL})
	private List<ForecastingBatch> batchesToExpire = new ArrayList<ForecastingBatch>();


	/**
	 * Orders to arrive during forecasting period
	 */
	@OneToMany(mappedBy="forecastingMedicine", cascade={CascadeType.ALL})
	private List<ForecastingOrder> orders = new ArrayList<ForecastingOrder>();
	
	/**
	 * Keep temporary list of results for the current medicine
	 */
	@Transient
	private List<ForecastingResult> results;

	
	@Transient
	private List<ForecastingPeriod> periods = new ArrayList<ForecastingPeriod>();
	
	
	/**
	 * Return the list of batches that will expire but are not provenient of a future order
	 * @return
	 */
	public List<ForecastingBatch> getBatchesNotInOrders() {
		ArrayList<ForecastingBatch> lst = new ArrayList<ForecastingBatch>();
		for (ForecastingBatch batch: batchesToExpire) {
			if (!batch.isBatchOnOrder())
				lst.add(batch);
		}
		
		return lst;
	}
	
	/**
	 * Calculate the order date of the current medicine
	 * @return
	 */
	public Date getOrderDate() {
		if ((forecasting == null) || (results == null))
			return null;

		Date stockOutDate = forecasting.getEndDate();
		stockOutDate = DateUtils.incMonths(stockOutDate, forecasting.getBufferStock());
		for (ForecastingPeriod period: periods) {
			if (period.getQuantityMissing() < 0) {
				stockOutDate = period.getPeriod().getEndDate();
				// calculate the number of missing days for the consumption
				int days = period.getPeriod().getDays() * period.getQuantityMissing() / period.getEstimatedConsumption();
				// estimate the stock out date
				stockOutDate = DateUtils.incDays(stockOutDate, days);
				break;
			}
		}

		int num = forecasting.getLeadTime();
		
		return DateUtils.incMonths(stockOutDate, -num);
	}

	/**
	 * Increment stock on order before lead time 
	 * @param qtd
	 */
	public void addStockOnOrderLT(int qtd) {
		stockOnOrderLT += qtd;
	}


	/**
	 * Increment stock on order during review period
	 * @param qtd
	 */
	public void addStockOnOrder(int qtd) {
		stockOnOrder += qtd;
	}
	
	
	/**
	 * Increment quantity in consumption before lead time
	 * @param qtd
	 */
	public void addConsumptionLT(int qtd) {
		consumptionLT += qtd;
	}

	
	/**
	 * Increment quantity expired before lead time
	 * @param qtd
	 */
	public void addQuantityExpiredLT(int qtd) {
		quantityExpiredLT += qtd;
	}

	
	/**
	 * Increment quantity expired during review period
	 * @param qtd
	 */
	public void addQuantityExpired(int qtd) {
		quantityExpired += qtd;
	}

	
	/**
	 * Increment consumption of cases on treatment during review period
	 * @param qtd
	 */
	public void addConsumptionCases(int qtd) {
		consumptionCases += qtd;
	}
	
	
	/**
	 * Increment consumption of new cases during review period
	 * @param qtd
	 */
	public void addConsumptionNewCases(int qtd) {
		consumptionNewCases += qtd;
	}


	/**
	 * Return the quantity dispensed to patient during lead time 
	 * @return
	 */
	public int getDispensingQuantityLT() {
		int qtd = consumptionLT + quantityMissingLT;
		return qtd < 0 ? 0: qtd;
	}


	/**
	 * @return
	 */
	public int getStockOnHandAfterLT() {
		int val = getStockOnHand() + stockOnOrderLT - getDispensingQuantityLT() - quantityExpiredLT;
		return (val < 0? 0: val);
	}


	/**
	 * @return
	 */
	public int getEstimatedQty() {
		int val = consumptionCases + consumptionNewCases - getStockOnHandAfterLT() + quantityExpired;
		return (val < 0? 0: val);
	}


	/**
	 * @return
	 */
	public List<ForecastingResult> getResults() {
		if (results == null) {
			if (forecasting.getResults().size() == 0)
				return null;

			results = new ArrayList<ForecastingResult>();
			
			for (ForecastingResult res: forecasting.getResults()) {
				if (res.getMedicine().equals(medicine))
					results.add(res);
			}
			
			// sort the list by month index
			Collections.sort(results, new Comparator<ForecastingResult>() {
				public int compare(ForecastingResult res1, ForecastingResult res2) {
					return ((Integer)res1.getMonthIndex()).compareTo(res2.getMonthIndex());
				}
			});
		}
		return results;
	}


	/**
	 * Find {@link ForecastingResult} by its month index
	 * @return
	 */
	public ForecastingResult findResultByMonthIndex(int monthIndex) {
		for (ForecastingResult res: getResults()) {
			if (res.getMonthIndex() == monthIndex)
				return res;
		}
		return null;
	}


	/**
	 * Initialize values 
	 */
	public void initialize() {
		stockOnOrderLT = 0;
		quantityExpiredLT = 0;
		stockOnOrder = 0;
		quantityExpired = 0;
		quantityExpiredLT = 0;
		consumptionCases = 0;
		consumptionLT = 0;
		consumptionNewCases = 0;
		quantityMissingLT = 0;
		results = null;
		for (ForecastingBatch b: batchesToExpire) {
			b.initialize();
		}
		periods.clear();
	}


	/**
	 * Return the total price of the estimated quantity
	 * @return
	 */
	public float getTotalPrice() {
		return unitPrice * getEstimatedQty();
	}

	
	/**
	 * Update the quantity of the stock on hand based on the batches available
	 */
	public void updateStockOnHand() {
		stockOnHand = 0;
		for (ForecastingBatch batch: batchesToExpire)
			if (!batch.isBatchOnOrder())
				stockOnHand += batch.getQuantity();
	}
	
	
	/**
	 * Find first batch with available quantity and where expiring date is after the given date
	 * @param dt
	 * @return
	 */
	public ForecastingBatch findAvailableBatch(Date dt) {
		for (ForecastingBatch batch: batchesToExpire) {
			if ((batch.getQuantityAvailable() > 0) && (!batch.getExpiryDate().before(dt))) {
				return batch;
			}
		}
		return null;
	}


	/**
	 * Calculate the stock on hand based on the batches informed
	 * @return
	 */
	public int getStockOnHand() {
		return stockOnHand;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	public float getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	/**
	 * I'm add this method because of funny Converter bug in JSF
	 * In primitive types we cannot use converters<br>
	 * AK 22/04/2012 
	 * @return
	 */
	public Number getUnitPriceF() {
		return new Float(unitPrice);
	}

	public void setUnitPriceF(Number unitPrice) {
		this.unitPrice = unitPrice.floatValue();
	}
	
	/**
	 * @param forecasting the forecasting to set
	 */
	public void setForecasting(Forecasting forecasting) {
		this.forecasting = forecasting;
	}

	/**
	 * @return the forecasting
	 */
	public Forecasting getForecasting() {
		return forecasting;
	}

	public List<ForecastingBatch> getBatchesToExpire() {
		return batchesToExpire;
	}

	public void setBatchesToExpire(List<ForecastingBatch> batchesToExpire) {
		this.batchesToExpire = batchesToExpire;
	}

	public List<ForecastingOrder> getOrders() {
		return orders;
	}

	public void setOrders(List<ForecastingOrder> orders) {
		this.orders = orders;
	}


	/**
	 * @return the stockOnOrder
	 */
	public int getStockOnOrder() {
		return stockOnOrder;
	}


	/**
	 * @param stockOnOrder the stockOnOrder to set
	 */
	public void setStockOnOrder(int stockOnOrder) {
		this.stockOnOrder = stockOnOrder;
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
	 * @return the stockOnOrderLT
	 */
	public int getStockOnOrderLT() {
		return stockOnOrderLT;
	}


	/**
	 * @param stockOnOrderLT the stockOnOrderLT to set
	 */
	public void setStockOnOrderLT(int stockOnOrderLT) {
		this.stockOnOrderLT = stockOnOrderLT;
	}


	/**
	 * @return the quantityExpiredLT
	 */
	public int getQuantityExpiredLT() {
		return quantityExpiredLT;
	}


	/**
	 * @param quantityExpiredLT the quantityExpiredLT to set
	 */
	public void setQuantityExpiredLT(int quantityExpiredLT) {
		this.quantityExpiredLT = quantityExpiredLT;
	}


	/**
	 * @return the consumptionLT
	 */
	public int getConsumptionLT() {
		return consumptionLT;
	}


	/**
	 * @param consumptionLT the consumptionLT to set
	 */
	public void setConsumptionLT(int consumptionLT) {
		this.consumptionLT = consumptionLT;
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


	/**
	 * @return the quantityMissingLT
	 */
	public int getQuantityMissingLT() {
		return quantityMissingLT;
	}


	/**
	 * @param quantityMissingLT the quantityMissingLT to set
	 */
	public void setQuantityMissingLT(int quantityMissingLT) {
		this.quantityMissingLT = quantityMissingLT;
	}


	/**
	 * @return the movements
	 */
	public List<ForecastingPeriod> getPeriods() {
		return periods;
	}

	/**
	 * @param stockOnHand the stockOnHand to set
	 */
	public void setStockOnHand(int stockOnHand) {
		this.stockOnHand = stockOnHand;
	}

	/**
	 * @return the quantityToProcure
	 */
	public int getQuantityToProcure() {
		return quantityToProcure;
	}

	/**
	 * @param quantityToProcure the quantityToProcure to set
	 */
	public void setQuantityToProcure(int quantityToProcure) {
		this.quantityToProcure = quantityToProcure;
	}
}
