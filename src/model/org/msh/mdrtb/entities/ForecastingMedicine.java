package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;

@Entity
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
	
	private int stockOnHand;
	
	private float unitPrice;
	
	private int estimatedQtyCases;
	private int estimatedQtyNewCases;
	private int bufferStock;
	private int requestedQty;
	private int dispensingLeadTime;


	/**
	 * Batches to expire during the forecasting period
	 */
	@OneToMany(mappedBy="forecastingMedicine")
	private List<ForecastingBatch> batchesToExpire = new ArrayList<ForecastingBatch>();


	/**
	 * Orders to arrive during forecasting period
	 */
	@OneToMany(mappedBy="forecastingMedicine")
	private List<ForecastingOrder> orders = new ArrayList<ForecastingOrder>();
	
	/**
	 * Estimated quantity for cases in a cohort
	 */
	private int estimatedQtyCohort;

	/**
	 * Keep temporary list of results for the current medicine
	 */
	@Transient
	private List<ForecastingResult> results;

	
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
	 * Refresh result list
	 */
	public void refreshResults() {
		results = null;
	}

	public int getTotalEstimatedQtyCases() {
		return estimatedQtyCases + estimatedQtyCohort;
	}

	public int getStockOnHandAfterLeadTime() {
		int val = stockOnHand - dispensingLeadTime;
		return (val < 0? 0: val);
	}
	
	public int getEstimatedQty() {
		int val = estimatedQtyCases + estimatedQtyNewCases + estimatedQtyCohort - getStockOnHandAfterLeadTime(); 
		return (val < 0? 0: val);
	}
	
	public float getTotalPrice() {
		return unitPrice * getEstimatedQty();
	}
	
	public int getStockOnHand() {
		return stockOnHand;
	}

	public void setStockOnHand(int stockOnHand) {
		this.stockOnHand = stockOnHand;
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

	public int getEstimatedQtyCases() {
		return estimatedQtyCases;
	}

	public void setEstimatedQtyCases(int estimatedQtyCases) {
		this.estimatedQtyCases = estimatedQtyCases;
	}

	public int getBufferStock() {
		return bufferStock;
	}

	public void setBufferStock(int bufferStock) {
		this.bufferStock = bufferStock;
	}

	public int getRequestedQty() {
		return requestedQty;
	}

	public void setRequestedQty(int requestedQty) {
		this.requestedQty = requestedQty;
	}

	public void setEstimatedQtyNewCases(int estimatedQtyNewCases) {
		this.estimatedQtyNewCases = estimatedQtyNewCases;
	}

	public int getEstimatedQtyNewCases() {
		return estimatedQtyNewCases;
	}

	/**
	 * @return the dispensingLeadTime
	 */
	public int getDispensingLeadTime() {
		return dispensingLeadTime;
	}

	/**
	 * @param dispensingLeadTime the dispensingLeadTime to set
	 */
	public void setDispensingLeadTime(int dispensingLeadTime) {
		this.dispensingLeadTime = dispensingLeadTime;
	}


	/**
	 * @param estimatedQtyCohort the estimatedQtyCohort to set
	 */
	public void setEstimatedQtyCohort(int estimatedQtyCohort) {
		this.estimatedQtyCohort = estimatedQtyCohort;
	}

	/**
	 * @return the estimatedQtyCohort
	 */
	public int getEstimatedQtyCohort() {
		return estimatedQtyCohort;
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
}
